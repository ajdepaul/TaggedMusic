/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

import java.io.IOException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/** [AudioFileSource] that retrieves audio files from a local directory. */
class LocalAudioFileSource(private val songDirectory: Path) : AudioFileSource {

    override fun hasAudioFile(fileName: String): Boolean {
        return songDirectory.resolve(fileName).toFile().isFile
    }

    override fun pushAudioFile(audioPath: Path, fileName: String): Boolean {
        val dest = songDirectory.resolve(fileName)
        return try {
            Files.copy(audioPath, dest, StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (_: DirectoryNotEmptyException) {
            false
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    override fun pullAudioFile(fileName: String): Path? {
        val songFile = songDirectory.resolve(fileName)
        return if (songFile.toFile().isFile) songFile else null
    }

    override fun removeAudioFile(fileName: String): Boolean {
        return try {
            songDirectory.resolve(fileName).toFile().delete()
        } catch (_: SecurityException) {
            false
        }
    }
}
