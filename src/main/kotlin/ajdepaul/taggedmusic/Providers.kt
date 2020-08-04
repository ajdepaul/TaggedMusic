package ajdepaul.taggedmusic

import com.google.gson.Gson

import java.io.File
import java.io.FileReader
import java.io.FileWriter

interface LibraryProvider {
    /** @return the requested song library (null if failed) */
    fun pull(): SongLibrary?
    /** @param songLibrary song library to push to the provider */
    fun push(songLibrary: SongLibrary)
}

interface SongProvider {
    /** @return the local path to the song (null if failed) */
    fun retrieveSong(songPath: String): String?
}

/* ----------------------------- Local Provider ----------------------------- */

class LocalLibraryProvider(val libraryPath: String) : LibraryProvider {

    override fun pull(): SongLibrary {
        return SongLibrary(Gson().fromJson(FileReader(libraryPath), SongLibrary.JsonData::class.java))
    }

    override fun push(songLibrary: SongLibrary) {
        Gson().toJson(songLibrary.toJsonData(), FileWriter(libraryPath))
    }
}

class LocalSongProvider(val songDirectory: String) : SongProvider {

    override fun retrieveSong(songPath: String): String {
        return File(songDirectory).resolve(songPath).path
    }
}
