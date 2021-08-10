/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
package ajdepaul.taggedmusic.librarysources

import ajdepaul.taggedmusic.TagType
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Paths
import java.util.*

class TestMysqlLibrarySource {

    @Rule
    @JvmField
    val tempDir = TemporaryFolder()

    private val testServerProperties = this.javaClass.getResource(
        Paths.get("TestMysqlLibrarySource", "server.properties").toString()
    )

    /** Name the tests should use for the temporary testing database. */
    private val tempDatabase = "test_tagged_music"

    /**
     * Get the test server information from the server.properties resource.
     * @return null if the test should be skipped
     */
    private fun createServerSession(): MysqlDataSource? {
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
        dataSource.databaseName = prop.getProperty("database_name") ?: "tagged_music"

        return dataSource
    }

    /** Drops the [tempDatabase] database from the MySQL server. */
    private fun cleanServer() {
        val dataSource = createServerSession() ?: return

        with (dataSource.connection) {
            with(this.createStatement()) {
                this.execute("DROP DATABASE $tempDatabase;")
            }
        }
    }

    /** Tests the [MysqlDataSource] constructors. */
    @Test
    fun testConstructor() {
        if (createServerSession() == null) {
            println(
                "[WARNING] MySQL library source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        try {
            // initialize the server with default values
            MysqlLibrarySource(createServerSession() ?: return, TagType(0)).close()

            // test new library source using that server
            with (MysqlLibrarySource(createServerSession() ?: return)) {
                TestLibrarySourceUtil.assertDefaults(this)
            }

        } finally {
            cleanServer()
        }
    }

    /** Tests [MysqlDataSource.updater]. */
    @Test
    fun testUpdater() {
        if (createServerSession() == null) {
            println(
                "[WARNING] MySQL library source test skipped. To run this test see " +
                        "`${testServerProperties}`."
            )
            return
        }

        try {
            // test making changes
            val songLibraryData = with (MysqlLibrarySource(createServerSession() ?: return)) {
                TestLibrarySourceUtil.assertUpdates(this)
            }

            // test changes were saved
            with (MysqlLibrarySource(createServerSession() ?: return)) {
                TestLibrarySourceUtil.assertUpdated(this, songLibraryData)
            }

        } finally {
            cleanServer()
        }
    }
}
