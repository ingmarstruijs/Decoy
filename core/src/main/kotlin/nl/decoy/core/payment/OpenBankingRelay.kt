package nl.decoy.core.payment

/**
 * Minimal open-banking / W3C Web Payments relay contract.
 *
 * Decoy never sends IBAN or card PAN to the venue. The relay (or bank app) is the
 * only party that sees payment-instrument details. Residual PSP/bank linkability
 * remains — see docs/threat-model.md.
 */
data class RelayPaymentRequest(
    val amountCents: Int,
    val currency: String,
    val description: String,
    /** Opaque merchant/capability label — not a vendor form field. */
    val merchantHint: String = "decoy-capability",
)

data class RelayPaymentResponse(
    val status: PaymentStatus,
    val relayReference: String? = null,
    val detail: String? = null,
)

/**
 * Client that talks to an open-banking / Web Payments relay.
 * Staging uses an in-process mock; a future HTTP client can hit a real staging URL.
 */
interface PaymentRelayClient {
    fun initiate(request: RelayPaymentRequest): RelayPaymentResponse
}

/**
 * In-process staging relay: authorize → settle, no WAN.
 * Models the Web Payment Request / open-banking redirect happy path.
 */
class StagingOpenBankingRelayClient(
    private val mode: Mode = Mode.AlwaysSucceed,
) : PaymentRelayClient {
    enum class Mode { AlwaysSucceed, AlwaysFail, UserCancel }

    override fun initiate(request: RelayPaymentRequest): RelayPaymentResponse {
        require(request.amountCents >= 0) { "amount must be non-negative" }
        require(request.currency.isNotBlank()) { "currency required" }
        return when (mode) {
            Mode.AlwaysSucceed -> RelayPaymentResponse(
                status = PaymentStatus.Success,
                relayReference = "obr-staging-${request.amountCents}-${request.currency}",
                detail = "Staging open-banking relay settled (no production PSP)",
            )
            Mode.AlwaysFail -> RelayPaymentResponse(
                status = PaymentStatus.Failed,
                detail = "Staging relay declined",
            )
            Mode.UserCancel -> RelayPaymentResponse(
                status = PaymentStatus.Cancelled,
                detail = "User cancelled bank authorization",
            )
        }
    }
}

/**
 * PaymentPort backed by a [PaymentRelayClient].
 * This is the Phase 4 “real open path” shape; CI still defaults to [FakePaymentAdapter].
 */
class OpenBankingRelayAdapter(
    private val relay: PaymentRelayClient,
) : PaymentPort {
    override val adapterId: String = "open-banking-relay"

    override fun pay(request: PaymentRequest): PaymentResult {
        val response = relay.initiate(
            RelayPaymentRequest(
                amountCents = request.amountCents,
                currency = request.currency,
                description = request.description,
            ),
        )
        return PaymentResult(
            status = response.status,
            reference = response.relayReference,
            message = response.detail,
            adapterId = adapterId,
        )
    }
}
