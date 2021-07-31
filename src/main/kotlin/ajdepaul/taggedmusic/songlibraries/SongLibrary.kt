/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.songlibraries

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.librarysources.LibrarySource
import kotlinx.collections.immutable.*

/**
 * [SongLibrary]s are represented as three separate maps.
 * 1. Songs | key: file name of the [Song] in its
 *    [AudioFileProvider][ajdepaul.taggedmusic.audiofileproviders.AudioFileProvider], value: the
 *    [Song] data object
 * 2. Tags | key: the [Tag] name, value: the [Tag] data object
 * 3. Tag Types | key: the [TagType] name, value: the [TagType] data object
 *
 * You can read or write to these maps using the various put, remove, and get functions.
 * @param librarySource [LibrarySource] used to retrieve data about a user's [SongLibrary]
 */
abstract class SongLibrary(
    /** [LibrarySource] used to retrieve data about a user's [SongLibrary]. */
    val librarySource: LibrarySource
) {

    companion object {
        /** Indicates what specification this Tagged Music library expects. */
        val VERSION = "1.0"
    }

    /** The [TagType] to use when a [Tag] has no [TagType]. */
    abstract var defaultTagType: TagType

    init {
        // check the version of librarySource
        val libSrcVer = librarySource.getVersion()
        if (libSrcVer != VERSION)
            println(
                "The library source version ($libSrcVer) does not match this Tagged Music " +
                        "version ($VERSION). Unexpected behavior is likely to occur."
            )
    }

/* -------------------------------------------- Songs ------------------------------------------- */

    /**
     * Adds or updates [song] to the [Song] map and adds any new [Tag]s in [song]'s [Tag]s to the
     * [Tag] map.
     * @param fileName if blank, no change is made
     */
    fun putSong(fileName: String, song: Song) {
        if (fileName.isBlank()) return
        _putSong(fileName, song)
    }

    /**
     * Implementation for [putSong].
     * @param fileName will never be blank
     */
    protected abstract fun _putSong(fileName: String, song: Song)

    /**
     * Removes a [Song] from the [Song] map using the key [fileName].
     * @param fileName if blank, no change is made
     */
    fun removeSong(fileName: String) {
        if (fileName.isBlank()) return
        _removeSong(fileName)
    }

    /**
     * Implementation for [removeSong].
     * @param fileName will never be blank
     */
    protected abstract fun _removeSong(fileName: String)

    /**
     * Checks if [fileName] is a key used in the [Song] map.
     * @param fileName if blank, returns false
     */
    fun hasSong(fileName: String): Boolean {
        if (fileName.isBlank()) return false
        return _hasSong(fileName)
    }

    /**
     * Implementation for [hasSong].
     * @param fileName will never be blank
     */
    protected abstract fun _hasSong(fileName: String): Boolean

    /**
     * Retrieves the [Song] that corresponds to the key [fileName].
     * @param fileName if blank, returns null
     * @return null if the key does not exist
     */
    fun getSong(fileName: String): Song? {
        if (fileName.isBlank()) return null
        return _getSong(fileName)
    }

    /**
     * Implementation for [getSong].
     * @param fileName will never be blank
     */
    protected abstract fun _getSong(fileName: String): Song?

    /** Returns a map of all of the [Song]s in the [SongLibrary]. */
    abstract fun getAllSongs(): PersistentMap<String, Song>

    /**
     * Retrieves a map of songs according to the provided filters.
     * @param includeTags songs must have all of these tags (if empty, includes all tags)
     * @param excludeTags songs cannot have any of these tags (if empty, excludes no tags)
     */
    abstract fun getSongsByTags(
        includeTags: PersistentSet<String> = persistentHashSetOf(),
        excludeTags: PersistentSet<String> = persistentHashSetOf()
    ): PersistentMap<String, Song>

/* -------------------------------------------- Tags -------------------------------------------- */

    /**
     * Adds [tag] to the [Tag] map. If the [tag]'s [TagType] is new, it is added to the [TagType]
     * map.
     * @param tagName if blank, no change is made
     */
    fun putTag(tagName: String, tag: Tag) {
        if (tagName.isBlank()) return
        _putTag(tagName, tag)
    }

    /**
     * Implementation for [putTag].
     * @param tagName will never be blank
     */
    protected abstract fun _putTag(tagName: String, tag: Tag)

    /**
     * Removes a [Tag] from the [Tag] map using the key [tagName].
     * @param tagName if blank, no change is made
     */
    fun removeTag(tagName: String) {
        if (tagName.isBlank()) return
        _removeTag(tagName)
    }

    /**
     * Implementation for [removeTag].
     * @param tagName will never be blank
     */
    protected abstract fun _removeTag(tagName: String)

    /**
     * Checks if [tagName] is a key used in the [Tag] map.
     * @param tagName if blank, returns false
     */
    fun hasTag(tagName: String): Boolean {
        if (tagName.isBlank()) return false
        return _hasTag(tagName)
    }

    /**
     * Implementation for [hasTag].
     * @param tagName will never be blank
     */
    protected abstract fun _hasTag(tagName: String): Boolean

    /**
     * Retrieves the [Tag] that corresponds to the key [tagName].
     * @param tagName if blank, returns null
     * @return null if the key does not exist
     */
    fun getTag(tagName: String): Tag? {
        if (tagName.isBlank()) return null
        return _getTag(tagName)
    }

    /**
     * Implementation for [getTag].
     * @param tagName will never be blank
     */
    protected abstract fun _getTag(tagName: String): Tag?

    /** Retrieves a map of all the [Tag]s in the [SongLibrary]. */
    abstract fun getAllTags(): PersistentMap<String, Tag>

/* ------------------------------------------ Tag Types ----------------------------------------- */

    /**
     * Adds [tagType] to the [TagType] map.
     * @param tagTypeName if blank, no change is made
     */
    fun putTagType(tagTypeName: String, tagType: TagType) {
        if (tagTypeName.isBlank()) return
        _putTagType(tagTypeName, tagType)
    }

    /**
     * Implementation for [putTagType].
     * @param tagTypeName will never be blank
     */
    protected abstract fun _putTagType(tagTypeName: String, tagType: TagType)

    /**
     * Removes a [TagType] from the [TagType] map using the key [tagTypeName].
     * @param tagTypeName if blank, no change is made
     */
    fun removeTagType(tagTypeName: String) {
        if (tagTypeName.isBlank()) return
        _removeTagType(tagTypeName)
    }

    /**
     * Implementation for [removeTagType].
     * @param tagTypeName will never be blank
     */
    protected abstract fun _removeTagType(tagTypeName: String)

    /**
     * Checks if [tagTypeName] is a key used in the [TagType] map.
     * @param tagTypeName if blank, returns false
     */
    fun hasTagType(tagTypeName: String): Boolean {
        if (tagTypeName.isBlank()) return false
        return _hasTagType(tagTypeName)
    }

    /**
     * Implementation for [hasTagType].
     * @param tagTypeName will never be blank
     */
    protected abstract fun _hasTagType(tagTypeName: String): Boolean

    /**
     * Retrieves the [TagType] that corresponds to the key [tagTypeName].
     * @param tagTypeName if blank, returns null
     * @return null if the key does not exist
     */
    fun getTagType(tagTypeName: String): TagType? {
        if (tagTypeName.isBlank()) return null
        return _getTagType(tagTypeName)
    }

    /**
     * Implementation for [getTagType].
     * @param tagTypeName will never be blank
     */
    protected abstract fun _getTagType(tagTypeName: String): TagType?

    /** Retrieves a map of all the [TagType]s in the [CachedSongLibrary]. */
    abstract fun getAllTagTypes(): PersistentMap<String, TagType>
}
