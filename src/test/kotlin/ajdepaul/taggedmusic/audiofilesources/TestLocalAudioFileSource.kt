/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
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

        assertFalse(localAudioFileSource.hasAudioFile("song.mp3"))

        val songFile = songDirectory.resolve("song.mp3").toFile()
        songFile.createNewFile()

        assertTrue(localAudioFileSource.hasAudioFile("song.mp3"))

        songFile.delete()

        assertFalse(localAudioFileSource.hasAudioFile("song.mp3"))
    }

    /** Tests [LocalAudioFileSource.pushAudioFile]. */
    @Test
    fun testPushAudioFile() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)

        val songFile = tempDir.newFile()

        assertTrue(localAudioFileSource.pushAudioFile(songFile.toPath(), "song.mp3"))
        assertTrue(songDirectory.resolve("song.mp3").toFile().isFile)

        val blockingDir = songDirectory.resolve("blocking_dir.mp3").toFile()
        blockingDir.mkdir()
        blockingDir.resolve("filler.txt").createNewFile()

        assertFalse(localAudioFileSource.pushAudioFile(songFile.toPath(), "blocking_dir.mp3"))
        assertTrue(songDirectory.resolve("blocking_dir.mp3").toFile().isDirectory)
    }

    /** Tests [LocalAudioFileSource.pullAudioFile]. */
    @Test
    fun testPullAudioFile() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)

        assertNull(localAudioFileSource.pullAudioFile("song.mp3"))

        val songFile = songDirectory.resolve("song.mp3")
        songFile.toFile().createNewFile()

        assertEquals(songFile, localAudioFileSource.pullAudioFile("song.mp3"))

        songFile.toFile().delete()

        assertNull(localAudioFileSource.pullAudioFile("song.mp3"))
    }

    /** Tests [LocalAudioFileSource.removeAudioFile]. */
    @Test
    fun testRemoveAudioFile() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)

        val songFile = songDirectory.resolve("song.mp3")

        assertFalse(localAudioFileSource.removeAudioFile(songFile.fileName.toString()))

        songFile.toFile().createNewFile()

        assertTrue(localAudioFileSource.removeAudioFile(songFile.fileName.toString()))

        assertFalse(songFile.toFile().exists())

        assertFalse(localAudioFileSource.removeAudioFile(songFile.fileName.toString()))
    }
}
