package nl.decoy.core.retention

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class MultiDayVaultTest {
    @Test
    fun encryptsAndDecryptsPayload() {
        val vault = AesGcmMultiDayVault(AesGcmMultiDayVault.generateKey())
        val service = MultiDayService(vault, defaultTtl = Duration.ofDays(3))
        service.startStay("stay-42", "campsite", """{"pitch":"42"}""")
        val loaded = service.get("stay-42")
        assertNotNull(loaded)
        assertEquals("""{"pitch":"42"}""", loaded!!.plaintextPayload)
        assertTrue(loaded.kind == "campsite")
    }

    @Test
    fun ttlAutoDestroys() {
        val start = Instant.parse("2026-06-01T00:00:00Z")
        var now = start
        val clock = object : Clock() {
            override fun getZone() = ZoneOffset.UTC
            override fun withZone(zone: java.time.ZoneId?) = this
            override fun instant() = now
        }
        val vault = AesGcmMultiDayVault(AesGcmMultiDayVault.generateKey(), clock)
        val service = MultiDayService(vault, clock, Duration.ofDays(1))
        service.startStay("stay-1", "campsite", "pitch=1")
        now = start.plus(Duration.ofHours(12))
        assertEquals(0, service.tickAutoDestroy())
        assertNotNull(service.get("stay-1"))
        now = start.plus(Duration.ofDays(2))
        assertEquals(1, service.tickAutoDestroy())
        assertNull(service.get("stay-1"))
    }

    @Test
    fun endDeletesRecord() {
        val vault = AesGcmMultiDayVault(AesGcmMultiDayVault.generateKey())
        val service = MultiDayService(vault)
        service.startStay("x", "campsite", "p")
        service.end("x")
        assertNull(service.get("x"))
        assertEquals(0, vault.size())
    }

    @Test
    fun listActiveReturnsNonExpired() {
        val vault = AesGcmMultiDayVault(AesGcmMultiDayVault.generateKey())
        val service = MultiDayService(vault)
        service.startStay("a", "cabin", "{}")
        service.startStay("b", "festival-pass", "{}")
        assertEquals(2, service.listActive().size)
        service.end("a")
        assertEquals(1, service.listActive().size)
        assertEquals("b", service.listActive().single().id)
    }
}
