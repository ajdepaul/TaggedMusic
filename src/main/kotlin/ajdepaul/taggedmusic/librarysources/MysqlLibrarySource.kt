/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import java.io.Closeable
import java.sql.Connection

/**
 * [LibrarySource] that is saved using a MySQL database server. The provided `mysql_init.sql` can be
 * used to initialize the MySQL server with the required tables. The connection is opened using
 * [dataSource] on instantiation.
 * @throws java.sql.SQLException if there is an issue connecting to or updating the MySQL server
 */
class MysqlLibrarySource(
    /** [MysqlDataSource] for opening a connection to the MySQL server. */
    private val dataSource: MysqlDataSource
) : LibrarySource, Closeable {

    /** The current open connection to the MySQL server. null if the connection is closed. */
    private var connection: Connection = dataSource.connection

    /** Closes the connection to the currently connected MySQL server. */
    override fun close() {
        connection.close()
    }

/* ----------------------------------------- Retrieving ----------------------------------------- */

    override fun getVersion(): String {
        return with(connection.createStatement()) {
            with(this.executeQuery("SELECT * FROM library")) {
                this.getString("version")
            }
        }
    }

    override fun getDefaultTagType(): TagType {
        TODO("Not yet implemented")
    }

    override fun hasSong(fileName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSong(fileName: String): Song? {
        TODO("Not yet implemented")
    }

    override fun getAllSongs(): PersistentMap<String, Song> {
        TODO("Not yet implemented")
    }

    override fun getSongsByTags(
        includeTags: PersistentSet<String>,
        excludeTags: PersistentSet<String>
    ): PersistentMap<String, Song> {
        TODO("Not yet implemented")
    }

    override fun hasTag(tagName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getTag(tagName: String): Tag? {
        TODO("Not yet implemented")
    }

    override fun getAllTags(): PersistentMap<String, Tag> {
        TODO("Not yet implemented")
    }

    override fun hasTagType(tagTypeName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getTagType(tagTypeName: String): TagType? {
        TODO("Not yet implemented")
    }

    override fun getAllTagTypes(): PersistentMap<String, TagType> {
        TODO("Not yet implemented")
    }

/* ------------------------------------------ Updating ------------------------------------------ */

    override fun updater(): LibrarySource.UpdateBuilder {
        return UpdateBuilder()
    }

    /** See [LibrarySource.UpdateBuilder]. */
    private class UpdateBuilder : LibrarySource.UpdateBuilder {

        override fun setDefaultTagType(tagType: TagType): LibrarySource.UpdateBuilder {
            TODO("Not yet implemented")
        }

        override fun putSong(fileName: String, song: Song): LibrarySource.UpdateBuilder {
            TODO("Not yet implemented")
        }

        override fun removeSong(fileName: String): LibrarySource.UpdateBuilder {
            TODO("Not yet implemented")
        }

        override fun putTag(tagName: String, tag: Tag): LibrarySource.UpdateBuilder {
            TODO("Not yet implemented")
        }

        override fun removeTag(tagName: String): LibrarySource.UpdateBuilder {
            TODO("Not yet implemented")
        }

        override fun putTagType(
            tagTypeName: String,
            tagType: TagType
        ): LibrarySource.UpdateBuilder {
            TODO("Not yet implemented")
        }

        override fun removeTagType(tagTypeName: String): LibrarySource.UpdateBuilder {
            TODO("Not yet implemented")
        }

        override fun commit() {
            TODO("Not yet implemented")
        }
    }
}
