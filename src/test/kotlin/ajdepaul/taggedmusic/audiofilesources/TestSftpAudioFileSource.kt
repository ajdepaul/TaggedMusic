/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestSftpAudioFileSource {

    @Rule
    @JvmField
    val tempDir = TemporaryFolder()

    private val testServerProperties = this.javaClass.getResource(
        Paths.get("TestSftpAudioFileSource", "server.properties").toString()
    )

    /** SFTP remote directory for temporary files for this set of tests. */
    private val remoteTempDir = Paths.get(".test_tagged_music_audio_files")

    /**
     * Get the test server information from the server.properties resource.
     * @return null if the test should be skipped
     */
    private fun loadProperties(): Session? {
        val prop = Properties()
        prop.load(testServerProperties!!.openStream())

        if (prop.getProperty("test") != "true") {
            return null
        }

        val jsch = JSch()
        if (prop.getProperty("known_hosts") != null) jsch.setKnownHosts(prop.getProperty("known_hosts"))

        val session = jsch.getSession(
            prop.getProperty("username"),
            prop.getProperty("host"),
            prop.getProperty("port")?.toInt() ?: 22
        )

        if (prop.getProperty("password") != null) session.setPassword(prop.getProperty("password"))

        return session
    }

    /** Removes the temporary files created in the SFTP server. */
    @After
    fun cleanServer() {

        val session = loadProperties() ?: return
        try {
            session.connect()
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()

            // recursively deletes files in an SFTP directory
            fun emptyDir(dirName: String) {
                // cd into next directory, give up if failed
                try {
                    channel.cd(dirName)
                } catch (_: Exception) {
                    return
                }

                for (entry in channel.ls(".")) {
                    val lsEntry = entry as ChannelSftp.LsEntry

                    if (lsEntry.filename != "." && lsEntry.filename != "..") {
                        if (lsEntry.attrs.isDir) { // recurse directories
                            emptyDir(lsEntry.filename)
                        } else {
                            channel.rm(lsEntry.filename)
                        }
                    }
                }

                channel.cd("..")
                channel.rmdir(dirName)
            }

            emptyDir(remoteTempDir.toString())

        } finally {
            session.disconnect()
        }
    }

    /** Tests [SftpAudioFileSource.hasAudioFile]. */
    @Test
    fun testHasAudioFile() {
        if (loadProperties() == null) {
            println(
                "[WARNING] SFTP audio file source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        // check for file
        with(
            SftpAudioFileSource(
                loadProperties()!!,
                tempDir.newFolder().toPath(),
                remoteTempDir.resolve("testHasAudioFile")
            )
        ) {
            assertFalse(this.hasAudioFile("song.mp3"))
        }

        // create audio file
        val session = loadProperties()!!
        try {
            session.connect()
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()

            channel.cd(remoteTempDir.resolve("testHasAudioFile").toString())
            channel.put(tempDir.newFile("song.mp3").toString(), "song.mp3")
        } finally {
            session.disconnect()
        }

        // recheck for file
        with(
            SftpAudioFileSource(
                loadProperties()!!,
                tempDir.newFolder().toPath(),
                remoteTempDir.resolve("testHasAudioFile")
            )
        ) {
            assertTrue(this.hasAudioFile("song.mp3"))
        }
    }

    /** Tests [SftpAudioFileSource.pushAudioFile]. */
    @Test
    fun testPushAudioFile() {
        if (loadProperties() == null) {
            println(
                "[WARNING] SFTP audio file source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        val audioFile = tempDir.newFile("song.mp3").toPath()
        audioFile.toFile().writeText("abc")

        // push the audio file
        with(
            SftpAudioFileSource(
                loadProperties()!!,
                tempDir.newFolder().toPath(),
                remoteTempDir.resolve("testPushAudioFile")
            )
        ) {
            assertTrue(this.pushAudioFile(audioFile, "song.mp3"))
        }

        // get the audio file
        val session = loadProperties()!!
        try {
            session.connect()
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()

            channel.cd(remoteTempDir.resolve("testPushAudioFile").toString())
            channel.get("song.mp3", audioFile.toString().replace(".mp3", "2.mp3"))

            assertEquals("abc", File(audioFile.toString().replace(".mp3", "2.mp3")).readText())
        } finally {
            session.disconnect()
        }
    }

    /** Tests [SftpAudioFileSource.pullAudioFile]. */
    @Test
    fun testPullAudioFile() {
        if (loadProperties() == null) {
            println(
                "[WARNING] SFTP audio file source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        // upload an audio file
        val audioFile = tempDir.newFile("song.mp3").toPath()
        audioFile.toFile().writeText("abc")

        val session = loadProperties()!!
        try {
            session.connect()
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()

            channel.mkdir(remoteTempDir.toString())
            channel.cd(remoteTempDir.toString())
            channel.mkdir("testPullAudioFile")
            channel.cd("testPullAudioFile")
            channel.put(audioFile.toString(), "song.mp3")
        } finally {
            session.disconnect()
        }

        val localSongDir = tempDir.newFolder().toPath()

        with(
            SftpAudioFileSource(
                loadProperties()!!,
                localSongDir,
                remoteTempDir.resolve("testPullAudioFile")
            )
        ) {
            assertNull(this.pullAudioFile("not-a-song.mp3"))
            assertEquals(localSongDir.resolve("song.mp3"), this.pullAudioFile("song.mp3"))
            assertEquals("abc", localSongDir.resolve("song.mp3").toFile().readText())
        }
    }

    /** Tests [SftpAudioFileSource.removeAudioFile]. */
    @Test
    fun testRemoveAudioFile() {
        if (loadProperties() == null) {
            println(
                "[WARNING] SFTP audio file source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        // upload an audio file
        val audioFile = tempDir.newFile("song.mp3").toPath()

        val session1 = loadProperties()!!
        try {
            session1.connect()
            val channel = session1.openChannel("sftp") as ChannelSftp
            channel.connect()

            channel.mkdir(remoteTempDir.toString())
            channel.cd(remoteTempDir.toString())
            channel.mkdir("testRemoveAudioFile")
            channel.cd("testRemoveAudioFile")
            channel.put(audioFile.toString(), "song.mp3")
        } finally {
            session1.disconnect()
        }

        with(
            SftpAudioFileSource(
                loadProperties()!!,
                tempDir.newFolder().toPath(),
                remoteTempDir.resolve("testRemoveAudioFile")
            )
        ) {
            assertTrue(this.removeAudioFile("song.mp3"))
            assertFalse(this.removeAudioFile("song.mp3"))
        }

        // check if the file exists
        val session2 = loadProperties()!!
        try {
            session2.connect()
            val channel = session2.openChannel("sftp") as ChannelSftp
            channel.connect()

            channel.cd(remoteTempDir.resolve("testRemoveAudioFile").toString())
            var foundFile = false
            for (entry in channel.ls(".")) {
                val lsEntry = entry as ChannelSftp.LsEntry
                if (lsEntry.filename == "song.mp3") foundFile = true
            }

            assertFalse(foundFile)
        } finally {
            session2.disconnect()
        }
    }
}
