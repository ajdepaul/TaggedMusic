package ajdepaul.taggedmusiccli

import ajdepaul.taggedmusic.LocalLibraryProvider
import ajdepaul.taggedmusiccli.util.badArgs
import ajdepaul.taggedmusiccli.util.binaryQuestion
import ajdepaul.taggedmusiccli.util.pathResponse

internal fun libraryProviderCommand(cli: CLI, args: List<String>) {
    // print summary
    if (args.size == 1) {
        when (cli.libraryProvider) {
            null -> println("There is no loaded library provider.")

            is LocalLibraryProvider -> {
                println("Type: local library provider")
                println("Library path: ${(cli.libraryProvider as LocalLibraryProvider).libraryPath}")
            }

            else -> {
                println("Unknown library provider type.")
                println("Library provider: ${cli.libraryProvider}")
            }
        }
    }
    // other commands
    else when (args[1].toLowerCase()) {
        "new", "n" -> new(cli, args)
        "push", "s" -> push(cli, args)
        "pull", "l" -> pull(cli, args)
        "modify", "m" -> modify(cli, args)
        else -> badArgs("library-provider")
    }
}

/** Creates a new library provider for the CLI. */
private fun new(cli: CLI, args: List<String>) {
    if (args.size != 3) {
        badArgs("library-provider"); return }
    if (cli.libraryProvider != null) {
        print("Creating a new library provider will overwrite the old one. ")
        if (binaryQuestion("Are you sure?: ", cli.posResponses, cli.negResponses) != true) return
    }

    when (args[2]) {
        "local" -> {
            print("What is the path to the library file?: ")
            val path = pathResponse()
            if (path == "" || path == null) { println("Aborted."); return }
            cli.libraryProvider = LocalLibraryProvider(path)
            println("Loaded new library provider.")
        }
        else -> badArgs("song-provider")
    }
}

private fun push(cli: CLI, input: List<String>) {
    when {
        input.size != 2 -> badArgs("library-provider")
        cli.libraryProvider == null -> println("There is no loaded library provider to push to.")
        cli.libraryProvider!!.push(cli.songLibrary) -> println("Pushed successfully.")
        else -> println("Push failed.")
    }
}

private fun pull(cli: CLI, input: List<String>) {
    when {
        input.size != 2 -> badArgs("library-provider")
        cli.libraryProvider == null -> println("There is no loaded library provider to pull from.")
        else -> {
            val loaded = cli.libraryProvider!!.pull()
            if (loaded != null) {
                cli.songLibrary = loaded
                println("Pulled successfully.")
            } else println("Pull failed.")
        }
    }
}

private fun modify(cli: CLI, input: List<String>) {
    when {
        input.size != 2 -> badArgs("library-provider")
        cli.libraryProvider == null -> println("There is no loaded library provider to modify.")
        else -> {
            when (cli.libraryProvider) {

                is LocalLibraryProvider -> {
                    if (binaryQuestion("Would you like to change the library path?: ", cli.posResponses, cli.negResponses) != true) return
                    print("What is the path to the library file?: ")
                    val path = pathResponse()
                    if (path == "" || path == null) { println("Aborted."); return }
                    (cli.libraryProvider as LocalLibraryProvider).libraryPath = path
                    println("Library path set.")
                }

                else -> println("Cannot modify unknown library provider type.")
            }
        }
    }
}
