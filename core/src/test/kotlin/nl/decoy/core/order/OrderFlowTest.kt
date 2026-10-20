package nl.decoy.core.order

import nl.decoy.core.ContextResolver
import nl.decoy.core.payment.FakePaymentAdapter
import nl.decoy.core.payment.PaymentStatus
import nl.decoy.core.retention.InMemorySessionStore
import nl.decoy.core.retention.RetentionController
import nl.decoy.fixtures.CampsiteOrderPlugin
import nl.decoy.fixtures.FixtureCatalog
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.MenuItem
import nl.decoy.plugins.api.PluginRegistry
import nl.decoy.plugins.api.PluginUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderFlowTest {
    private fun resolverAndStore(): Pair<ContextResolver, InMemorySessionStore> {
        val store = InMemorySessionStore()
        val resolver = ContextResolver(
            PluginRegistry(FixtureCatalog.allPlugins()),
            RetentionController(store),
        )
        return resolver to store
    }

    @Test
    fun campsiteOrderPaysAndWipesSession() {
        val (resolver, store) = resolverAndStore()
        val session = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedCampsiteQr))!!
        assertTrue(session.ui is PluginUiState.OrderForm)
        assertEquals(1, store.size())

        val flow = OrderFlow(
            payments = FakePaymentAdapter(),
            endSession = { s, ok -> resolver.complete(s, ok) },
        )
        val outcome = flow.placeOrder(
            session = session,
            selected = MenuItem("sandwich", "Sandwich", 650),
            contextualValues = mapOf("pitch" to "42"),
        )

        assertEquals(PaymentStatus.Success, outcome.payment.status)
        assertTrue(outcome.completed)
        assertNull(outcome.submission!!.outbound["iban"])
        assertEquals("42", outcome.submission.outbound["pitch"])
        assertEquals(0, store.size())
    }

    @Test
    fun failedPaymentKeepsSession() {
        val (resolver, store) = resolverAndStore()
        val session = resolver.resolve(DiscoveryInput.Qr(CampsiteOrderPlugin.FIXTURE_QR))!!
        val flow = OrderFlow(
            payments = FakePaymentAdapter(FakePaymentAdapter.Mode.AlwaysFail),
            endSession = { s, ok -> resolver.complete(s, ok) },
        )
        val outcome = flow.placeOrder(
            session,
            MenuItem("soup", "Soup", 450),
            mapOf("pitch" to "1"),
        )
        assertEquals(PaymentStatus.Failed, outcome.payment.status)
        assertEquals(1, store.size())
    }

    @Test
    fun terraceQrResolvesOrderForm() {
        val (resolver, _) = resolverAndStore()
        val session = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedTerraceQr))!!
        val form = session.ui as PluginUiState.OrderForm
        assertTrue(form.title.contains("table 7"))
        assertTrue(form.menu.any { it.id == "beer" })
    }
}
