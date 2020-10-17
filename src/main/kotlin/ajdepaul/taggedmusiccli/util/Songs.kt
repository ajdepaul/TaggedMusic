package ajdepaul.taggedmusiccli.util

import ajdepaul.taggedmusic.Song
import ajdepaul.taggedmusiccli.CLI
import com.mpatric.mp3agic.Mp3File
import java.io.File

import kotlinx.collections.immutable.*

/**
 * Prompts the users with questions to create a [Song] with properties defined by the user. Returns null if [songFile]
 * does not exist or EOF.
 */
internal fun songWizard(cli: CLI, songFile: File): Song? {
    if (!songFile.exists()) return null
    var song = loadSongFromMp3(songFile)!!

    // title
    if (!(binaryQuestion("Is the title: \"${song.title}\" correct?: ", cli.posResponses, cli.negResponses)
                    ?: return null)) {
        print("What is the title? (blank for ${songFile.name}): ")
        val input = readLine()?.trim() ?: return null
        if (input != "") song.mutate { title = songFile.name }
        else song.mutate { title = input }
    }

    // artist
    var askForProperty = true
    if (song.artist != null) {
        if ((binaryQuestion("Is the artist: \"${song.artist}\" correct?: ", cli.posResponses, cli.negResponses)
                        ?: return null)) {
            askForProperty = false
        }
    }
    if (askForProperty) {
        print("What is the artist? (blank for none): ")
        val input = readLine()?.trim() ?: return null
        if (input != "") song = song.mutate { artist = null }
        else song = song.mutate { artist = input }
    }

    // album
    askForProperty = true
    if (song.album != null) {
        if ((binaryQuestion("Is the album: \"${song.album}\" correct?: ", cli.posResponses, cli.negResponses)
                        ?: return null)) {
            askForProperty = false
        }
    }
    if (askForProperty) {
        print("What is the album? (blank for none): ")
        val input = readLine()?.trim() ?: return null
        if (input != "") song = song.mutate { album = input }
        else song = song.mutate { album = null }
    }

    // track number
    askForProperty = true
    if (song.trackNum != null) {
        if ((binaryQuestion("Is the track number: ${song.trackNum} correct?: ", cli.posResponses, cli.negResponses)
                        ?: return null)) {
            askForProperty = false
        }
    }
    if (askForProperty) {
        print("What is the track number? (blank for none): ")
        val input = readLine()?.trim() ?: return null
        if (input != "") {
            song = song.mutate { trackNum = input.toIntOrNull() }
            if (song.trackNum == null) println("Track number left unassigned.")
        } else song = song.mutate { trackNum = null }
    }

    // year
    askForProperty = true
    if (song.year != null || song.year != 0) {
        if ((binaryQuestion("Is the year: ${song.year} correct?: ", cli.posResponses, cli.negResponses)
                        ?: return null)) {
            askForProperty = false
        }
    }
    if (askForProperty) {
        print("What is the year? (blank for none): ")
        val input = readLine()?.trim() ?: return null
        if (input != "") {
            song = song.mutate { year = input.toIntOrNull() }
            if (song.year == null) println("Year left unassigned.")
        } else song = song.mutate { year = null }
    }

    // tags
    if (binaryQuestion("Would you like to tag the song?: ", cli.posResponses, cli.negResponses)
                    ?: return null) {
        var tagging = true
        while (tagging) {
            print("Enter a tag to add (blank to stop): ")
            val input = readLine()?.trim() ?: return null
            if (input != "") song = song.mutate { tags += input }
            else tagging = false
        }
    }

    return song
}

/** Creates a [Song] from [songFile]. Returns null if [songFile] does not exist. */
internal fun loadSongFromMp3(songFile: File): Song? {
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
