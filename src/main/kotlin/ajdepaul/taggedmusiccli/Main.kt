
package ajdepaul.taggedmusiccli

import ajdepaul.taggedmusic.*
import com.google.gson.Gson
import com.mpatric.mp3agic.Mp3File
import kotlinx.collections.immutable.*
import java.io.File

fun main(args: Array<String>) {
    CLI().run()
}

private class CLI {

    val initialMessage = """
        --- Tagged Music CLI ---
        Use "list" to list all commands.
    """.trimIndent()

    val prompt = "> "
    val posResponses = listOf("yes", "y")
    val negResponses = listOf("no", "n")

    data class DescriptionsData(val shortDescriptions: List<String>, val fullDescriptions: Map<String, List<String>>)
    val descriptions: DescriptionsData =
            Gson().fromJson(CLI::class.java.getResource("/descriptions.json").readText(), DescriptionsData::class.java)
    // used for list command
    val shortDescriptions = descriptions.shortDescriptions
    // used for help command
    val fullDescriptions = descriptions.fullDescriptions

    var libraryProvider: LibraryProvider? = null
    var songProvider: SongProvider? = null
    var songLibrary = SongLibrary(TagType(0))
    var songQueue: ImmutableList<Pair<String, Song>>? = null

    fun run() {
        println(initialMessage)

        var running = true
        while (running) {

            print(prompt)
            val input = readLine()?.trim()?.split(" ")
                    ?.map { it.replaceFirst("~${File.separator}", System.getProperty("user.home") + File.separator) }
                    ?: break
            when (input[0].toLowerCase()) {
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
        if (input.size != 1) println("Usage: list")
        else for (line in shortDescriptions) println(line)
    }

/* ---------------------------------- Help ---------------------------------- */

    fun help(input: List<String>) {
        if (input.size != 2) println("Usage: help [command]")
        else fullDescriptions[input[1]]?.forEach { println(it) }
                ?: println("Not a command. Use \"list\" for available commands.")
    }

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
            when (libraryProvider) {
                null -> println("There is no loaded library provider.")

                is LocalLibraryProvider -> {
                    println("Type: local library provider")
                    println("Library path: ${(libraryProvider as LocalLibraryProvider).libraryPath}")
                }

                else -> {
                    println("Unknown library provider type.")
                    println("Library provider: $libraryProvider")
                }
            }
        }
        // other commands
        else when (input[1].toLowerCase()) {
            "new", "n" -> lpNew(input)
            "push", "s" -> lpPush(input)
            "pull", "l" -> lpPull(input)
            "modify", "m" -> lpModify(input)
            else -> badArgs("library-provider")
        }
    }

    fun lpNew(input: List<String>) {
        if (input.size != 2) { badArgs("library-provider"); return }
        if (libraryProvider != null) {
            print("Creating a new library provider will overwrite the old one. ")
            if (binaryQuestion("Are you sure?: ", posResponses, negResponses) != true) return
        }

        // used when for future expansion
        when (askUntilCorrect("What type of library provider would you like to create?", "local")) {
            "local" -> {
                print("What is the path to the library file?: ")
                val path = stringResponse()?.replace("\"", "")
                if (path == "" || path == null) { println("Aborted."); return }
                libraryProvider = LocalLibraryProvider(path)
                println("Loaded new library provider.")
            }
        }
    }

    fun lpPush(input: List<String>) {
        when {
            input.size != 2 -> badArgs("library-provider")
            libraryProvider == null -> println("There is no loaded library provider to push to.")
            libraryProvider!!.push(songLibrary) -> println("Pushed successfully.")
            else -> println("Push failed.")
        }
    }

    fun lpPull(input: List<String>) {
        when {
            input.size != 2 -> badArgs("library-provider")
            libraryProvider == null -> println("There is no loaded library provider to pull from.")
            else -> {
                val loaded = libraryProvider!!.pull()
                if (loaded != null) {
                    songLibrary = loaded
                    println("Pulled successfully.")
                } else println("Pull failed.")
            }
        }
    }

    fun lpModify(input: List<String>) {
        when {
            input.size != 2 -> badArgs("library-provider")
            libraryProvider == null -> println("There is no loaded library provider to modify.")
            else -> {
                // used when for future expansion
                when (libraryProvider) {
                    is LocalLibraryProvider -> {
                        print("Only the library path can be modified. ")
                        if (binaryQuestion("Would you like to change it?: ", posResponses, negResponses) != true) return
                        print("What is the path to the library file?: ")
                        val path = stringResponse()?.replace("\"", "")
                        if (path == "" || path == null) { println("Aborted."); return }
                        (libraryProvider as LocalLibraryProvider).libraryPath = path
                        println("Library path set.")
                    }
                    else -> println("Cannot modify unknown library provider type.")
                }
            }
        }
    }

/* ------------------------------ Song Provider ----------------------------- */

    fun songProviderCommand(input: List<String>) {
        // print summary
        if (input.size == 1) {
            when (songProvider) {
                null -> println("There is no loaded song provider.")
                is LocalSongProvider -> {
                    println("Type: local song provider")
                    println("Song directory path: ${(songProvider as LocalSongProvider).songDirectory}")
                } else -> {
                    println("Unknown song provider type.")
                    println("Song provider: $songProvider")
                }
            }
        }

        // other commands
        else when (input[1].toLowerCase()) {
            "new", "n" -> spNew(input)
            "has", "h" -> spHas(input)
            "push", "s" -> spPush(input)
            "remove", "r" -> spRemove(input)
            "modify", "m" -> spModify(input)
            else -> badArgs("song-provider")
        }
    }

    fun spNew(input: List<String>) {
        if (input.size != 2) { badArgs("song-provider"); return }
        if (songProvider != null) {
            print("Creating a new song provider will overwrite the old one. ")
            if (binaryQuestion("Are you sure?: ", posResponses, negResponses) != true) return
        }

        // used when for future expansion
        when (askUntilCorrect("What type of song provider would you like to create?", "local")) {
            "local" -> {
                print("What is the path to the song directory?: ")
                val path = stringResponse()?.replace("\"", "")
                if (path == "" || path == null) { println("Aborted."); return }
                songProvider = LocalSongProvider(path)
                println("Loaded new song provider.")
            }
        }
    }

    fun spHas(input: List<String>) {
        when {
            input.size < 3 -> badArgs("song-provider")
            songProvider == null -> println("There is no loaded song provider to pull from.")
            else -> {
                var songPath = input[2] + " "
                if (input.size > 3) for (i in 3 until input.size) songPath += input[i] + " "
                songPath = songPath.trim().replace("\"", "")
                println(if (songProvider!!.hasSong(songPath)) "true" else "false")
            }
        }
    }

    fun spPush(input: List<String>) {
        when {
            input.size < 3 -> badArgs("song-provider")
            songProvider == null -> println("There is no loaded song provider to push to.")
            else -> {
                var songPath = input[2] + " "
                if (input.size > 3) for (i in 3 until input.size) songPath += input[i] + " "
                songPath = songPath.trim().replace("\"", "")
                val songFile = File(songPath)

                if (songProvider!!.hasSong(songFile.name)) {
                    print("The song provider already has this song. ")
                    if (binaryQuestion("Would you still like to push it? ", posResponses, negResponses) != true) return
                }

                if (songProvider!!.pushSong(songPath, songFile.name)) {
                    println("Pushed successfully.")

                    // add song to library?
                    if (!songLibrary.songs.keys.contains(songFile.name)) {
                        print("The song is not in the loaded library. ")
                        if (binaryQuestion("Would you like to add it?: ", posResponses, negResponses) == true) {
                            val song = songWizard(songFile)
                            if (song != null) {
                                songLibrary.putSong(songFile.name, song)
                                println("Song added.")
                            }
                        }
                    }
                } else println("Push failed.")
            }
        }
    }

    fun spRemove(input: List<String>) {

    }

    fun spModify(input: List<String>) {

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
        val input = stringResponse()?.toLowerCase()
        return when {
            trueResponses.contains(input) -> true
            falseResponses.contains(input) -> false
            input == null -> null
            else -> binaryQuestion(question, trueResponses, falseResponses)
        }
    }

    fun askUntilCorrect(question: String, vararg responses: String): String? {
        print("$question (")
        for (response in responses) if (response != responses.last()) print("$response, ")
        print("${responses.last()}): ")

        val input = stringResponse()
        return when {
            responses.contains(input) -> input
            input == "" || input == null -> null
            else -> askUntilCorrect(question, *responses)
        }
    }

    fun stringResponse(homeTilde: Boolean = true): String? {
        var string = readLine()?.trim()
        if (homeTilde) string = string?.replaceFirst("~${File.separator}", System.getProperty("user.home") + File.separator)
        return string
    }

    fun badArgs(command: String) = println("Bad arguments. Use \"help $command\" for correct usage.")

    fun songWizard(songFile: File): Song? {
        if (!songFile.exists()) return null
        var song = loadSongFromMp3(songFile)!!

        // title
        if (!(binaryQuestion("Is the title: \"${song.title}\" correct?: ", posResponses, negResponses) ?: return null)) {
            print("What is the title? (blank for ${songFile.name}): ")
            val input = stringResponse() ?: return null
            if (input != "") song.mutate { title = songFile.name }
            else song.mutate { title = input }
        }

        // artist
        if (!(binaryQuestion("Is the artist: \"${song.artist}\" correct?: ", posResponses, negResponses) ?: return null)) {
            print("What is the artist? (blank for none): ")
            val input = stringResponse() ?: return null
            if (input != "") song = song.mutate { artist = null }
            else song = song.mutate { artist = input }
        }

        // album
        if (!(binaryQuestion("Is the album: \"${song.album}\" correct?: ", posResponses, negResponses) ?: return null)) {
            print("What is the album? (blank for none): ")
            val input = stringResponse() ?: return null
            if (input != "") song = song.mutate { album = input }
            else song = song.mutate { album = null }
        }

        // track number
        if (!(binaryQuestion("Is the track number: ${song.trackNum} correct?: ", posResponses, negResponses) ?: return null)) {
            print("What is the track number? (blank for none): ")
            val input = stringResponse() ?: return null
            if (input != "") {
                song = song.mutate { trackNum = input.toIntOrNull() }
                if (song.trackNum == null) println("Track number left unassigned.")
            } else song = song.mutate { trackNum = null }
        }

        // year
        if (!(binaryQuestion("Is the year: ${song.year} correct?: ", posResponses, negResponses) ?: return null)) {
            print("What is the year? (blank for none): ")
            val input = stringResponse() ?: return null
            if (input != "") {
                song = song.mutate { year = input.toIntOrNull() }
                if (song.year == null) println("Year left unassigned.")
            } else song = song.mutate { year = null }
        }

        // tags
        if (binaryQuestion("Would you like to tag the song?: ", posResponses, negResponses) ?: return null) {
            var tagging = true
            while (tagging) {
                print("Enter a tag to add (blank to stop): ")
                val input = stringResponse() ?: return null
                if (input != "") song = song.mutate { tags += input }
                else tagging = false
            }
        }

        return song
    }

    fun loadSongFromMp3(songFile: File): Song? {
        if (!songFile.exists()) return null
        val mp3 = Mp3File(songFile)

        var title: String? = null
        val duration: Int = mp3.lengthInMilliseconds.toInt()
        var artist: String? = null
        var album: String? = null
        var trackNum: Int? = null
        var year: Int? = null

        if (mp3.hasId3v2Tag()) {
            val tag = mp3.id3v2Tag
            title = tag.title
            artist = tag.artist
            album = tag.album
            trackNum = tag.track.toIntOrNull()
            year = tag.year.toIntOrNull()
        } else if (mp3.hasId3v1Tag()) {
            val tag = mp3.id3v1Tag
            title = tag.title
            artist = tag.artist
            album = tag.album
            trackNum = tag.track.toIntOrNull()
            year = tag.year.toIntOrNull()
        }

        return Song(title ?: songFile.name, duration, artist, album, trackNum, year)
    }
}
