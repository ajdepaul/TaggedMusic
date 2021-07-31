/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

import java.io.File
import java.io.IOException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/** [AudioFileSource] that retrieves audio files from a local directory. */
class LocalAudioFileSource(var songDirectory: String) : AudioFileSource {

    override fun hasAudioFile(fileName: String): Boolean {
        return File(songDirectory).resolve(fileName).isFile
    }

    override fun pushAudioFile(songPath: String, fileName: String): Boolean {
        val dest = File(songDirectory).resolve(fileName).toPath()
        return try {
            Files.copy(Paths.get(songPath), dest, StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (_: DirectoryNotEmptyException) {
            false
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    override fun pullAudioFile(fileName: String): String? {
        val songFile = File(songDirectory).resolve(fileName)
        return if (songFile.isFile) songFile.path else null
    }

    override fun removeAudioFile(fileName: String): Boolean {
        return try {
            File(songDirectory).resolve(fileName).delete()
        } catch (_: SecurityException) {
            false
        }
    }
}
