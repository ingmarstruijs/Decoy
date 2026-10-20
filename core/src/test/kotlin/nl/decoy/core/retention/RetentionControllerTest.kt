package nl.decoy.core.retention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class RetentionControllerTest {
    @Test
    fun oneShotEndsOnExplicitEnd() {
        val store = InMemorySessionStore()
        val controller = RetentionController(store)
        val session = controller.startOneShot()
        assertEquals(1, store.size())
        controller.endSession(session.sessionId)
        assertNull(store.get(session.sessionId))
    }

    @Test
    fun safetyTtlAutoDestroysUnlessPinned() {
        val start = Instant.parse("2026-01-01T00:00:00Z")
        var now = start
        val mutableClock = object : Clock() {
            override fun getZone() = ZoneOffset.UTC
            override fun withZone(zone: java.time.ZoneId?) = this
            override fun instant() = now
        }
        val store = InMemorySessionStore()
        val controller = RetentionController(
            store = store,
            clock = mutableClock,
            defaultOneShotSafetyTtl = Duration.ofHours(1),
        )
        val session = controller.startOneShot()
        now = start.plus(Duration.ofMinutes(30))
        assertEquals(0, controller.tickAutoDestroy())
        assertFalse(session.copy(createdAt = start, safetyTtl = Duration.ofHours(1)).isExpired(now))

        now = start.plus(Duration.ofHours(2))
        assertEquals(1, controller.tickAutoDestroy())
        assertNull(store.get(session.sessionId))
    }

    @Test
    fun pinnedSurvivesTtl() {
        val start = Instant.parse("2026-01-01T00:00:00Z")
        var now = start
        val mutableClock = object : Clock() {
            override fun getZone() = ZoneOffset.UTC
            override fun withZone(zone: java.time.ZoneId?) = this
            override fun instant() = now
        }
        val store = InMemorySessionStore()
        val controller = RetentionController(
            store = store,
            clock = mutableClock,
            defaultOneShotSafetyTtl = Duration.ofMinutes(1),
        )
        val session = controller.startOneShot()
        controller.pin(session.sessionId)
        now = start.plus(Duration.ofHours(5))
        assertEquals(0, controller.tickAutoDestroy())
        assertTrue(store.get(session.sessionId)!!.pinned)
    }
}
