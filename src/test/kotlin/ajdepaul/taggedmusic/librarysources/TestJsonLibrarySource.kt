/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.TagType
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TestJsonLibrarySource {

    @Rule
    @JvmField
    val tempDir = TemporaryFolder()

    /** Tests the [JsonLibrarySource] constructors. */
    @Test
    fun testConstructor() {
        val jsonFilePath = tempDir.newFile().toPath()

        // initial text to test the text is overwritten
        jsonFilePath.toFile().writeText("bad text")

        // create the json file with default values
        JsonLibrarySource(jsonFilePath, TagType(0))

        // test new library source using that file
        TestLibrarySourceUtil.assertDefaults(JsonLibrarySource(jsonFilePath))
    }

    /** Tests [JsonLibrarySource.updater]. */
    @Test
    fun testUpdater() {
        val jsonFilePath = tempDir.newFile().toPath()

        // test making changes
        val songLibraryData =
            TestLibrarySourceUtil.assertUpdates(JsonLibrarySource(jsonFilePath, TagType(0)))

        // test changes were saved
        TestLibrarySourceUtil.assertUpdated(JsonLibrarySource(jsonFilePath), songLibraryData)
    }
}
