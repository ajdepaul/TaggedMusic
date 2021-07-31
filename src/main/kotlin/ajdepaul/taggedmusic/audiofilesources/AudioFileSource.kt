/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

/** [AudioFileSource]s are used to retrieve audio files of songs by file name. */
interface AudioFileSource {

    /** Checks if the [AudioFileSource] has the song available to pull. */
    fun hasAudioFile(fileName: String): Boolean

    /**
     * Transfers an audio file from a local path onto the [AudioFileSource].
     * @param audioPath the local path to the audio file
     * @param fileName the file name the [AudioFileSource] will use to fetch this audio file
     * @return true if success, false if failed
     */
    fun pushAudioFile(audioPath: String, fileName: String): Boolean

    /**
     * Fetches an audio file from the [AudioFileSource].
     * @return the local path to the song or null if failed
     */
    fun pullAudioFile(fileName: String): String?

    /** Deletes an audio file from the [AudioFileSource]. */
    fun removeAudioFile(fileName: String): Boolean
}
