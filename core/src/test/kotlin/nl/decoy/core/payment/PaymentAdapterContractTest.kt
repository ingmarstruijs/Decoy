package nl.decoy.core.payment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Contract: Fake and staging open-banking relay share the same [PaymentPort] surface.
 */
@RunWith(Parameterized::class)
class PaymentAdapterContractTest(
    private val name: String,
    private val port: PaymentPort,
) {
    @Test
    fun successfulPayReturnsSuccessAndReference() {
        val result = port.pay(
            PaymentRequest(amountCents = 650, description = "Campsite: Sandwich"),
        )
        assertEquals("$name status", PaymentStatus.Success, result.status)
        assertNotNull("$name reference", result.reference)
        assertEquals("$name adapterId", port.adapterId, result.adapterId)
    }

    @Test
    fun zeroAmountAllowedForPortalStyleConnect() {
        val result = port.pay(
            PaymentRequest(amountCents = 0, description = "Guest Wi-Fi connect"),
        )
        assertEquals(PaymentStatus.Success, result.status)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun adapters(): Collection<Array<Any>> = listOf(
            arrayOf("fake", FakePaymentAdapter()),
            arrayOf(
                "open-banking-relay",
                OpenBankingRelayAdapter(StagingOpenBankingRelayClient()),
            ),
        )
    }
}

class PaymentBackendFactoryTest {
    @Test
    fun switchingPortDefaultsToFakeAndCanStage() {
        val port = SwitchingPaymentPort()
        assertEquals("fake", port.adapterId)
        assertEquals(PaymentStatus.Success, port.pay(PaymentRequest(100, description = "x")).status)

        port.use(PaymentBackend.StagingOpenBankingRelay)
        assertEquals("open-banking-relay", port.adapterId)
        val staged = port.pay(PaymentRequest(250, description = "Parking"))
        assertEquals(PaymentStatus.Success, staged.status)
        assertTrue(staged.reference!!.startsWith("obr-staging-"))

        port.use(PaymentBackend.Fake)
        assertEquals("fake", port.adapterId)
    }

    @Test
    fun stagingRelayDeclinePath() {
        val port = OpenBankingRelayAdapter(
            StagingOpenBankingRelayClient(StagingOpenBankingRelayClient.Mode.AlwaysFail),
        )
        assertEquals(
            PaymentStatus.Failed,
            port.pay(PaymentRequest(100, description = "x")).status,
        )
    }
}
