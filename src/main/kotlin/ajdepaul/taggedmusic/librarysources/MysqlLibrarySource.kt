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
 * [LibrarySource] that is saved using a MySQL database server. Specifications on how to set up the
 * database can be found here: TODO.
 *
 * The connection is opened using [dataSource] on instantiation.
 */
class MysqlLibrarySource(
    /** [MysqlDataSource] for opening a connection to the MySQL server. */
    private val dataSource: MysqlDataSource
) : LibrarySource, Closeable {

//    val ds = MysqlDataSource()
//        ds.serverName = "10.0.0.25"
//        ds.databaseName = "tm-db"
//        ds.port = 1258
//        ds.user = "root"
//        ds.setPassword("tv3RTHND2FxojH")
//
//        val conn = ds.connection
//        val stmt = conn.createStatement()
//        val rs = stmt.executeQuery("SELECT * FROM library")
//        rs.next()
//        println(rs.getInt(1))
//        rs.close()
//        stmt.close()
//        conn.close()

    /** The current open connection to the MySQL server. null if the connection is closed. */
    private var connection: Connection = dataSource.connection

    /** Closes the connection to the currently connected MySQL server. */
    override fun close() {
        connection.close()
    }

/* ----------------------------------------- Retrieving ----------------------------------------- */

    override fun getVersion(): String {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }
}
