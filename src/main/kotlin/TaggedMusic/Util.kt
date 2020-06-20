package TaggedMusic

import TaggedMusic.Song

object SftpUtil {

    fun getSongMetaData(file: String): SongMetaData {
        // TODO implement
        // TODO if title is null, update it with the file name
        return SongMetaData(file, duration=1000)
    }

    fun setSongMetaData(file: String, metaData: SongMetaData) {
        // TODO implement
    }
}

data class SongMetaData(val title:       String? = null,
                        val artist:      String? = null,
                        val album:       String? = null,
                        val trackNum:    Int?    = null,
                        val year:        Int?    = null,
                        val duration:      Long)

interface Observer<D> { fun update(data: D) }

interface Subject<D> {
    fun addObserver(observer: Observer<D>)
    fun removeObserver(observer: Observer<D>)
    fun notifySubjects()
}