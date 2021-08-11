/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.songlibraries.SongLibrary
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import kotlinx.collections.immutable.*
import java.io.Closeable
import java.nio.file.Paths
import java.sql.Connection
import java.sql.Statement
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * [LibrarySource] that is saved using a MySQL database server. The connection is opened using
 * [dataSource] on instantiation.
 * @throws java.sql.SQLException if there is an issue connecting to or updating the MySQL server
 */
class MysqlLibrarySource : LibrarySource, Closeable {

    /** [MysqlDataSource] for opening a connection to the MySQL server. */
    private val dataSource: MysqlDataSource

    /** The current open connection to the MySQL server. null if the connection is closed. */
    private var connection: Connection

    constructor(dataSource: MysqlDataSource) {
        this.dataSource = dataSource
        this.connection = dataSource.connection
    }

    /**
     * Creates an empty [MysqlLibrarySource] and initializes the MySQL server with the required
     * database.
     * @param defaultTagType the initial default tag type for the [SongLibrary]
     */
    constructor(dataSource: MysqlDataSource, defaultTagType: TagType) {
        this.dataSource = dataSource

        // create database
        val databaseName = dataSource.databaseName
        dataSource.databaseName = ""

        with(dataSource.connection) {
            with(this.createStatement()) {
                this.execute("CREATE DATABASE $databaseName;")
            }
        }

        // initialize database
        dataSource.databaseName = databaseName
        this.connection = dataSource.connection

        val libraryInitScript = this.javaClass.getResource(
            Paths.get("MysqlLibrarySource", "library_init.sql").toString()
        )!!
            .readText()
            .replace("<<version>>", SongLibrary.VERSION)
            .replace("<<default_tag_type_color>>", defaultTagType.color.toString())

        with(connection.createStatement()) {
            for (command in libraryInitScript.split(";").filterNot { it.isBlank() }) {
                this.addBatch("$command;")
            }
            this.executeBatch()
        }
    }

    /** Closes the connection to the currently connected MySQL server. */
    override fun close() {
        connection.close()
    }

/* ----------------------------------------- Retrieving ----------------------------------------- */

    override fun getVersion(): String {
        return with(connection.createStatement()) {
            with(this.executeQuery("SELECT * FROM library;")) {
                this.first()
                this.getString("version")
            }
        }
    }

    override fun getDefaultTagType(): TagType {
        return with(connection.createStatement()) {
            with(this.executeQuery("SELECT * FROM tag_types WHERE name = '';")) {
                this.first()
                TagType(this.getInt("color"))
            }
        }
    }

    override fun hasSong(fileName: String): Boolean {
        return with(connection.createStatement()) {
            with(
                this.executeQuery("SELECT COUNT(file_name) FROM songs WHERE file_name = '$fileName';")
            ) {
                this.first()
                this.getInt(1) == 1
            }
        }
    }

    override fun getSong(fileName: String): Song? {
        return with(connection.createStatement()) {
            with(this.executeQuery("SELECT * FROM songs WHERE file_name = '$fileName';")) {

                if (!this.first()) null
                else {
                    Song(
                        this.getString("title"),
                        this.getInt("duration"),
                        this.getInt("track_num"),
                        this.getInt("year"),
                        this.getDate("date_created").toInstant().atZone(ZoneId.systemDefault())
                            .toLocalDateTime(),
                        this.getDate("last_modified").toInstant().atZone(ZoneId.systemDefault())
                            .toLocalDateTime(),
                        this.getInt("playCount")
                    )
                }
            }
        }
    }

    override fun getAllSongs(): PersistentMap<String, Song> {
        return with(connection.createStatement()) {
            with(this.executeQuery("SELECT * FROM songs;")) {
                val result = persistentHashMapOf<String, Song>().builder()

                while (this.next()) {
                    result[this.getString("file_name")] = Song(
                        this.getString("title"),
                        this.getInt("duration"),
                        this.getInt("track_num"),
                        this.getInt("year"),
                        this.getDate("date_created").toInstant().atZone(ZoneId.systemDefault())
                            .toLocalDateTime(),
                        this.getDate("last_modified").toInstant().atZone(ZoneId.systemDefault())
                            .toLocalDateTime(),
                        this.getInt("playCount")
                    )
                }

                result.build()
            }
        }
    }

    override fun getSongsByTags(
        includeTags: PersistentSet<String>,
        excludeTags: PersistentSet<String>
    ): PersistentMap<String, Song> {
        return with(connection.createStatement()) {
            with(this.executeQuery("")) {
                val result = persistentHashMapOf<String, Song>().builder()

                TODO()

                result.build()
            }
        }
    }

    override fun hasTag(tagName: String): Boolean {
        return with(connection.createStatement()) {
            with(
                this.executeQuery("SELECT COUNT(name) FROM tags WHERE name = '$tagName';")
            ) {
                this.first()
                this.getInt(1) == 1
            }
        }
    }

    override fun getTag(tagName: String): Tag? {
        return with(connection.createStatement()) {
            with(this.executeQuery("SELECT * FROM tags WHERE name = '$tagName';")) {

                if (!this.first()) null
                else Tag(this.getString("type"), this.getString("description"))
            }
        }
    }

    override fun getAllTags(): PersistentMap<String, Tag> {
        return with(connection.createStatement()) {
            with(this.executeQuery("SELECT * FROM songs;")) {
                val result = persistentHashMapOf<String, Tag>().builder()

                while (this.next()) {
                    result[this.getString("name")] =
                        Tag(this.getString("type"), this.getString("description"))
                }

                result.build()
            }
        }
    }

    override fun hasTagType(tagTypeName: String): Boolean {
        return with(connection.createStatement()) {
            with(
                this.executeQuery("SELECT COUNT(name) FROM tag_types WHERE name = '$tagTypeName';")
            ) {
                this.first()
                this.getInt(1) == 1
            }
        }
    }

    override fun getTagType(tagTypeName: String): TagType? {
        return with(connection.createStatement()) {
            with(this.executeQuery("SELECT * FROM tag_types WHERE name = '$tagTypeName';")) {

                if (!this.first()) null
                else TagType(this.getInt("color"))
            }
        }
    }

    override fun getAllTagTypes(): PersistentMap<String, TagType> {
        return with(connection.createStatement()) {
            with(this.executeQuery("SELECT * FROM tag_types;")) {
                val result = persistentHashMapOf<String, TagType>().builder()

                while (this.next()) {
                    result[this.getString("name")] = TagType(this.getInt("color"))
                }

                result.build()
            }
        }
    }

    override fun hasData(key: String): Boolean {
        return with(connection.createStatement()) {
            with(
                this.executeQuery("SELECT COUNT(k) FROM data WHERE name = '$key';")
            ) {
                this.first()
                this.getInt(1) == 1
            }
        }
    }

    override fun getData(key: String): String? {
        return with(connection.createStatement()) {
            with(this.executeQuery("SELECT * FROM data WHERE k = '$key';")) {

                if (!this.first()) null
                else this.getString("v")
            }
        }
    }

    override fun getAllData(): PersistentMap<String, String> {
        return with(connection.createStatement()) {
            with(this.executeQuery("SELECT * FROM data;")) {
                val result = persistentHashMapOf<String, String>().builder()

                while (this.next()) {
                    result[this.getString("k")] = this.getString("v")
                }

                result.build()
            }
        }
    }

/* ------------------------------------------ Updating ------------------------------------------ */

    override fun updater(): LibrarySource.UpdateBuilder {
        return UpdateBuilder(
            connection,
            getAllSongs(),
            getAllTags(),
            getAllTagTypes(),
            getAllData()
        )
    }

    /** See [LibrarySource.UpdateBuilder]. */
    private class UpdateBuilder(
        val connection: Connection,
        var songs: PersistentMap<String, Song>,
        var tags: PersistentMap<String, Tag>,
        var tagTypes: PersistentMap<String, TagType>,
        var data: PersistentMap<String, String>
    ) : LibrarySource.UpdateBuilder {

        private val updateQueue: Queue<LibrarySource.Update> = LinkedList()

        /** Formats this [LocalDateTime] to the SQL DATETIME format. */
        private fun LocalDateTime.formatDate(): String {
            return this.toString().replace('T', ' ').dropLast(4)
        }

        override fun setDefaultTagType(tagType: TagType): LibrarySource.UpdateBuilder {
            updateQueue.add(LibrarySource.SetDefaultTagTypeUpdate(tagType))
            return this
        }

        override fun putSong(fileName: String, song: Song): LibrarySource.UpdateBuilder {
            updateQueue.add(LibrarySource.PutSongUpdate(fileName, song))
            return this
        }

        override fun removeSong(fileName: String): LibrarySource.UpdateBuilder {
            updateQueue.add(LibrarySource.RemoveSongUpdate(fileName))
            return this
        }

        override fun putTag(tagName: String, tag: Tag): LibrarySource.UpdateBuilder {
            updateQueue.add(LibrarySource.PutTagUpdate(tagName, tag))
            return this
        }

        override fun removeTag(tagName: String): LibrarySource.UpdateBuilder {
            updateQueue.add(LibrarySource.RemoveTagUpdate(tagName))
            return this
        }

        override fun putTagType(
            tagTypeName: String,
            tagType: TagType
        ): LibrarySource.UpdateBuilder {
            updateQueue.add(LibrarySource.PutTagTypeUpdate(tagTypeName, tagType))
            return this
        }

        override fun removeTagType(tagTypeName: String): LibrarySource.UpdateBuilder {
            updateQueue.add(LibrarySource.RemoveTagTypeUpdate(tagTypeName))
            return this
        }

        override fun putData(key: String, value: String): LibrarySource.UpdateBuilder {
            updateQueue.add(LibrarySource.PutDataUpdate(key, value))
            return this
        }

        override fun removeData(key: String): LibrarySource.UpdateBuilder {
            updateQueue.add(LibrarySource.RemoveDataUpdate(key))
            return this
        }

        override fun commit() {
            with(connection.createStatement()) {

                // add each update to the batch
                while (updateQueue.isNotEmpty()) {
                    when (val update = updateQueue.remove()) {

                        is LibrarySource.SetDefaultTagTypeUpdate ->
                            this.addBatch(
                                "UPDATE tag_types " +
                                        "SET color = ${update.tagType.color} " +
                                        "WHERE name = '';"
                            )

                        is LibrarySource.PutSongUpdate ->
                            putSong(this, update.fileName, update.song)

                        is LibrarySource.RemoveSongUpdate ->
                            songs -= update.fileName

                        is LibrarySource.PutTagUpdate -> {
                            tags += update.tagName to update.tag
                            // add the tag type to the tag type map if it's new
                            if (update.tag.type != null) {
                                tagTypes += update.tag.type to songLibraryData.defaultTagType
                            }
                        }

                        is LibrarySource.RemoveTagUpdate -> {
                            tags -= update.tagName
                            // remove the tag from every song
                            for (entry in songs) {
                                songs += entry.key to JsonLibrarySource.songToJsonData(
                                    entry.value.toSong()
                                        .mutate(false) { this.tags -= update.tagName })
                            }
                        }

                        is LibrarySource.PutTagTypeUpdate ->
                            tagTypes += update.tagTypeName to update.tagType

                        is LibrarySource.RemoveTagTypeUpdate -> {
                            tagTypes -= update.tagTypeName
                            // for every tag that uses this tag type, set its type to null
                            for (entry in tags) {
                                if (entry.value.type == update.tagTypeName) {
                                    tags += entry.key to entry.value.mutate { type = null }
                                }
                            }
                        }

                        is LibrarySource.PutDataUpdate -> {
                            data += update.key to update.value
                        }

                        is LibrarySource.RemoveDataUpdate -> {
                            data -= update.key
                        }

                        else -> error("Unexpected LibrarySource.Up}date type.")
                    }
                }

                // execute each command in the batch
                this.executeBatch()
            }
        }

/* ----------------------------------------- SQL Helpers ---------------------------------------- */

        // TODO move SQL commands to functions/procedures

        private fun putSong(statement: Statement, fileName: String, song: Song) {
            // update song
            if (songs.containsKey(fileName)) {
                statement.addBatch(
                    """
                        Update
                          songs
                        SET
                          title = '${song.title}'
                          duration = ${song.duration}
                          track_num = ${song.trackNum}
                          year = ${song.year}
                          date_created = ${song.dateCreated.formatDate()}
                          last_modified = ${song.lastModified.formatDate()}
                          play_count = ${song.playCount}
                        WHERE
                          file_name = '${fileName}';
                    """.trimIndent()
                )
            }
            // insert song
            else {
                statement.addBatch(
                    """
                        INSERT INTO
                          songs(
                            file_name,
                            title,
                            duration,
                            track_num,
                            year,
                            date_created,
                            last_modified,
                            play_count
                          )
                        VALUES(
                          '${fileName}',
                          '${song.title}',
                          ${song.duration},
                          ${song.trackNum},
                          ${song.year},
                          '${song.dateCreated.formatDate()}',
                          '${song.lastModified.formatDate()}',
                          ${song.playCount}
                        )
                    """.trimIndent()
                )
            }


//                            // add any new tags to the tag map
//                            tags += update.song.tags.associateWith { Tag(null) }
        }

    }
}
