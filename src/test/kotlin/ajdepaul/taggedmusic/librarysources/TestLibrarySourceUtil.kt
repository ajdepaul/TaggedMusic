/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.songlibraries.SongLibrary
import kotlinx.collections.immutable.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Utility functions for testing [LibrarySource]s. */
class TestLibrarySourceUtil {

    companion object {

        /**
         * Asserts that [librarySource] has the correct version, default tag type, songs map, tags
         * map, and tag types map.
         */
        fun assertDefaults(librarySource: LibrarySource) {
            assertEquals(SongLibrary.VERSION, librarySource.getVersion())
            assertEquals(TagType(0), librarySource.getDefaultTagType())

            assertFalse(librarySource.hasSong("song1.mp3"))
            assertNull(librarySource.getSong("song1.mp3"))
            assertEquals(persistentHashMapOf(), librarySource.getAllSongs())

            assertFalse(librarySource.hasTag("tag1"))
            assertNull(librarySource.getTag("tag1"))
            assertEquals(persistentHashMapOf(), librarySource.getAllTags())

            assertFalse(librarySource.hasTagType("type1"))
            assertNull(librarySource.getTagType("type1"))
            assertEquals(persistentHashMapOf(), librarySource.getAllTagTypes())

            assertFalse(librarySource.hasData("key"))
            assertNull(librarySource.getData("key"))
            assertEquals(persistentHashMapOf(), librarySource.getAllData())
        }

        /**
         * Asserts that [librarySource] properly updates the default tag type, songs map, tags map,
         * and tag types map.
         * @return the expected [SongLibraryData] for the librarySource; see [assertUpdated]
         */
        fun assertUpdates(librarySource: LibrarySource): SongLibraryData {
            val song1 = Song("title1", 1000)
            val song2 = Song("title2", 1000, tags = persistentHashSetOf("tag3"))

            librarySource.updater()
                .setDefaultTagType(TagType(100))
                .putTagType("type1", TagType(101))
                .putTag("tag1", Tag(null))
                .putTag("tag2", Tag("type2")) // should add a new tag type
                .putSong("song1.mp3", song1)
                .putSong("song2.mp3", song2) // should add a new tag
                .putData("key", "value")
                .commit()

            assertEquals(TagType(100), librarySource.getDefaultTagType())

            assertTrue(librarySource.hasTagType("type1"))
            assertEquals(TagType(101), librarySource.getTagType("type1"))

            assertTrue(librarySource.hasTag("tag1"))
            assertEquals(Tag(null), librarySource.getTag("tag1"))

            assertTrue(librarySource.hasTag("tag2"))
            assertEquals(Tag("type2"), librarySource.getTag("tag2"))

            assertTrue(librarySource.hasTagType("type2"))
            assertEquals(TagType(100), librarySource.getTagType("type2"))

            assertTrue(librarySource.hasSong("song1.mp3"))
            assertEquals(song1, librarySource.getSong("song1.mp3"))

            assertTrue(librarySource.hasSong("song2.mp3"))
            assertEquals(song2, librarySource.getSong("song2.mp3"))

            assertTrue(librarySource.hasTag("tag3"))
            assertEquals(Tag(null), librarySource.getTag("tag3"))

            assertTrue(librarySource.hasData("key"))
            assertEquals("value", librarySource.getData("key"))

            librarySource.updater()
                .setDefaultTagType(TagType(99))
                .removeTagType("type2") // should set tag2 type to null
                .removeTag("tag3") // should remove tag3 from song song2.mp3
                .removeSong("song1.mp3")
                .removeData("key")
                .commit()

            assertEquals(TagType(99), librarySource.getDefaultTagType())

            assertFalse(librarySource.hasTagType("type2"))
            assertNull(librarySource.getTagType("type2"))

            assertTrue(librarySource.hasTag("tag2"))
            assertEquals(Tag(null), librarySource.getTag("tag2"))

            assertFalse(librarySource.hasTag("tag3"))
            assertNull(librarySource.getTag("tag3"))

            assertTrue(librarySource.hasSong("song2.mp3"))
            assertEquals(song2.mutate(false) { tags -= "tag3" }, librarySource.getSong("song2.mp3"))

            assertFalse(librarySource.hasSong("song1.mp3"))
            assertNull(librarySource.getSong("song1.mp3"))

            assertFalse(librarySource.hasData("key"))
            assertNull(librarySource.getData("key"))

            // unchanged
            assertTrue(librarySource.hasTagType("type1"))
            assertEquals(TagType(101), librarySource.getTagType("type1"))

            assertTrue(librarySource.hasTag("tag1"))
            assertEquals(Tag(null), librarySource.getTag("tag1"))

            return SongLibraryData(
                librarySource.getDefaultTagType(),
                librarySource.getAllSongs(),
                librarySource.getAllTags(),
                librarySource.getAllTagTypes(),
                librarySource.getAllData()
            )
        }

        /**
         * Asserts that [librarySource] matches [songLibraryData]. Intended to be used in
         * conjunction with [assertUpdates] on a new [LibrarySource] to make sure the new source
         * loads values properly.
         */
        fun assertUpdated(librarySource: LibrarySource, songLibraryData: SongLibraryData) {
            assertEquals(songLibraryData.defaultTagType, librarySource.getDefaultTagType())
            assertEquals(songLibraryData.songs, librarySource.getAllSongs())
            assertEquals(songLibraryData.tags, librarySource.getAllTags())
            assertEquals(songLibraryData.tagTypes, librarySource.getAllTagTypes())
            assertEquals(songLibraryData.data, librarySource.getAllData())
        }

        /** Tests [LibrarySource.getSongsByTags]. */
        fun testGetSongsByTags(librarySource: LibrarySource) {
            val song1 = Song("title1", 1000, tags = persistentHashSetOf("A", "B", "D"))
            val song2 = Song("title2", 1000, tags = persistentHashSetOf("A", "B", "C", "E"))
            val song3 = Song("title3", 1000, tags = persistentHashSetOf("A", "C", "F"))

            librarySource.updater()
                .putSong("song1.mp3", song1)
                .putSong("song2.mp3", song2)
                .putSong("song3.mp3", song3)
                .commit()

            // includes one tag
            assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2, "song3.mp3" to song3), librarySource.getSongsByTags(persistentHashSetOf("A")))
            assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2), librarySource.getSongsByTags(persistentHashSetOf("B")))
            assertEquals(mapOf("song2.mp3" to song2, "song3.mp3" to song3), librarySource.getSongsByTags(persistentHashSetOf("C")))
            assertEquals(mapOf("song1.mp3" to song1), librarySource.getSongsByTags(persistentHashSetOf("D")))
            assertEquals(mapOf("song2.mp3" to song2), librarySource.getSongsByTags(persistentHashSetOf("E")))
            assertEquals(mapOf("song3.mp3" to song3), librarySource.getSongsByTags(persistentHashSetOf("F")))

            // includes two tags
            assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2), librarySource.getSongsByTags(persistentHashSetOf("A", "B")))
            assertEquals(mapOf("song2.mp3" to song2, "song3.mp3" to song3), librarySource.getSongsByTags(persistentHashSetOf("A", "C")))
            assertEquals(mapOf("song2.mp3" to song2), librarySource.getSongsByTags(persistentHashSetOf("B", "C")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentHashSetOf("B", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentHashSetOf("C", "D")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentHashSetOf("D", "E")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentHashSetOf("D", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentHashSetOf("E", "F")))

            // includes three tags
            assertEquals(mapOf("song1.mp3" to song1), librarySource.getSongsByTags(persistentHashSetOf("A", "B", "D")))
            assertEquals(mapOf("song3.mp3" to song3), librarySource.getSongsByTags(persistentHashSetOf("A", "C", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentHashSetOf("A", "B", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentHashSetOf("A", "C", "D")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentHashSetOf("D", "E", "F")))

            // includes four tags
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentHashSetOf("A", "B", "C", "D")))
            assertEquals(mapOf("song2.mp3" to song2), librarySource.getSongsByTags(persistentHashSetOf("A", "B", "C", "E")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentHashSetOf("A", "B", "C", "F")))

            // excludes one tag
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("A")))
            assertEquals(mapOf("song3.mp3" to song3), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("B")))
            assertEquals(mapOf("song1.mp3" to song1), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("C")))
            assertEquals(mapOf("song2.mp3" to song2, "song3.mp3" to song3), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("D")))
            assertEquals(mapOf("song1.mp3" to song1, "song3.mp3" to song3), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("E")))
            assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("F")))

            // excludes two tags
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("A", "B")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("A", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("B", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("C", "D")))
            assertEquals(mapOf("song3.mp3" to song3), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("B", "D")))
            assertEquals(mapOf("song1.mp3" to song1), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("C", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("B", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("C", "D")))
            assertEquals(mapOf("song3.mp3" to song3), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("D", "E")))
            assertEquals(mapOf("song2.mp3" to song2), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("D", "F")))
            assertEquals(mapOf("song1.mp3" to song1), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("E", "F")))

            // excludes three tags
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("A", "B", "C")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("A", "B", "D")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("B", "C", "D")))
            assertEquals(mapOf("song3.mp3" to song3), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("B", "D", "E")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("B", "D", "F")))
            assertEquals(mapOf("song1.mp3" to song1), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("C", "E", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("C", "E", "D")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentHashSetOf("D", "E", "F")))

            // excludes four tags
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(excludeTags = persistentSetOf("B", "D", "E", "F")))

            // include and exclude one tag
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentHashSetOf("A"), persistentHashSetOf("A")))
            assertEquals(mapOf("song3.mp3" to song3), librarySource.getSongsByTags(persistentSetOf("A"), persistentSetOf("B")))
            assertEquals(mapOf("song2.mp3" to song2, "song3.mp3" to song3), librarySource.getSongsByTags(persistentSetOf("A"), persistentSetOf("D")))
            assertEquals(mapOf("song2.mp3" to song2), librarySource.getSongsByTags(persistentSetOf("B"), persistentSetOf("D")))
            assertEquals(mapOf("song1.mp3" to song1, "song2.mp3" to song2), librarySource.getSongsByTags(persistentSetOf("B"), persistentSetOf("F")))
            assertEquals(mapOf("song1.mp3" to song1), librarySource.getSongsByTags(persistentSetOf("D"), persistentSetOf("E")))

            // include and exclude two tags
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentSetOf("A", "B"), persistentSetOf("D", "E")))
            assertEquals(mapOf("song2.mp3" to song2), librarySource.getSongsByTags(persistentSetOf("A", "B"), persistentSetOf("D", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentSetOf("B", "C"), persistentSetOf("D", "E")))
            assertEquals(mapOf("song2.mp3" to song2), librarySource.getSongsByTags(persistentSetOf("B", "C"), persistentSetOf("D", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentSetOf("D", "E"), persistentSetOf("C", "F")))

            // include and exclude three tags
            assertEquals(mapOf("song2.mp3" to song2), librarySource.getSongsByTags(persistentSetOf("A", "B", "C"), persistentSetOf("D", "F")))
            assertEquals(mapOf<String, Song>(), librarySource.getSongsByTags(persistentSetOf("A", "B", "C"), persistentSetOf("D", "E", "F")))
        }
    }

    /** Stores all the important data that makes up a [SongLibrary]. */
    data class SongLibraryData(
        val defaultTagType: TagType,
        val songs: PersistentMap<String, Song>,
        val tags: PersistentMap<String, Tag>,
        val tagTypes: PersistentMap<String, TagType>,
        val data: PersistentMap<String, String>
    )
}
