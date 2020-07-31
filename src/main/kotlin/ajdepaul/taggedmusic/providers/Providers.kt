package ajdepaul.taggedmusic.providers

import ajdepaul.taggedmusic.SongLibrary
import ajdepaul.taggedmusic.Song

interface LibraryProvider {
    fun download(): SongLibrary
    fun upload(songLibrary: SongLibrary)
}

interface SongProvider {
    fun download(file: String): Song
}
