/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import org.apache.commons.io.FileUtils
import java.io.Closeable
import java.nio.file.Path

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
 * @throws com.jcraft.jsch.JSchException if there is an issue connecting to the SFTP server or
 * uploading/creating remote files/directories
 * @throws java.io.IOException if there is an issue creating local directories
 * [session]
 */
class SftpLibrarySource(
    /** [Session] for opening a connection to the SFTP server. */
    private val session: Session,
    /**
     * The list of file paths relative to [localDirectory] to retrieve and update on the SFTP server
     * that are required by the [localLibrarySource].
     */
    private val requiredFiles: Iterable<Path>,
    /** Path to a local directory where the [localLibrarySource]'s files can be stored. */
    private val localDirectory: Path,
    /**
     * Path to a directory on the SFTP server where the [localLibrarySource]'s files can be stored.
     */
    private val remoteDirectory: Path,
    /** The wrapped [LibrarySource] that relies on the files fetched from the SFTP server. */
    private val localLibrarySource: LibrarySource,
    /**
     * Set to true if it's the first time using this SFTP server as a [LibrarySource]. The files
     * required for the [localLibrarySource] will be uploaded on instantiation.
     */
    initialize: Boolean = false
) : LibrarySource, Closeable {

    private val channel: ChannelSftp

    init {
        // open the connection
        session.connect()
        channel = session.openChannel("sftp") as ChannelSftp
        channel.connect()

        // local working directory
        FileUtils.forceMkdir(localDirectory.toFile())
        channel.lcd(localDirectory.toString())

        // remote working directory
        if (initialize) {
            // create remote directory one directory at a time & cd into it
            for (nextDir in remoteDirectory) {
                // check if the next directory exists
                if (channel.ls(".")
                        .map { it as ChannelSftp.LsEntry }
                        .none { it.filename == nextDir.toString() }
                ) {
                    // make it if it doesn't
                    channel.mkdir(nextDir.toString())
                }
                // if the there is a file with the nextDir name, let it throw an exception
                channel.cd(nextDir.toString())
            }

            // upload local library required files
            for (f in requiredFiles) {
                channel.put(f.toString(), f.toString())
            }

        } else {
            channel.cd(remoteDirectory.toString())

            // download local library required files
            for (f in requiredFiles) {
                channel.get(f.toString(), f.toString())
            }
        }
    }

    override fun close() {
        session.disconnect()
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
        return UpdateBuilder(channel, requiredFiles, localLibrarySource.updater())
    }

    /** See [LibrarySource.UpdateBuilder]. */
    private class UpdateBuilder(
        private val channel: ChannelSftp,
        private val requiredFiles: Iterable<Path>,
        private val localLibrarySourceUpdater: LibrarySource.UpdateBuilder
    ) : LibrarySource.UpdateBuilder {

        override fun setDefaultTagType(tagType: TagType): LibrarySource.UpdateBuilder {
            localLibrarySourceUpdater.setDefaultTagType(tagType)
            return this
        }

        override fun putSong(fileName: String, song: Song): LibrarySource.UpdateBuilder {
            localLibrarySourceUpdater.putSong(fileName, song)
            return this
        }

        override fun removeSong(fileName: String): LibrarySource.UpdateBuilder {
            localLibrarySourceUpdater.removeSong(fileName)
            return this
        }

        override fun putTag(tagName: String, tag: Tag): LibrarySource.UpdateBuilder {
            localLibrarySourceUpdater.putTag(tagName, tag)
            return this
        }

        override fun removeTag(tagName: String): LibrarySource.UpdateBuilder {
            localLibrarySourceUpdater.removeTag(tagName)
            return this
        }

        override fun putTagType(
            tagTypeName: String,
            tagType: TagType
        ): LibrarySource.UpdateBuilder {
            localLibrarySourceUpdater.putTagType(tagTypeName, tagType)
            return this
        }

        override fun removeTagType(tagTypeName: String): LibrarySource.UpdateBuilder {
            localLibrarySourceUpdater.removeTagType(tagTypeName)
            return this
        }

        override fun commit() {
            localLibrarySourceUpdater.commit()

            // upload local library required files
            for (f in requiredFiles) {
                channel.put(f.toString(), f.toString())
            }
        }
    }
}
