/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestCachedAudioFileSource {

    @Rule
    @JvmField
    val tempDir = TemporaryFolder()

    /**
     * Uses a [LocalAudioFileSource] to test [CachedAudioFileSource.hasAudioFile].
     * [LocalAudioFileSource] isn't meant to be used with [CachedAudioFileSource], but it's easier
     * to use for testing.
     */
    @Test
    fun testHasAudioFile() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)
        val cachedAudioFileSource = CachedAudioFileSource(localAudioFileSource, 50)

        val audioFile = songDirectory.resolve("song.mp3").toFile()

        assertFalse(cachedAudioFileSource.hasAudioFile("song.mp3"))
        audioFile.createNewFile()
        assertTrue(cachedAudioFileSource.hasAudioFile("song.mp3"))
        audioFile.delete()
        assertFalse(cachedAudioFileSource.hasAudioFile("song.mp3"))
    }

    /**
     * Uses a [LocalAudioFileSource] to test [CachedAudioFileSource.pushAudioFile].
     * [LocalAudioFileSource] isn't meant to be used with [CachedAudioFileSource], but it's easier
     * to use for testing.
     */
    @Test
    fun testPushAudioFile() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)
        val cachedAudioFileSource = CachedAudioFileSource(localAudioFileSource, 50)

        val audioFile = tempDir.newFile()

        assertTrue(cachedAudioFileSource.pushAudioFile(audioFile.toPath(), "song.mp3"))
        assertTrue(songDirectory.resolve("song.mp3").toFile().isFile)

        val blockingDir = songDirectory.resolve("blocking_dir.mp3").toFile()
        blockingDir.mkdir()
        blockingDir.resolve("filler.txt").createNewFile()

        assertFalse(cachedAudioFileSource.pushAudioFile(audioFile.toPath(), "blocking_dir.mp3"))
        assertTrue(songDirectory.resolve("blocking_dir.mp3").toFile().isDirectory)
    }

    /**
     * Uses a [LocalAudioFileSource] to test [CachedAudioFileSource.pullAudioFile].
     * [LocalAudioFileSource] isn't meant to be used with [CachedAudioFileSource], but it's easier
     * to use for testing.
     */
    @Test
    fun testPullAudioFile() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)
        val cachedAudioFileSource = CachedAudioFileSource(localAudioFileSource, 50)

        val audioFile = songDirectory.resolve("song.mp3").toFile()

        assertNull(cachedAudioFileSource.pullAudioFile("song.mp3"))
        audioFile.createNewFile()
        assertEquals(audioFile.toPath(), cachedAudioFileSource.pullAudioFile("song.mp3"))
        audioFile.delete()
        assertEquals(audioFile.toPath(), cachedAudioFileSource.pullAudioFile("song.mp3"))
    }

    /**
     * Uses a [LocalAudioFileSource] to test [CachedAudioFileSource.removeAudioFile].
     * [LocalAudioFileSource] isn't meant to be used with [CachedAudioFileSource], but it's easier
     * to use for testing.
     */
    @Test
    fun testRemoveAudioFile() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)
        val cachedAudioFileSource = CachedAudioFileSource(localAudioFileSource, 50)

        val audioFile = songDirectory.resolve("song.mp3").toFile()

        assertFalse(cachedAudioFileSource.removeAudioFile("song.mp3"))
        audioFile.createNewFile()
        assertTrue(cachedAudioFileSource.removeAudioFile("song.mp3"))
        assertFalse(audioFile.isFile)
        assertFalse(cachedAudioFileSource.removeAudioFile("song.mp3"))
    }

    /**
     * Uses a [LocalAudioFileSource] to test the caching behavior of [CachedAudioFileSource].
     * [LocalAudioFileSource] isn't meant to be used with [CachedAudioFileSource], but it's easier
     * to use for testing.
     */
    @Test
    fun testCaching() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)
        val cachedAudioFileSource = CachedAudioFileSource(localAudioFileSource, 50)

        val audioFile = songDirectory.resolve("song.mp3").toFile()
        audioFile.createNewFile()

        cachedAudioFileSource.pullAudioFile("song.mp3")

        audioFile.delete()

        assertTrue(cachedAudioFileSource.hasAudioFile("song.mp3"))
        assertEquals(audioFile.toPath(), cachedAudioFileSource.pullAudioFile("song.mp3"))

        assertFalse(cachedAudioFileSource.removeAudioFile("song.mp3"))

        assertFalse(cachedAudioFileSource.hasAudioFile("song.mp3"))
        assertNull(cachedAudioFileSource.pullAudioFile("song.mp3"))
    }

    /**
     * Uses a [LocalAudioFileSource] to test [CachedAudioFileSource] auto deleting audio files when
     * [maxSpace][CachedAudioFileSource.maxSpace] is reached. [LocalAudioFileSource] isn't meant to
     * be used with [CachedAudioFileSource], but it's easier to use for testing.
     */
    @Test
    fun testAutoDelete() {
        val songDirectory = tempDir.newFolder("songs").toPath()
        val localAudioFileSource = LocalAudioFileSource(songDirectory)
        val cachedAudioFileSource = CachedAudioFileSource(localAudioFileSource, 50)

        fun addAudioFile(fileName: String): Path {
            val audioFile = songDirectory.resolve(fileName).toFile()
            audioFile.createNewFile()
            audioFile.writeText("a".repeat(25)) // write 25 bytes
            return audioFile.toPath()
        }

        val audioFile1 = addAudioFile("song1.mp3")
        val audioFile2 = addAudioFile("song2.mp3")
        val audioFile3 = addAudioFile("song3.mp3")

        // cache three songs
        cachedAudioFileSource.pullAudioFile("song1.mp3")
        cachedAudioFileSource.pullAudioFile("song2.mp3")
        cachedAudioFileSource.pullAudioFile("song3.mp3") // cache full, delete song1.mp3

        assertFalse(audioFile1.toFile().isFile)
        assertTrue(audioFile2.toFile().isFile)
        assertTrue(audioFile3.toFile().isFile)

        addAudioFile("song1.mp3")

        // cache songs again, but pull song1.mp3 again so that song2.mp3 is the eldest in the cache
        cachedAudioFileSource.pullAudioFile("song1.mp3")
        cachedAudioFileSource.pullAudioFile("song2.mp3")
        cachedAudioFileSource.pullAudioFile("song1.mp3")
        cachedAudioFileSource.pullAudioFile("song3.mp3") // cache full, delete song2.mp3

        assertTrue(audioFile1.toFile().isFile)
        assertFalse(audioFile2.toFile().isFile)
        assertTrue(audioFile3.toFile().isFile)
    }
}
