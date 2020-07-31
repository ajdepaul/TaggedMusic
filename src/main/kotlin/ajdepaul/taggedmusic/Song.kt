package ajdepaul.taggedmusic

import com.google.gson.Gson

import java.time.LocalDateTime

class Song internal constructor(
    val file: String,
    metaData: SongMetaData,
    val dateAdded: LocalDateTime = LocalDateTime.now()
) {

    // observers
    internal val tagUpdateSubject = Subject<TagsBeforeAfter>()
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

    var tags = setOf<String>()
        set(value) {
            val before = field
            field = value
            tagUpdateSubject.notifySubjects(Pair(before, value))
            _lastModified = LocalDateTime.now()
        }

    // ---------------- Functions ---------------- //

    override fun hashCode(): Int { return file.hashCode() }

    override fun equals(other: Any?): Boolean {
        return if (other is Song) this.file == other.file else false
    }

    // ---------------- JSON ---------------- //

    /** Save song as JSON */
    internal fun toJson(): String {
        return Gson().toJson(JsonData(file,
                                      dateAdded.toString(),
                                      title,
                                      artist,
                                      album,
                                      trackNum,
                                      year,
                                      duration,
                                      lastModified.toString(),
                                      playCount,
                                      tags))
    }

    companion object {

        /** Load song from JSON */
        internal fun fromJson(json: String): Song {

            val jsonData = Gson().fromJson(json, JsonData::class.java)

            val metaData = SongMetaData(jsonData.title,
                                        jsonData.artist,
                                        jsonData.album,
                                        jsonData.track_num,
                                        jsonData.year,
                                        jsonData.duration)

            return Song(jsonData.file, metaData, LocalDateTime.parse(jsonData.date_added))
                .apply {
                    playCount = jsonData.play_count
                    tags = jsonData.tags
                    _lastModified = LocalDateTime.parse(jsonData.last_modified)
                }
        }
    }

    private data class JsonData(val file: String,
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

internal data class SongMetaData(val title:     String? = null,
                                 val artist:    String? = null,
                                 val album:     String? = null,
                                 val trackNum:  Int?    = null,
                                 val year:      Int?    = null,
                                 val duration:  Long)