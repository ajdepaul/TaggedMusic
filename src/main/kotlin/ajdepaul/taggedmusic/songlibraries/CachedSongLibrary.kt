/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.songlibraries

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.extensions.filterByTags
import ajdepaul.taggedmusic.librarysources.LibrarySource
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.plus

/**
 * [SongLibrary] that stores all library data in memory for improved read and write times. Because
 * all changes made are stored in memory, [commit] must be used to apply the changes to the
 * [librarySource].
 * - Constructing this [SongLibrary] loads all library data from [librarySource] into memory and is
 *   likely expensive.
 * - [commit] can also be expensive depending on the number of changes being made and the
 *   [librarySource] used.
 *
 * *[CachedSongLibrary]'s functions are not designed with thread safety in mind.* If you are
 * utilizing multiple threads, write and commit functions should be synchronized.
 * @param librarySource [LibrarySource] used to retrieve data about a user's [SongLibrary]
 */
class CachedSongLibrary(librarySource: LibrarySource) : SongLibrary(librarySource) {

    /** Used to keep track of changes to make to [librarySource]. */
    private val librarySourceUpdater = librarySource.updater()

    override var defaultTagType: TagType = librarySource.getDefaultTagType()
        set(value) {
            librarySourceUpdater.setDefaultTagType(value)
            field = value
        }

    /** [PersistentMap] of all the [Song]s in this [SongLibrary]. */
    private var songs: PersistentMap<String, Song> = librarySource.getAllSongs()

    /** [PersistentMap] of all the [Tag]s in this [SongLibrary]. */
    private var tags: PersistentMap<String, Tag> = librarySource.getAllTags()

    /** [PersistentMap] of all the [TagType]s in this [SongLibrary]. */
    private var tagTypes: PersistentMap<String, TagType> = librarySource.getAllTagTypes()

    /** [PersistentMap] of all the data [String]s in this [SongLibrary]. */
    private var data: PersistentMap<String, String> = librarySource.getAllData()

    /**
     * Applies changes made through put or remove function to the [librarySource].
     *
     * *Not thread safe. Do not make other changes while this function is running.*
     */
    fun commit() {
        librarySourceUpdater.commit()
    }

/* -------------------------------------------- Songs ------------------------------------------- */

    override fun _putSong(fileName: String, song: Song) {
        librarySourceUpdater.putSong(fileName, song)
        songs += fileName to song
    }

    override fun _removeSong(fileName: String) {
        librarySourceUpdater.removeSong(fileName)
        songs -= fileName
    }

    override fun _hasSong(fileName: String): Boolean {
        return fileName in songs
    }

    override fun _getSong(fileName: String): Song? {
        return songs[fileName]
    }

    override fun getAllSongs(): PersistentMap<String, Song> {
        return songs
    }

    override fun getSongsByTags(
        includeTags: PersistentSet<String>,
        excludeTags: PersistentSet<String>
    ): PersistentMap<String, Song> {
        return songs.filterByTags(includeTags, excludeTags)
    }

/* -------------------------------------------- Tags -------------------------------------------- */

    override fun _putTag(tagName: String, tag: Tag) {
        librarySourceUpdater.putTag(tagName, tag)
        tags += tagName to tag

        // add new tag type
        if (tag.type != null && tag.type !in tagTypes) {
            tagTypes += tag.type to defaultTagType
        }
    }

    override fun _removeTag(tagName: String) {
        librarySourceUpdater.removeSong(tagName)
        tags -= tagName
        // remove the tag from every song
        for (entry in songs) {
            songs += entry.key to entry.value.mutate(false) { this.tags -= tagName }
        }
    }

    override fun _hasTag(tagName: String): Boolean {
        return tagName in tags
    }

    override fun _getTag(tagName: String): Tag? {
        return tags[tagName]
    }

    override fun getAllTags(): PersistentMap<String, Tag> {
        return tags
    }

/* ------------------------------------------ Tag Types ----------------------------------------- */

    override fun _putTagType(tagTypeName: String, tagType: TagType) {
        librarySourceUpdater.putTagType(tagTypeName, tagType)
        tagTypes += tagTypeName to tagType
    }

    override fun _removeTagType(tagTypeName: String) {
        librarySourceUpdater.removeTagType(tagTypeName)
        tagTypes -= tagTypeName
        // for every tag that uses this tag type, set its type to null
        for (entry in tags) {
            if (entry.value.type == tagTypeName) {
                tags += entry.key to entry.value.mutate { type = null }
            }
        }
    }

    override fun _hasTagType(tagTypeName: String): Boolean {
        return tagTypeName in tagTypes
    }

    override fun _getTagType(tagTypeName: String): TagType? {
        return tagTypes[tagTypeName]
    }

    override fun getAllTagTypes(): PersistentMap<String, TagType> {
        return tagTypes
    }

/* -------------------------------------------- Data -------------------------------------------- */

    override fun _putData(key: String, value: String) {
        librarySourceUpdater.putData(key, value)
        data += key to value
    }

    override fun _removeData(key: String) {
        librarySourceUpdater.removeData(key)
        data -= key
    }

    override fun _hasData(key: String): Boolean {
        return key in data
    }

    override fun _getData(key: String): String? {
        return data[key]
    }

    override fun getAllData(): PersistentMap<String, String> {
        return data
    }
}
