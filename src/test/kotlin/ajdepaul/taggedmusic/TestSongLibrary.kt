package ajdepaul.taggedmusic

import java.time.LocalDateTime
import kotlin.test.*

class TestDataClassesTest {

    private fun assertUpdated(before: LocalDateTime, after: LocalDateTime): LocalDateTime {
        assertTrue(before < after)
        return after
    }

    @Test fun testSongLibrary() {
    }

    @Test fun testSongUpdates() {
    }

    @Test fun testTagUpdates() {
        val s = SongLibrary()
        s.toJsonData()
    }

    @Test fun testTagTypeUpdates() {
    }

    @Test fun testTagFilter() {
        // TODO implement
    }

    @Test fun testJson() {
        // TODO implement
    }

    @Test fun testObservers() {
        // TODO implement
    }
}
