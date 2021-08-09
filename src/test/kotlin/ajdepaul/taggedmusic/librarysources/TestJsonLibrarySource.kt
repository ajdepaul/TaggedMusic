/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.TagType
import org.apache.commons.io.FileUtils
import org.junit.Test
import java.nio.file.Paths

class TestJsonLibrarySource {

    /** Directory for temporary files for this set of tests. */
    private val sharedTestDir = Paths.get("test", "librarysources", "TestJsonLibrarySource")

    /** Tests the [JsonLibrarySource] constructors. */
    @Test
    fun testConstructor() {
        val testDir = sharedTestDir.resolve("testConstructor")
        FileUtils.forceMkdir(testDir.toFile())
        val jsonFilePath = testDir.resolve("library.json")

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
        val testDir = sharedTestDir.resolve("testUpdater")
        FileUtils.forceMkdir(testDir.toFile())
        val jsonFilePath = testDir.resolve("library.json")

        // test making changes
        val songLibraryData =
            TestLibrarySourceUtil.assertUpdates(JsonLibrarySource(jsonFilePath, TagType(0)))

        // test changes were saved
        TestLibrarySourceUtil.assertUpdated(JsonLibrarySource(jsonFilePath), songLibraryData)
    }
}
