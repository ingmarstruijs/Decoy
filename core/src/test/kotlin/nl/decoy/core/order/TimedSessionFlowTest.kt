package nl.decoy.core.order

import nl.decoy.core.ContextResolver
import nl.decoy.core.DisposeReason
import nl.decoy.core.SessionLifecycle
import nl.decoy.core.payment.FakePaymentAdapter
import nl.decoy.core.payment.PaymentStatus
import nl.decoy.core.retention.InMemorySessionStore
import nl.decoy.core.retention.RetentionController
import nl.decoy.fixtures.FixtureCatalog
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginRegistry
import nl.decoy.plugins.api.PluginUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimedSessionFlowTest {
    private val store = InMemorySessionStore()
    private val retention = RetentionController(store)
    private val resolver = ContextResolver(
        PluginRegistry(FixtureCatalog.allPlugins()),
        retention,
    )
    private val lifecycle = SessionLifecycle(retention)
    private val timed = TimedSessionFlow(payments = FakePaymentAdapter())

    @Test
    fun parkingStartKeepsSessionUntilStopWipe() {
        val session = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedParkingQr))!!
        assertTrue(session.ui is PluginUiState.TimedSession)

        val start = timed.start(session, emptyMap())
        assertEquals(PaymentStatus.Success, start.payment.status)
        assertTrue(start.running)
        assertTrue(store.get(session.session.sessionId) != null)
        assertTrue(start.submission!!.omittedFieldIds.contains("plate"))
        assertTrue(start.submission.aliasedFieldIds.contains("email"))
        assertTrue(start.submission.payElsewhereFieldIds.contains("iban"))

        lifecycle.dispose(session, DisposeReason.Completed, success = true)
        assertTrue(store.get(session.session.sessionId) == null)
    }

    @Test
    fun parkingPlateOnlyWithConsent() {
        val session = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedParkingQr))!!
        val withConsent = timed.start(
            session,
            mapOf("plate" to "AB-123-C", "consent_plate" to "true"),
        )
        assertEquals("AB-123-C", withConsent.submission!!.outbound["plate"])

        val session2 = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedParkingQr))!!
        val without = timed.start(session2, mapOf("plate" to "AB-123-C"))
        assertFalse(without.submission!!.outbound.containsKey("plate"))
        assertTrue(without.submission.omittedFieldIds.contains("plate"))
    }

    @Test
    fun evChargingTimedFlow() {
        val session = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedEvQr))!!
        assertEquals("fixture.ev.charging", session.plugin.id)
        val start = timed.start(session, emptyMap())
        assertTrue(start.running)
        lifecycle.dispose(session, DisposeReason.Completed, success = true)
        assertTrue(store.get(session.session.sessionId) == null)
    }
}
