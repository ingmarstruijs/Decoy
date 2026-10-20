package nl.decoy.core.payment

enum class PaymentStatus {
    Success,
    Failed,
    Cancelled,
}

data class PaymentRequest(
    val amountCents: Int,
    val currency: String = "EUR",
    val description: String,
)

data class PaymentResult(
    val status: PaymentStatus,
    val reference: String? = null,
    val message: String? = null,
)

/**
 * Out-of-band payment. Never pastes IBAN into vendor forms.
 */
interface PaymentPort {
    fun pay(request: PaymentRequest): PaymentResult
}

/**
 * Deterministic fake for emulator / CI.
 */
class FakePaymentAdapter(
    private val mode: Mode = Mode.AlwaysSucceed,
) : PaymentPort {
    enum class Mode { AlwaysSucceed, AlwaysFail, AlwaysCancel }

    override fun pay(request: PaymentRequest): PaymentResult = when (mode) {
        Mode.AlwaysSucceed -> PaymentResult(
            status = PaymentStatus.Success,
            reference = "fake-${request.amountCents}",
            message = "Stub payment OK",
        )
        Mode.AlwaysFail -> PaymentResult(
            status = PaymentStatus.Failed,
            message = "Stub payment failed",
        )
        Mode.AlwaysCancel -> PaymentResult(
            status = PaymentStatus.Cancelled,
            message = "Stub payment cancelled",
        )
    }
}
