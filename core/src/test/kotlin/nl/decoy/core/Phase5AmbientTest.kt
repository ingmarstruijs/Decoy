package nl.decoy.core

import nl.decoy.core.retention.AesGcmMultiDayVault
import nl.decoy.core.retention.InMemorySessionStore
import nl.decoy.core.retention.MultiDayService
import nl.decoy.core.retention.RetentionController
import nl.decoy.fixtures.AmbientFixtures
import nl.decoy.fixtures.CaptivePortalPlugin
import nl.decoy.fixtures.FixtureCatalog
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginRegistry
import nl.decoy.plugins.api.PluginUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class Phase5AmbientTest {
    private val store = InMemorySessionStore()
    private val retention = RetentionController(store)
    private val resolver = ContextResolver(
        PluginRegistry(FixtureCatalog.allPlugins()),
        retention,
    )

    @Test
    fun bleCampsiteOrderResolves() {
        val session = resolver.resolve(AmbientFixtures.ble())!!
        assertEquals("fixture.campsite.order", session.plugin.id)
        assertTrue(session.ui is PluginUiState.OrderForm)
    }

    @Test
    fun bleStayAndCabinAndFestivalResolve() {
        assertEquals(
            "fixture.campsite.stay",
            resolver.resolve(DiscoveryInput.Ble(AmbientFixtures.BLE_CAMPSITE_STAY))!!.plugin.id,
        )
        assertEquals(
            "fixture.cabin.stay",
            resolver.resolve(DiscoveryInput.Ble(AmbientFixtures.BLE_CABIN))!!.plugin.id,
        )
        assertEquals(
            "fixture.festival.pass",
            resolver.resolve(DiscoveryInput.Ble(AmbientFixtures.BLE_FESTIVAL))!!.plugin.id,
        )
    }

    @Test
    fun uwbSameContractAsBle() {
        val session = resolver.resolve(AmbientFixtures.uwb())!!
        assertEquals("fixture.campsite.order", session.plugin.id)
    }

    @Test
    fun captivePortalHtmlMarkerAndBle() {
        val html = resolver.resolve(DiscoveryInput.Url(CaptivePortalPlugin.FIXTURE_HTML_URL))!!
        assertEquals("fixture.captive.portal", html.plugin.id)
        val ble = resolver.resolve(DiscoveryInput.Ble(AmbientFixtures.BLE_PORTAL))!!
        assertEquals("fixture.captive.portal", ble.plugin.id)
        assertEquals("Fixture-Guest-WiFi", ble.context.attributes["ssid"])
    }

    @Test
    fun multiDayCabinAndPassVaultTtl() {
        val start = Instant.parse("2026-07-01T00:00:00Z")
        var now = start
        val clock = object : Clock() {
            override fun getZone() = ZoneOffset.UTC
            override fun withZone(zone: java.time.ZoneId?) = this
            override fun instant() = now
        }
        val vault = AesGcmMultiDayVault(AesGcmMultiDayVault.generateKey(), clock)
        val multi = MultiDayService(vault, clock, Duration.ofDays(2))

        val cabin = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedCabinQr))!!
        multi.startStay(
            cabin.context.attributes["stayId"]!!,
            cabin.context.attributes["kind"]!!,
            cabin.context.attributes["vaultPayload"]!!,
        )
        val pass = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedFestivalQr))!!
        multi.startStay(
            pass.context.attributes["stayId"]!!,
            pass.context.attributes["kind"]!!,
            pass.context.attributes["vaultPayload"]!!,
            ttl = Duration.ofDays(3),
        )

        assertNotNull(multi.get("cabin-3"))
        assertNotNull(multi.get("pass-FX26"))
        now = start.plus(Duration.ofDays(2))
        assertEquals(1, multi.tickAutoDestroy())
        assertNull(multi.get("cabin-3"))
        assertNotNull(multi.get("pass-FX26"))
        now = start.plus(Duration.ofDays(3))
        assertEquals(1, multi.tickAutoDestroy())
        assertNull(multi.get("pass-FX26"))
    }

    @Test
    fun oneShotAndMultiDayDoNotCrossLeakIds() {
        val vault = AesGcmMultiDayVault(AesGcmMultiDayVault.generateKey())
        val multi = MultiDayService(vault)
        val lifecycle = SessionLifecycle(retention)

        val order = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedCampsiteQr))!!
        val stay = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedStayQr))!!
        assertNotEquals(order.session.sessionId, stay.session.sessionId)

        multi.startStay(
            stay.context.attributes["stayId"]!!,
            "campsite",
            stay.context.attributes["vaultPayload"]!!,
        )
        lifecycle.dispose(order, DisposeReason.Completed, success = true)

        assertNull(store.get(order.session.sessionId))
        assertNotNull(store.get(stay.session.sessionId))
        assertNotNull(multi.get("stay-42"))

        // No shared stable device id attributes
        assertTrue(order.session.attributes.none { it.key.contains("device", ignoreCase = true) })
        assertTrue(stay.session.attributes.none { it.key.contains("android", ignoreCase = true) })
        assertNull(order.context.attributes["gaid"])
        assertNull(stay.context.attributes["gaid"])

        lifecycle.dispose(stay, DisposeReason.Completed, success = true)
        assertNull(store.get(stay.session.sessionId))
        assertNotNull(multi.get("stay-42")) // vault independent of One-shot wipe
    }
}
