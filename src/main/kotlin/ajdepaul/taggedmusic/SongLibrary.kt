package ajdepaul.taggedmusic

import kotlinx.collections.immutable.*

import java.time.LocalDateTime

import com.google.gson.Gson

class SongLibrary() {
    
/* ------------------------------- Properties ------------------------------- */

    // observer updates
    private val onAnyUpdate = { dat: LocalDateTime -> _lastModified = dat }
    private val onSongTagsUpdate = { dat: Set<String> -> tags += dat.map { tag -> Pair(tag, null) } }

    private var _lastModified = LocalDateTime.now()
    val lastModified: LocalDateTime get() { return _lastModified }

    // must be persistent set as the efficiency is relied on
    var songs: PersistentSet<Song> = persistentHashSetOf<Song>()
        set(value) {
            // songs removed
            for (song in Util.findRemoved(field, value)) {
                // update observers
                song.tagUpdateSubject.removeObserver(onSongTagsUpdate)
                song.anyUpdateSubject.removeObserver(onAnyUpdate)
            }

            // songs added
            for (song in Util.findAdded(field, value)) {
                // add new tags
                tags += song.tags.filterNot { tag -> tags.contains(tag) }
                                 .map { tag -> Pair(tag, null) }
                // update observers
                song.tagUpdateSubject.addObserver(onSongTagsUpdate)
                song.anyUpdateSubject.addObserver(onAnyUpdate)
            }

            field = value
            _lastModified = LocalDateTime.now()
        }

    // must be persistent set as the efficiency is relied on
    /** key: tag, value: tag type name */
    var tags: PersistentMap<String, String?> = persistentHashMapOf()
        set(value) {
            // if there are new tag types mapped, add them to the tagTypes set
            tagTypes += value.filter { entry -> field[entry.key] != entry.value
                                                && !tagTypes.keys.contains(entry.value) }
                            // entry.value guaranteed not null
                             .map { entry -> Pair(entry.value!!, defaultTagType.copy()) }
            
            // removed the tags that have been removed from each song
            val removedTags = Util.findRemoved(field, value).map { it.key }
            songs.forEach { it._tags -= removedTags }

            field = value
            _lastModified = LocalDateTime.now()
        }

    /** Default tag type data when either:
     *  1. a tag name is mapped to null in the tags map, or
     *  2. a new tag type is created indirectly by adding a tag type name to the
     *     tags map that isn't already in the tagTypes map
     */
    var defaultTagType = TagTypeData(0)

    /** key: tag type name, value: tag type data */
    var tagTypes: PersistentMap<String, TagTypeData> = persistentHashMapOf()
        set(value) {
            Util.findAdded(field, value)
            field = value
            _lastModified = LocalDateTime.now()
        }

/* -------------------------------- Functions ------------------------------- */

    /**
     * @param inclTags songs must include these tag names
     * @param exclTags songs cannot have these tag names
     */
    fun tagFilter(inclTags: Set<String>, exclTags: Set<String>): Set<Song> {
        return songs.filter { song -> song.tags.containsAll(inclTags) }
                    .filterNot { song -> song.tags.any { tag -> exclTags.contains(tag) }}.toSet()
    }

/* ---------------------------------- JSON ---------------------------------- */

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
                // _tags = songs.flatMap { song -> song.tags }.toPersistentHashSetOf()
            }
        }
    }

    private data class JsonData(val last_modified: String)
}

/* ------------------------------ Tag Type Data ----------------------------- */

data class TagTypeData(val color: Int)
