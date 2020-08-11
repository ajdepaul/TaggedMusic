
package ajdepaul.taggedmusiccli

import ajdepaul.taggedmusic.*
import com.google.gson.Gson
import kotlinx.collections.immutable.*

fun main(args: Array<String>) {
    CLI().run()
}

private class CLI {

    val initialMessage = """
        --- Tagged Music CLI ---
        Use "list" to list all commands.
    """.trimIndent()

    val prompt = "> "

    val descriptions: DescriptionsData =
            Gson().fromJson(CLI::class.java.getResource("/descriptions.json").readText(), DescriptionsData::class.java)
    // used for list command
    val shortDescriptions = descriptions.shortDescriptions
    // used for help command
    val fullDescriptions = descriptions.fullDescriptions

    var libraryProvider: LibraryProvider? = null
    var songProvider: SongProvider? = null
    var songLibrary: SongLibrary? = null
    var songQueue: ImmutableList<Pair<String, Song>>? = null

    fun run() {

        println(initialMessage)

        var running = true
        while (running) {
            print(prompt)
            val input = readLine()?.toLowerCase()?.split(" ") ?: break

            when (input[0]) {
                "list", "l" -> list(input)
                "help", "h" -> help(input)
                "quit", "x" -> running = false
                "now-playing", "p" -> nowPlaying(input)
                "queue", "q" -> queue(input)
                "volume", "v" -> volume(input)
                "library-provider", "lp" -> libraryProviderCommand(input)
                "song-provider", "sp" -> songProviderCommand(input)
                "songs", "s" -> songs(input)
                "tags", "t" -> tags(input)
                "tag-types", "tt" -> tagTypes(input)
                "" -> {}
                else -> println("Not a command. Use \"list\" to see commands.")
            }
        }
    }



/* ---------------------------------- List ---------------------------------- */

    fun list(input: List<String>) {
        if (input.size != 1) {
            println("Usage: list"); return
        }
        for (line in shortDescriptions) println(line)
    }

// /* ----------------------------------- New ---------------------------------- */

//     fun new(input: List<String>) {
//         if (input.size == 1 || input.size > 2) {
//             println("Usage: new [type]"); return
//         }

//         when (input[1]) {
//             "library-provider", "lp" -> newLibraryProvider()
//             "song-provider", "sp" -> newSongProvider()
//             "library", "l" -> newLibrary()
//             "song", "s" -> newSong()
//             "tag", "t" -> newTag()
//             "tag-type", "tt" -> newTagType()
//             else -> println("Not a type. Use \"help new\" to list types.")
//         }
//     }

//     fun newLibraryProvider() {

//     }

//     fun newSongProvider() {

//     }

//     fun newLibrary() {
//         if (songLibrary != null) {
//             print("There is already a loaded library. Creating a new library will overwrite it. ")
//             if(yesNo("Are you sure? ") != true) return
//         }
//         print("Choose a default tag type color (#XXXXXX): ")

//         fun askColor(): Int? {
//             val input = readLine() ?: return null
//             if (input.length == 7 && input[0] == '#') {
//                 val result = try {
//                     java.lang.Long.parseLong(input.substring(1), 16).toInt()
//                 }
//                 catch (_: NumberFormatException) { -1 }

//                 if (result >= 0) return result
//             }
//             print("Please use the format #XXXXXX: ")
//             return askColor()
//         }

//         val color = askColor() ?: return
//         songLibrary = SongLibrary(TagType(color))
//         println("Loaded new library.")
//     }

//     fun newSong() {

//     }

//     fun newTag() {

//     }

//     fun newTagType() {

//     }

/* ---------------------------------- Help ---------------------------------- */

    fun help(input: List<String>) {
        if (input.size == 1 || input.size > 2) {
            println("Usage: help [command]"); return
        }
        fullDescriptions[input[1]]?.forEach { println(it) }
                ?: println("Use the full name of the command. Use \"list\" to see available commands.")
    }

    data class DescriptionsData(
            val shortDescriptions: List<String>,
            val fullDescriptions: Map<String, List<String>>
    )

/* ------------------------------- Now Playing ------------------------------ */

    fun nowPlaying(input: List<String>) {

    }

/* ---------------------------------- Queue --------------------------------- */

    fun queue(input: List<String>) {

    }

/* --------------------------------- Volume --------------------------------- */

    fun volume(input: List<String>) {

    }

/* ---------------------------- Library Provider ---------------------------- */

    fun libraryProviderCommand(input: List<String>) {

        // print summary
        if (input.size == 1) {
            if (libraryProvider == null) {
                println("There is no loaded library provider.")

            } else if (libraryProvider is LocalLibraryProvider) {
                val localProvider = libraryProvider as LocalLibraryProvider
                println("Type: local library provider")
                println("Library path: " + localProvider.libraryPath)

            } else {
                println("Unknown library provider type.")
                println("Library provider: $libraryProvider")
            }
        }

        // other commands
        else {
            when (input[1]) {

                "new", "n" -> {

                    if (libraryProvider != null) {
                        print("There is already a loaded library provider. Creating a new one will overwrite it. ")
                        if(binaryQuestion("Are you sure?: ", listOf("yes", "y"), listOf("no", "n")) != true) return
                    }

                    when (askUntilCorrectOrEmpty("What type of library provider would you like to create? (local): ", "local")) {
                        "local" -> {
                            print("What is the path to the library file?: ")
                            val path = readLine()?.toLowerCase()?.trim()
                            if (path == "" || path == null) return
                            libraryProvider = LocalLibraryProvider(path)
                            println("Loaded new library provider.")
                        }
                        null -> return
                    }
                }

                "push", "s" -> {

                }

                "pull", "l" -> {

                }

                "modify", "m" -> {

                }
            }
        }

    }

/* ------------------------------ Song Provider ----------------------------- */

    fun songProviderCommand(input: List<String>) {

    }

/* ---------------------------------- Songs --------------------------------- */

    fun songs(input: List<String>) {

    }

/* ---------------------------------- Tags ---------------------------------- */

    fun tags(input: List<String>) {

    }

/* -------------------------------- Tag Types ------------------------------- */

    fun tagTypes(input: List<String>) {

    }

/* ---------------------------------- Util ---------------------------------- */

    fun binaryQuestion(question: String, trueResponses: List<String>, falseResponses: List<String>): Boolean? {
        print(question)
        val input = readLine()?.toLowerCase()?.trim()
        return when {
            trueResponses.contains(input) -> true
            falseResponses.contains(input) -> false
            input == null -> null
            else -> binaryQuestion(question, trueResponses, falseResponses)
        }
    }

    fun askUntilCorrectOrEmpty(question: String, vararg responses: String): String? {
        print(question)
        val input = readLine()?.toLowerCase()?.trim()
        return when {
            responses.contains(input) -> input
            input == "" || input == null -> null
            else -> askUntilCorrectOrEmpty(question, *responses)
        }
    }
}
