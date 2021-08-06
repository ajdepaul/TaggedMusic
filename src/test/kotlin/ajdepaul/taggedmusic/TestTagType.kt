/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic

import kotlin.test.Test
import kotlin.test.assertEquals

class TestTagType {

    /** Tests [TagType.mutate]. */
    @Test
    fun testMutate() {
        var tagType = TagType(0)
        assertEquals(0, tagType.color)

        tagType = tagType.mutate { color = 1 }
        assertEquals(1, tagType.color)
    }
}
