/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources.old

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.songlibraries.CachedSongLibrary
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import java.io.Closeable
import java.sql.Connection

/**
 * [CachedSongLibrary] that is saved using a MySQL server. Specifications on how to set up the database
 * can be found here: TODO.
 */
class MysqlSongLibrary(private val dataSource: MysqlDataSource?) : CachedSongLibrary(), Closeable {

    private var connection: Connection? = null

    override lateinit var defaultTagType: TagType

    override val version: String
        get() = TODO("Not yet implemented")

    constructor(
        host: String,
        database: String,
        port: Int = 3306,
        user: String = "root",
        password: String = "",
        timeout: Int = 5
    ) : this(
        MysqlDataSource().apply {
            this.serverName = host
            this.databaseName = database
            this.port = port
            this.user = user
            this.setPassword(password)
//            this.timeout = 5 TODO fix timeout
        })

    init {
        defaultTagType = TagType(32) // TODO get default tag type from [dataSource]
    }

    /**
     * Opens a connection to the MySQL server using the [MysqlDataSource] provided. The connection
     * must be open to use any of the put or remove methods.
     */
    fun connect() {
        connection = dataSource?.connection // TODO double check null
    }

    /**
     * Closes the connection to the MySQL server using the [MysqlDataSource] provided. The connection
     * must be open to use any of the put or remove methods.
     */
    override fun close() {
        if (connection != null) {
            connection!!.close()
            connection = null
        }
    }

    /*


    TODO VERSION NUMBER STRING OR INT? & COMMIT CHANGES


    val ds = MysqlDataSource()
    ds.serverName = "10.0.0.25"
    ds.databaseName = "tm-db"
    ds.port = 1258
    ds.user = "root"
    ds.setPassword("tv3RTHND2FxojH")

    val conn = ds.connection
    val stmt = conn.createStatement()
    val rs = stmt.executeQuery("SELECT * FROM library")
    rs.next()
    println(rs.getInt(1))
    rs.close()
    stmt.close()
    conn.close()
     */

/* -------------------------------------------- Songs ------------------------------------------- */

    override fun putSongImpl(fileName: String, song: Song) {
        TODO("Not yet implemented")
    }

    override fun removeSong(fileName: String) {
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

/* -------------------------------------------- Tags -------------------------------------------- */

    override fun putTagImpl(tagName: String, tag: Tag) {
        TODO("Not yet implemented")
    }

    override fun removeTag(tagName: String) {
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

/* ------------------------------------------ Tag Types ----------------------------------------- */

    override fun putTagTypeImpl(tagTypeName: String, tagType: TagType) {
        TODO("Not yet implemented")
    }

    override fun removeTagType(tagTypeName: String) {
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
}
