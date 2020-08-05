package ajdepaul.taggedmusic

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.IOException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

interface LibraryProvider {

    /** @return the requested song library (null if failed) */
    fun pull(): SongLibrary?

    /**
     * @param songLibrary song library to push to the provider
     * @return true if success, false if failed
     */
    fun push(songLibrary: SongLibrary): Boolean
}

interface SongProvider {

    fun hasSong(fileName: String): Boolean

    /**
     * @param songPath the local path to the song file
     * @return true if success, false if failed
     */
    fun pushSong(songPath: String, fileName: String): Boolean

    /** @return the local path to the song (null if failed) */
    fun pullSong(fileName: String): String?

    /** @return true if success, false if failed */
    fun removeSong(fileName: String): Boolean


}

/* ----------------------------- Local Provider ----------------------------- */

class LocalLibraryProvider(private val libraryPath: String) : LibraryProvider {

    override fun pull(): SongLibrary? {
        return try { File(libraryPath).reader().use { Gson().fromJson(it, SongLibrary.JsonData::class.java).toSongLibrary() }}
        catch (_: JsonIOException) { null }
        catch (_: JsonSyntaxException) { null }
        catch (_: IOException) { null }
    }

    override fun push(songLibrary: SongLibrary): Boolean {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return try { File(libraryPath).writer().use { gson.toJson(songLibrary.toJsonData(), it) }; true }
        catch (_: JsonIOException) { return false }
        catch (_: IOException) { return false }
    }
}

class LocalSongProvider(private val songDirectory: String) : SongProvider {

    override fun hasSong(fileName: String): Boolean {
        return File(songDirectory).resolve(fileName).isFile
    }

    override fun pushSong(songPath: String, fileName: String): Boolean {
        val dest = File(songDirectory).resolve(fileName).toPath()
        return try { Files.copy(Paths.get(songPath), dest, StandardCopyOption.REPLACE_EXISTING); true }
        catch (_: DirectoryNotEmptyException) { false }
        catch (_: IOException) { false }
        catch (_: SecurityException) { false }
    }

    override fun pullSong(fileName: String): String? {
        val songFile = File(songDirectory).resolve(fileName)
        return if (songFile.isFile) songFile.path else null
    }

    override fun removeSong(fileName: String): Boolean {
        return try { File(songDirectory).resolve(fileName).delete() }
        catch (_: SecurityException) { false }
    }
}
