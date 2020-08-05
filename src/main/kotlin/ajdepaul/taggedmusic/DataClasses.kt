package ajdepaul.taggedmusic

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.toPersistentHashSet
import java.time.LocalDateTime

/* ---------------------------------- Song ---------------------------------- */

/** Cannot be converted to json directly. Use [toJsonData] for a json friendly format. */
data class Song(
        val title: String,
        val duration: Int,
        val artist: String? = null,
        val album: String? = null,
        val trackNum: Int? = null,
        val year: Int? = null,
        val lastModified: LocalDateTime = LocalDateTime.now(),
        val playCount: Int = 0,
        val tags: PersistentSet<String> = persistentHashSetOf()
) {

    fun mutate(
            title: String = this.title,
            duration: Int = this.duration,
            artist: String? = this.artist,
            album: String? = this.album,
            trackNum: Int? = this.trackNum,
            year: Int? = this.year,
            playCount: Int = this.playCount,
            tags: PersistentSet<String> = this.tags,
            updateLastModified: Boolean = true
    ): Song {
        return if (updateLastModified)
            Song(title, duration, artist, album, trackNum, year, LocalDateTime.now(), playCount, tags)
        else Song(title, duration, artist, album, trackNum, year, lastModified, playCount, tags)
    }

    data class JsonData internal constructor(val title: String, val duration: Int, val artist: String?, val album: String?,
            val trackNum: Int?, val year: Int?, val lastModified: String, val playCount: Int, val tags: Set<String>) {

        fun toSong(): Song {
            return Song(this.title, this.duration, this.artist, this.album, this.trackNum,
                    this.year, LocalDateTime.parse(this.lastModified), this.playCount, this.tags.toPersistentHashSet())
        }
    }

    internal fun toJsonData(): JsonData {
        return JsonData(title, duration, artist, album, trackNum, year, lastModified.toString(), playCount, tags)
    }
}

/* ----------------------------------- Tag ---------------------------------- */

data class Tag(val type: String?, val description: String? = null) {

    fun mutate(type: String? = this.type, description: String? = this.description): Tag {
        return Tag(type, description)
    }
}

/* -------------------------------- Tag Type -------------------------------- */

data class TagType(val color: Int) {

    fun mutate(color: Int = this.color): TagType {
        return TagType(color)
    }
}
