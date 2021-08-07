/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.TagType
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.junit.Test
import java.nio.file.Paths
import java.util.*

class TestSftpLibrarySource {

    /** Directory for temporary files for this set of tests. */
    private val sharedTestDir = Paths.get("test", "librarysources", "TestJsonLibrarySource")

    private val testServerProperties = this.javaClass.getResource(
        Paths.get("TestSftpLibrarySource", "server.properties").toString()
    )

    /**
     * Get the test server information from the server.properties resource.
     * @return null if the test should be skipped
     */
    private fun createServerSession(): Session? {
        val prop = Properties()
        prop.load(testServerProperties!!.openStream())

        if (prop.getProperty("test") != "true") {
            println(
                "[WARNING] SFTP library source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return null
        }

        val jsch = JSch()
        if (prop.getProperty("host") != null) jsch.setKnownHosts(prop.getProperty("host"))

        val session = jsch.getSession(
            prop.getProperty("username"),
            prop.getProperty("host"),
            prop.getProperty("port")?.toInt() ?: 22
        )

        if (prop.getProperty("password") != null) session.setPassword(prop.getProperty("password"))

        return session
    }

    /** Tests the [SftpLibrarySource] constructors. */
    @Test
    fun testConstructor() {
        val session = createServerSession() ?: return

        // TODO

        // initialize the server with default values
        SftpLibrarySource(
            session,
            listOf(Paths.get("")),
            Paths.get(""),
            Paths.get(""),
            JsonLibrarySource(Paths.get(""), TagType(0)),
            true
        ).close()

        // test new library source using that server
        SftpLibrarySource(
            session,
            listOf(Paths.get("")),
            Paths.get(""),
            Paths.get(""),
            JsonLibrarySource(Paths.get(""))
        ).also { TestLibrarySourceUtil.assertDefaults(it) }.close()


//        val jsonLibrarySource = JsonLibrarySource("testLibrary.json", TagType(0))
//        SftpLibrarySource(session, listOf("testLibrary.json"), ".taggedmusic/banana/apple", ".taggedmusic/banana/apple/", jsonLibrarySource, true)

//        val testDir = "$sharedTestDir/testCreateJsonFileConstructor"
//        File(testDir).mkdirs()
//        val jsonFilePath = "$testDir/library.json"
//
//        // initial text to test the text is overwritten
//        File(jsonFilePath).writeText("bad text")
//
//        // create the json file with default values
//        JsonLibrarySource(jsonFilePath, TagType(0))
//
//        // create new library source using that file
//        TestLibrarySourceUtil.assertDefaults(JsonLibrarySource(jsonFilePath))
    }

    /** Tests [JsonLibrarySource.updater]. */
    @Test
    fun testUpdater() {
//        val testDir = "$sharedTestDir/testUpdater"
//        File(testDir).mkdirs()
//        val jsonFilePath = "$testDir/library.json"
//
//        TestLibrarySourceUtil.assertUpdates(JsonLibrarySource(jsonFilePath, TagType(0)))
    }
}
