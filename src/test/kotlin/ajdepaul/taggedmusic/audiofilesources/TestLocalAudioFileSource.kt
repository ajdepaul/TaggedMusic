/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestLocalAudioFileSource {

    @Rule
    @JvmField
    val tempDir = TemporaryFolder()

    /** Tests [LocalAudioFileSource.hasAudioFile]. */
    @Test
    fun testHasAudioFile() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)

        assertFalse(localAudioFileSource.hasAudioFile(Paths.get("song.mp3")))

        val songFile = songDirectory.resolve("song.mp3").toFile().also { it.createNewFile() }

        assertTrue(localAudioFileSource.hasAudioFile(Paths.get("song.mp3")))

        songFile.delete()

        assertFalse(localAudioFileSource.hasAudioFile(Paths.get("song.mp3")))
    }

    /** Tests [LocalAudioFileSource.pushAudioFile]. */
    @Test
    fun testPushAudioFile() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)

        val songFile = tempDir.newFile("song_to_upload.mp3")
        assertTrue(
            localAudioFileSource.pushAudioFile(songFile.toPath(), Paths.get("uploaded_song.mp3"))
        )
        assertTrue(songDirectory.resolve("uploaded_song.mp3").toFile().isFile)

        val blockingDir = songDirectory.resolve("blocking_dir.mp3").toFile().also { it.mkdir() }
        blockingDir.resolve("filler.txt").createNewFile()

        assertFalse(
            localAudioFileSource.pushAudioFile(songFile.toPath(), Paths.get("blocking_dir.mp3"))
        )
        assertTrue(songDirectory.resolve("blocking_dir.mp3").toFile().isDirectory)
    }

    /** Tests [LocalAudioFileSource.pullAudioFile]. */
    @Test
    fun testPullAudioFile() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)

        assertNull(localAudioFileSource.pullAudioFile(Paths.get("song.mp3")))

        val songFile = songDirectory.resolve("song.mp3").also { it.toFile().createNewFile() }

        assertEquals(songFile, localAudioFileSource.pullAudioFile(Paths.get("song.mp3")))

        songFile.toFile().delete()

        assertNull(localAudioFileSource.pullAudioFile(Paths.get("song.mp3")))
    }

    /** Tests [LocalAudioFileSource.removeAudioFile]. */
    @Test
    fun testRemoveAudioFile() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)

        val songFile = songDirectory.resolve("song.mp3")

        assertFalse(localAudioFileSource.removeAudioFile(songFile))

        songFile.toFile().createNewFile()

        assertTrue(localAudioFileSource.removeAudioFile(songFile))

        assertFalse(songFile.toFile().exists())

        assertFalse(localAudioFileSource.removeAudioFile(songFile))
    }
}
