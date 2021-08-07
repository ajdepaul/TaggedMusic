/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

import java.io.File
import java.io.IOException
import java.nio.file.*

/** [AudioFileSource] that retrieves audio files from a local directory. */
class LocalAudioFileSource(var songDirectory: Path) : AudioFileSource {

    override fun hasAudioFile(fileName: Path): Boolean {
        return songDirectory.resolve(fileName).toFile().isFile
    }

    override fun pushAudioFile(songPath: Path, fileName: Path): Boolean {
        val dest = songDirectory.resolve(fileName)
        return try {
            Files.copy(songPath, dest, StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (_: DirectoryNotEmptyException) {
            false
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    override fun pullAudioFile(fileName: Path): Path? {
        val songFile = songDirectory.resolve(fileName)
        return if (songFile.toFile().isFile) songFile else null
    }

    override fun removeAudioFile(fileName: Path): Boolean {
        return try {
            songDirectory.resolve(fileName).toFile().delete()
        } catch (_: SecurityException) {
            false
        }
    }
}
