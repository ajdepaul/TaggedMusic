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
import kotlin.test.assertTrue

class TestCachedSongLibrary {

    @Rule
    @JvmField
    val tempDir = TemporaryFolder()

    /** Tests that [CachedSongLibrary] properly retrieves data from a [JsonLibrarySource]. */
    @Test
    fun assertRetrieving() {
        val jsonLibrarySource = JsonLibrarySource(tempDir.newFile().toPath(), TagType(0))

        val defaultTagType = TagType(100)
        val tagType1 = TagType(5)
        val tag1 = Tag("type1", "description")
        val song1 = Song("title1", 1000, 1, tags = persistentSetOf("tag1"))

        jsonLibrarySource.updater()
            .setDefaultTagType(defaultTagType)
            .putTagType("type1", tagType1)
            .putTag("tag1", tag1)
            .putSong("song1.mp3", song1)
            .putData("key", "value")
            .commit()

        val cachedSongLibrary = CachedSongLibrary(jsonLibrarySource)

        // default tag type
        assertEquals(defaultTagType, cachedSongLibrary.getDefaultTagType())
        jsonLibrarySource.updater().setDefaultTagType(TagType(200)).commit()
        assertEquals(defaultTagType, cachedSongLibrary.getDefaultTagType())

        // song
        assertTrue(cachedSongLibrary.hasSong("song1.mp3"))
        assertEquals(song1, cachedSongLibrary.getSong("song1.mp3"))
        assertEquals(mapOf("song1.mp3" to song1), cachedSongLibrary.getAllSongs())

        jsonLibrarySource.updater()
            .putSong("song2.mp3", Song("title2", 2000, 2, tags = persistentSetOf("tag2")))
            .commit()

        assertTrue(cachedSongLibrary.hasSong("song1.mp3"))
        assertEquals(song1, cachedSongLibrary.getSong("song1.mp3"))
        assertEquals(mapOf("song1.mp3" to song1), cachedSongLibrary.getAllSongs())

        // tag
        assertTrue(cachedSongLibrary.hasTag("tag1"))
        assertEquals(tag1, cachedSongLibrary.getTag("tag1"))
        assertEquals(mapOf("tag1" to tag1), cachedSongLibrary.getAllTags())

        jsonLibrarySource.updater().putTag("tag2", Tag("type2", "description2")).commit()

        assertTrue(cachedSongLibrary.hasTag("tag1"))
        assertEquals(tag1, cachedSongLibrary.getTag("tag1"))
        assertEquals(mapOf("tag1" to tag1), cachedSongLibrary.getAllTags())

        // tag type
        assertTrue(cachedSongLibrary.hasTagType("type1"))
        assertEquals(tagType1, cachedSongLibrary.getTagType("type1"))
        assertEquals(mapOf("type1" to tagType1), cachedSongLibrary.getAllTagTypes())

        jsonLibrarySource.updater().putTagType("type2", TagType(15)).commit()

        assertTrue(cachedSongLibrary.hasTagType("type1"))
        assertEquals(tagType1, cachedSongLibrary.getTagType("type1"))
        assertEquals(mapOf("type1" to tagType1), cachedSongLibrary.getAllTagTypes())

        // data
        assertTrue(cachedSongLibrary.hasData("key"))
        assertEquals("value", cachedSongLibrary.getData("key"))
        assertEquals(mapOf("key" to "value"), cachedSongLibrary.getAllData())

        jsonLibrarySource.updater().putData("key2", "value2").commit()

        assertTrue(cachedSongLibrary.hasData("key"))
        assertEquals("value", cachedSongLibrary.getData("key"))
        assertEquals(mapOf("key" to "value"), cachedSongLibrary.getAllData())
    }

    /** Tests that [CachedSongLibrary] properly updates data on a [JsonLibrarySource]. */
    @Test
    fun testAssertUpdating() {
        val jsonLibrarySource = JsonLibrarySource(tempDir.newFile().toPath(), TagType(0))
        val cachedSongLibrary = CachedSongLibrary(jsonLibrarySource)

        // default tag type
        cachedSongLibrary.setDefaultTagType(TagType(100))
        assertEquals(TagType(100), cachedSongLibrary.getDefaultTagType())
        assertEquals(TagType(0), jsonLibrarySource.getDefaultTagType())
        cachedSongLibrary.commit()
        assertEquals(TagType(100), jsonLibrarySource.getDefaultTagType())

        // put song
        val song1 = Song("title1", 1000, 1, tags = persistentSetOf("tag1"))
        cachedSongLibrary.putSong("song1.mp3", song1)
        assertEquals(mapOf("song1.mp3" to song1), cachedSongLibrary.getAllSongs())
        assertEquals(mapOf("tag1" to Tag(null)), cachedSongLibrary.getAllTags())

        assertTrue(jsonLibrarySource.getAllSongs().isEmpty())
        assertTrue(jsonLibrarySource.getAllTags().isEmpty())

        cachedSongLibrary.commit()

        assertEquals(mapOf("song1.mp3" to song1), jsonLibrarySource.getAllSongs())
        assertEquals(mapOf("tag1" to Tag(null)), jsonLibrarySource.getAllTags())

        val mutatedSong1 = song1.mutate { tags += "tag2" }
        cachedSongLibrary.putSong("song1.mp3", mutatedSong1)

        assertEquals(mapOf("song1.mp3" to mutatedSong1), cachedSongLibrary.getAllSongs())
        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null)),
            cachedSongLibrary.getAllTags()
        )

        assertEquals(mapOf("song1.mp3" to song1), jsonLibrarySource.getAllSongs())
        assertEquals(mapOf("tag1" to Tag(null)), jsonLibrarySource.getAllTags())

        cachedSongLibrary.commit()

        assertEquals(mapOf("song1.mp3" to mutatedSong1), jsonLibrarySource.getAllSongs())
        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null)),
            jsonLibrarySource.getAllTags()
        )

        // put tag
        val tag3 = Tag("type1", "description")
        cachedSongLibrary.putTag("tag3", tag3)

        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null), "tag3" to tag3),
            cachedSongLibrary.getAllTags()
        )
        assertEquals(mapOf("type1" to TagType(100)), cachedSongLibrary.getAllTagTypes())

        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null)),
            jsonLibrarySource.getAllTags()
        )
        assertTrue(jsonLibrarySource.getAllTagTypes().isEmpty())

        cachedSongLibrary.commit()

        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null), "tag3" to tag3),
            jsonLibrarySource.getAllTags()
        )
        assertEquals(mapOf("type1" to TagType(100)), jsonLibrarySource.getAllTagTypes())

        val mutatedTag3 = tag3.mutate { type = "type2" }
        cachedSongLibrary.putTag("tag3", mutatedTag3)

        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null), "tag3" to mutatedTag3),
            cachedSongLibrary.getAllTags()
        )
        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100)),
            cachedSongLibrary.getAllTagTypes()
        )

        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null), "tag3" to tag3),
            jsonLibrarySource.getAllTags()
        )
        assertEquals(mapOf("type1" to TagType(100)), jsonLibrarySource.getAllTagTypes())

        cachedSongLibrary.commit()

        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null), "tag3" to mutatedTag3),
            jsonLibrarySource.getAllTags()
        )
        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100)),
            jsonLibrarySource.getAllTagTypes()
        )

        // put tag type
        val tagType3 = TagType(5)
        cachedSongLibrary.putTagType("type3", tagType3)

        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100), "type3" to tagType3),
            cachedSongLibrary.getAllTagTypes()
        )
        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100)),
            jsonLibrarySource.getAllTagTypes()
        )

        cachedSongLibrary.commit()

        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100), "type3" to tagType3),
            jsonLibrarySource.getAllTagTypes()
        )

        val mutatedTagType3 = tagType3.mutate { color = 6 }
        cachedSongLibrary.putTagType("type3", mutatedTagType3)

        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100), "type3" to mutatedTagType3),
            cachedSongLibrary.getAllTagTypes()
        )
        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100), "type3" to tagType3),
            jsonLibrarySource.getAllTagTypes()
        )

        cachedSongLibrary.commit()

        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100), "type3" to mutatedTagType3),
            jsonLibrarySource.getAllTagTypes()
        )

        // put data
        cachedSongLibrary.putData("key", "value")
        assertEquals(mapOf("key" to "value"), cachedSongLibrary.getAllData())
        assertTrue(jsonLibrarySource.getAllData().isEmpty())
        cachedSongLibrary.commit()
        assertEquals(mapOf("key" to "value"), jsonLibrarySource.getAllData())

        cachedSongLibrary.putData("key", "value2")

        assertEquals(mapOf("key" to "value2"), cachedSongLibrary.getAllData())
        assertEquals(mapOf("key" to "value"), jsonLibrarySource.getAllData())
        cachedSongLibrary.commit()
        assertEquals(mapOf("key" to "value2"), jsonLibrarySource.getAllData())

        // remove song
        cachedSongLibrary.removeSong("song1.mp3")
        assertTrue(cachedSongLibrary.getAllSongs().isEmpty())
        assertEquals(mapOf("song1.mp3" to mutatedSong1), jsonLibrarySource.getAllSongs())
        cachedSongLibrary.commit()
        assertTrue(jsonLibrarySource.getAllSongs().isEmpty())

        // remove tag
        cachedSongLibrary.removeTag("tag2")
        assertEquals(
            mapOf("tag1" to Tag(null), "tag3" to mutatedTag3),
            cachedSongLibrary.getAllTags()
        )
        assertEquals(
            mapOf("tag1" to Tag(null), "tag2" to Tag(null), "tag3" to mutatedTag3),
            jsonLibrarySource.getAllTags()
        )
        cachedSongLibrary.commit()
        assertEquals(
            mapOf("tag1" to Tag(null), "tag3" to mutatedTag3),
            jsonLibrarySource.getAllTags()
        )

        // remove tag type
        cachedSongLibrary.removeTagType("type2")
        assertEquals(
            mapOf("type1" to TagType(100), "type3" to mutatedTagType3),
            cachedSongLibrary.getAllTagTypes()
        )
        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(100), "type3" to mutatedTagType3),
            jsonLibrarySource.getAllTagTypes()
        )
        cachedSongLibrary.commit()
        assertEquals(
            mapOf("type1" to TagType(100), "type3" to mutatedTagType3),
            jsonLibrarySource.getAllTagTypes()
        )

        // remove data
        cachedSongLibrary.removeData("key")
        assertTrue(cachedSongLibrary.getAllData().isEmpty())
        assertEquals(mapOf("key" to "value2"), jsonLibrarySource.getAllData())
        cachedSongLibrary.commit()
        assertTrue(jsonLibrarySource.getAllData().isEmpty())

        // multiple changes
        cachedSongLibrary.setDefaultTagType(TagType(200))
        val song2 = Song("title1", 5000)
        cachedSongLibrary.putSong("song1.mp3", song2)
        cachedSongLibrary.putTag("tag2", Tag(null))
        cachedSongLibrary.putTagType("type2", TagType(200))
        cachedSongLibrary.putData("key", "value")

        assertEquals(TagType(100), jsonLibrarySource.getDefaultTagType())
        assertTrue(jsonLibrarySource.getAllSongs().isEmpty())
        assertEquals(
            // tag type 2 was removed so tag 3 type changed to null
            mapOf("tag1" to Tag(null), "tag3" to mutatedTag3.mutate { type = null }),
            jsonLibrarySource.getAllTags()
        )
        assertEquals(
            mapOf("type1" to TagType(100), "type3" to mutatedTagType3),
            jsonLibrarySource.getAllTagTypes()
        )
        assertTrue(jsonLibrarySource.getAllData().isEmpty())

        cachedSongLibrary.commit()

        assertEquals(TagType(200), jsonLibrarySource.getDefaultTagType())
        assertEquals(mapOf("song1.mp3" to song2), jsonLibrarySource.getAllSongs())
        assertEquals(
            mapOf(
                "tag1" to Tag(null),
                "tag2" to Tag(null),
                "tag3" to mutatedTag3.mutate { type = null }),
            jsonLibrarySource.getAllTags()
        )
        assertEquals(
            mapOf("type1" to TagType(100), "type2" to TagType(200), "type3" to mutatedTagType3),
            jsonLibrarySource.getAllTagTypes()
        )
        assertEquals(mapOf("key" to "value"), jsonLibrarySource.getAllData())
    }
}
