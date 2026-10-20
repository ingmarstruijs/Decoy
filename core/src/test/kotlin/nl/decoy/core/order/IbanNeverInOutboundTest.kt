package nl.decoy.core.order

import nl.decoy.core.ContextResolver
import nl.decoy.core.payment.FakePaymentAdapter
import nl.decoy.core.payment.OpenBankingRelayAdapter
import nl.decoy.core.payment.PaymentPort
import nl.decoy.core.payment.PaymentStatus
import nl.decoy.core.payment.StagingOpenBankingRelayClient
import nl.decoy.core.retention.InMemorySessionStore
import nl.decoy.core.retention.RetentionController
import nl.decoy.fixtures.FixtureCatalog
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.MenuItem
import nl.decoy.plugins.api.PluginRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 4 invariant: order flow never places IBAN into vendor field payloads,
 * regardless of payment backend and even if the caller tries to sneak it in.
 */
class IbanNeverInOutboundTest {
    private fun flow(payments: PaymentPort): Pair<OrderFlow, ContextResolver> {
        val store = InMemorySessionStore()
        val resolver = ContextResolver(
            PluginRegistry(FixtureCatalog.allPlugins()),
            RetentionController(store),
        )
        val flow = OrderFlow(
            payments = payments,
            endSession = { s, ok -> resolver.complete(s, ok) },
        )
        return flow to resolver
    }

    @Test
    fun sneakedIbanDroppedWithFakeBackend() {
        assertIbanDropped(FakePaymentAdapter())
    }

    @Test
    fun sneakedIbanDroppedWithStagingRelay() {
        assertIbanDropped(OpenBankingRelayAdapter(StagingOpenBankingRelayClient()))
    }

    @Test
    fun heroOrderSucceedsViaStagingRelay() {
        val (flow, resolver) = flow(OpenBankingRelayAdapter(StagingOpenBankingRelayClient()))
        val session = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedCampsiteQr))!!
        val outcome = flow.placeOrder(
            session,
            MenuItem("sandwich", "Sandwich", 650),
            mapOf("pitch" to "42"),
        )
        assertEquals(PaymentStatus.Success, outcome.payment.status)
        assertEquals("open-banking-relay", outcome.payment.adapterId)
        assertTrue(outcome.completed)
        assertNull(outcome.submission!!.outbound["iban"])
        assertTrue(outcome.submission.payElsewhereFieldIds.contains("iban"))
    }

    private fun assertIbanDropped(payments: PaymentPort) {
        val (flow, resolver) = flow(payments)
        val session = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedCampsiteQr))!!
        val outcome = flow.placeOrder(
            session,
            MenuItem("sandwich", "Sandwich", 650),
            mapOf(
                "pitch" to "42",
                "iban" to "NL00BANK0123456789",
                "card" to "4111111111111111",
            ),
        )
        assertEquals(PaymentStatus.Success, outcome.payment.status)
        val outbound = outcome.submission!!.outbound
        assertNull(outbound["iban"])
        assertFalse(outbound.values.any { it.contains("NL00", ignoreCase = true) })
        assertFalse(outbound.keys.any { it.contains("card", ignoreCase = true) })
        assertTrue(outcome.submission.payElsewhereFieldIds.contains("iban"))
    }
}
