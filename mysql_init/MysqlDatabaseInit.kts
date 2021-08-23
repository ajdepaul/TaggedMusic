/*
 * Copyright © 2021 Anthony DePaul
 * Licensed under the MIT License https://ajdepaul.mit-license.org/
 */
import java.io.File
import kotlin.system.exitProcess

fun main() {
    // options
    var outputFile = "library_init.sql"
    var databaseName = "tagged_music"
    var suppFracSec = true
    var defaultTagTypeColor = 0

    for (arg in args) {
        val split = arg.split("=")
        if (split.size != 2) printUsage()
        if (split[1].isBlank()) printUsage()

        when (split[0]) {
            "output-file" -> outputFile = split[1]
            "database-name" -> databaseName = split[1]
            "supports-fractional-seconds" -> {
                if (split[1] == "true") suppFracSec = true
                else if (split[1] == "false") suppFracSec = false
                else printUsage()
            }
            "default-tag-type-color" -> {
                val color = split[1].toIntOrNull()
                if (color == null) printUsage()
                defaultTagTypeColor = color!!
            }
            else -> printUsage()
        }
    }

    // modify script template
    val resultScript = File(".library_init.sql").readText()
        .replace("{database_name}", databaseName)
        .replace("{supports-fractional-seconds}", if (suppFracSec) "DATETIME(3)" else "VARCHAR(23)")
        .replace("{default-tag-type-color}", defaultTagTypeColor.toString())

    // write to file
    File(outputFile).writeText(resultScript)

    println("""
        Generated SQL script `$outputFile` with the options:
            database-name=$databaseName
            supports-fractional-seconds=$suppFracSec
            default-tag-type-color=$defaultTagTypeColor
    """.trimIndent())
}

fun printUsage() {
    println(
        """
        Usage: kotlinc -script MysqlDatabaseInit.kts [OPTIONS]
        
        Generates a SQL script to initialize a MySQL server to be used as a library source for Tagged Music.
        
        Options:
            output-file=string                          Set the name of the resulting SQL script file (default "library_init.sql")
            database-name=string                        Set the name to use for the database (default "tagged_music")
            supports-fractional-seconds=(true|false)    Whether the MySQL server supports fractional seconds (default "true")
            default-tag-type-color=integer              Set the default tag type color (default "0")
    """.trimIndent()
    )
    exitProcess(0)
}

main()
