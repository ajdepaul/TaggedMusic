package ajdepaul.taggedmusic

import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.plus
import java.time.LocalDateTime
import kotlin.test.*

class SongTest {

    private fun assertLastModifiedUpdated(before: LocalDateTime, after: LocalDateTime): LocalDateTime {
        assertTrue(before < after)
        return after
    }

    @Test fun testSong() {

        // constructor 1
        var song = Song("file.mp3", SongMetaData(duration=1000))

        assertEquals("file.mp3", song.file)
        assertNotNull(song.dateAdded)
        assertEquals("file.mp3", song.title)
        assertNull(song.artist)
        assertNull(song.album)
        assertNull(song.trackNum)
        assertNull(song.year)
        assertEquals(1000, song.duration)

        assertEquals(0, song.playCount)
        assertTrue(song.tags.isEmpty())

        // constructor 2
        song = Song("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))

        assertEquals("file.mp3", song.file)
        assertNotNull(song.dateAdded)
        assertEquals("title", song.title)
        assertEquals("artist", song.artist)
        assertEquals("album", song.album)
        assertEquals(1, song.trackNum)
        assertEquals(2020, song.year)
        assertEquals(1000, song.duration)

        assertEquals(0, song.playCount)
        assertTrue(song.tags.isEmpty())

        // modifications
        var before: LocalDateTime = song.lastModified

        song.title = "title2"
        before = assertLastModifiedUpdated(before, song.lastModified)
        assertEquals("title2", song.title)

        song.artist = "artist2"
        before = assertLastModifiedUpdated(before, song.lastModified)
        assertEquals("artist2", song.artist)

        song.album = "album2"
        before = assertLastModifiedUpdated(before, song.lastModified)
        assertEquals("album2", song.album)

        song.trackNum = 2
        before = assertLastModifiedUpdated(before, song.lastModified)
        assertEquals(2, song.trackNum)

        song.year = 2021
        before = assertLastModifiedUpdated(before, song.lastModified)
        assertEquals(2021, song.year)

        song.playCount++
        before = assertLastModifiedUpdated(before, song.lastModified)
        assertEquals(1, song.playCount)

        // tags
        song.tags += "A"
        before = assertLastModifiedUpdated(before, song.lastModified)
        assertEquals(setOf("A"), song.tags)

        song.tags += "B"
        before = assertLastModifiedUpdated(before, song.lastModified)
        assertEquals(setOf("A", "B"), song.tags)

        song.tags -= "A"
        before = assertLastModifiedUpdated(before, song.lastModified)
        assertEquals(setOf("B"), song.tags)

        song.tags -= "B"
        assertLastModifiedUpdated(before, song.lastModified)
        assertTrue(song.tags.isEmpty())
    }

    @Test fun testHashAndEquals() {

        var song1 = Song("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
        var song2 = Song("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
        assertEquals(song1, song2)
        assertEquals(song2, song1)
        assertEquals(song1.hashCode(), song2.hashCode())

        assertFalse(song1.equals("string"))

        song1 = Song("file.mp3", SongMetaData("title1", "artist1", "album1", 1, 2021, 1001))
        song2 = Song("file.mp3", SongMetaData("title2", "artist2", "album2", 2, 2022, 1002))
        assertEquals(song1, song2)
        assertEquals(song2, song1)
        assertEquals(song1.hashCode(), song2.hashCode())

        song1 = Song("file1.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
        song2 = Song("file2.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
        assertNotEquals(song1, song2)
        assertNotEquals(song2, song1)
        assertNotEquals(song1.hashCode(), song2.hashCode())
    }

    @Test fun testJson() {

        val song1 = Song("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
            .apply {
                playCount = 10
                tags = persistentHashSetOf("A", "B", "C")
            }

        val song2 = Song.fromJsonData(song1.toJsonData())

        assertEquals(song1.file, song2.file)
        assertEquals(song1.dateAdded.toString(), song2.dateAdded.toString())
        assertEquals(song1.title, song2.title)
        assertEquals(song1.artist, song2.artist)
        assertEquals(song1.album, song2.album)
        assertEquals(song1.trackNum, song2.trackNum)
        assertEquals(song1.year, song2.year)
        assertEquals(song1.duration, song2.duration)
        assertEquals(song1.lastModified, song2.lastModified)
        assertEquals(song1.playCount, song2.playCount)
        assertEquals(song1.tags, song2.tags)
    }

    private class TagUpdateObserver {

        var updated = false
            get() {
                var value = field
                field = false
                return value
            }
        
        val updateFun = { _: Set<String> -> updated = true }
    }

    private class AnyUpdateObserver {

        var updated = false
            get() {
                var value = field
                field = false
                return value
            }

        val updateFun = { _: LocalDateTime -> updated = true }
    }

    @Test fun testObservers() {
        
        var song = Song("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))

        val tagObserver = TagUpdateObserver()
            .also { song.tagUpdateSubject.addObserver(it.updateFun) }
        val anyObserver = AnyUpdateObserver()
            .also { song.anyUpdateSubject.addObserver(it.updateFun) }

        assertFalse(tagObserver.updated)
        assertFalse(anyObserver.updated)

        song.title = "title2"
        assertFalse(tagObserver.updated)
        assertTrue(anyObserver.updated)

        song.artist = "artist2"
        assertFalse(tagObserver.updated)
        assertTrue(anyObserver.updated)

        song.album = "album2"
        assertFalse(tagObserver.updated)
        assertTrue(anyObserver.updated)

        song.trackNum = 2
        assertFalse(tagObserver.updated)
        assertTrue(anyObserver.updated)

        song.year = 2021
        assertFalse(tagObserver.updated)
        assertTrue(anyObserver.updated)

        song.playCount++
        assertFalse(tagObserver.updated)
        assertTrue(anyObserver.updated)

        song.tags += "tag"
        assertTrue(tagObserver.updated)
        assertTrue(anyObserver.updated)
    }
}
