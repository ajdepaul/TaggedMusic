package ajdepaul.taggedmusiccli

import ajdepaul.taggedmusic.LocalSongProvider
import ajdepaul.taggedmusiccli.util.badArgs
import ajdepaul.taggedmusiccli.util.binaryQuestion
import ajdepaul.taggedmusiccli.util.pathResponse
import ajdepaul.taggedmusiccli.util.songWizard
import java.io.File

internal fun songProviderCommand(cli: CLI, input: List<String>) {
    // print summary
    if (input.size == 1) {
        when (cli.songProvider) {
            null -> println("There is no loaded song provider.")
            is LocalSongProvider -> {
                println("Type: local song provider")
                println("Song directory path: ${(cli.songProvider as LocalSongProvider).songDirectory}")
            } else -> {
            println("Unknown song provider type.")
            println("Song provider: ${cli.songProvider}")
        }
        }
    }

    // other commands
    else when (input[1].toLowerCase()) {
        "new", "n" -> new(cli, input)
        "has", "h" -> has(cli, input)
        "push", "s" -> push(cli, input)
        "remove", "r" -> remove(input)
        "modify", "m" -> modify(input)
        else -> badArgs("song-provider")
    }
}

private fun new(cli: CLI, input: List<String>) {
    if (input.size != 3) { badArgs("song-provider"); return }
    if (cli.songProvider != null) {
        print("Creating a new song provider will overwrite the old one. ")
        if (binaryQuestion("Are you sure?: ", cli.posResponses, cli.negResponses) != true) return
    }

    when (input[2]) {
        "local" -> {
            print("What is the path to the song directory?: ")
            val path = pathResponse()
            if (path == "" || path == null) { println("Aborted."); return }
            cli.songProvider = LocalSongProvider(path)
            println("Loaded new song provider.")
        }
        else -> badArgs("song-provider")
    }
}

private fun has(cli: CLI, input: List<String>) {
    when {
        input.size < 3 -> badArgs("song-provider")
        cli.songProvider == null -> println("There is no loaded song provider to pull from.")
        else -> {
            var songPath = input[2] + " "
            if (input.size > 3) for (i in 3 until input.size) songPath += input[i] + " "
            songPath = songPath.trim().replace("\"", "")
            println(if (cli.songProvider!!.hasSong(songPath)) "true" else "false")
        }
    }
}

private fun push(cli: CLI, input: List<String>) {
    when {
        input.size < 3 -> badArgs("song-provider")
        cli.songProvider == null -> println("There is no loaded song provider to push to.")
        else -> {
            val songPath = input.drop(2).reduce { a, b -> "$a $b" }.trim().replace("\"", "")
            val songFile = File(songPath)

            val songFileName = when (binaryQuestion("Use ${songFile.name} for the file name?: ", cli.posResponses, cli.negResponses)) {
                true -> songFile.name
                false -> {
                    print("What file name should be used?: ")
                    readLine()?.trim() ?: return
                }
                null -> return
            }

            if (cli.songProvider!!.hasSong(songFileName)) {
                print("The song provider already has this song. ")
                if (binaryQuestion("Would you still like to push it?: ", cli.posResponses, cli.negResponses) != true) return
            }

            if (cli.songProvider!!.pushSong(songPath, songFileName)) {
                println("Pushed successfully.")

                // add song to library?
                if (!cli.songLibrary.songs.keys.contains(songFileName)) {
                    print("The song is not in the loaded library. ")
                    if (binaryQuestion("Would you like to add it?: ", cli.posResponses, cli.negResponses) == true) {
                        val song = songWizard(cli, songFile)
                        if (song != null) {
                            cli.songLibrary.putSong(songFileName, song)
                            println("Song added.")
                        }
                    }
                }
            } else println("Push failed.")
        }
    }
}

private fun remove(input: List<String>) { /* TODO */ }

private fun modify(input: List<String>) { /* TODO */ }
