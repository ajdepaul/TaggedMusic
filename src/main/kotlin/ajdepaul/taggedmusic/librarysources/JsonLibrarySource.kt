/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusic.Tag
import ajdepaul.taggedmusic.TagType
import ajdepaul.taggedmusic.extensions.filterByTags
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import kotlinx.collections.immutable.*
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.*

/**
 * [LibrarySource] that uses a JSON file to store [SongLibrary] data. Every read and write to this
 * [LibrarySource] will parse the entire JSON text, so it is recommended to use this with a
 * [CahedSongLibrary][ajdepaul.taggedmusic.songlibraries.CachedSongLibrary].
 *
 * Every read or write can throw a [JsonIOException], [JsonSyntaxException], or [IOException] if
 * there is an issue with the JSON reader/writer, JSON file syntax, or reaching the file at
 * [jsonFilePath] respectively.
 * @param jsonFilePath local path to the [SongLibrary]
 * [ajdepaul.taggedmusic.songlibraries.SongLibrary]
 */
class JsonLibrarySource(
    /** Local path to the [SongLibrary][ajdepaul.taggedmusic.songlibraries.SongLibrary]. */
    var jsonFilePath: String
) : LibrarySource {

/* ----------------------------------------- Retrieving ----------------------------------------- */

    override fun getVersion(): String {
        return readJson().version
    }

    override fun getDefaultTagType(): TagType {
        return readJson().defaultTagType
    }

    override fun hasSong(fileName: String): Boolean {
        return fileName in readJson().songs
    }

    override fun getSong(fileName: String): Song? {
        return readJson().songs[fileName]?.toSong()
    }

    override fun getAllSongs(): PersistentMap<String, Song> {
        return readJson().songs
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
        return tagName in readJson().tags
    }

    override fun getTag(tagName: String): Tag? {
        return readJson().tags[tagName]
    }

    override fun getAllTags(): PersistentMap<String, Tag> {
        return readJson().tags.toPersistentHashMap()
    }

    override fun hasTagType(tagTypeName: String): Boolean {
        return tagTypeName in readJson().tagTypes
    }

    override fun getTagType(tagTypeName: String): TagType? {
        return readJson().tagTypes[tagTypeName]
    }

    override fun getAllTagTypes(): PersistentMap<String, TagType> {
        return readJson().tagTypes.toPersistentHashMap()
    }

/* ------------------------------------------ Updating ------------------------------------------ */

    override fun updater(): LibrarySource.UpdateBuilder {
        return UpdateBuilder(this)
    }

    /** See [LibrarySource.UpdateBuilder]. */
    private class UpdateBuilder(val jsonLibrarySource: JsonLibrarySource) :
        LibrarySource.UpdateBuilder {

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

        override fun commit() {
            val songLibraryData = jsonLibrarySource.readJson()
            var songs = songLibraryData.songs.toPersistentHashMap()
            var tags = songLibraryData.tags.toPersistentHashMap()
            var tagTypes = songLibraryData.tagTypes.toPersistentHashMap()

            // apply each update
            while (updateQueue.isNotEmpty()) {
                when (val update = updateQueue.remove()) {
                    is LibrarySource.SetDefaultTagTypeUpdate ->
                        songLibraryData.defaultTagType = update.tagType
                    is LibrarySource.PutSongUpdate ->
                        songs += (update.fileName to update.song.toJsonData())
                    is LibrarySource.RemoveSongUpdate ->
                        songs -= update.fileName
                    is LibrarySource.PutTagUpdate ->
                        tags += update.tagName to update.tag
                    is LibrarySource.RemoveTagUpdate ->
                        tags -= update.tagName
                    is LibrarySource.PutTagTypeUpdate ->
                        tagTypes += update.tagTypeName to update.tagType
                    is LibrarySource.RemoveTagTypeUpdate ->
                        tagTypes -= update.tagTypeName
                    else -> error("Unexpected LibrarySource.Up}date type.")
                }
            }
            songLibraryData.songs = songs
            songLibraryData.tags = tags
            songLibraryData.tagTypes = tagTypes

            songLibraryData.writeJson(jsonLibrarySource.jsonFilePath)
        }

        /**
         * Creates/updates the file with a JSON string using this [SongLibraryData].
         * @throws JsonIOException if there was a problem writing to the JSON writer
         * @throws IOException if there was a problem writing to the file at [jsonFilePath]
         */
        @Throws(JsonIOException::class, IOException::class)
        fun SongLibraryData.writeJson(jsonFilePath: String) {
            val gson = GsonBuilder().setPrettyPrinting().create()
            File(jsonFilePath).writer().use { gson.toJson(this, it) }
        }

        /** Converts this [Song] into a [SongData]. */
        fun Song.toJsonData(): SongData {
            return SongData(
                this.title, this.duration, this.artist, this.album, this.trackNum, this.year,
                this.lastModified.toString(), this.playCount, this.tags
            )
        }
    }

/* -------------------------------------- JSON Data Helpers ------------------------------------- */

    /**
     * Creates [SongLibraryData] from [jsonFilePath].
     * @throws JsonIOException if there was a problem reading from the JSON Reader
     * @throws JsonSyntaxException if [jsonFilePath] uses bad JSON format
     * @throws IOException if there was a problem reading the file at [jsonFilePath]
     */
    @Throws(JsonIOException::class, JsonSyntaxException::class, IOException::class)
    private fun readJson(): SongLibraryData {
        return File(jsonFilePath).reader().use {
            Gson().fromJson(it, SongLibraryData::class.java)
        }
    }

    /**
     * [SongLibrary][ajdepaul.taggedmusic.songlibraries.SongLibrary] data stored in a format for
     * reading/writing JSON strings.
     */
    private data class SongLibraryData(
        val version: String,
        var defaultTagType: TagType,
        var songs: Map<String, SongData>,
        var tags: Map<String, Tag>,
        var tagTypes: Map<String, TagType>
    )

    /** [Song] data stored in a format for reading/writing JSON strings. */
    private data class SongData(
        val title: String, val duration: Int, val artist: String?, val album: String?,
        val trackNum: Int?, val year: Int?, val lastModified: String, val playCount: Int,
        val tags: Set<String>
    ) {

        /** Converts this [SongData] into a [Song]. */
        fun toSong(): Song {
            return Song(
                this.title, this.duration, this.artist, this.album, this.trackNum, this.year,
                LocalDateTime.parse(this.lastModified), this.playCount,
                this.tags.toPersistentHashSet()
            )
        }
    }
}
