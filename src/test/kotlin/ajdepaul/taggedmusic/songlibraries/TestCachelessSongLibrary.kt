/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.songlibraries

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.librarysources.JsonLibrarySource
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.plus
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestCachelessSongLibrary {

    @Rule
    @JvmField
    val tempDir = TemporaryFolder()

    /** Tests that [CachelessSongLibrary] properly retrieves data from a [JsonLibrarySource]. */
    @Test
    fun assertRetrieving() {
        val jsonLibrarySource = JsonLibrarySource(tempDir.newFile().toPath(), TagType(0))
        val cachelessSongLibrary = CachelessSongLibrary(jsonLibrarySource)

        // default tag type
        assertEquals(TagType(0), cachelessSongLibrary.getDefaultTagType())
        jsonLibrarySource.updater().setDefaultTagType(TagType(100)).commit()
        assertEquals(TagType(100), cachelessSongLibrary.getDefaultTagType())

        // song
        assertFalse(cachelessSongLibrary.hasSong("song1.mp3"))
        assertNull(cachelessSongLibrary.getSong("song1.mp3"))
        assertEquals(mapOf<String, Song>(), cachelessSongLibrary.getAllSongs())

        val song1 = Song("title1", 1000, 1, tags = persistentSetOf("tag1"))
        jsonLibrarySource.updater().putSong("song1.mp3", song1).commit()

        assertTrue(cachelessSongLibrary.hasSong("song1.mp3"))
        assertEquals(song1, cachelessSongLibrary.getSong("song1.mp3"))
        assertEquals(mapOf("song1.mp3" to song1), cachelessSongLibrary.getAllSongs())

        // tag
        assertTrue(cachelessSongLibrary.hasTag("tag1"))
        assertEquals(Tag(null), cachelessSongLibrary.getTag("tag1"))
        assertEquals(mapOf("tag1" to Tag(null)), cachelessSongLibrary.getAllTags())

        val tag2 = Tag("type1", "description")
        jsonLibrarySource.updater().putTag("tag2", tag2).commit()

        assertTrue(cachelessSongLibrary.hasTag("tag2"))
        assertEquals(tag2, cachelessSongLibrary.getTag("tag2"))
        assertEquals(mapOf("tag1" to Tag(null), "tag2" to tag2), cachelessSongLibrary.getAllTags())

        // tag type
        assertTrue(cachelessSongLibrary.hasTagType("type1"))
        assertEquals(TagType(100), cachelessSongLibrary.getTagType("type1"))
        assertEquals(mapOf("type1" to TagType(100)), cachelessSongLibrary.getAllTagTypes())

        val type2 = TagType(5)
        jsonLibrarySource.updater().putTagType("type2", type2).commit()

        assertTrue(cachelessSongLibrary.hasTagType("type2"))
        assertEquals(type2, cachelessSongLibrary.getTagType("type2"))
        assertEquals(
            mapOf("type1" to TagType(100), "type2" to type2),
            cachelessSongLibrary.getAllTagTypes()
        )

        // data
        assertFalse(cachelessSongLibrary.hasData("key"))
        assertNull(cachelessSongLibrary.getData("key"))
        assertEquals(mapOf<String, String>(), cachelessSongLibrary.getAllData())

        jsonLibrarySource.updater().putData("key", "value").commit()

        assertTrue(cachelessSongLibrary.hasData("key"))
        assertEquals("value", cachelessSongLibrary.getData("key"))
        assertEquals(mapOf("key" to "value"), cachelessSongLibrary.getAllData())
    }

    /** Tests that [CachelessSongLibrary] properly updates data on a [JsonLibrarySource]. */
    @Test
    fun testAssertUpdating() {
        val jsonLibrarySource = JsonLibrarySource(tempDir.newFile().toPath(), TagType(0))
        val cachelessSongLibrary = CachelessSongLibrary(jsonLibrarySource)

        // default tag type
        cachelessSongLibrary.setDefaultTagType(TagType(100))
        assertEquals(TagType(100), jsonLibrarySource.getDefaultTagType())

        // put song
        var song1 = Song("title1", 1000, 1, tags = persistentSetOf("tag1"))
        cachelessSongLibrary.putSong("song1.mp3", song1)
        assertEquals(mapOf("song1.mp3" to song1), jsonLibrarySource.getAllSongs())
        assertEquals(mapOf("tag1" to Tag(null)), jsonLibrarySource.getAllTags())

        song1 = song1.mutate { tags += "tag2" }
        cachelessSongLibrary.putSong("song1.mp3", song1)

        assertEquals(mapOf("song1.mp3" to song1), jsonLibrarySource.getAllSongs())
        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null)),
            jsonLibrarySource.getAllTags()
        )

        // put tag
        var tag3 = Tag("type1", "description")
        cachelessSongLibrary.putTag("tag3", tag3)
        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null), "tag3" to tag3),
            jsonLibrarySource.getAllTags()
        )
        assertEquals(mapOf("type1" to TagType(100)), jsonLibrarySource.getAllTagTypes())

        tag3 = tag3.mutate { type = "type2" }
        cachelessSongLibrary.putTag("tag3", tag3)

        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null), "tag3" to tag3),
            jsonLibrarySource.getAllTags()
        )
        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100)),
            jsonLibrarySource.getAllTagTypes()
        )

        // put tag type
        var tagType3 = TagType(5)
        cachelessSongLibrary.putTagType("type3", tagType3)
        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100), "type3" to tagType3),
            jsonLibrarySource.getAllTagTypes()
        )

        tagType3 = tagType3.mutate { color = 6 }
        cachelessSongLibrary.putTagType("type3", tagType3)

        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100), "type3" to tagType3),
            jsonLibrarySource.getAllTagTypes()
        )

        // put data
        cachelessSongLibrary.putData("key", "value")
        assertEquals(mapOf("key" to "value"), jsonLibrarySource.getAllData())

        cachelessSongLibrary.putData("key", "value2")

        assertEquals(mapOf("key" to "value2"), jsonLibrarySource.getAllData())

        // remove song
        cachelessSongLibrary.removeSong("song1.mp3")
        assertTrue(jsonLibrarySource.getAllSongs().isEmpty())

        // remove tag
        cachelessSongLibrary.removeTag("tag2")
        assertEquals(mapOf("tag1" to Tag(null), "tag3" to tag3), jsonLibrarySource.getAllTags())

        // remove tag type
        cachelessSongLibrary.removeTagType("type2")
        assertEquals(
            mapOf("type1" to TagType(100), "type3" to tagType3),
            jsonLibrarySource.getAllTagTypes()
        )

        // remove data
        cachelessSongLibrary.removeData("key")
        assertTrue(jsonLibrarySource.getAllData().isEmpty())
    }
}
