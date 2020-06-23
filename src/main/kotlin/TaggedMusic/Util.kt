package TaggedMusic

import kotlin.collections.Collection

internal object Util {

    fun <E> findAdded(before: Collection<E>, after: Collection<E>): Collection<E> {
        return after.filter { e -> !before.contains(e) }
    }

    fun <E> findRemoved(before: Collection<E>, after: Collection<E>): Collection<E> {
        return before.filter { e -> !after.contains(e) }
    }
}

internal object SftpUtil {

    fun getSongMetaData(file: String): SongMetaData {
        // TODO implement
        // TODO if title is null, update it with the file name
        return SongMetaData(file, duration=1000)
    }

    fun setSongMetaData(file: String, metaData: SongMetaData) {
        // TODO implement
    }
}

internal interface Observer<D> { fun update(dat: D) }

internal open class Subject<D> {

    private var observers = listOf<Observer<D>>()

    fun addObserver(observer: Observer<D>) { observers += observer }
    fun removeObserver(observer: Observer<D>) { observers -= observer }
    fun notifySubjects(dat: D) { observers.forEach { o -> o.update(dat) } }
}