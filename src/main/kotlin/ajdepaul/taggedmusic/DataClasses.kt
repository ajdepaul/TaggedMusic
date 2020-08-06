package ajdepaul.taggedmusic

import java.time.LocalDateTime
import kotlinx.collections.immutable.*

/* ---------------------------------- Song ---------------------------------- */

/** Cannot be converted to json directly. Use [toJsonData] for a json friendly format. */
data class Song private constructor(
        val title: String,
        val duration: Int,
        val artist: String?,
        val album: String?,
        val trackNum: Int?,
        val year: Int?,
        val lastModified: LocalDateTime,
        val playCount: Int,
        val tags: PersistentSet<String>
) {
    // second constructor allows tags to be passed as a generic set
    constructor(
            title: String,
            duration: Int,
            artist: String? = null,
            album: String? = null,
            trackNum: Int? = null,
            year: Int? = null,
            lastModified: LocalDateTime = LocalDateTime.now(),
            playCount: Int = 0,
            tags: Set<String> = persistentHashSetOf()
    ) : this(title, duration, artist, album, trackNum, year, lastModified, playCount, tags.toPersistentHashSet())

    // Mutations

    /** Returns a new [Song] with the the changes from [mutator] applied. */
    fun mutate(updateLastModified: Boolean = true, mutator: MutableSong.() -> Unit): Song {
        return MutableSong(this).apply(mutator).build(updateLastModified)
    }

    data class MutableSong internal constructor(private val song: Song) {
        var title: String = song.title
        var duration: Int = song.duration
        var artist: String? = song.artist
        var album: String? = song.album
        var trackNum: Int? = song.trackNum
        var year: Int? = song.year
        var playCount: Int = song.playCount
        var tags: PersistentSet<String> = song.tags

        internal fun build(updateLastModified: Boolean): Song {
            return Song(title, duration, artist, album, trackNum, year,
                    if(updateLastModified) LocalDateTime.now() else song.lastModified, playCount, tags)
        }
    }

    // JSON

    internal fun toJsonData(): JsonData {
        return JsonData(title, duration, artist, album, trackNum, year, lastModified.toString(), playCount, tags)
    }

    data class JsonData internal constructor(val title: String, val duration: Int, val artist: String?, val album: String?,
            val trackNum: Int?, val year: Int?, val lastModified: String, val playCount: Int, val tags: Set<String>) {

        fun toSong(): Song {
            return Song(this.title, this.duration, this.artist, this.album, this.trackNum,
                    this.year, LocalDateTime.parse(this.lastModified), this.playCount, this.tags.toPersistentHashSet())
        }
    }
}

/* ----------------------------------- Tag ---------------------------------- */

data class Tag(val type: String?, val description: String? = null) {

    /** Returns a new [Tag] with the the changes from [mutator] applied. */
    fun mutate(mutator: MutableTag.() -> Unit): Tag {
        return MutableTag(this).apply(mutator).build()
    }

    data class MutableTag internal constructor(private val tag: Tag) {
        var type: String? = tag.type
        var description: String? = tag.description

        internal fun build(): Tag {
            return Tag(type, description)
        }
    }
}

/* -------------------------------- Tag Type -------------------------------- */

data class TagType(val color: Int) {

    /** Returns a new [TagType] with the the changes from [mutator] applied. */
    fun mutate(mutator: MutableTagType.() -> Unit): TagType {
        return MutableTagType(this).apply(mutator).build()
    }

    data class MutableTagType internal constructor(private val tagType: TagType) {
        var color: Int = tagType.color

        internal fun build(): TagType {
            return TagType(color)
        }
    }
}
