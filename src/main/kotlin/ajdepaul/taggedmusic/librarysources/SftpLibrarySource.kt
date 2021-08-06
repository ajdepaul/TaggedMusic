/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet

/**
 * [LibrarySource] that retrieves required files from a SFTP server for the wrapped
 * [localLibrarySource] that relies on local files to store
 * [SongLibrary][ajdepaul.taggedmusic.songlibraries.SongLibrary] data. The files will be fetched
 * from the SFTP server when instantiated.
 *
 * Retrieving data from this [SftpLibrarySource] is equivalent to retrieving data from the wrapped
 * [localLibrarySource] directly. When applying updates, the equivalent functions are called on the
 * [localLibrarySource] and then the files are pushed to the SFTP server when
 * [LibrarySource.UpdateBuilder.commit] is called.
 */
class SftpLibrarySource(
    /** The wrapped [LibrarySource] that relies on the files fetched from the SFTP server. */
    private val localLibrarySource: LibrarySource
) : LibrarySource {

    init {
        TODO("Fetch from SFTP server")
    }

/* ----------------------------------------- Retrieving ----------------------------------------- */

    override fun getVersion(): String {
        return localLibrarySource.getVersion()
    }

    override fun getDefaultTagType(): TagType {
        return localLibrarySource.getDefaultTagType()
    }

    override fun hasSong(fileName: String): Boolean {
        return localLibrarySource.hasTag(fileName)
    }

    override fun getSong(fileName: String): Song? {
        return localLibrarySource.getSong(fileName)
    }

    override fun getAllSongs(): PersistentMap<String, Song> {
        return localLibrarySource.getAllSongs()
    }

    override fun getSongsByTags(
        includeTags: PersistentSet<String>,
        excludeTags: PersistentSet<String>
    ): PersistentMap<String, Song> {
        return localLibrarySource.getSongsByTags(includeTags, excludeTags)
    }

    override fun hasTag(tagName: String): Boolean {
        return localLibrarySource.hasTag(tagName)
    }

    override fun getTag(tagName: String): Tag? {
        return localLibrarySource.getTag(tagName)
    }

    override fun getAllTags(): PersistentMap<String, Tag> {
        return localLibrarySource.getAllTags()
    }

    override fun hasTagType(tagTypeName: String): Boolean {
        return localLibrarySource.hasTagType(tagTypeName)
    }

    override fun getTagType(tagTypeName: String): TagType? {
        return localLibrarySource.getTagType(tagTypeName)
    }

    override fun getAllTagTypes(): PersistentMap<String, TagType> {
        return localLibrarySource.getAllTagTypes()
    }

/* ------------------------------------------ Updating ------------------------------------------ */

    override fun updater(): LibrarySource.UpdateBuilder {
        return UpdateBuilder(localLibrarySource.updater())
    }

    /** See [LibrarySource.UpdateBuilder]. */
    private class UpdateBuilder(
        val localLibrarySourceUpdater: LibrarySource.UpdateBuilder
    ) : LibrarySource.UpdateBuilder {

        override fun setDefaultTagType(tagType: TagType): LibrarySource.UpdateBuilder {
            return localLibrarySourceUpdater.setDefaultTagType(tagType)
        }

        override fun putSong(fileName: String, song: Song): LibrarySource.UpdateBuilder {
            return localLibrarySourceUpdater.putSong(fileName, song)
        }

        override fun removeSong(fileName: String): LibrarySource.UpdateBuilder {
            return localLibrarySourceUpdater.removeSong(fileName)
        }

        override fun putTag(tagName: String, tag: Tag): LibrarySource.UpdateBuilder {
            return localLibrarySourceUpdater.putTag(tagName, tag)
        }

        override fun removeTag(tagName: String): LibrarySource.UpdateBuilder {
            return localLibrarySourceUpdater.removeTag(tagName)
        }

        override fun putTagType(
            tagTypeName: String,
            tagType: TagType
        ): LibrarySource.UpdateBuilder {
            return localLibrarySourceUpdater.putTagType(tagTypeName, tagType)
        }

        override fun removeTagType(tagTypeName: String): LibrarySource.UpdateBuilder {
            return localLibrarySourceUpdater.removeTagType(tagTypeName)
        }

        override fun commit() {
            localLibrarySourceUpdater.commit()
            TODO("Update SFTP server")
        }
    }
}
