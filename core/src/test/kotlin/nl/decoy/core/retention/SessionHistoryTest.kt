package nl.decoy.core.retention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class SessionHistoryTest {
    @Test
    fun recordsNewestFirstWithoutPayload() {
        val history = SessionHistoryService(InMemorySessionHistoryStore())
        history.record(
            kind = "parking",
            label = "zone 4021",
            retention = HistoryRetentionClass.OneShot,
            endReason = HistoryEndReason.Completed,
        )
        history.record(
            kind = "campsite",
            label = "stay-42",
            retention = HistoryRetentionClass.MultiDay,
            endReason = HistoryEndReason.Ended,
        )
        val recent = history.listRecent()
        assertEquals(2, recent.size)
        assertEquals("campsite", recent[0].kind)
        assertEquals("parking", recent[1].kind)
        assertTrue(recent.none { it.label.contains("{") })
    }

    @Test
    fun ttlWipesAndClearEmpties() {
        val start = Instant.parse("2026-06-01T00:00:00Z")
        var now = start
        val clock = object : Clock() {
            override fun getZone() = ZoneOffset.UTC
            override fun withZone(zone: java.time.ZoneId?) = this
            override fun instant() = now
        }
        val history = SessionHistoryService(
            store = InMemorySessionHistoryStore(),
            clock = clock,
            defaultTtl = Duration.ofDays(1),
        )
        history.record("parking", "4021", HistoryRetentionClass.OneShot, HistoryEndReason.Dismissed)
        now = start.plus(Duration.ofHours(12))
        assertEquals(1, history.listRecent().size)
        now = start.plus(Duration.ofDays(2))
        assertEquals(0, history.listRecent().size)

        history.record("ev", "bay-1", HistoryRetentionClass.OneShot, HistoryEndReason.Completed)
        assertEquals(1, history.listRecent().size)
        history.clear()
        assertEquals(0, history.listRecent().size)
    }

    @Test
    fun capsMaxEntries() {
        val history = SessionHistoryService(
            store = InMemorySessionHistoryStore(),
            maxEntries = 3,
        )
        repeat(5) { i ->
            history.record("parking", "z$i", HistoryRetentionClass.OneShot, HistoryEndReason.Completed)
        }
        val recent = history.listRecent()
        assertEquals(3, recent.size)
        assertEquals("z4", recent[0].label)
        assertEquals("z2", recent[2].label)
    }
}
