package TaggedMusic

import com.google.gson.Gson

import java.time.LocalDateTime

import TaggedMusic.SongMetaData
import TaggedMusic.Subject
import TaggedMusic.Observer

class Song internal constructor(
    val file: String,
    metaData: SongMetaData,
    val dateAdded: LocalDateTime = LocalDateTime.now()
) {

    // observers
    private val tagUpdateSubject = TagUpdateSubject()
    private val anyUpdateSubject = AnyUpdateSubject()

    // metadata
    var title: String = if (metaData.title != null) metaData.title else file
        set(value) { field = value; anyUpdateSubject.notifySubjects() }

    var artist: String? = metaData.artist
        set(value) { field = value; anyUpdateSubject.notifySubjects() }

    var album: String? = metaData.album
        set(value) { field = value; anyUpdateSubject.notifySubjects() }

    var trackNum: Int? = metaData.trackNum
        set(value) { field = value; anyUpdateSubject.notifySubjects() }

    var year: Int? = metaData.year
        set(value) { field = value; anyUpdateSubject.notifySubjects() }

    /** Time in milliseconds */
    val duration: Long = metaData.duration

    // other
    var playCount: Int = 0
        set(value) { field = value; anyUpdateSubject.notifySubjects() }
    var tags = setOf<String>()
        set(value) { field = value; tagUpdateSubject.notifySubjects(); anyUpdateSubject.notifySubjects() }

    // ---------------- Functions ---------------- //

    override fun hashCode(): Int { return file.hashCode() }

    override fun equals(other: Any?): Boolean {
        return if (other is Song) this.file == other.file else false
    }

    // ---------------- JSON ---------------- //

    internal fun toJson(): String {

        val jsonData = JsonData(file,
                                title,
                                artist,
                                album,
                                trackNum,
                                year,
                                duration,
                                dateAdded.toString(),
                                playCount,
                                tags)

        return Gson().toJson(jsonData)
    }

    companion object {
        internal fun fromJson(json: String): Song {

            val jsonData = Gson().fromJson(json, JsonData::class.java)

            val metaData = SongMetaData(jsonData.title,
                                        jsonData.artist,
                                        jsonData.album,
                                        jsonData.track_num,
                                        jsonData.year,
                                        jsonData.duration)

            val song = Song(jsonData.file, metaData, LocalDateTime.parse(jsonData.date_added))
            song.playCount = jsonData.play_count
            song.tags = jsonData.tags

            return song
        }
    }

    private data class JsonData(val file: String,
                            val title: String,
                            val artist: String?,
                            val album: String?,
                            val track_num: Int?,
                            val year: Int?,
                            val duration: Long,
                            val date_added: String,
                            val play_count: Int,
                            val tags: Set<String>)

    // ---------------- Observers ---------------- //

    /** Subject used to send out updates when tags are modified */
    private inner class TagUpdateSubject : Subject<Set<String>> {

        val observers = mutableListOf<Observer<Set<String>>>()

        override fun addObserver(observer : Observer<Set<String>>) { observers.add(observer) }
        override fun removeObserver(observer: Observer<Set<String>>) { observers.remove(observer) }
        override fun notifySubjects() { for (o in observers) o.update(tags) }
    }

    /** Subject used to send out updates for any modification */
    private inner class AnyUpdateSubject : Subject<Any?> {

        val observers = mutableListOf<Observer<Any?>>()

        override fun addObserver(observer : Observer<Any?>) { observers.add(observer) }
        override fun removeObserver(observer: Observer<Any?>) { observers.remove(observer) }
        override fun notifySubjects() { for (o in observers) o.update(null) }
    }
}
