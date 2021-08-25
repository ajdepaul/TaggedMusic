/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.extensions.filterByTags
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.plus
import java.io.Closeable
import java.sql.*
import java.time.LocalDateTime
import java.util.*

/**
 * [LibrarySource] that is saved using a MySQL database server. The connection is opened using
 * [dataSource] on instantiation. This class is not thread safe, and it is recommended make sure
 * that all functions calls are synchronized.
 * @throws java.sql.SQLException if there is an issue connecting to or updating the MySQL server
 */
class MysqlLibrarySource(
    /** [MysqlDataSource] for opening a connection to the MySQL server. */
    private val dataSource: MysqlDataSource,
    /** Whether the MySQL server supports fractional seconds. */
    private val suppFracSec: Boolean = true
) : LibrarySource, Closeable {

    /** The current open connection to the MySQL server. null if the connection is closed. */
    private var connection: Connection = dataSource.connection

    /** All the callable procedures on the MySQL server. */
    private val callableStatements: CallableStatements = CallableStatements(connection)

    /** Closes the connection to the currently connected MySQL server. */
    override fun close() {
        connection.close()
    }

/* ----------------------------------------- Retrieving ----------------------------------------- */

    override fun getVersion(): String {
        return with(callableStatements.libraryGetVersion.executeQuery()) {
            this.first()
            this.getString(1)
        }
    }

    override fun getDefaultTagType(): TagType {
        return with(callableStatements.tagTypesGetDefault.executeQuery()) {
            this.first()
            TagType(this.getInt("color"))
        }
    }

    override fun hasSong(fileName: String): Boolean {
        callableStatements.songsSelect.setString("arg_file_name", fileName)
        return with(callableStatements.songsSelect.executeQuery()) {
            this.next()
        }
    }

    override fun getSong(fileName: String): Song? {
        // load song data
        callableStatements.songsSelect.setString("arg_file_name", fileName)
        var song = with(callableStatements.songsSelect.executeQuery()) {
            if (!this.first()) null
            else {
                var trackNum: Int? = this.getInt("track_num")
                if (this.wasNull()) trackNum = null
                Song(
                    this.getString("title"),
                    this.getInt("duration"),
                    trackNum,
                    this.getLocalDateTime("release_date"),
                    this.getLocalDateTime("create_date")!!,
                    this.getLocalDateTime("modify_date")!!,
                    this.getInt("play_count")
                )
            }
        } ?: return null

        // load load song tags
        song = song.mutate(false) {
            callableStatements.songHasTagSelectSongTags.setString("arg_song_file", fileName)
            with(callableStatements.songHasTagSelectSongTags.executeQuery()) {
                while (this.next()) {
                    tags += this.getString("tag")
                }
            }
        }

        return song
    }

    override fun getAllSongs(): PersistentMap<String, Song> {
        // load all the songs
        var songs = with(callableStatements.songsSelectAll.executeQuery()) {
            val result = persistentHashMapOf<String, Song>().builder()

            while (this.next()) {
                var trackNum: Int? = this.getInt("track_num")
                if (this.wasNull()) trackNum = null
                result[this.getString("file_name")] = Song(
                    this.getString("title"),
                    this.getInt("duration"),
                    trackNum,
                    this.getLocalDateTime("release_date"),
                    this.getLocalDateTime("create_date")!!,
                    this.getLocalDateTime("modify_date")!!,
                    this.getInt("play_count")
                )
            }

            result.build()
        }

        // load the song tags
        with(callableStatements.songHasTagSelectAll.executeQuery()) {
            while (this.next()) {
                val fileName = this.getString("song_file")
                songs += fileName to songs[fileName]!!
                    .mutate(false) { tags += this@with.getString("tag") }
            }
        }

        return songs
    }

    override fun getSongsByTags(
        includeTags: PersistentSet<String>,
        excludeTags: PersistentSet<String>
    ): PersistentMap<String, Song> {
        return getAllSongs().filterByTags(includeTags, excludeTags)
    }

    override fun hasTag(tagName: String): Boolean {
        callableStatements.tagsSelect.setString("arg_name", tagName)
        return with(callableStatements.tagsSelect.executeQuery()) {
            this.next()
        }
    }

    override fun getTag(tagName: String): Tag? {
        callableStatements.tagsSelect.setString("arg_name", tagName)
        return with(callableStatements.tagsSelect.executeQuery()) {
            this.next()
            if (!this.first()) null
            else Tag(this.getString("type"), this.getString("description"))
        }
    }

    override fun getAllTags(): PersistentMap<String, Tag> {
        return with(callableStatements.tagsSelectAll.executeQuery()) {
            val result = persistentHashMapOf<String, Tag>().builder()

            while (this.next()) {
                result[this.getString("name")] =
                    Tag(this.getString("type"), this.getString("description"))
            }

            result.build()
        }
    }

    override fun hasTagType(tagTypeName: String): Boolean {
        callableStatements.tagTypesSelect.setString("arg_name", tagTypeName)
        return with(callableStatements.tagTypesSelect.executeQuery()) {
            this.next()
        }
    }

    override fun getTagType(tagTypeName: String): TagType? {
        callableStatements.tagTypesSelect.setString("arg_name", tagTypeName)
        return with(callableStatements.tagTypesSelect.executeQuery()) {
            this.next()
            if (!this.first()) null
            else TagType(this.getInt("color"))
        }
    }

    override fun getAllTagTypes(): PersistentMap<String, TagType> {
        return with(callableStatements.tagTypesSelectAll.executeQuery()) {
            val result = persistentHashMapOf<String, TagType>().builder()

            while (this.next()) {
                result[this.getString("name")] = TagType(this.getInt("color"))
            }

            result.build()
        }
    }

    override fun hasData(key: String): Boolean {
        callableStatements.dataSelect.setString("arg_k", key)
        return with(callableStatements.dataSelect.executeQuery()) {
            this.next()
        }
    }

    override fun getData(key: String): String? {
        callableStatements.dataSelect.setString("arg_k", key)
        return with(callableStatements.dataSelect.executeQuery()) {
            if (!this.next()) null
            else this.getString("v")
        }
    }

    override fun getAllData(): PersistentMap<String, String> {
        return with(callableStatements.dataSelectAll.executeQuery()) {
            val result = persistentHashMapOf<String, String>().builder()

            while (this.next()) {
                result[this.getString("k")] = this.getString("v")
            }

            result.build()
        }
    }

    /**
     * Retrieves a [LocalDateTime] from this [ResultSet] with [columnLabel]. Correctly parses the
     * date, if it is stored as a [String] according to [suppFracSec].
     */
    private fun ResultSet.getLocalDateTime(columnLabel: String): LocalDateTime? {
        return if (suppFracSec) this.getTimestamp(columnLabel)?.toLocalDateTime()
        else {
            val dateString = this.getString(columnLabel)
            if (dateString == null) null
            else LocalDateTime.parse(dateString)
        }
    }

/* ------------------------------------------ Updating ------------------------------------------ */

    override fun updater(): LibrarySource.UpdateBuilder {
        return UpdateBuilder(connection, suppFracSec, callableStatements)
    }

    /** See [LibrarySource.UpdateBuilder]. */
    private class UpdateBuilder(
        private val connection: Connection,
        private val suppFracSec: Boolean,
        private val callableStatements: CallableStatements
    ) : LibrarySource.UpdateBuilder {

        private val updateQueue: Queue<LibrarySource.Update> = LinkedList()

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

                        is LibrarySource.SetDefaultTagTypeUpdate -> {
                            val cs = callableStatements.tagTypesPut
                            cs.setString("arg_name", "")
                            cs.setInt("arg_color", update.tagType.color)
                            cs.executeQuery().close()
                        }

                        is LibrarySource.PutSongUpdate -> {
                            // put song
                            val songCs = callableStatements.songsPut

                            songCs.setString("arg_file_name", update.fileName)
                            songCs.setString("arg_title", update.song.title)
                            songCs.setInt("arg_duration", update.song.duration)

                            if (update.song.trackNum != null)
                                songCs.setInt("arg_track_num", update.song.trackNum)
                            else
                                songCs.setNull("arg_track_num", Types.INTEGER)

                            if (update.song.releaseDate != null)
                                songCs.setLocalDateTime("arg_release_date", update.song.releaseDate)
                            else
                                songCs.setNull("arg_release_date", Types.TIMESTAMP)

                            songCs.setLocalDateTime("arg_create_date", update.song.createDate)
                            songCs.setLocalDateTime("arg_modify_date", update.song.modifyDate)
                            songCs.setInt("arg_play_count", update.song.playCount)

                            songCs.executeQuery().close()

                            // remove stored song tags
                            val removeTagsCs = callableStatements.songHasTagRemoveAllForSong
                            removeTagsCs.setString("arg_song_file", update.fileName)
                            removeTagsCs.executeQuery().close()

                            // check tag map
                            val selectAllTagsCs = callableStatements.tagsSelectAll
                            val allTags = with(selectAllTagsCs.executeQuery()) {
                                val result = persistentHashMapOf<String, Tag>().builder()

                                while (this.next()) {
                                    result[this.getString("name")] =
                                        Tag(this.getString("type"), this.getString("description"))
                                }

                                result.build()
                            }

                            // add each tag not in tag map
                            val putTagCs = callableStatements.tagsPut
                            for (tag in update.song.tags - allTags.keys) {
                                putTagCs.setString("arg_name", tag)
                                putTagCs.setNull("arg_type", Types.VARCHAR)
                                putTagCs.setNull("arg_description", Types.VARCHAR)
                                putTagCs.addBatch()
                            }
                            putTagCs.executeBatch()

                            // store new song tags
                            val putHasTagCs = callableStatements.songHasTagPut
                            for (tag in update.song.tags) {
                                putHasTagCs.setString("arg_song_file", update.fileName)
                                putHasTagCs.setString("arg_tag", tag)
                                putHasTagCs.addBatch()
                            }
                            putHasTagCs.executeBatch()
                        }

                        is LibrarySource.RemoveSongUpdate -> {
                            val cs = callableStatements.songsRemove
                            cs.setString("arg_file_name", update.fileName)
                            cs.executeQuery().close()
                        }

                        is LibrarySource.PutTagUpdate -> {
                            // check if tag type exists
                            if (update.tag.type != null) {
                                val tagTypesSelectCs = callableStatements.tagTypesSelect
                                tagTypesSelectCs.setString("arg_name", update.tag.type)

                                if (!with(tagTypesSelectCs.executeQuery()) { this.next() }) {
                                    // get default tag type
                                    val defaultTagType =
                                        with(callableStatements.tagTypesGetDefault.executeQuery()) {
                                            this.first()
                                            TagType(this.getInt("color"))
                                        }

                                    // add new tag type
                                    val tagTypesPutCs = callableStatements.tagTypesPut
                                    tagTypesPutCs.setString("arg_name", update.tag.type)
                                    tagTypesPutCs.setInt("arg_color", defaultTagType.color)
                                    tagTypesPutCs.executeQuery().close()
                                }
                            }

                            // add tag
                            val tagCs = callableStatements.tagsPut
                            tagCs.setString("arg_name", update.tagName)
                            tagCs.setString("arg_type", update.tag.type)

                            if (update.tag.description != null)
                                tagCs.setString("arg_description", update.tag.description)
                            else
                                tagCs.setNull("arg_description", Types.VARCHAR)

                            tagCs.executeQuery().close()
                        }

                        is LibrarySource.RemoveTagUpdate -> {
                            val cs = callableStatements.tagsRemove
                            cs.setString("arg_name", update.tagName)
                            cs.executeQuery().close()
                        }

                        is LibrarySource.PutTagTypeUpdate -> {
                            val cs = callableStatements.tagTypesPut
                            cs.setString("arg_name", update.tagTypeName)
                            cs.setInt("arg_color", update.tagType.color)
                            cs.executeQuery().close()
                        }

                        is LibrarySource.RemoveTagTypeUpdate -> {
                            val cs = callableStatements.tagTypesRemove
                            cs.setString("arg_name", update.tagTypeName)
                            cs.executeQuery().close()
                        }

                        is LibrarySource.PutDataUpdate -> {
                            val cs = callableStatements.dataPut
                            cs.setString("arg_k", update.key)
                            cs.setString("arg_v", update.value)
                            cs.executeQuery().close()
                        }

                        is LibrarySource.RemoveDataUpdate -> {
                            val cs = callableStatements.dataRemove
                            cs.setString("arg_k", update.key)
                            cs.executeQuery().close()
                        }

                        else -> error("Unexpected LibrarySource.Update type.")
                    }
                }
            }
        }

        /**
         * Sets the [parameterName] argument for this [CallableStatement] to [localDateTime]. The
         * date is correctly parsed and stored as a [String] according to [suppFracSec].
         */
        private fun CallableStatement.setLocalDateTime(
            parameterName: String,
            localDateTime: LocalDateTime
        ) {
            if (suppFracSec) this.setTimestamp(parameterName, Timestamp.valueOf(localDateTime))
            else this.setString(parameterName, localDateTime.toString())
        }
    }

/* ---------------------------------- MySQL Callable Statements --------------------------------- */

    /** Utility class for [CallableStatements] to execute procedures on the MySQL server. */
    private class CallableStatements(val connection: Connection) {

        /* ----- Retrieving Procedures -----*/

        /** Result: the version of the library. */
        val libraryGetVersion = connection.prepareCall("{call Library_get_version()}")

        /** Result: the default tag type. */
        val tagTypesGetDefault = connection.prepareCall("{call TagTypes_get_default()}")

        /** Result: the `file_name` song. */
        val songsSelect = connection.prepareCall("{call Songs_select(?)}")

        /** Result: all the songs. */
        val songsSelectAll = connection.prepareCall("{call Songs_select_all()}")

        /** Result: the `name` tag. */
        val tagsSelect = connection.prepareCall("{call Tags_select(?)}")

        /** Result: all the tags. */
        val tagsSelectAll = connection.prepareCall("{call Tags_select_all()}")

        /** Result: the `name` tag type. */
        val tagTypesSelect = connection.prepareCall("{call TagTypes_select(?)}")

        /** Result: all the tag types. */
        val tagTypesSelectAll = connection.prepareCall("{call TagTypes_select_all()}")

        /** Result: all the tags that `file_name` song has. */
        val songHasTagSelectSongTags =
            connection.prepareCall("{call SongHasTag_select_song_tags(?)}")

        /** Result: all song has tags relationships. */
        val songHasTagSelectAll = connection.prepareCall("{call SongHasTag_select_all()}")

        /** Result: the `k` data entry. */
        val dataSelect = connection.prepareCall("{call Data_select(?)}")

        /** Result: all the data entries. */
        val dataSelectAll = connection.prepareCall("{call Data_select_all()}")

        /* ----- Updating Procedures -----*/

        /** Inserts/updates a song. */
        val songsPut = connection.prepareCall("{call Songs_put(?, ?, ?, ?, ?, ?, ?, ?)}")

        /** Removes a song. */
        val songsRemove = connection.prepareCall("{call Songs_remove(?)}")

        /** Inserts/updates a tag. */
        val tagsPut = connection.prepareCall("{call Tags_put(?, ?, ?)}")

        /** Removes a tag. */
        val tagsRemove = connection.prepareCall("{call Tags_remove(?)}")

        /** Inserts/updates a tag type.  */
        val tagTypesPut = connection.prepareCall("{call TagTypes_put(?, ?)}")

        /** Removes a tag type. */
        val tagTypesRemove = connection.prepareCall("{call TagTypes_remove(?)}")

        /** Inserts a new song has tag relationship. */
        val songHasTagPut = connection.prepareCall("{call SongHasTag_put(?, ?)}")

        /** Removes all song has tag relationships for a song. */
        val songHasTagRemoveAllForSong =
            connection.prepareCall("{call SongHasTag_remove_all_for_song(?)}")

        /** Inserts/updates a data entry. */
        val dataPut = connection.prepareCall("{call Data_put(?, ?)}")

        /** Removes a data entry. */
        val dataRemove = connection.prepareCall("{call Data_remove(?)}")
    }
}
