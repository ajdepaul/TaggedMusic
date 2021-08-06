/*
package ajdepaul.taggedmusic

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import java.io.File
import kotlinx.collections.immutable.*
import com.google.gson.Gson

class _TestLocalProviders {

    @Test fun testLocalLibraryProvider() {
        File("test").mkdir()
        File("test/file.mp3").delete()

        var provider = LocalLibraryProvider("test/testLibrary.json")

        val song = Song(
                "title",
                1000,
                "artist",
                tags = persistentHashSetOf("A", "B"))
        val songLibrary = SongLibrary(TagType(0)).apply {
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
}
*/