package ajdepaul.taggedmusic

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
        before = assertUpdated(before, songLibrary.lastModified)
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
        before = assertUpdated(before, songLibrary.lastModified)
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
        // TODO implement
    }

    @Test fun testJson() {
        // TODO implement
    }

    @Test fun testObservers() {
        // TODO implement
    }
}
