/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

import com.jcraft.jsch.Session
import java.nio.file.Path

/** [AudioFileSource] that retrieves audio files from a SFTP server. */
class SftpAudioFileSource(
    /** [Session] for opening a connection to the SFTP server. */
    private val session: Session
) : AudioFileSource {
    override fun hasAudioFile(fileName: Path): Boolean {
        TODO("Not yet implemented")
    }

    override fun pushAudioFile(songPath: Path, fileName: Path): Boolean {
        TODO("Not yet implemented")
    }

    override fun pullAudioFile(fileName: Path): Path? {
        TODO("Not yet implemented")
    }

    override fun removeAudioFile(fileName: Path): Boolean {
        TODO("Not yet implemented")
    }
}
