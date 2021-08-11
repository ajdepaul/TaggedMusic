/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.extensions.filterByTags
import ajdepaul.taggedmusic.songlibraries.SongLibrary
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import kotlinx.collections.immutable.*
import java.io.IOException
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*

/**
 * [LibrarySource] that uses a JSON file to store [SongLibrary] data. Every read and write to this
 * [LibrarySource] will parse the entire JSON text, so it is recommended to use this with a
 * [CachedSongLibrary][ajdepaul.taggedmusic.songlibraries.CachedSongLibrary].
 *
 * Every read or write can throw a [JsonIOException], [JsonSyntaxException], or [IOException] if
 * there is an issue with the JSON reader/writer, JSON file syntax, or reaching the file at
 * [jsonFilePath] respectively.
 * @param jsonFilePath local path to the [SongLibrary]
 * [ajdepaul.taggedmusic.songlibraries.SongLibrary]
 */
class JsonLibrarySource(
    /** Local path to the [SongLibrary][ajdepaul.taggedmusic.songlibraries.SongLibrary]. */
    var jsonFilePath: Path
) : LibrarySource {

    /**
     * Creates an empty [JsonLibrarySource] and creates the json file to go along with it.
     * @param defaultTagType the initial default tag type for the [SongLibrary]
     */
    constructor(jsonFilePath: Path, defaultTagType: TagType) : this(jsonFilePath) {
        writeJson(
            jsonFilePath,
            SongLibraryData(SongLibrary.VERSION, defaultTagType, mapOf(), mapOf(), mapOf(), mapOf())
        )
    }

/* ----------------------------------------- Retrieving ----------------------------------------- */

    override fun getVersion(): String {
        return readJson(jsonFilePath).version
    }

    override fun getDefaultTagType(): TagType {
        return readJson(jsonFilePath).defaultTagType
    }

    override fun hasSong(fileName: String): Boolean {
        return fileName in readJson(jsonFilePath).songs
    }

    override fun getSong(fileName: String): Song? {
        return readJson(jsonFilePath).songs[fileName]?.toSong()
    }

    override fun getAllSongs(): PersistentMap<String, Song> {
        return readJson(jsonFilePath).songs
            .map { it.key to it.value.toSong() }
            .toMap()
            .toPersistentHashMap()
    }

    override fun getSongsByTags(
        includeTags: PersistentSet<String>,
        excludeTags: PersistentSet<String>
    ): PersistentMap<String, Song> {
        return getAllSongs().filterByTags(includeTags, excludeTags)
    }

    override fun hasTag(tagName: String): Boolean {
        return tagName in readJson(jsonFilePath).tags
    }

    override fun getTag(tagName: String): Tag? {
        return readJson(jsonFilePath).tags[tagName]
    }

    override fun getAllTags(): PersistentMap<String, Tag> {
        return readJson(jsonFilePath).tags.toPersistentHashMap()
    }

    override fun hasTagType(tagTypeName: String): Boolean {
        return tagTypeName in readJson(jsonFilePath).tagTypes
    }

    override fun getTagType(tagTypeName: String): TagType? {
        return readJson(jsonFilePath).tagTypes[tagTypeName]
    }

    override fun getAllTagTypes(): PersistentMap<String, TagType> {
        return readJson(jsonFilePath).tagTypes.toPersistentHashMap()
    }

    override fun hasData(key: String): Boolean {
        return key in readJson(jsonFilePath).data
    }

    override fun getData(key: String): String? {
        return readJson(jsonFilePath).data[key]
    }

    override fun getAllData(): PersistentMap<String, String> {
        return readJson(jsonFilePath).data.toPersistentHashMap()
    }

/* ------------------------------------------ Updating ------------------------------------------ */

    override fun updater(): LibrarySource.UpdateBuilder {
        return UpdateBuilder(this, jsonFilePath)
    }

    /** See [LibrarySource.UpdateBuilder]. */
    private class UpdateBuilder(
        val jsonLibrarySource: JsonLibrarySource,
        val jsonFilePath: Path
    ) : LibrarySource.UpdateBuilder {

        val updateQueue: Queue<LibrarySource.Update> = LinkedList()

        override fun setDefaultTagType(tagType: TagType): UpdateBuilder {
            updateQueue.add(LibrarySource.SetDefaultTagTypeUpdate(tagType))
            return this
        }

        override fun putSong(fileName: String, song: Song): UpdateBuilder {
            updateQueue.add(LibrarySource.PutSongUpdate(fileName, song))
            return this
        }

        override fun removeSong(fileName: String): UpdateBuilder {
            updateQueue.add(LibrarySource.RemoveSongUpdate(fileName))
            return this
        }

        override fun putTag(tagName: String, tag: Tag): UpdateBuilder {
            updateQueue.add(LibrarySource.PutTagUpdate(tagName, tag))
            return this
        }

        override fun removeTag(tagName: String): UpdateBuilder {
            updateQueue.add(LibrarySource.RemoveTagUpdate(tagName))
            return this
        }

        override fun putTagType(tagTypeName: String, tagType: TagType): UpdateBuilder {
            updateQueue.add(LibrarySource.PutTagTypeUpdate(tagTypeName, tagType))
            return this
        }

        override fun removeTagType(tagTypeName: String): UpdateBuilder {
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
            val songLibraryData = readJson(jsonFilePath)
            var songs = songLibraryData.songs.toPersistentHashMap()
            var tags = songLibraryData.tags.toPersistentHashMap()
            var tagTypes = songLibraryData.tagTypes.toPersistentHashMap()
            var data = songLibraryData.data.toPersistentHashMap()

            // apply each update
            while (updateQueue.isNotEmpty()) {
                when (val update = updateQueue.remove()) {

                    is LibrarySource.SetDefaultTagTypeUpdate ->
                        songLibraryData.defaultTagType = update.tagType

                    is LibrarySource.PutSongUpdate -> {
                        songs += (update.fileName to songToJsonData(update.song))
                        // add any new tags to the tag map
                        tags += update.song.tags.associateWith { Tag(null) }
                    }

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
                            songs += entry.key to songToJsonData(
                                entry.value.toSong().mutate(false) { this.tags -= update.tagName })
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
            songLibraryData.songs = songs
            songLibraryData.tags = tags
            songLibraryData.tagTypes = tagTypes
            songLibraryData.data = data

            writeJson(jsonLibrarySource.jsonFilePath, songLibraryData)
        }
    }

/* -------------------------------------- JSON Data Helpers ------------------------------------- */

    private companion object {
        /**
         * Creates [SongLibraryData] from [jsonFilePath].
         * @throws JsonIOException if there was a problem reading from the JSON Reader
         * @throws JsonSyntaxException if [jsonFilePath] uses bad JSON format
         * @throws IOException if there was a problem reading the file at [jsonFilePath]
         */
        @Throws(JsonIOException::class, JsonSyntaxException::class, IOException::class)
        fun readJson(jsonFilePath: Path): SongLibraryData {
            return jsonFilePath.toFile().reader().use {
                Gson().fromJson(it, SongLibraryData::class.java)
            }
        }

        /**
         * Creates/updates the file with a JSON string using [songLibraryData].
         * @throws JsonIOException if there was a problem writing to the JSON writer
         * @throws IOException if there was a problem writing to the file at [jsonFilePath]
         */
        @Throws(JsonIOException::class, IOException::class)
        fun writeJson(jsonFilePath: Path, songLibraryData: SongLibraryData) {
            val gson = GsonBuilder().setPrettyPrinting().create()
            jsonFilePath.toFile().writer().use { gson.toJson(songLibraryData, it) }
        }

        /** Converts [song] into [SongData]. */
        fun songToJsonData(song: Song): SongData {
            return SongData(
                song.title, song.duration, song.trackNum, song.year, song.dateCreated.toString(),
                song.lastModified.toString(), song.playCount, song.tags
            )
        }

        /**
         * [SongLibrary][ajdepaul.taggedmusic.songlibraries.SongLibrary] data stored in a format for
         * reading/writing JSON strings.
         */
        data class SongLibraryData(
            val version: String,
            var defaultTagType: TagType,
            var songs: Map<String, SongData>,
            var tags: Map<String, Tag>,
            var tagTypes: Map<String, TagType>,
            var data: Map<String, String>
        )

        /** [Song] data stored in a format for reading/writing JSON strings. */
        data class SongData(
            val title: String, val duration: Int, val trackNum: Int?, val year: Int?,
            val dateCreated: String, val lastModified: String, val playCount: Int,
            val tags: Set<String>
        ) {

            /** Converts this [SongData] into a [Song]. */
            fun toSong(): Song {
                return Song(
                    this.title, this.duration, this.trackNum, this.year,
                    LocalDateTime.parse(this.dateCreated), LocalDateTime.parse(this.lastModified),
                    this.playCount, this.tags.toPersistentHashSet()
                )
            }
        }
    }
}
