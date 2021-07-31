/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.extensions

import ajdepaul.taggedmusic.Song
import kotlinx.collections.immutable.*

/*
 * To use these extensions, be sure to import `ajdepaul.taggedmusic.extensions.*`.
 */

/**
 * Returns the result of removing all the entries in this map that match the given [predicate].
 * @return a new [PersistentMap] containing the entries from [this] with the entries that match the
 * given [predicate] removed
 */
fun <K, V> PersistentMap<K, V>.removeAll(predicate: (Map.Entry<K, V>) -> Boolean):
        PersistentMap<K, V> {
    return this.mutate { mutableMap ->
        mutableMap.entries.forEach { entry ->
            if (predicate(entry)) mutableMap.remove(entry.key)
        }
    }
}

/**
 * Returns the result of removing all the entries in this map that do not match the given
 * [predicate].
 * @return a new [PersistentMap] containing only the entries from [this] that match the given
 * [predicate]
 */
fun <K, V> PersistentMap<K, V>.keepOnly(predicate: (Map.Entry<K, V>) -> Boolean):
        PersistentMap<K, V> {
    return this.mutate { mutableMap ->
        mutableMap.entries.forEach { entry ->
            if (!predicate(entry)) mutableMap.remove(entry.key)
        }
    }
}

/**
 * Returns the result of removing all the entries in this map that do not match the given filters.
 * @param includeTags songs must have all of these tags (if empty, includes all tags)
 * @param excludeTags songs cannot have any of these tags (if empty, excludes no tags)
 * @return a new [PersistentMap] containing only the entries from [this] that match the given
 * filters.
 */
fun PersistentMap<String, Song>.filterByTags(
    includeTags: PersistentSet<String> = persistentHashSetOf(),
    excludeTags: PersistentSet<String> = persistentHashSetOf()
): PersistentMap<String, Song> {
    return this.keepOnly { it.value.tags.containsAll(includeTags) }
        .removeAll { it.value.tags.any { tag -> excludeTags.contains(tag) } }
}
