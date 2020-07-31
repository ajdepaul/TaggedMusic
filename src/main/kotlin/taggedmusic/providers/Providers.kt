package taggedmusic.providers

import taggedmusic.SongLibrary
import taggedmusic.Song

interface LibraryProvider {
    fun download(): SongLibrary
    fun upload(songLibrary: SongLibrary)
}

interface SongProvider {
    fun download(file: String): Song
}
