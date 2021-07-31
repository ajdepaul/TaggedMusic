/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic

/** All the data stored about a single [Tag]. */
data class Tag(val type: String? = null, val description: String? = null) {

    /** Returns a new [Tag] with the changes from [mutator] applied. */
    fun mutate(mutator: TagBuilder.() -> Unit): Tag {
        return TagBuilder(this).apply(mutator).build()
    }

    data class TagBuilder internal constructor(private val tag: Tag) {
        var type: String? = tag.type
        var description: String? = tag.description

        internal fun build(): Tag {
            return Tag(type, description)
        }
    }
}
