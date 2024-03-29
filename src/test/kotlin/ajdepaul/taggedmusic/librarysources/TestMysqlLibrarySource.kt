/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.TagType
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import org.junit.After
import org.junit.Test
import java.nio.file.Paths
import java.util.*

class TestMysqlLibrarySource {

    private val testServerProperties = this.javaClass.getResource(
        Paths.get("TestMysqlLibrarySource", "server.properties").toString()
    )

    /**
     * Get the test server information from the server.properties resource.
     * @return null if the test should be skipped
     */
    private fun loadProperties(): PropertiesData? {
        val prop = Properties()
        prop.load(testServerProperties!!.openStream())

        if (prop.getProperty("test") != "true") {
            return null
        }

        val dataSource = MysqlDataSource()
        dataSource.serverName = prop.getProperty("host")
        dataSource.port = prop.getProperty("port")?.toInt() ?: 3306
        dataSource.user = prop.getProperty("username")
        dataSource.setPassword(prop.getProperty("password"))
        dataSource.databaseName = prop.getProperty("databaseName") ?: "tagged_music"

        val suppFracSec = prop.getProperty("supportsFractionalSeconds") == "true"

        val defaultTagType = TagType(prop.getProperty("defaultTagTypeColor")?.toInt() ?: 0)

        return PropertiesData(dataSource, suppFracSec, defaultTagType)
    }

    /** Removes all rows from the in the MySQL server database and resets the default tag type. */
    @After
    fun cleanServer() {
        val (dataSource, _, defaultTagType) = loadProperties() ?: return

        val prop = Properties()
        prop.load(testServerProperties!!.openStream())

        with(dataSource.connection) {
            with(this.createStatement()) {
                this.addBatch("DELETE FROM Songs;")
                this.addBatch("DELETE FROM Tags;")
                this.addBatch("DELETE FROM TagTypes;")
                this.addBatch("DELETE FROM SongHasTag;")
                this.addBatch("DELETE FROM Data;")
                this.addBatch(
                    """
                    INSERT INTO TagTypes(name, color)
                    VALUES ('', ${defaultTagType.color})
                    ON DUPLICATE KEY UPDATE color = ${defaultTagType.color};
                """.trimIndent()
                )
                this.executeBatch()
            }
        }
    }

    /** Tests the [MysqlDataSource] constructors. */
    @Test
    fun testConstructor() {
        val propertiesData = loadProperties()

        if (propertiesData == null) {
            println(
                "[WARNING] MySQL library source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        val (dataSource, suppFracSec, defaultTagType) = propertiesData

        with(MysqlLibrarySource(dataSource, suppFracSec)) {
            TestLibrarySourceUtil.assertDefaults(this, defaultTagType)
        }
    }

    /** Tests [MysqlLibrarySource.updater]. */
    @Test
    fun testUpdater() {
        val propertiesData = loadProperties()

        if (propertiesData == null) {
            println(
                "[WARNING] MySQL library source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        val (dataSource, suppFracSec, _) = propertiesData

        // test making changes
        val songLibraryData = with(MysqlLibrarySource(dataSource, suppFracSec)) {
            TestLibrarySourceUtil.assertUpdates(this)
        }

        // test changes were saved
        with(MysqlLibrarySource(dataSource, suppFracSec)) {
            TestLibrarySourceUtil.assertUpdated(this, songLibraryData)
        }
    }

    /** Tests [SftpLibrarySource.getSongsByTags]. */
    @Test
    fun testGetSongsByTags() {
        val propertiesData = loadProperties()

        if (propertiesData == null) {
            println(
                "[WARNING] MySQL library source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        val (dataSource, suppFracSec, _) = propertiesData

        // use util class for tests
        with(MysqlLibrarySource(dataSource, suppFracSec)) {
            TestLibrarySourceUtil.testGetSongsByTags(this)
        }
    }

    /** Contains data loaded from the server.properties resource file. */
    data class PropertiesData(
        val dataSource: MysqlDataSource,
        val suppFracSec: Boolean,
        val defaultTagType: TagType
    )
}
