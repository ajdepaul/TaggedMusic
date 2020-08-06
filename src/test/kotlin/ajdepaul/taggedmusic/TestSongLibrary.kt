package ajdepaul.taggedmusic

import com.google.gson.Gson
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.time.LocalDateTime
import kotlinx.collections.immutable.*

class TestSongLibrary {

    private fun assertUpdated(before: LocalDateTime, after: LocalDateTime): LocalDateTime {
        assertTrue(before < after)
        return after
    }

    @Test fun testSongLibrary() {
        val songLibrary = SongLibrary(TagType(0))

        assertTrue(songLibrary.songs.isEmpty())
        assertTrue(songLibrary.tags.isEmpty())
        assertEquals(mapOf<String?, TagType>(null to TagType(0)), songLibrary.tagTypes)
    }

    @Test fun testSongsModifications() {
        val songLibrary = SongLibrary(TagType(0))
        var before = songLibrary.lastModified

        // adding songs
        var song1 = Song("title1", 1000)
        songLibrary.putSong("song1.mp3", song1)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf("song1.mp3" to song1), songLibrary.songs)

        var song2 = Song("title2", 2000).mutate { tags += "A" }
        songLibrary.putSong("song2.mp3", song2)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2), songLibrary.songs)
        assertEquals(mapOf("A" to Tag(null, null)), songLibrary.tags)

        // modifying songs
        song1 = song1.mutate { tags += "B" }
        songLibrary.putSong("song1.mp3", song1)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2), songLibrary.songs)
        assertEquals(mapOf("A" to Tag(null, null), "B" to Tag(null, null)), songLibrary.tags)

        song2 = song2.mutate { playCount += + 1 }
        songLibrary.putSong("song2.mp3", song2)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2), songLibrary.songs)
        assertEquals(mapOf("A" to Tag(null, null), "B" to Tag(null, null)), songLibrary.tags)

        // removing songs
        songLibrary.removeSong("song1.mp3")
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf("song2.mp3" to song2), songLibrary.songs)
        assertEquals(mapOf("A" to Tag(null, null), "B" to Tag(null, null)), songLibrary.tags)

        songLibrary.removeSong("song2.mp3")
        assertUpdated(before, songLibrary.lastModified)
        assertTrue(songLibrary.songs.isEmpty())
        assertEquals(mapOf("A" to Tag(null, null), "B" to Tag(null, null)), songLibrary.tags)
    }

    @Test fun testTagsModifications() {
        val songLibrary = SongLibrary(TagType(0))
        var before = songLibrary.lastModified

        // adding songs
        var tag1 = Tag(null, null)
        songLibrary.putTag("tag1", tag1)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf("tag1" to tag1), songLibrary.tags)

        var tag2 = Tag("tagType2", "description2")
        songLibrary.putTag("tag2", tag2)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf("tag1" to tag1, "tag2" to tag2), songLibrary.tags)
        assertEquals(mapOf(null to songLibrary.defaultTagType, "tagType2" to songLibrary.defaultTagType),
                songLibrary.tagTypes)

        // modifying tags
        tag1 = tag1.mutate { type = "tagType1"; description = "description1" }
        songLibrary.putTag("tag1", tag1)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf("tag1" to tag1, "tag2" to tag2), songLibrary.tags)
        assertEquals(mapOf(null to songLibrary.defaultTagType, "tagType2" to songLibrary.defaultTagType, "tagType1" to songLibrary.defaultTagType),
                songLibrary.tagTypes)

        tag2 = tag2.mutate { description = null }
        songLibrary.putTag("tag2", tag2)
        assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf("tag1" to tag1, "tag2" to tag2), songLibrary.tags)

        // removing tags
        songLibrary.putSong("song.mp3", Song("title", 1000).mutate { tags += "tag1" })
        before = songLibrary.lastModified

        songLibrary.removeTag("tag1")
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf("tag2" to tag2), songLibrary.tags)
        assertTrue(songLibrary.songs["song.mp3"]!!.tags.isEmpty())

        songLibrary.removeTag("tag2")
        assertUpdated(before, songLibrary.lastModified)
        assertTrue(songLibrary.tags.isEmpty())
        assertEquals(mapOf(null to songLibrary.defaultTagType, "tagType2" to songLibrary.defaultTagType, "tagType1" to songLibrary.defaultTagType),
                songLibrary.tagTypes)
    }

    @Test fun testTagTypesModifications() {
        val songLibrary = SongLibrary(TagType(0))
        var before = songLibrary.lastModified

        // adding tagTypes
        var tagType1 = TagType(10)
        songLibrary.putTagType("tagType1", tagType1)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf(null to songLibrary.defaultTagType, "tagType1" to tagType1), songLibrary.tagTypes)

        var tagType2 = TagType(20)
        songLibrary.putTagType("tagType2", tagType2)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf(null to songLibrary.defaultTagType, "tagType1" to tagType1, "tagType2" to tagType2),
                songLibrary.tagTypes)

        // modifying tagTypes
        tagType1 = TagType(30)
        songLibrary.putTagType("tagType1", tagType1)
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf(null to songLibrary.defaultTagType, "tagType1" to tagType1, "tagType2" to tagType2),
                songLibrary.tagTypes)

        tagType2 = TagType(40)
        songLibrary.putTagType("tagType2", tagType2)
        assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf(null to songLibrary.defaultTagType, "tagType1" to tagType1, "tagType2" to tagType2),
                songLibrary.tagTypes)

        // removing tagTypes
        songLibrary.putTag("tag", Tag("tagType1"))
        before = songLibrary.lastModified

        songLibrary.removeTagType("tagType1")
        before = assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf(null to songLibrary.defaultTagType, "tagType2" to tagType2),
                songLibrary.tagTypes)
        assertNull(songLibrary.tags["tag"]!!.type)

        songLibrary.removeTagType("tagType1")
        assertUpdated(before, songLibrary.lastModified)
        assertEquals(mapOf(null to songLibrary.defaultTagType, "tagType2" to tagType2),
                songLibrary.tagTypes)
    }

    @Test fun testTagFilter() {
        val songLibrary = SongLibrary(TagType(0))
        val song1 = Song("title1", 1000).mutate { this.tags += setOf("A", "B",      "D") }
        val song2 = Song("title2", 2000).mutate { this.tags += setOf("A", "B", "C", "E") }
        val song3 = Song("title3", 3000).mutate { this.tags += setOf("A",      "C", "F") }
        songLibrary.putSong("song1.mp3", song1)
        songLibrary.putSong("song2.mp3", song2)
        songLibrary.putSong("song3.mp3", song3)

        assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2, "song3.mp3" to song3), songLibrary.tagFilter())

        // include tags
        assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2, "song3.mp3" to song3),
                songLibrary.tagFilter(setOf("A")))
        assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2), songLibrary.tagFilter(setOf("B")))
        assertEquals(mapOf("song2.mp3" to song2, "song3.mp3" to song3), songLibrary.tagFilter(setOf("C")))
        assertEquals(mapOf("song1.mp3" to song1), songLibrary.tagFilter(setOf("D")))
        assertEquals(mapOf("song2.mp3" to song2), songLibrary.tagFilter(setOf("E")))
        assertEquals(mapOf("song3.mp3" to song3), songLibrary.tagFilter(setOf("F")))

        assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2), songLibrary.tagFilter(setOf("A", "B")))
        assertEquals(mapOf("song2.mp3" to song2, "song3.mp3" to song3), songLibrary.tagFilter(setOf("A", "C")))
        assertEquals(mapOf("song2.mp3" to song2), songLibrary.tagFilter(setOf("B", "C")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("B", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("C", "D")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("D", "E")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("D", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("E", "F")))

        assertEquals(mapOf("song1.mp3" to song1), songLibrary.tagFilter(setOf("A", "B", "D")))
        assertEquals(mapOf("song3.mp3" to song3), songLibrary.tagFilter(setOf("A", "C", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("A", "B", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("A", "C", "D")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("D", "E", "F")))

        assertEquals(mapOf(), songLibrary.tagFilter(setOf("A", "B", "C", "D")))
        assertEquals(mapOf("song2.mp3" to song2), songLibrary.tagFilter(setOf("A", "B", "C", "E")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("A", "B", "C", "F")))

        // exclude tags
        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("A")))
        assertEquals(mapOf("song3.mp3" to song3), songLibrary.tagFilter(excludeTags = setOf("B")))
        assertEquals(mapOf("song1.mp3" to song1), songLibrary.tagFilter(excludeTags = setOf("C")))
        assertEquals(mapOf("song2.mp3" to song2, "song3.mp3" to song3), songLibrary.tagFilter(excludeTags = setOf("D")))
        assertEquals(mapOf("song1.mp3" to song1, "song3.mp3" to song3), songLibrary.tagFilter(excludeTags = setOf("E")))
        assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2), songLibrary.tagFilter(excludeTags = setOf("F")))

        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("A", "B")))
        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("A", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("B", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("C", "D")))
        assertEquals(mapOf("song3.mp3" to song3), songLibrary.tagFilter(excludeTags = setOf("B", "D")))
        assertEquals(mapOf("song1.mp3" to song1), songLibrary.tagFilter(excludeTags = setOf("C", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("B", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("C", "D")))
        assertEquals(mapOf("song3.mp3" to song3), songLibrary.tagFilter(excludeTags = setOf("D", "E")))
        assertEquals(mapOf("song2.mp3" to song2), songLibrary.tagFilter(excludeTags = setOf("D", "F")))
        assertEquals(mapOf("song1.mp3" to song1), songLibrary.tagFilter(excludeTags = setOf("E", "F")))

        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("A", "B", "C")))
        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("A", "B", "D")))
        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("B", "C", "D")))
        assertEquals(mapOf("song3.mp3" to song3), songLibrary.tagFilter(excludeTags = setOf("B", "D", "E")))
        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("B", "D", "F")))
        assertEquals(mapOf("song1.mp3" to song1), songLibrary.tagFilter(excludeTags = setOf("C", "E", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("C", "E", "D")))
        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("D", "E", "F")))

        assertEquals(mapOf(), songLibrary.tagFilter(excludeTags = setOf("B", "D", "E", "F")))

        // include and exclude tags
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("A"), setOf("A")))
        assertEquals(mapOf("song3.mp3" to song3), songLibrary.tagFilter(setOf("A"), setOf("B")))
        assertEquals(mapOf("song2.mp3" to song2, "song3.mp3" to song3), songLibrary.tagFilter(setOf("A"), setOf("D")))
        assertEquals(mapOf("song2.mp3" to song2), songLibrary.tagFilter(setOf("B"), setOf("D")))
        assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2), songLibrary.tagFilter(setOf("B"), setOf("F")))
        assertEquals(mapOf("song1.mp3" to song1), songLibrary.tagFilter(setOf("D"), setOf("E")))

        assertEquals(mapOf(), songLibrary.tagFilter(setOf("A", "B"), setOf("D", "E")))
        assertEquals(mapOf("song2.mp3" to song2), songLibrary.tagFilter(setOf("A", "B"), setOf("D", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("B", "C"), setOf("D", "E")))
        assertEquals(mapOf("song2.mp3" to song2), songLibrary.tagFilter(setOf("B", "C"), setOf("D", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("D", "E"), setOf("C", "F")))

        assertEquals(mapOf("song2.mp3" to song2), songLibrary.tagFilter(setOf("A", "B", "C"), setOf("D", "F")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("A", "B", "C"), setOf("D", "E", "F")))

        // tags that don't exist
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("Z")))
        assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2, "song3.mp3" to song3),
                songLibrary.tagFilter(excludeTags = setOf("Z")))
        assertEquals(mapOf(), songLibrary.tagFilter(setOf("Z"), setOf("Y")))
    }

    @Test fun testJson() {
        val songLibrary = SongLibrary(TagType(0))

        songLibrary.putSong("song1.mp3", Song("title1", 1000).mutate { setOf("A", "C") })
        songLibrary.putSong("song2.mp3", Song("title2", 2000).mutate {
            artist = "artist2"
            album = "album2"
            trackNum = 2
            year = 2002
            playCount = 2
            tags += setOf("A", "B")
        })

        songLibrary.putTag("A", Tag("tagType1", "descriptionA"))
        songLibrary.putTag("B", Tag("tagType2", "descriptionB"))
        songLibrary.putTag("D", Tag("tagType2"))

        songLibrary.putTagType("tagType2", TagType(2))
        songLibrary.putTagType("tagType3", TagType(3))

        songLibrary.defaultTagType = TagType(4)

        val loadedSongLibrary = Gson().fromJson(Gson().toJson(songLibrary.toJsonData()), SongLibrary.JsonData::class.java).toSongLibrary()

        assertEquals(songLibrary.songs, loadedSongLibrary.songs)
        assertEquals(songLibrary.tags, loadedSongLibrary.tags)
        assertEquals(songLibrary.tagTypes, loadedSongLibrary.tagTypes)
        assertEquals(songLibrary.defaultTagType, loadedSongLibrary.defaultTagType)
    }
}
