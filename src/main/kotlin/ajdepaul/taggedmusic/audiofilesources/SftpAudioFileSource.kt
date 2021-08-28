/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session
import org.apache.commons.io.FileUtils
import java.io.Closeable
import java.nio.file.Path

/** [AudioFileSource] that retrieves audio files from a SFTP server. */
class SftpAudioFileSource(
    /** [Session] for opening a connection to the SFTP server. */
    private val session: Session,
    /** Path to a local directory where audio files can be downloaded to. */
    private val localDirectory: Path,
    /**
     * Path to a directory on the SFTP server where the audio files can be uploaded or retrieved.
     */
    remoteDirectory: Path
) : AudioFileSource, Closeable {

    private val channel: ChannelSftp

    init {
        // open the connection
        session.connect()
        channel = session.openChannel("sftp") as ChannelSftp
        channel.connect()

        // local working directory
        FileUtils.forceMkdir(localDirectory.toFile())
        channel.lcd(localDirectory.toString())

        // remote working directory
        channel.cd(remoteDirectory.toString())
    }

    override fun close() {
        session.disconnect()
    }

    override fun hasAudioFile(fileName: String): Boolean {
        return try {
            !channel.stat(fileName).isDir
        } catch (e: Exception) {
            false
        }
    }

    override fun pushAudioFile(audioPath: Path, fileName: String): Boolean {
        return try {
            channel.put(audioPath.toAbsolutePath().toString(), fileName)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun pullAudioFile(fileName: String): Path? {
        return try {
            channel.get(fileName, fileName)
            localDirectory.resolve(fileName)
        } catch (e: Exception) {
            null
        }
    }

    override fun removeAudioFile(fileName: String): Boolean {
        return try {
            channel.rm(fileName)
            true
        } catch (e: Exception) {
            false
        }
    }
}
