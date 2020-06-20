package TaggedMusic

import kotlin.test.Test
import kotlin.test.assertTrue

class TestSongTest {

    @Test fun testSongLibrary() {
        val songLibrary = SongLibrary()
        assertTrue(songLibrary.tags.isEmpty())
        assertTrue(songLibrary.songs.isEmpty())

        // TODO test tags modifications
        // TODO Test songs modifications
    }

    @Test fun testTagFilter() {
        val songLibrary = SongLibrary()
        // val song = Song("path")
        // songLibrary.songs.add(song)
    }
}
