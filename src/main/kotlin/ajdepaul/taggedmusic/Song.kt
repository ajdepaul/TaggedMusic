/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import java.time.LocalDateTime

/** All the data stored about a single [Song]. */
data class Song constructor(
    val title: String,
    val duration: Int,
    val trackNum: Int? = null,
    val year: Int? = null,
    val dateCreated: LocalDateTime = LocalDateTime.now(),
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val playCount: Int = 0,
    val tags: PersistentSet<String> = persistentHashSetOf()
) {

    /** Returns a new [Song] with the changes from [mutator] applied. */
    fun mutate(updateLastModified: Boolean = true, mutator: SongBuilder.() -> Unit): Song {
        return SongBuilder(this).apply(mutator).build(updateLastModified)
    }

    data class SongBuilder internal constructor(private val song: Song) {
        var title: String = song.title
        var duration: Int = song.duration
        var trackNum: Int? = song.trackNum
        var year: Int? = song.year
        var playCount: Int = song.playCount
        var tags: PersistentSet<String> = song.tags

        internal fun build(updateLastModified: Boolean): Song {
            return Song(
                title,
                duration,
                trackNum,
                year,
                song.dateCreated,
                if (updateLastModified) LocalDateTime.now() else song.lastModified,
                playCount,
                tags
            )
        }
    }
}
