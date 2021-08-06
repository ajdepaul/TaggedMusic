/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import java.time.LocalDateTime

// TODO add dateAdded & make artist nullable

/**
 * All the data stored about a single [Song]. Cannot be converted to json directly. Use [toJsonData]
 * for a json friendly format.
 */
data class Song constructor(
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

    /** Returns a new [Song] with the changes from [mutator] applied. */
    fun mutate(updateLastModified: Boolean = true, mutator: SongBuilder.() -> Unit): Song {
        return SongBuilder(this).apply(mutator).build(updateLastModified)
    }

    data class SongBuilder internal constructor(private val song: Song) {
        var title: String = song.title
        var duration: Int = song.duration
        var artist: String? = song.artist
        var album: String? = song.album
        var trackNum: Int? = song.trackNum
        var year: Int? = song.year
        var playCount: Int = song.playCount
        var tags: PersistentSet<String> = song.tags

        internal fun build(updateLastModified: Boolean): Song {
            return Song(
                title,
                duration,
                artist,
                album,
                trackNum,
                year,
                if (updateLastModified) LocalDateTime.now() else song.lastModified,
                playCount,
                tags
            )
        }
    }
}
