/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

import org.junit.Test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestLocalAudioFileSource {

    /**
     * Tests a combination of [LocalAudioFileSource.hasAudioFile], [LocalAudioFileSource.pullAudioFile],
     * [LocalAudioFileSource.removeAudioFile], and [LocalAudioFileSource.pushAudioFile] by creating
     * temporary files in a test directory.
     */
    @Test
    fun testHasPullRemovePushSong() {
        File("test").mkdir()
        File("test/song.mp3").createNewFile()
        File("test/songs").mkdir()
        File("test/songs/song.mp3/file.txt").delete()
        File("test/songs/song.mp3").delete()

        var provider = LocalAudioFileSource("test/songs")

        assertFalse(provider.hasAudioFile("song.mp3"))
        assertNull(provider.pullAudioFile("song.mp3"))
        assertFalse(provider.removeAudioFile("song.mp3"))

        assertTrue(provider.pushAudioFile("test/song.mp3", "song.mp3"))
        assertTrue(File("test/songs/song.mp3").isFile)
        assertTrue(provider.hasAudioFile("song.mp3"))
        assertTrue(File(provider.pullAudioFile("song.mp3")).isFile)
        assertTrue(provider.removeAudioFile("song.mp3"))
        assertFalse(File("test/songs/song.mp3").exists())

        // exception catching
        if (File("not_a_path").exists()) {
            error("'not_a_path' cannot be a valid path for this test")
        }

        assertFalse(provider.pushAudioFile("not_a_path/song.mp3", "song.mp3"))

        File("test/songs/song.mp3").mkdir()
        File("test/songs/song.mp3/file.txt").createNewFile()
        assertFalse(provider.pushAudioFile("test/song.mp3", "song.mp3"))
        File("test/songs/song.mp3/file.txt").delete()
        File("test/songs/song.mp3").delete()

        provider = LocalAudioFileSource("not_a_path/songs")

        assertFalse(provider.hasAudioFile("song.mp3"))
        assertFalse(provider.hasAudioFile(""))
        assertFalse(provider.pushAudioFile("test/song.mp3", "song.mp3"))
        assertNull(provider.pullAudioFile("song.mp3"))
        assertFalse(provider.removeAudioFile("song.mp3"))
    }
}
