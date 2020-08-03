package ajdepaul.taggedmusic

import kotlin.collections.Collection

/* -------------------------------- Functions ------------------------------- */

internal object Util {

    /** Finds elements that are in [after], but not in [before] */
    fun <E> findAdded(before: Set<E>, after: Set<E>): Collection<E> {
        return after.filter { !before.contains(it) }
    }

    /** Finds elements that are in [before], but not in [after] */
    fun <E> findRemoved(before: Set<E>, after: Set<E>): Collection<E> {
        return before.filter { !after.contains(it) }
    }

    /** Finds entries with keys that are in [after], but not in [before] */
    fun <K,V> findAdded(before: Map<K,V>, after: Map<K,V>): Map<K,V> {
        return after.filter { !before.contains(it.key) }
    }

    /** Finds entries with keys that are in [before], but not in [after] */
    fun <K,V> findRemoved(before: Map<K,V>, after: Map<K,V>): Map<K,V> {
        return before.filter { !after.contains(it.key) }
    }
}

/* --------------------------------- Classes -------------------------------- */

internal open class Subject<D> {

    private var observers = listOf<(dat: D) -> Unit >()

    fun addObserver(observer: (dat: D) -> Unit ) { observers += observer }
    fun removeObserver(observer: (dat: D) -> Unit) { observers -= observer }
    fun notifySubjects(dat: D) { observers.forEach { o -> o(dat) } }
}
