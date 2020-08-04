package ajdepaul.taggedmusic

import kotlinx.collections.immutable.*

import java.time.LocalDateTime

class SongLibrary(defaultTagType: TagType = TagType(0)) {
    
/* ------------------------------- Properties ------------------------------- */

    val lastModified: LocalDateTime get() { return _lastModified }
    private var _lastModified = LocalDateTime.now()

    /** key: file path, value: song data */
    val songs: Map<String, Song> get() { return _songs }
    private var _songs: PersistentMap<String, Song> = persistentHashMapOf()

    /** key: tag, value: tag data */
    val tags: Map<String, Tag> get() { return _tags }
    private var _tags: PersistentMap<String, Tag> = persistentHashMapOf()

    /**
     * key: tag type name, value: tag type data
     * null key refers to the default tag type
     */
    val tagTypes: Map<String?, TagType> get() { return _tagTypes }
    private var _tagTypes: PersistentMap<String?, TagType> = persistentHashMapOf<String?, TagType>()

    var defaultTagType: TagType
        get() { return tagTypes[null] ?: error("Default tag type not in tagTypes[null]") }
        set(value) { _tagTypes += null to value }

    init { this.defaultTagType = defaultTagType }

/* -------------------------------- Functions ------------------------------- */

    fun putSong(songPath: String, song: Song) {
        _songs += songPath to song
        // add new tags
        _tags += song.tags.filterNot { tags.contains(it) }
                          .map { it to Tag(null, null) }
    }

    fun removeSong(songPath: String) { _songs -= songPath }

    fun putTag(tagName: String, tag: Tag) {
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

    fun putTagType(tagTypeName: String, tagType: TagType) { _tagTypes += tagTypeName to tagType }

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

    fun toJsonData(): JsonData {
        return JsonData(lastModified.toString(), songs, tags, tagTypes)
    }

    /** Load song library from JSON */
    constructor(jsonData: JsonData) : this() {
        _lastModified = LocalDateTime.parse(jsonData.lastModified)
        _songs        = jsonData.songs.toPersistentHashMap()
        _tags         = jsonData.tags.toPersistentHashMap()
        _tagTypes     = jsonData.tagTypes.toPersistentHashMap()
    }

    data class JsonData(val lastModified: String,
                        val songs:        Map<String, Song>,
                        val tags:         Map<String, Tag>,
                        val tagTypes:     Map<String?, TagType>)
}
