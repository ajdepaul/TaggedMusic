package TaggedMusic

import com.google.gson.Gson

import java.time.LocalDateTime

internal typealias TagsBeforeAfter = Pair<Set<String>,Set<String>>

class SongLibrary() {
    
    // observers
    private val tagUpdateObserver = TagUpdateObserver()
    private val anyUpdateObserver = AnyUpdateObserver()

    private var _lastModified = LocalDateTime.now()
    val lastModified: LocalDateTime get() { return _lastModified }

    var songs = setOf<Song>()
        set(value) {
            // songs removed
            for (song in Util.findRemoved(field, value)) {
                // remove any tags that are unused after the song is removed
                _tags -= song.tags.filter { tag -> value.all { song -> !song.tags.contains(tag) }}
                // update observers
                song.tagUpdateSubject.removeObserver(tagUpdateObserver)
                song.anyUpdateSubject.removeObserver(anyUpdateObserver)
            }

            // songs added
            for (song in Util.findAdded(field, value)) {
                // add new tags
                _tags += song.tags
                // update observers
                song.tagUpdateSubject.addObserver(tagUpdateObserver)
                song.anyUpdateSubject.addObserver(anyUpdateObserver)
            }

            field = value
            _lastModified = LocalDateTime.now()
        }
    
    private var _tags = setOf<String>()
    // val tags get() = _tags
    val tags: Set<String>
        get() {
            if (true) {
                _tags = songs.flatMap { song -> song.tags }.toSet()
            }
            return _tags
        }

    var tagTypes = setOf<TagType>()
        set(value) {
            var removed = Util.findRemoved(field, value)
            tagToType -= removed.map { tagType -> tagType.name }
            field = value
            _lastModified = LocalDateTime.now()
        }

    var tagToType = mapOf<String, TagType>()
        set(value) {
            var added = value.entries.filter { entry -> field.keys.contains(entry.key) }
            for (entry in added) tagTypes += entry.value
            field = value
            _lastModified = LocalDateTime.now()
        }

    // ---------------- Functions ---------------- //

    /**
     * @param inclTags songs must include these tags
     * @param exclTags songs cannot have these tags
     */
    fun tagFilter(inclTags: Set<String>, exclTags: Set<String>): Set<Song> {
        return songs.filter { song -> song.tags.containsAll(inclTags) }
                    .filter { song -> !song.tags.any { tag -> exclTags.contains(tag) }}
                    .toSet()
    }

    // ---------------- JSON ---------------- //

    /** Save song library as JSON */
    internal fun toJson(): String {
        return Gson().toJson(
            JsonData(_lastModified.toString()))
    }

    companion object {

        /** Load song library from JSON */
        internal fun fromJson(json: String): SongLibrary {

            val jsonData = Gson().fromJson(json, JsonData::class.java)

            return SongLibrary().apply {
                _lastModified = LocalDateTime.parse(jsonData.last_modified)
                _tags = songs.flatMap { song -> song.tags }.toSet()
            }
        }
    }

    private data class JsonData(val last_modified: String)

    // ---------------- Observers ---------------- //

    private inner class TagUpdateObserver : Observer<TagsBeforeAfter> {

        override fun update(dat : TagsBeforeAfter) {
            val (before, after) = dat

            // add new tags
            _tags += after

            // remove tags that are no longer in any songs
            _tags -= before.filter { tag -> songs.all { song -> !song.tags.contains(tag) }}
        }
    }

    private inner class AnyUpdateObserver : Observer<LocalDateTime> {
        override fun update(dat : LocalDateTime) { _lastModified = dat }
    }
}

data class TagType(val name: String, var color: Int) {
    override fun hashCode(): Int { return name.hashCode() }
    override fun equals(other: Any?): Boolean {
        return if (other is TagType) this.name == other.name else false
    }
}