/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic

/** All the data stored about a single [TagType]. */
data class TagType(val color: Int) {

    /** Returns a new [TagType] with the changes from [mutator] applied. */
    fun mutate(mutator: TagTypeBuilder.() -> Unit): TagType {
        return TagTypeBuilder(this).apply(mutator).build()
    }

    data class TagTypeBuilder internal constructor(private val tagType: TagType) {
        var color: Int = tagType.color

        internal fun build(): TagType {
            return TagType(color)
        }
    }
}
