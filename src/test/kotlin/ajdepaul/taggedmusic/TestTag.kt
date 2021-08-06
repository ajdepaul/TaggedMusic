/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic

import kotlin.test.Test
import kotlin.test.assertEquals

class TestTag {

    /** Tests the default [Tag] values. */
    @Test
    fun testDefaults() {
        var tag = Tag()
        assertEquals(null, tag.type)
        assertEquals(null, tag.description)
    }

    /** Tests [Tag.mutate]. */
    @Test
    fun testMutate() {
        var tag = Tag()

        tag = tag.mutate { type = "type" }
        assertEquals("type", tag.type)

        tag = tag.mutate { description = "description" }
        assertEquals("description", tag.description)

        tag = tag.mutate { type = "type2"; description = "description2" }
        assertEquals("type2", tag.type)
        assertEquals("description2", tag.description)
    }
}