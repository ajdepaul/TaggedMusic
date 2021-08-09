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
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
            assertEquals(persistentHashMapOf(), librarySource.getAllSongs())
            assertEquals(persistentHashMapOf(), librarySource.getAllTags())
            assertEquals(persistentHashMapOf(), librarySource.getAllTagTypes())
        }

        /**
         * Asserts that [librarySource] properly updates the default tag type, songs map, tags map,
         * and tag types map.
         * @return the expected [SongLibraryData] for the librarySource; see [assertUpdated]
         */
        fun assertUpdates(librarySource: LibrarySource): SongLibraryData {
            val song1 = Song("title1", 1000, "artist1")
            val song2 = Song("title2", 1000, "artist2", tags = persistentHashSetOf("tag3"))

            librarySource.updater()
                .setDefaultTagType(TagType(100))
                .putTagType("type1", TagType(101))
                .putTag("tag1", Tag(null))
                .putTag("tag2", Tag("type2")) // should add a new tag type
                .putSong("filename1", song1)
                .putSong("filename2", song2) // should add a new tag
                .commit()

            assertEquals(TagType(100), librarySource.getDefaultTagType())
            assertEquals(TagType(101), librarySource.getTagType("type1"))
            assertEquals(Tag(null), librarySource.getTag("tag1"))
            assertEquals(Tag("type2"), librarySource.getTag("tag2"))
            assertEquals(TagType(100), librarySource.getTagType("type2"))
            assertEquals(song1, librarySource.getSong("filename1"))
            assertEquals(song2, librarySource.getSong("filename2"))
            assertEquals(Tag(null), librarySource.getTag("tag3"))

            librarySource.updater()
                .setDefaultTagType(TagType(99))
                .removeTagType("type2") // should set tag2 type to null
                .removeTag("tag3") // should remove tag3 from song filename2
                .removeSong("filename1")
                .commit()

            assertEquals(TagType(99), librarySource.getDefaultTagType())
            assertNull(librarySource.getTagType("type2"))
            assertEquals(Tag(null), librarySource.getTag("tag2"))
            assertNull(librarySource.getTag("tag3"))
            assertEquals(song2.mutate(false) { tags -= "tag3" }, librarySource.getSong("filename2"))
            assertNull(librarySource.getSong("filename1"))
            // unchanged
            assertEquals(TagType(101), librarySource.getTagType("type1"))
            assertEquals(Tag(null), librarySource.getTag("tag1"))

            return SongLibraryData(
                librarySource.getDefaultTagType(),
                librarySource.getAllSongs(),
                librarySource.getAllTags(),
                librarySource.getAllTagTypes()
            )
        }

        /**
         * Asserts that [librarySource] matches [songLibraryData]. Intended to be used in
         * conjunction with [assertUpdates] on a new [LibrarySource] to make sure the new source
         * loads values properly.
         */
        fun assertUpdated(librarySource: LibrarySource, songLibraryData: SongLibraryData) {
            assertEquals(songLibraryData.defaultTagType, librarySource.getDefaultTagType())
            assertEquals(songLibraryData.songs.size, librarySource.getAllSongs().size)
            for (entry in songLibraryData.songs) {
                assertEquals(entry.value, librarySource.getSong(entry.key))
            }
            assertEquals(songLibraryData.tags.size, librarySource.getAllTags().size)
            for (entry in songLibraryData.tags) {
                assertEquals(entry.value, librarySource.getTag(entry.key))
            }
            assertEquals(songLibraryData.tagTypes.size, librarySource.getAllTagTypes().size)
            for (entry in songLibraryData.tagTypes) {
                assertEquals(entry.value, librarySource.getTagType(entry.key))
            }
        }
    }

    /** Stores all the important data that makes up a [SongLibrary]. */
    data class SongLibraryData(
        val defaultTagType: TagType,
        val songs: Map<String, Song>,
        val tags: Map<String, Tag>,
        val tagTypes: Map<String, TagType>
    )
}
