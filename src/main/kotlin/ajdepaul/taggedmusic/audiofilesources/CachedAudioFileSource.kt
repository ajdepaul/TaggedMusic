/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

class CachedAudioFileSource : AudioFileSource {
    override fun hasAudioFile(fileName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun pushAudioFile(songPath: String, fileName: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun pullAudioFile(fileName: String): String? {
        TODO("Not yet implemented")
    }

    override fun removeAudioFile(fileName: String): Boolean {
        TODO("Not yet implemented")
    }
}