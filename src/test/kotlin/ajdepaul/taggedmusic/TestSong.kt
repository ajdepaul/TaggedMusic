/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic

import kotlinx.collections.immutable.persistentHashSetOf
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestSong {

    /**
     * Asserts that [before] came before [after].
     * @return [after]
     */
    private fun assertUpdated(before: LocalDateTime, after: LocalDateTime): LocalDateTime {
        assertTrue(before < after)
        return after
    }

    /** Tests the default [Song] values. */
    @Test
    fun testDefaults() {
        var song = Song("title", 1000)
        assertEquals("title", song.title)
        assertEquals(1000, song.duration)
        assertEquals(null, song.album)
        assertEquals(null, song.artist)
        assertEquals(null, song.trackNum)
        assertEquals(null, song.year)
        assertEquals(0, song.playCount)
        assertEquals(persistentHashSetOf(), song.tags)
    }

    /** Tests [Song.mutate]. */
    @Test
    fun testMutate() {
        var song = Song("title", 1000)
        var before = song.lastModified

        // individual mutates
        song = song.mutate { title = "title2" }
        before = assertUpdated(before, song.lastModified)
        assertEquals("title2", song.title)

        song = song.mutate { duration = 2000 }
        before = assertUpdated(before, song.lastModified)
        assertEquals(2000, song.duration)

        song = song.mutate { artist = "artist" }
        before = assertUpdated(before, song.lastModified)
        assertEquals("artist", song.artist)

        song = song.mutate { album = "album" }
        before = assertUpdated(before, song.lastModified)
        assertEquals("album", song.album)

        song = song.mutate { trackNum = 1 }
        before = assertUpdated(before, song.lastModified)
        assertEquals(1, song.trackNum)

        song = song.mutate { year = 2020 }
        before = assertUpdated(before, song.lastModified)
        assertEquals(2020, song.year)

        song = song.mutate { playCount++ }
        before = assertUpdated(before, song.lastModified)
        assertEquals(1, song.playCount)

        song = song.mutate { tags = persistentHashSetOf("A", "B", "C") }
        before = assertUpdated(before, song.lastModified)
        assertEquals(persistentHashSetOf("A", "B", "C"), song.tags)

        // multiple mutates
        song = song.mutate { duration = 3000; year = 2010; album = "album2" }
        before = assertUpdated(before, song.lastModified)
        assertEquals(3000, song.duration)
        assertEquals(2010, song.year)
        assertEquals("album2", song.album)

        song = song.mutate { artist = "artist2"; title = "title3"; tags = persistentHashSetOf("A", "C", "D") }
        before = assertUpdated(before, song.lastModified)
        assertEquals("artist2", song.artist)
        assertEquals("title3", song.title)
        assertEquals(persistentHashSetOf("A", "C", "D"), song.tags)

        song = song.mutate { playCount++; year = 2000; trackNum = 2 }
        before = assertUpdated(before, song.lastModified)
        assertEquals(2, song.playCount)
        assertEquals(2000, song.year)
        assertEquals(2, song.trackNum)

        // don't update last modified
        song = song.mutate(false) { duration = 4000; year = 1990; album = "album3" }
        assertEquals(before, song.lastModified)
        assertEquals(4000, song.duration)
        assertEquals(1990, song.year)
        assertEquals("album3", song.album)

        song = song.mutate(false) { artist = "artist3"; title = "title4"; tags = persistentHashSetOf("B", "D", "E") }
        assertEquals(before, song.lastModified)
        assertEquals("artist3", song.artist)
        assertEquals("title4", song.title)
        assertEquals(persistentHashSetOf("B", "D", "E"), song.tags)

        song = song.mutate(false) { playCount++; year = 1980; trackNum = 3 }
        assertEquals(before, song.lastModified)
        assertEquals(3, song.playCount)
        assertEquals(1980, song.year)
        assertEquals(3, song.trackNum)
    }
}
