/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.audiofilesources

import java.nio.file.Path

/**
 * [AudioFileSource] that retrieves audio files from [remoteAudioFileSource] and then deletes them
 * once the files stored takes up more than [maxSpace] bytes.
 *
 * The cached audio files are stored where [remoteAudioFileSource] presents them. The files are not
 * copied to a temporary cache directory. Because of this, **do not use this with a
 * [LocalAudioFileSource]**. If the maximum cache size is reached, the audio files stored in the
 * [LocalAudioFileSource] directory will start getting deleted.
 */
class CachedAudioFileSource(
    private val remoteAudioFileSource: LocalAudioFileSource,
    /** The maximum amount of space available for storing audio files in bytes. */
    private val maxSpace: Int
) : AudioFileSource {

    /** The current amount of space used by the cache in bytes. */
    private var spaceUsed = 0L

    /** key: song file name; value: audio file path */
    private val cachedSongs = object : LinkedHashMap<String, Path>() {

        /**
         * Deletes the eldest cached file if [spaceUsed] is greater than [maxSpace] and updates
         * [spaceUsed].
         */
        override fun removeEldestEntry(
            eldestEntry: MutableMap.MutableEntry<String, Path>
        ): Boolean {
            val eldestCachedSong = eldestEntry.value.toFile()

            return if (spaceUsed > maxSpace) {
                spaceUsed -= eldestCachedSong.length()
                eldestCachedSong.delete()
                true
            } else {
                false
            }
        }

        /** Updates [spaceUsed] if the [key] is new. */
        override fun put(key: String, value: Path): Path? {
            if (!keys.contains(key)) spaceUsed += value.toFile().length()
            return super.put(key, value)
        }

        /**
         * Subtracts the amount of spaced used by the file associated with [key] from [spaceUsed].
         * This does not delete the audio file.
         */
        override fun remove(key: String): Path? {
            spaceUsed -= get(key)?.toFile()?.length() ?: 0
            return super.remove(key)
        }
    }

    override fun hasAudioFile(fileName: String): Boolean {
        return if (cachedSongs.containsKey(fileName)) true
        else remoteAudioFileSource.hasAudioFile(fileName)
    }

    override fun pushAudioFile(audioPath: Path, fileName: String): Boolean {
        return remoteAudioFileSource.pushAudioFile(audioPath, fileName)
    }

    override fun pullAudioFile(fileName: String): Path? {
        val cachedAudioFile = cachedSongs[fileName]

        // song is cached
        return if (cachedAudioFile != null) {
            // make this the newest entry in the map
            cachedSongs.remove(fileName)
            cachedSongs[fileName] = cachedAudioFile

            cachedAudioFile
        }
        // song is not cached
        else {
            val pulledAudioFile = remoteAudioFileSource.pullAudioFile(fileName)
            if (pulledAudioFile != null) cachedSongs[fileName] = pulledAudioFile
            pulledAudioFile
        }
    }

    override fun removeAudioFile(fileName: String): Boolean {
        cachedSongs.remove(fileName)?.toFile()?.delete()
        return remoteAudioFileSource.removeAudioFile(fileName)
    }
}
