package ajdepaul.taggedmusic

import com.google.gson.Gson
import kotlinx.collections.immutable.persistentHashSetOf
import java.io.File
import kotlin.test.*

class TestLocalProviders {

    @Test fun testLocalLibraryProvider() {
        File("test").mkdir()
        File("test/file.mp3").delete()

        var provider = LocalLibraryProvider("test/testLibrary.json")

        val song = Song(
                "title",
                1000,
                "artist",
                tags = persistentHashSetOf("A", "B"))
        val songLibrary = SongLibrary().apply {
            this.defaultTagType = TagType(1)
            this.putSong("file.mp3", song)
            this.putTag("C", Tag("type1", "description"))
            this.putTagType("type2", TagType(2))
        }

        assertTrue(provider.push(songLibrary))

        val loadedLibrary = provider.pull()
        assertNotNull(loadedLibrary)
        assertEquals(songLibrary.defaultTagType, loadedLibrary.defaultTagType)
        assertEquals(songLibrary.songs, loadedLibrary.songs)
        assertEquals(songLibrary.tags, loadedLibrary.tags)
        assertEquals(songLibrary.tagTypes, loadedLibrary.tagTypes)

        // exception catching
        if (File("not_a_path").exists()) { error("'not_a_path' cannot be a valid path for this test") }
        provider = LocalLibraryProvider("not_a_path/songLibrary.json")
        assertFalse(provider.push(songLibrary))
        assertNull(provider.pull())

        File("test/badFormat.json").writer().use { Gson().toJson("badFormat", it) }
        provider = LocalLibraryProvider("test/badFormat.json")
        assertNull(provider.pull())
    }

    @Test fun testLocalSongProvider() {
        File("test").mkdir()
        File("test/song.mp3").createNewFile()
        File("test/songs").mkdir()
        File("test/songs/song.mp3/file.txt").delete()
        File("test/songs/song.mp3").delete()

        var provider = LocalSongProvider("test/songs")

        assertFalse(provider.hasSong("song.mp3"))
        assertNull(provider.pullSong("song.mp3"))
        assertFalse(provider.removeSong("song.mp3"))

        assertTrue(provider.pushSong("test/song.mp3", "song.mp3"))
        assertTrue(File("test/songs/song.mp3").isFile)
        assertTrue(provider.hasSong("song.mp3"))
        assertTrue(File(provider.pullSong("song.mp3")).isFile)
        assertTrue(provider.removeSong("song.mp3"))
        assertFalse(File("test/songs/song.mp3").exists())

        // exception catching
        if (File("not_a_path").exists()) { error("'not_a_path' cannot be a valid path for this test") }

        assertFalse(provider.pushSong("not_a_path/song.mp3", "song.mp3"))

        File("test/songs/song.mp3").mkdir()
        File("test/songs/song.mp3/file.txt").createNewFile()
        assertFalse(provider.pushSong("test/song.mp3", "song.mp3"))
        File("test/songs/song.mp3/file.txt").delete()
        File("test/songs/song.mp3").delete()

        provider = LocalSongProvider("not_a_path/songs")

        assertFalse(provider.hasSong("song.mp3"))
        assertFalse(provider.hasSong(""))
        assertFalse(provider.pushSong("test/song.mp3", "song.mp3"))
        assertNull(provider.pullSong("song.mp3"))
        assertFalse(provider.removeSong("song.mp3"))
    }
}