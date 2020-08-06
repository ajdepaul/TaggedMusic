package ajdepaul.taggedmusic

import java.time.LocalDateTime
import kotlinx.collections.immutable.*

const val JSON_VERSION = "1.0"

class SongLibrary(defaultTagType: TagType) {
    
/* ------------------------------- Properties ------------------------------- */

    val lastModified: LocalDateTime get() { return _lastModified }
    private var _lastModified = LocalDateTime.now()

    /** key: file name, value: song data */
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
    private var _tagTypes: PersistentMap<String?, TagType> = persistentHashMapOf()

    var defaultTagType: TagType
        get() { return tagTypes[null] ?: error("Default tag type not in tagTypes[null]") }
        set(value) { _tagTypes += null to value }

    init { this.defaultTagType = defaultTagType }

/* -------------------------------- Functions ------------------------------- */

    /** @param fileName if blank, no change is made */
    fun putSong(fileName: String, song: Song) {
        fileName.ifBlank { return }
        _songs += fileName to song

        // add new tags
        _tags += song.tags.filterNot { tags.contains(it) }
                .map { it to Tag(null, null) }

        _lastModified = LocalDateTime.now()
    }

    fun removeSong(fileName: String) {
        _songs -= fileName
        _lastModified = LocalDateTime.now()
    }

    /** @param tagName if blank, no change is made */
    fun putTag(tagName: String, tag: Tag) {
        tagName.ifBlank { return }
        _tags += tagName to tag

        // add new tag type
        if (tag.type !in tagTypes) { _tagTypes += tag.type to defaultTagType }

        _lastModified = LocalDateTime.now()
    }

    fun removeTag(tagName: String) {
        _tags -= tagName

        // remove tag from songs
        for ((key, song) in songs) {
            _songs += key to (song.mutate { tags -= tagName })
        }

        _lastModified = LocalDateTime.now()
    }

    /** @param tagTypeName if blank, no change is made */
    fun putTagType(tagTypeName: String, tagType: TagType) {
        tagTypeName.ifBlank { return }
        _tagTypes += tagTypeName to tagType
        _lastModified = LocalDateTime.now()
    }

    fun removeTagType(tagTypeName: String) {
        _tagTypes -= tagTypeName

        // remove tag type from tags
        for ((key, tag) in tags) {
            if (tag.type == tagTypeName) { _tags += key to (tag.mutate { type = null }) }
        }

        _lastModified = LocalDateTime.now()
    }

    /**
     * Returns a map of songs according to the provided filters.
     *
     * @param includeTags songs must have all of these tags
     * @param excludeTags songs cannot have any of these tags
     */
    fun tagFilter(includeTags: Set<String> = setOf(), excludeTags: Set<String> = setOf()): Map<String, Song> {
        return songs.filter { it.value.tags.containsAll(includeTags) }
                .filterNot { it.value.tags.any { tag -> excludeTags.contains(tag) }}
    }

/* ---------------------------------- JSON ---------------------------------- */

    fun toJsonData(): JsonData {
        return JsonData(
                JSON_VERSION,
                lastModified.toString(),
                songs.map { it.key to it.value.toJsonData() }.toMap(),
                tags,
                (tagTypes - (null as String?)).map { it.key!! to it.value }.toMap(),    // remove null key
                defaultTagType
        )
    }

    data class JsonData internal constructor(
            val jsonVersion: String,
            private val lastModified: String,
            private val songs: Map<String, Song.JsonData>,
            private val tags: Map<String, Tag>,
            private val tagTypes: Map<String, TagType>,
            private val defaultTagType: TagType
    ) {

        fun toSongLibrary(): SongLibrary {
            return SongLibrary(this@JsonData.defaultTagType).apply {
                _songs += this@JsonData.songs.map { it.key to it.value.toSong() }
                _tags += this@JsonData.tags
                _tagTypes += this@JsonData.tagTypes.map { (it.key as String?) to it.value }
                _lastModified = LocalDateTime.parse(this@JsonData.lastModified)
            }
        }
    }
}
