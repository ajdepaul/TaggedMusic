package ajdepaul.taggedmusic

import kotlinx.collections.immutable.*

import java.time.LocalDateTime

import com.google.gson.Gson

class Song internal constructor(
    val file: String,
    metaData: SongMetaData,
    val dateAdded: LocalDateTime = LocalDateTime.now()
) {

/* ------------------------------- Properties ------------------------------- */

    // observers
    internal val tagUpdateSubject = Subject<Set<String>>()
    internal val anyUpdateSubject = Subject<LocalDateTime>()

    // metadata
    var title: String = metaData.title ?: file
        set(value) { field = value; _lastModified = LocalDateTime.now() }

    var artist: String? = metaData.artist
        set(value) { field = value; _lastModified = LocalDateTime.now() }

    var album: String? = metaData.album
        set(value) { field = value; _lastModified = LocalDateTime.now() }

    var trackNum: Int? = metaData.trackNum
        set(value) { field = value; _lastModified = LocalDateTime.now() }

    var year: Int? = metaData.year
        set(value) { field = value; _lastModified = LocalDateTime.now() }

    /** Time in milliseconds */
    val duration: Long = metaData.duration

    // other
    private var _lastModified: LocalDateTime = dateAdded
        set(value) { field = value; anyUpdateSubject.notifySubjects(field) }
    val lastModified: LocalDateTime get() { return _lastModified }

    var playCount: Int = 0
        set(value) { field = value; _lastModified = LocalDateTime.now() }

    // must be persistent map as the efficiency is relied on
    var tags: PersistentSet<String>
        get() { return _tags }
        set(value) {
            _tags = value
            tagUpdateSubject.notifySubjects(value)
            _lastModified = LocalDateTime.now()
        }

    // provides a way of manipulating the tags without notifying observers
    internal var _tags: PersistentSet<String> = persistentHashSetOf()

/* -------------------------------- Functions ------------------------------- */

    override fun hashCode(): Int { return file.hashCode() }

    override fun equals(other: Any?): Boolean {
        return if (other is Song) this.file == other.file else false
    }

/* ---------------------------------- JSON ---------------------------------- */

    /** Save song as JsonData */
    internal fun toJsonData(): JsonData {
        return JsonData(file, dateAdded.toString(), title, artist, album, trackNum,
                        year, duration, lastModified.toString(), playCount, tags)
    }

    companion object {

        /** Load song from JsonData */
        internal fun fromJsonData(jsonData: JsonData): Song {

            val metaData = SongMetaData(jsonData.title, jsonData.artist, jsonData.album,
                                        jsonData.track_num, jsonData.year, jsonData.duration)

            return Song(jsonData.file, metaData, LocalDateTime.parse(jsonData.date_added))
                .apply {
                    playCount = jsonData.play_count
                    tags = jsonData.tags.toPersistentHashSet()
                    _lastModified = LocalDateTime.parse(jsonData.last_modified)
                }
        }
    }

    internal data class JsonData(val file: String,
                                val date_added: String,
                                val title: String,
                                val artist: String?,
                                val album: String?,
                                val track_num: Int?,
                                val year: Int?,
                                val duration: Long,
                                val last_modified: String,
                                val play_count: Int,
                                val tags: Set<String>)
}

/* ------------------------------ Data Classes ------------------------------ */

internal data class SongMetaData(val title:    String? = null,
                                 val artist:   String? = null,
                                 val album:    String? = null,
                                 val trackNum: Int?    = null,
                                 val year:     Int?    = null,
                                 val duration: Long)
