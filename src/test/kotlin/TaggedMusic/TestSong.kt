package TaggedMusic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

import TaggedMusic.SongMetaData

class SongTest {

    @Test fun testSong() {

        // constructor 1
        var metaData = SongMetaData(duration=1000)
        var song = Song("file.mp3", metaData)

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
        metaData = SongMetaData("title", "artist", "album", 1, 2020, 1000)
        song = Song("file.mp3", metaData)

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
        song.title = "title2"
        assertEquals("title2", song.title)
        song.artist = "artist2"
        assertEquals("artist2", song.artist)
        song.album = "album2"
        assertEquals("album2", song.album)
        song.trackNum = 2
        assertEquals(2, song.trackNum)
        song.year = 2021
        assertEquals(2021, song.year)
        song.playCount++
        assertEquals(1, song.playCount)

        song.tags += "A"
        assertTrue(song.tags.contains("A"))
        assertFalse(song.tags.contains("B"))

        song.tags += "B"
        assertTrue(song.tags.contains("A"))
        assertTrue(song.tags.contains("B"))

        song.tags -= "A"
        assertFalse(song.tags.contains("A"))
        assertTrue(song.tags.contains("B"))

        song.tags -= "B"
        assertFalse(song.tags.contains("A"))
        assertFalse(song.tags.contains("B"))
        assertTrue(song.tags.isEmpty())
    }

    @Test fun testEquals() {

        var song1 = Song("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
        var song2 = Song("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
        assertTrue(song1 == song2)
        assertTrue(song2 == song1)

        assertFalse(song1.equals("string"))

        song1 = Song("file.mp3", SongMetaData("title1", "artist1", "album1", 1, 2021, 1001))
        song2 = Song("file.mp3", SongMetaData("title2", "artist2", "album2", 2, 2022, 1002))
        assertTrue(song1 == song2)
        assertTrue(song2 == song1)

        song1 = Song("file1.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
        song2 = Song("file2.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
        assertFalse(song1 == song2)
        assertFalse(song2 == song1)
    }

    @Test fun testJson() {
        
        val song1 = Song("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
        song1.playCount = 10
        song1.tags = setOf("A", "B", "C")

        val song2 = Song.fromJson(song1.toJson())

        assertEquals(song1.file, song2.file)
        assertEquals(song1.dateAdded.toString(), song2.dateAdded.toString())
        assertEquals(song1.title, song2.title)
        assertEquals(song1.artist, song2.artist)
        assertEquals(song1.album, song2.album)
        assertEquals(song1.trackNum, song2.trackNum)
        assertEquals(song1.year, song2.year)
        assertEquals(song1.duration, song2.duration)
        assertEquals(song1.playCount, song2.playCount)
        assertEquals(song1.tags, song2.tags)
    }
}
