
package ajdepaul.taggedmusiccli

import ajdepaul.taggedmusic.*
import ajdepaul.taggedmusiccli.util.toArgs
import com.google.gson.Gson
import kotlinx.collections.immutable.*

fun main() { CLI().run() }

internal class CLI {

    private val initialMessage = """
        --- Tagged Music CLI ---
        Use "list" to list all commands.
    """.trimIndent()

    private val prompt = "> "
    internal val posResponses = listOf("yes", "y")
    internal val negResponses = listOf("no", "n")

    private data class DescriptionsData(val shortDescriptions: List<String>, val fullDescriptions: Map<String, List<String>>)
    private val descriptions: DescriptionsData =
            Gson().fromJson(CLI::class.java.getResource("/descriptions.json").readText(), DescriptionsData::class.java)
    // used for list command
    private val shortDescriptions = descriptions.shortDescriptions
    // used for help command
    private val fullDescriptions = descriptions.fullDescriptions

    internal var libraryProvider: LibraryProvider? = null
    internal var songProvider: SongProvider? = null
    internal var songLibrary = SongLibrary(TagType(0))
    private var songQueue: ImmutableList<Pair<String, Song>>? = null

    fun run() {
        println(initialMessage)

        var running = true
        while (running) {

            print(prompt)
            val args = readLine()?.toArgs()?: break
            when (args[0].toLowerCase()) {
                "list", "l" -> list(args)
                "help", "h" -> help(args)
                "quit", "x" -> running = false
                "now-playing", "p" -> nowPlaying(args)
                "queue", "q" -> queue(args)
                "volume", "v" -> volume(args)
                "library-provider", "lp" -> libraryProviderCommand(this, args)
                "song-provider", "sp" -> songProviderCommand(this, args)
                "songs", "s" -> songs(args)
                "tags", "t" -> tags(args)
                "tag-types", "tt" -> tagTypes(args)
                "" -> {}
                else -> println("Not a command. Use \"list\" to see commands.")
            }
        }
    }

    private fun list(input: List<String>) {
        if (input.size != 1) println("Usage: list")
            else for (line in shortDescriptions) println(line)
    }

    private fun help(input: List<String>) {
        if (input.size != 2) println("Usage: help [command]")
        else fullDescriptions[input[1]]?.forEach { println(it) }
                ?: println("Not a command. Use \"list\" for available commands.")
    }

    // TODO
    private fun nowPlaying(input: List<String>) { }
    private fun queue(input: List<String>) { }
    private fun volume(input: List<String>) { }
    private fun songs(input: List<String>) { }
    private fun tags(input: List<String>) { }
    private fun tagTypes(input: List<String>) { }
}
