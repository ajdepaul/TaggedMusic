/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.TagType
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Paths
import java.util.*

class TestSftpLibrarySource {

    @Rule
    @JvmField
    val tempDir = TemporaryFolder()

    private val testServerProperties = this.javaClass.getResource(
        Paths.get("TestSftpLibrarySource", "server.properties").toString()
    )

    /** SFTP remote directory for temporary files for this set of tests. */
    private val remoteTempDir = Paths.get(".test_tagged_music")

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

    /** Tests the [SftpLibrarySource] constructors. */
    @Test
    fun testConstructor() {
        if (loadProperties() == null) {
            println(
                "[WARNING] SFTP library source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        val sftpDir = tempDir.newFolder("sftpDir").toPath()
        val jsonLibraryFilePath = sftpDir.resolve("library.json")
        jsonLibraryFilePath.toFile().createNewFile()

        // initialize the server with default values
        SftpLibrarySource(
            loadProperties()!!,
            listOf(Paths.get("library.json")),
            sftpDir,
            remoteTempDir.resolve(Paths.get("testConstructor", "test")),
            JsonLibrarySource(jsonLibraryFilePath, TagType(0)),
            true
        ).close()

        jsonLibraryFilePath.toFile().delete()

        // test new library source using that server
        with(
            SftpLibrarySource(
                loadProperties()!!,
                listOf(Paths.get("library.json")),
                sftpDir,
                remoteTempDir.resolve(Paths.get("testConstructor", "test")),
                JsonLibrarySource(jsonLibraryFilePath)
            )
        ) {
            TestLibrarySourceUtil.assertDefaults(this)
        }
    }

    /** Tests [SftpLibrarySource.updater]. */
    @Test
    fun testUpdater() {
        if (loadProperties() == null) {
            println(
                "[WARNING] SFTP library source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        val sftpDir = tempDir.newFolder("sftpDir").toPath()
        val jsonLibraryFilePath = sftpDir.resolve("library.json")
        jsonLibraryFilePath.toFile().createNewFile()

        // test making changes
        val songLibraryData = with(
            SftpLibrarySource(
                loadProperties()!!,
                listOf(Paths.get("library.json")),
                sftpDir,
                remoteTempDir.resolve(Paths.get("testUpdater")),
                JsonLibrarySource(jsonLibraryFilePath, TagType(0)),
                true
            )
        ) {
            TestLibrarySourceUtil.assertUpdates(this)
        }

        // test changes were saved
        with(
            SftpLibrarySource(
                loadProperties()!!,
                listOf(Paths.get("library.json")),
                sftpDir,
                remoteTempDir.resolve(Paths.get("testUpdater")),
                JsonLibrarySource(jsonLibraryFilePath)
            )
        ) {
            TestLibrarySourceUtil.assertUpdated(this, songLibraryData)
        }
    }

    /** Tests [SftpLibrarySource.getSongsByTags]. */
    @Test
    fun testGetSongsByTags() {
        if (loadProperties() == null) {
            println(
                "[WARNING] SFTP library source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        val sftpDir = tempDir.newFolder("sftpDir").toPath()
        val jsonLibraryFilePath = sftpDir.resolve("library.json")
        jsonLibraryFilePath.toFile().createNewFile()

        // use util class for tests
        with(
            SftpLibrarySource(
                loadProperties()!!,
                listOf(Paths.get("library.json")),
                sftpDir,
                remoteTempDir.resolve(Paths.get("testGetSongsByTags")),
                JsonLibrarySource(jsonLibraryFilePath, TagType(0)),
                true
            )
        ) {
            TestLibrarySourceUtil.testGetSongsByTags(this)
        }
    }
}
