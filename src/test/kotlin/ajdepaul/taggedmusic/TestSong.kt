/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic

import kotlinx.collections.immutable.persistentHashSetOf
import org.junit.Test
import java.time.LocalDateTime
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
        val song = Song("title", 1000)
        assertEquals("title", song.title)
        assertEquals(1000, song.duration)
        assertEquals(null, song.trackNum)
        assertEquals(null, song.releaseDate)
        assertEquals(0, song.playCount)
        assertEquals(persistentHashSetOf(), song.tags)
    }

    /** Tests [Song.mutate]. */
    @Test
    fun testMutate() {
        var song = Song("title", 1000)
        val createDate = song.createDate
        var before = song.modifyDate
        val delay = 1L

        // individual mutates
        Thread.sleep(delay)
        song = song.mutate { title = "title2" }
        before = assertUpdated(before, song.modifyDate)
        assertEquals("title2", song.title)

        Thread.sleep(delay)
        song = song.mutate { duration = 2000 }
        before = assertUpdated(before, song.modifyDate)
        assertEquals(2000, song.duration)

        Thread.sleep(delay)
        song = song.mutate { trackNum = 1 }
        before = assertUpdated(before, song.modifyDate)
        assertEquals(1, song.trackNum)

        Thread.sleep(delay)
        song = song.mutate { releaseDate = LocalDateTime.of(2020, 1, 1, 0, 0) }
        before = assertUpdated(before, song.modifyDate)
        assertEquals(LocalDateTime.of(2020, 1, 1, 0, 0), song.releaseDate)

        Thread.sleep(delay)
        song = song.mutate { playCount++ }
        before = assertUpdated(before, song.modifyDate)
        assertEquals(1, song.playCount)

        Thread.sleep(delay)
        song = song.mutate { tags = persistentHashSetOf("A", "B", "C") }
        before = assertUpdated(before, song.modifyDate)
        assertEquals(persistentHashSetOf("A", "B", "C"), song.tags)

        // multiple mutates
        Thread.sleep(delay)
        song = song.mutate { duration = 3000; releaseDate = LocalDateTime.of(2010, 1, 1, 0, 0); }
        before = assertUpdated(before, song.modifyDate)
        assertEquals(3000, song.duration)
        assertEquals(LocalDateTime.of(2010, 1, 1, 0, 0), song.releaseDate)

        Thread.sleep(delay)
        song = song.mutate { title = "title3"; tags = persistentHashSetOf("A", "C", "D") }
        before = assertUpdated(before, song.modifyDate)
        assertEquals("title3", song.title)
        assertEquals(persistentHashSetOf("A", "C", "D"), song.tags)

        Thread.sleep(delay)
        song = song.mutate {
            playCount++; releaseDate = LocalDateTime.of(2000, 1, 1, 0, 0); trackNum = 2
        }
        before = assertUpdated(before, song.modifyDate)
        assertEquals(2, song.playCount)
        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 0), song.releaseDate)
        assertEquals(2, song.trackNum)

        // don't update last modified
        Thread.sleep(delay)
        song = song.mutate(false) {
            duration = 4000; releaseDate = LocalDateTime.of(1990, 1, 1, 0, 0)
        }
        assertEquals(before, song.modifyDate)
        assertEquals(4000, song.duration)
        assertEquals(LocalDateTime.of(1990, 1, 1, 0, 0), song.releaseDate)

        Thread.sleep(delay)
        song = song.mutate(false) { title = "title4"; tags = persistentHashSetOf("B", "D", "E") }
        assertEquals(before, song.modifyDate)
        assertEquals("title4", song.title)
        assertEquals(persistentHashSetOf("B", "D", "E"), song.tags)

        Thread.sleep(delay)
        song = song.mutate(false) {
            playCount++; releaseDate = LocalDateTime.of(1980, 1, 1, 0, 0); trackNum = 3
        }
        assertEquals(before, song.modifyDate)
        assertEquals(3, song.playCount)
        assertEquals(LocalDateTime.of(1980, 1, 1, 0, 0), song.releaseDate)
        assertEquals(3, song.trackNum)

        assertEquals(createDate, song.createDate)
    }
}
