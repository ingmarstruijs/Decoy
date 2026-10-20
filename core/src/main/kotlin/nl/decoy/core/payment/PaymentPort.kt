package nl.decoy.core.payment

enum class PaymentStatus {
    Success,
    Failed,
    Cancelled,
}

/**
 * Which [PaymentPort] implementation Decoy uses.
 * CI and release default: [Fake]. Debug lab may switch to [StagingOpenBankingRelay].
 */
enum class PaymentBackend {
    /** Deterministic stub — CI default. */
    Fake,

    /**
     * Open-banking / Web Payments style relay with an in-process staging client.
     * No production PSP; simulates relay authorize → settle.
     */
    StagingOpenBankingRelay,
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
    /** Adapter id for UI / tests: `fake`, `open-banking-relay`. */
    val adapterId: String? = null,
)

/**
 * Out-of-band payment. Never pastes IBAN/card into vendor forms.
 */
interface PaymentPort {
    val adapterId: String

    fun pay(request: PaymentRequest): PaymentResult
}

/**
 * Deterministic fake for emulator / CI.
 */
class FakePaymentAdapter(
    private val mode: Mode = Mode.AlwaysSucceed,
) : PaymentPort {
    enum class Mode { AlwaysSucceed, AlwaysFail, AlwaysCancel }

    override val adapterId: String = "fake"

    override fun pay(request: PaymentRequest): PaymentResult = when (mode) {
        Mode.AlwaysSucceed -> PaymentResult(
            status = PaymentStatus.Success,
            reference = "fake-${request.amountCents}",
            message = "Stub payment OK",
            adapterId = adapterId,
        )
        Mode.AlwaysFail -> PaymentResult(
            status = PaymentStatus.Failed,
            message = "Stub payment failed",
            adapterId = adapterId,
        )
        Mode.AlwaysCancel -> PaymentResult(
            status = PaymentStatus.Cancelled,
            message = "Stub payment cancelled",
            adapterId = adapterId,
        )
    }
}

/**
 * Runtime switch so the debug lab can flip backends without rebuilding flows.
 * Default remains [FakePaymentAdapter] for CI.
 */
class SwitchingPaymentPort(
    initial: PaymentPort = FakePaymentAdapter(),
) : PaymentPort {
    @Volatile
    var delegate: PaymentPort = initial

    override val adapterId: String
        get() = delegate.adapterId

    override fun pay(request: PaymentRequest): PaymentResult = delegate.pay(request)

    fun use(backend: PaymentBackend) {
        delegate = PaymentBackends.create(backend)
    }
}

object PaymentBackends {
    fun create(backend: PaymentBackend): PaymentPort = when (backend) {
        PaymentBackend.Fake -> FakePaymentAdapter()
        PaymentBackend.StagingOpenBankingRelay ->
            OpenBankingRelayAdapter(relay = StagingOpenBankingRelayClient())
    }

    fun fromId(id: String?): PaymentBackend = when (id?.lowercase()) {
        "staging", "open-banking-relay", "relay" -> PaymentBackend.StagingOpenBankingRelay
        else -> PaymentBackend.Fake
    }
}
