package ajdepaul.taggedmusic

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

import java.time.LocalDateTime

class TestSongTest {

    private fun assertUpdated(before: LocalDateTime, after: LocalDateTime): LocalDateTime {
        assertTrue(before < after)
        return after
    }

    @Test fun testSongLibrary() {
        val songLibrary = SongLibrary()
        assertTrue(songLibrary.tags.isEmpty())
        assertTrue(songLibrary.songs.isEmpty())
        assertTrue(songLibrary.tagTypes.isEmpty())

        // modifications
        var before: LocalDateTime = songLibrary.lastModified
        
        val song = Song("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))

        songLibrary.songs += song
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf(song), songLibrary.songs)

        songLibrary.songs -= song
        before = assertUpdated(before, songLibrary.lastModified)
        assertTrue(songLibrary.songs.isEmpty())

        songLibrary.tagTypes += TagType("type", 1)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf(TagType("type", 1)), songLibrary.tagTypes)

        songLibrary.tagTypes -= TagType("type", 1)
        before = assertUpdated(before, songLibrary.lastModified)
        assertTrue(songLibrary.tagTypes.isEmpty())

        songLibrary.tagToType += Pair("tag", TagType("type", 1))
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf(Pair("tag", TagType("type", 1))), songLibrary.tagToType)

        songLibrary.tagToType -= "tag"
        assertUpdated(before, songLibrary.lastModified)
        assertTrue(songLibrary.tagToType.isEmpty())
    }

    @Test fun testSongUpdates() {
        val songLibrary = SongLibrary()
        val song = Song("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
            .also { songLibrary.songs += it }

        var before: LocalDateTime = songLibrary.lastModified

        song.title =  "title2"
        before = assertUpdated(before, songLibrary.lastModified)

        song.artist = "artist2"
        before = assertUpdated(before, songLibrary.lastModified)

        song.album = "album2"
        before = assertUpdated(before, songLibrary.lastModified)

        song.trackNum = 2
        before = assertUpdated(before, songLibrary.lastModified)

        song.year = 2021
        before = assertUpdated(before, songLibrary.lastModified)

        song.playCount++
        assertUpdated(before, songLibrary.lastModified)
    }

    @Test fun testTagUpdates() {
        val songLibrary = SongLibrary()
        val song1 = Song("file1.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
            .also { songLibrary.songs += it }

        val song2 = Song("file2.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
            .also { songLibrary.songs += it }

        var before: LocalDateTime = songLibrary.lastModified

        // adding tags by adding tags to songs
        song1.tags += "tagA"
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagA"), songLibrary.tags)

        song2.tags += "tagA"
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagA"), songLibrary.tags)

        song1.tags += "tagB"
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagA", "tagB"), songLibrary.tags)

        song2.tags += "tagB"
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagA", "tagB"), songLibrary.tags)

        // removing tags by removing tags from songs
        song1.tags -= "tagA"
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagA", "tagB"), songLibrary.tags)

        song2.tags -= "tagA"
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagB"), songLibrary.tags)
        
        song1.tags -= "tagB"
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagB"), songLibrary.tags)

        song2.tags -= "tagB"
        before = assertUpdated(before, songLibrary.lastModified)
        assertTrue(songLibrary.tags.isEmpty())

        // adding and removing tags from songs at the same time
        song1.tags = setOf("tagA", "tagB")
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagA", "tagB"), songLibrary.tags)

        song2.tags = setOf("tagA", "tagC")
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagA", "tagB", "tagC"), songLibrary.tags)

        song1.tags = setOf("tagD")  // added D and removed A & B (but A gets to stay)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagA", "tagC", "tagD"), songLibrary.tags)

        // removing tags by removing songs
        songLibrary.songs -= song1
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagA", "tagC"), songLibrary.tags)

        songLibrary.songs -= song2
        before = assertUpdated(before, songLibrary.lastModified)
        assertTrue(songLibrary.tags.isEmpty())

        // adding tags by adding songs
        songLibrary.songs += song1
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagD"), songLibrary.tags)

        songLibrary.songs += song2
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagA", "tagC", "tagD"), songLibrary.tags)

        // adding and removing songs at the same time
        song1.tags = setOf("tagA", "tagB")
        assertEquals(setOf("tagA", "tagB", "tagC"), songLibrary.tags)

        song2.tags = setOf("tagA", "tagB", "tagC", "tagD")
        assertEquals(setOf("tagA", "tagB", "tagC", "tagD"), songLibrary.tags)

        val song3 = Song("file3.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
            .apply { tags = setOf("tagB", "tagC", "tagE") }

        // remove a song that has a tag that should stay
        // remove a song that has a tag that should be removed
        // add a song that has a tag that is already there
        // add a song that has a tag that should be added
        // add and remove a tag so that the tag should stay as a result
        songLibrary.songs = songLibrary.songs - song2 + song3
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(setOf("tagA", "tagB", "tagC", "tagE"), songLibrary.tags)

        // removed songs no longer update the songlibrary
        song2.tags = setOf("tagF")
        assertEquals(before, songLibrary.lastModified)
        assertEquals(setOf("tagA", "tagB", "tagC", "tagE"), songLibrary.tags)

        songLibrary.songs = setOf()
        before = assertUpdated(before, songLibrary.lastModified)
        assertTrue(songLibrary.songs.isEmpty())

        song1.tags = setOf("tagA")
        assertEquals(before, songLibrary.lastModified)
        assertTrue(songLibrary.songs.isEmpty())

        song2.tags = setOf("tagA")
        assertEquals(before, songLibrary.lastModified)
        assertTrue(songLibrary.songs.isEmpty())
    }

    @Test fun testTagTypes() {
        val songLibrary = SongLibrary()
        val song = Song("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
            .apply { tags = setOf("tagA", "tagB") }
            .also { songLibrary.songs += it }

        // song.tagTypes += TagType("tagType", 1)

        // TODO implement
    }

    @Test fun testTagFilter() {
        val songLibrary = SongLibrary()
        // val song = Song("path")
        // songLibrary.songs.add(song)

        // TODO implement
    }

    @Test fun testJson() {
        // TODO implement
    }

    @Test fun testObservers() {
        // TODO implement
    }
}
