package ajdepaul.taggedmusic

import kotlinx.collections.immutable.*

import java.time.LocalDateTime

import com.google.gson.Gson

class SongLibrary(defaultTagType: TagType = TagType(0)) {
    
/* ------------------------------- Properties ------------------------------- */

    private var _lastModified = LocalDateTime.now()
    val lastModified: LocalDateTime get() { return _lastModified }

    /** key: file path, value: song data */
    val songs: Map<String, Song> get() { return _songs }
    var _songs: PersistentMap<String, Song> = persistentHashMapOf()

    /** key: tag, value: tag data */
    val tags: Map<String, Tag> get() { return _tags }
    var _tags: PersistentMap<String, Tag> = persistentHashMapOf()

    /**
     * key: tag type name, value: tag type data
     * null key refers to the default tag type
     */
    val tagTypes: Map<String?, TagType> get() { return _tagTypes }
    var _tagTypes: PersistentMap<String?, TagType> = persistentHashMapOf<String?, TagType>()
            .let { it + (null to defaultTagType)}   // default tag type

    private inline val defaultTagType: TagType
        get() { return tagTypes[null] ?: error("Default tag type not in tagTypes[null]") }

/* -------------------------------- Functions ------------------------------- */

    fun addSong(songPath: String, song: Song) {
        _songs += songPath to song
        // add new tags
        _tags += song.tags.filterNot { tags.contains(it) }
                          .map { it to Tag(null, null) }
    }

    fun removeSong(songPath: String) { _songs -= songPath }

    fun addTag(tagName: String, tag: Tag) {
        _tags += tagName to tag
        // add new tag type
        if (tag.type !in tagTypes) { _tagTypes += tag.type to defaultTagType }
    }

    fun removeTag(tagName: String) {
        _tags -= tagName
        // remove tag from songs
        for ((key, song) in songs) {
            _songs += key to (song.mutate(tags=song.tags - tagName))
        }
    }

    fun addTagType(tagTypeName: String, tagType: TagType) { _tagTypes += tagTypeName to tagType }

    fun removeTagType(tagTypeName: String) {
        _tagTypes -= tagTypeName
        // remove tag type from tags
        for ((key, tag) in tags) {
            if (tag.type == tagTypeName) { _tags += key to (tag.mutate(type=null)) }
        }
    }

    /**
     * Returns a map of songs according to the provided filters.
     *
     * @param inclTags songs must have all of these tags
     * @param exclTags songs cannot have any of these tags
     */
    fun tagFilter(inclTags: Set<String>, exclTags: Set<String>): Map<String, Song> {
        return songs.filter { it.value.tags.containsAll(inclTags) }
                    .filterNot { it.value.tags.any { tag -> exclTags.contains(tag) }}
    }

/* ---------------------------------- JSON ---------------------------------- */

    /** Save song library as JSON */
    internal fun toJson(): String {
        return Gson().toJson(JsonData(lastModified.toString(), songs, tags, tagTypes))
    }

    /** Load song library from JSON */
    constructor(json: String) : this() {
        val jsonData = Gson().fromJson(json, JsonData::class.java)
        _lastModified = LocalDateTime.parse(jsonData.lastModified)
        _songs        = jsonData.songs.toPersistentHashMap()
        _tags         = jsonData.tags.toPersistentHashMap()
        _tagTypes     = jsonData.tagTypes.toPersistentHashMap()
    }

    private data class JsonData(val lastModified: String,
                                val songs:         Map<String, Song>,
                                val tags:          Map<String, Tag>,
                                val tagTypes:      Map<String?, TagType>)
}
