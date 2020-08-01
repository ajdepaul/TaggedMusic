package ajdepaul.taggedmusic

import kotlin.collections.Collection

/* -------------------------------- Functions ------------------------------- */

internal object Util {

    fun <E> findAdded(before: Collection<E>, after: Collection<E>): Collection<E> {
        return after.filter { e -> !before.contains(e) }
    }

    fun <E> findRemoved(before: Collection<E>, after: Collection<E>): Collection<E> {
        return before.filter { e -> !after.contains(e) }
    }
}

/* --------------------------------- Classes -------------------------------- */

internal open class Subject<D> {

    private var observers = listOf<(dat: D) -> Unit >()

    fun addObserver(observer: (dat: D) -> Unit ) { observers += observer }
    fun removeObserver(observer: (dat: D) -> Unit) { observers -= observer }
    fun notifySubjects(dat: D) { observers.forEach { o -> o(dat) } }
}
