/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.songlibraries.SongLibrary
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

/** Used by a [SongLibrary] to retrieve data about a user's library. */
interface LibrarySource {

/* ----------------------------------------- Retrieving ----------------------------------------- */

    /** Indicates the specification of the saved [SongLibrary]. */
    fun getVersion(): String

    /** See [SongLibrary.defaultTagType]. */
    fun getDefaultTagType(): TagType

    /** See [SongLibrary.hasSong]. */
    fun hasSong(fileName: String): Boolean

    /** See [SongLibrary.getSong]. */
    fun getSong(fileName: String): Song?

    /** See [SongLibrary.getAllSongs]. */
    fun getAllSongs(): PersistentMap<String, Song>

    /** See [SongLibrary.getSongsByTags]. */
    fun getSongsByTags(
        includeTags: PersistentSet<String> = persistentSetOf(),
        excludeTags: PersistentSet<String> = persistentSetOf()
    ): PersistentMap<String, Song>

    /** See [SongLibrary.hasTag]. */
    fun hasTag(tagName: String): Boolean

    /** See [SongLibrary.getTag]. */
    fun getTag(tagName: String): Tag?

    /** See [SongLibrary.getAllTags]. */
    fun getAllTags(): PersistentMap<String, Tag>

    /** See [SongLibrary.hasTagType] */
    fun hasTagType(tagTypeName: String): Boolean

    /** See [SongLibrary.getTagType]. */
    fun getTagType(tagTypeName: String): TagType?

    /** See [SongLibrary.getAllTagTypes]. */
    fun getAllTagTypes(): PersistentMap<String, TagType>

/* ------------------------------------------ Updating ------------------------------------------ */

    /**
     * Creates a [UpdateBuilder] that can be used to apply changes to the [LibrarySource].
     */
    fun updater(): UpdateBuilder

    /**
     * Keeps track of updates made to a [LibrarySource] that can then be applied by calling
     * [commit]. *These functions should never be called outside a [SongLibrary].*
     */
    interface UpdateBuilder {

        /** See [SongLibrary.defaultTagType]. */
        fun setDefaultTagType(tagType: TagType): UpdateBuilder

        /**
         * See [SongLibrary.putSong].
         * @param fileName will never be an empty string
         * @return this [UpdateBuilder] for easy function chaining
         */
        fun putSong(fileName: String, song: Song): UpdateBuilder

        /**
         * See [SongLibrary.removeSong]
         * @param fileName will never be an empty string
         * @return this [UpdateBuilder] for easy function chaining
         */
        fun removeSong(fileName: String): UpdateBuilder

        /**
         * See [SongLibrary.putTag].
         * @param tagName will never be an empty string
         * @return this [UpdateBuilder] for easy function chaining
         */
        fun putTag(tagName: String, tag: Tag): UpdateBuilder

        /**
         * See [SongLibrary.removeTag].
         * @param tagName will never be an empty string
         * @return this [UpdateBuilder] for easy function chaining
         */
        fun removeTag(tagName: String): UpdateBuilder

        /**
         * See [SongLibrary.putTagType].
         * @param tagTypeName will never be an empty string
         * @return this [UpdateBuilder] for easy function chaining
         */
        fun putTagType(tagTypeName: String, tagType: TagType): UpdateBuilder

        /**
         * See [SongLibrary.removeTagType].
         * @param tagTypeName will never be an empty string
         * @return this [UpdateBuilder] for easy function chaining
         */
        fun removeTagType(tagTypeName: String): UpdateBuilder

        /**
         * Applies the changes made from this [UpdateBuilder] onto the [LibrarySource].
         *
         * *Not thread safe. Do not make other changes while this method is running.*
         */
        fun commit()
    }

    /**
     * Convenience data class for [UpdateBuilder] implementations. [Update]s stores information
     * about a single update to an [UpdateBuilder].
     */
    abstract class Update
    data class SetDefaultTagTypeUpdate(val tagType: TagType) : Update()
    data class PutSongUpdate(val fileName: String, val song: Song) : Update()
    data class RemoveSongUpdate(val fileName: String) : Update()
    data class PutTagUpdate(val tagName: String, val tag: Tag) : Update()
    data class RemoveTagUpdate(val tagName: String) : Update()
    data class PutTagTypeUpdate(val tagTypeName: String, val tagType: TagType) : Update()
    data class RemoveTagTypeUpdate(val tagTypeName: String) : Update()
}