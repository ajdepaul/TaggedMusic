package ajdepaul.taggedmusic

import kotlinx.collections.immutable.persistentHashSetOf
import java.time.LocalDateTime

// mutate functions used here are ugly in java

/* ---------------------------------- Song ---------------------------------- */

data class Song internal constructor(
        val title:        String,
        val artist:       String?       = null,
        val album:        String?       = null,
        val trackNum:     Int?          = null,
        val year:         Int?          = null,
        val duration:     Long,
        val lastModified: LocalDateTime = LocalDateTime.now(),
        val playCount:    Int           = 0,
        val tags:         Set<String>   = persistentHashSetOf()) {

    fun mutate(title:              String      = this.title,
               artist:             String?     = this.artist,
               album:              String?     = this.album,
               trackNum:           Int?        = this.trackNum,
               year:               Int?        = this.year,
               duration:           Long        = this.duration,
               playCount:          Int         = this.playCount,
               tags:               Set<String> = this.tags,
               updateLastModified: Boolean     = true
    ): Song {

        return if (updateLastModified)
            Song(title, artist, album, trackNum, year, duration, LocalDateTime.now(), playCount, tags)
        else Song(title, artist, album, trackNum, year, duration, lastModified, playCount, tags)
    }
}

/* ----------------------------------- Tag ---------------------------------- */

data class Tag(val description: String? = null,
               val type:        String?) {

    fun mutate(description: String? = this.description,
               type:     String? = this.type
            ): Tag {

        return Tag(description, type)
    }
}

/* -------------------------------- Tag Type -------------------------------- */

data class TagType(val color: Int) {

    fun mutate(color: Int = this.color): TagType {
        return TagType(color)
    }
}
