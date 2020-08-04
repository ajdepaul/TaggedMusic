package ajdepaul.taggedmusic

import kotlinx.collections.immutable.*

import java.time.LocalDateTime
import kotlin.test.*

class TestDataClassesTest {

    private fun assertUpdated(before: LocalDateTime, after: LocalDateTime): LocalDateTime {
        assertTrue(before < after)
        return after
    }

    @Test fun testSongLibrary() {
//        val songLibrary = SongLibrary()
//        assertTrue(songLibrary.tags.isEmpty())
//        assertTrue(songLibrary.songs.isEmpty())
//        assertTrue(songLibrary.tagTypes.isEmpty())
//
//        // modifications
//        var before: LocalDateTime = songLibrary.lastModified
//
//        val song = DataClasses("file.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
//
//        songLibrary.songs += song
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(persistentHashSetOf(song), songLibrary.songs)
//
//        // tags
//        songLibrary.tags += Pair("A", null)
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null)), songLibrary.tags)
//
//        songLibrary.tags += Pair("B", "tagType")
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", "tagType")), songLibrary.tags)
//        // tag
//        assertEquals(mapOf(Pair("tagType", songLibrary.defaultTagType)), songLibrary.tagTypes)
//
//        songLibrary.tags -= "A"
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("B", "tagType")), songLibrary.tags)
//
//        songLibrary.tags -= "B"
//        assertUpdated(before, songLibrary.lastModified)
//        assertTrue(songLibrary.tags.isEmpty())
//
//        // default tag type
//        songLibrary.defaultTagType = TagTypeData(1)
//        assertEquals(TagTypeData(1), songLibrary.defaultTagType)
//
//        songLibrary.defaultTagType = TagTypeData(2)
//        assertEquals(TagTypeData(2), songLibrary.defaultTagType)
//
//        // tag types
//        songLibrary.tagTypes = persistentHashMapOf()
//        assertTrue(songLibrary.tagTypes.isEmpty())
//
//        songLibrary.tagTypes += Pair("tagType1", TagTypeData(100))
//        assertEquals(mapOf(Pair("tagType1", TagTypeData(100))), songLibrary.tagTypes)
//
//        songLibrary.tagTypes += Pair("tagType2", TagTypeData(200))
//        assertEquals(mapOf(Pair("tagType1", TagTypeData(100)), Pair("tagType2", TagTypeData(200))), songLibrary.tagTypes)
//
//        songLibrary.tagTypes -= "tagType1"
//        assertEquals(mapOf(Pair("tagType2", TagTypeData(200))), songLibrary.tagTypes)
//
//        songLibrary.tagTypes -= "tagType2"
//        assertTrue(songLibrary.tagTypes.isEmpty())
    }

    @Test fun testSongUpdates() {
//        val songLibrary = SongLibrary()
//
//        val song1 = DataClasses("file1.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
//            .also { songLibrary.songs += it }
//
//        // general
//        var before: LocalDateTime = songLibrary.lastModified
//        song1.title =  "title2"
//        before = assertUpdated(before, songLibrary.lastModified)
//
//        song1.artist = "artist2"
//        before = assertUpdated(before, songLibrary.lastModified)
//
//        song1.album = "album2"
//        before = assertUpdated(before, songLibrary.lastModified)
//
//        song1.trackNum = 2
//        before = assertUpdated(before, songLibrary.lastModified)
//
//        song1.year = 2021
//        before = assertUpdated(before, songLibrary.lastModified)
//
//        song1.playCount++
//        before = assertUpdated(before, songLibrary.lastModified)
//
//        // tags
//        song1.tags += "A"
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null)), songLibrary.tags)
//
//        song1.tags += "B"
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", null)), songLibrary.tags)
//
//        song1.tags -= "A"
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", null)), songLibrary.tags)
//
//        song1.tags -= "B"
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", null)), songLibrary.tags)
//
//        val song2 = DataClasses("file2.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
//                .also { songLibrary.songs += it }
//
//        song2.tags += "A"
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", null)), songLibrary.tags)
//
//        song2.tags += "B"
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", null)), songLibrary.tags)
//
//        song2.tags -= "A"
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", null)), songLibrary.tags)
//
//        song2.tags -= "B"
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", null)), songLibrary.tags)
//
//        song2.tags += "C"
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", null), Pair("C", null)), songLibrary.tags)
//
//        song2.tags -= "C"
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", null), Pair("C", null)), songLibrary.tags)
//
//        // removing songs
//        songLibrary.songs -= song1
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", null), Pair("C", null)), songLibrary.tags)
//
//        songLibrary.songs -= song1
//        assertUpdated(before, songLibrary.lastModified)
//        assertEquals(mapOf<String, String?>(Pair("A", null), Pair("B", null), Pair("C", null)), songLibrary.tags)
    }

    @Test fun testTagUpdates() {
        val songLibrary = SongLibrary()

        // removing tags removes them from songs

//
//        // removing tags by removing songs
//        songLibrary.songs -= song1
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(persistentHashSetOf("tagA", "tagC"), songLibrary.tags)
//
//        songLibrary.songs -= song2
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertTrue(songLibrary.tags.isEmpty())
//
//        // adding tags by adding songs
//        songLibrary.songs += song1
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(persistentHashSetOf("tagD"), songLibrary.tags)
//
//        songLibrary.songs += song2
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(persistentHashSetOf("tagA", "tagC", "tagD"), songLibrary.tags)
//
//        // adding and removing songs at the same time
//        song1.tags = persistentHashSetOf("tagA", "tagB")
//        assertEquals(persistentHashSetOf("tagA", "tagB", "tagC"), songLibrary.tags)
//
//        song2.tags = persistentHashSetOf("tagA", "tagB", "tagC", "tagD")
//        assertEquals(persistentHashSetOf("tagA", "tagB", "tagC", "tagD"), songLibrary.tags)
//
//        val song3 = Song("file3.mp3", SongMetaData("title", "artist", "album", 1, 2020, 1000))
//            .apply { tags = persistentHashSetOf("tagB", "tagC", "tagE") }
//
//        // remove a song that has a tag that should stay
//        // remove a song that has a tag that should be removed
//        // add a song that has a tag that is already there
//        // add a song that has a tag that should be added
//        // add and remove a tag so that the tag should stay as a result
//        songLibrary.songs = songLibrary.songs - song2 + song3
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertEquals(persistentHashSetOf("tagA", "tagB", "tagC", "tagE"), songLibrary.tags)
//
//        // removed songs no longer update the songlibrary
//        song2.tags = persistentHashSetOf("tagF")
//        assertEquals(before, songLibrary.lastModified)
//        assertEquals(persistentHashSetOf("tagA", "tagB", "tagC", "tagE"), songLibrary.tags)
//
//        songLibrary.songs = persistentHashSetOf()
//        before = assertUpdated(before, songLibrary.lastModified)
//        assertTrue(songLibrary.songs.isEmpty())
//
//        song1.tags = persistentHashSetOf("tagA")
//        assertEquals(before, songLibrary.lastModified)
//        assertTrue(songLibrary.songs.isEmpty())
//
//        song2.tags = persistentHashSetOf("tagA")
//        assertEquals(before, songLibrary.lastModified)
//        assertTrue(songLibrary.songs.isEmpty())
    }

    @Test fun testTagTypeUpdates() {
    }

    @Test fun testTagFilter() {
        // val songLibrary = SongLibrary()
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
