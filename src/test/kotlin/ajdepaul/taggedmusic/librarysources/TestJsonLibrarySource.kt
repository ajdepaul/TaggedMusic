/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.songlibraries.SongLibrary
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentHashSetOf
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestJsonLibrarySource {

    /** Directory for temporary files for this set of tests. */
    private val sharedTestDir = "test/librarysources/TestJsonLibrarySource"

    /** Tests the [JsonLibrarySource] constructor that creates a json file to go along with it. */
    @Test
    fun testCreateJsonFileConstructor() {
        val testDir = "$sharedTestDir/testCreateJsonFileConstructor"
        File(testDir).mkdirs()
        val jsonFilePath = "$testDir/library.json"

        // initial text to test the text is overwritten
        File(jsonFilePath).writeText("bad text")

        // create the json file with default values
        JsonLibrarySource(jsonFilePath, TagType(0))

        // create new library source using that file
        val jls = JsonLibrarySource(jsonFilePath)

        assertEquals(SongLibrary.VERSION, jls.getVersion())
        assertEquals(TagType(0), jls.getDefaultTagType())
        assertEquals(persistentHashMapOf(), jls.getAllSongs())
        assertEquals(persistentHashMapOf(), jls.getAllTags())
        assertEquals(persistentHashMapOf(), jls.getAllTagTypes())
    }

    /** Tests [JsonLibrarySource.updater]. */
    @Test
    fun testUpdater() {
        val testDir = "$sharedTestDir/testUpdater"
        File(testDir).mkdirs()
        val jsonFilePath = "$testDir/library.json"

        val jls = JsonLibrarySource(jsonFilePath, TagType(0))

        val song1 = Song("title1", 1000, "artist1")
        val song2 = Song("title2", 1000, "artist2", tags = persistentHashSetOf("tag3"))

        jls.updater()
            .setDefaultTagType(TagType(100))
            .putTagType("type1", TagType(101))
            .putTag("tag1", Tag(null))
            .putTag("tag2", Tag("type2")) // should add a new tag type
            .putSong("filename1", song1)
            .putSong("filename2", song2) // should add a new tag
            .commit()

        assertEquals(TagType(100), jls.getDefaultTagType())
        assertEquals(TagType(101), jls.getTagType("type1"))
        assertEquals(Tag(null), jls.getTag("tag1"))
        assertEquals(Tag("type2"), jls.getTag("tag2"))
        assertEquals(TagType(100), jls.getTagType("type2"))
        assertEquals(song1, jls.getSong("filename1"))
        assertEquals(song2, jls.getSong("filename2"))
        assertEquals(Tag(null), jls.getTag("tag3"))

        jls.updater()
            .setDefaultTagType(TagType(99))
            .removeTagType("type2") // should set tag2 type to null
            .removeTag("tag3") // should remove tag3 from song filename2
            .removeSong("filename1")
            .commit()

        assertEquals(TagType(99), jls.getDefaultTagType())
        assertNull(jls.getTagType("type2"))
        assertEquals(Tag(null), jls.getTag("tag2"))
        assertNull(jls.getTag("tag3"))
        assertEquals(song2.mutate(false) { tags -= "tag3" }, jls.getSong("filename2"))
        assertNull(jls.getSong("filename1"))
        // unchanged
        assertEquals(TagType(101), jls.getTagType("type1"))
        assertEquals(Tag(null), jls.getTag("tag1"))
    }
}
