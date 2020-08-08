
package ajdepaul.taggedmusiccli

import ajdepaul.taggedmusic.LibraryProvider
import ajdepaul.taggedmusic.SongLibrary
import ajdepaul.taggedmusic.SongProvider
import ajdepaul.taggedmusic.TagType
import com.google.gson.Gson
import kotlinx.collections.immutable.*
import java.io.File

fun main(args: Array<String>) {
    CLI().run()
}

class CLI {

    val initialMessage = """
        --- Tagged Music CLI ---
        Use "list" to list all commands.
    """.trimIndent()

    val prompt = " > "

    val descriptions: DescriptionsData =
            Gson().fromJson(CLI::class.java.getResource("/descriptions.json").readText(), DescriptionsData::class.java)
    // used for list command
    val shortDescriptions = descriptions.shortDescriptions
    // used for help command
    val fullDescriptions = descriptions.fullDescriptions

    var songLibrary: SongLibrary? = null
    var libraryProvider: LibraryProvider? = null
    var songProvider: SongProvider? = null

    fun run() {

        println("""
            --- Tagged Music CLI ---
            Use "list" to see available commands.
        """.trimIndent())

        var running = true
        while (running) {
            print(prompt)
            val input = readLine()?.toLowerCase()?.split(" ") ?: break

            when (input[0]) {
                "list", "l" -> list(input)
                "help", "h" -> help(input)
                "new", "n" -> new(input)
                "quit", "q" -> running = false
                "" -> {}
                else -> println("Not a command. Use \"list\" to see available commands.")
            }
        }
    }

/* ---------------------------------- List ---------------------------------- */

    fun list(input: List<String>) {
        if (input.size != 1) {
            println("Usage: list"); return
        }
        for (line in shortDescriptions)println(line)
    }

/* ----------------------------------- New ---------------------------------- */

    fun new(input: List<String>) {
        if (input.size == 1 || input.size > 2) {
            println("Usage: new [type]"); return
        }

        when (input[1]) {
            "library-provider" -> newLibraryProvider()
            "song-provider" -> newSongProvider()
            "library" -> newLibrary()
            "song" -> newSong()
            "tag" -> newTag()
            "tag-type" -> newTagType()
            else -> println("Not a type. Use \"help new\" to list types.")
        }
    }

    fun newLibraryProvider() {

    }

    fun newSongProvider() {

    }

    fun newLibrary() {
        if (songLibrary != null) {
            print("There is already a loaded library. Creating a new library will overwrite it. ")
            if(yesNo("Are you sure? ") != true) return
        }
        print("Choose a default tag type color (#XXXXXX): ")

        fun askColor(): Int? {
            val input = readLine() ?: return null
            if (input.length == 7 && input[0] == '#') {
                val result = try {
                    java.lang.Long.parseLong(input.substring(1), 16).toInt()
                }
                catch (_: NumberFormatException) { -1 }

                if (result >= 0) return result
            }
            print("Please use the format #XXXXXX: ")
            return askColor()
        }

        val color = askColor() ?: return
        songLibrary = SongLibrary(TagType(color))
        println("Loaded new library.")
    }

    fun newSong() {

    }

    fun newTag() {

    }

    fun newTagType() {

    }

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

/* ---------------------------------- Other --------------------------------- */

    fun yesNo(question: String): Boolean? {
        print(question)
        return when (readLine()?.toLowerCase()?.trim()) {
            "yes", "y" -> true
            "no", "n" -> false
            null -> null
            else -> yesNo(question)
        }
    }
}
