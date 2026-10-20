package nl.decoy.core

import nl.decoy.core.input.ZoneDirectory
import nl.decoy.core.order.OrderFlow
import nl.decoy.core.payment.FakePaymentAdapter
import nl.decoy.core.payment.PaymentStatus
import nl.decoy.core.retention.InMemorySessionStore
import nl.decoy.core.retention.RetentionController
import nl.decoy.fixtures.FixtureCatalog
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginRegistry
import nl.decoy.plugins.api.PluginUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase3FixturesTest {
    private val store = InMemorySessionStore()
    private val retention = RetentionController(store)
    private val resolver = ContextResolver(
        PluginRegistry(FixtureCatalog.allPlugins()),
        retention,
    )
    private val zones = ZoneDirectory()
    private val lifecycle = SessionLifecycle(retention)
    private val orders = OrderFlow(
        payments = FakePaymentAdapter(),
        endSession = { session, ok ->
            if (ok) {
                lifecycle.dispose(session, DisposeReason.Completed, success = true)
            } else {
                lifecycle.dismiss(session)
            }
        },
    )

    @Test
    fun zone4021ResolvesParkingNotCampsite() {
        val input = zones.resolve("4021")!!
        val session = resolver.resolve(input)!!
        assertEquals("fixture.parking.session", session.plugin.id)
        assertTrue(session.ui is PluginUiState.TimedSession)
    }

    @Test
    fun captivePortalConnectWipes() {
        val session = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedPortalQr))!!
        assertEquals("fixture.captive.portal", session.plugin.id)
        val form = session.ui as PluginUiState.OrderForm
        val outcome = orders.placeOrder(session, form.menu.first(), emptyMap())
        assertEquals(PaymentStatus.Success, outcome.payment.status)
        assertTrue(outcome.completed)
        assertTrue(outcome.submission!!.aliasedFieldIds.contains("email"))
        assertTrue(outcome.submission.omittedFieldIds.contains("marketing"))
        assertTrue(store.get(session.session.sessionId) == null)
    }

    @Test
    fun vendingPayWipes() {
        val session = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedVendingQr))!!
        assertEquals("fixture.vending.pay", session.plugin.id)
        val form = session.ui as PluginUiState.OrderForm
        val item = form.menu.first { it.id == "locker" }
        val outcome = orders.placeOrder(session, item, emptyMap())
        assertTrue(outcome.completed)
        assertTrue(outcome.submission!!.payElsewhereFieldIds.contains("iban"))
        assertTrue(store.get(session.session.sessionId) == null)
    }

    @Test
    fun keypadWifiAndEvAndVend() {
        assertEquals(
            "fixture.captive.portal",
            resolver.resolve(zones.resolve("WIFI")!!)!!.plugin.id,
        )
        assertEquals(
            "fixture.ev.charging",
            resolver.resolve(zones.resolve("EV1")!!)!!.plugin.id,
        )
        assertEquals(
            "fixture.vending.pay",
            resolver.resolve(zones.resolve("VEND1")!!)!!.plugin.id,
        )
    }
}
