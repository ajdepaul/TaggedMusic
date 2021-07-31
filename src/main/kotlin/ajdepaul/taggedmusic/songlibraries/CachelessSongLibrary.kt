/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.songlibraries

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.librarysources.LibrarySource
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet

/**
 * [SongLibrary] that stores no library data in memory. All changes are applied to the
 * [librarySource] immediately.
 *
 * *[CachelessSongLibrary]'s functions are not designed with thread safety in mind.* If you are
 * utilizing multiple threads, write functions calls should be synchronized.
 * @param librarySource [LibrarySource] used to retrieve data about a user's [SongLibrary]
 */
class CachelessSongLibrary(librarySource: LibrarySource) : SongLibrary(librarySource) {

    /** Used to keep track of changes to make to [librarySource]. */
    private val librarySourceUpdater = librarySource.updater()

    override var defaultTagType: TagType
        get() = librarySource.getDefaultTagType()
        set(value) {
            librarySourceUpdater.setDefaultTagType(value)
            librarySourceUpdater.commit()
        }

/* -------------------------------------------- Songs ------------------------------------------- */

    override fun _putSong(fileName: String, song: Song) {
        librarySourceUpdater.putSong(fileName, song)
        librarySourceUpdater.commit()
    }

    override fun _removeSong(fileName: String) {
        librarySourceUpdater.removeSong(fileName)
        librarySourceUpdater.commit()
    }

    override fun _hasSong(fileName: String): Boolean {
        return librarySource.hasSong(fileName)
    }

    override fun _getSong(fileName: String): Song? {
        return librarySource.getSong(fileName)
    }

    override fun getAllSongs(): PersistentMap<String, Song> {
        return librarySource.getAllSongs()
    }

    override fun getSongsByTags(
        includeTags: PersistentSet<String>,
        excludeTags: PersistentSet<String>
    ): PersistentMap<String, Song> {
        return librarySource.getSongsByTags(includeTags, excludeTags)
    }

/* -------------------------------------------- Tags -------------------------------------------- */

    override fun _putTag(tagName: String, tag: Tag) {
        librarySourceUpdater.putTag(tagName, tag)
        librarySourceUpdater.commit()
    }

    override fun _removeTag(tagName: String) {
        librarySourceUpdater.removeSong(tagName)
        librarySourceUpdater.commit()
    }

    override fun _hasTag(tagName: String): Boolean {
        return librarySource.hasTag(tagName)
    }

    override fun _getTag(tagName: String): Tag? {
        return librarySource.getTag(tagName)
    }

    override fun getAllTags(): PersistentMap<String, Tag> {
        return librarySource.getAllTags()
    }

/* ------------------------------------------ Tag Types ----------------------------------------- */

    override fun _putTagType(tagTypeName: String, tagType: TagType) {
        librarySourceUpdater.putTagType(tagTypeName, tagType)
        librarySourceUpdater.commit()
    }

    override fun _removeTagType(tagTypeName: String) {
        librarySourceUpdater.removeTagType(tagTypeName)
        librarySourceUpdater.commit()
    }

    override fun _hasTagType(tagTypeName: String): Boolean {
        return librarySource.hasTagType(tagTypeName)
    }

    override fun _getTagType(tagTypeName: String): TagType? {
        return librarySource.getTagType(tagTypeName)
    }

    override fun getAllTagTypes(): PersistentMap<String, TagType> {
        return librarySource.getAllTagTypes()
    }
}
