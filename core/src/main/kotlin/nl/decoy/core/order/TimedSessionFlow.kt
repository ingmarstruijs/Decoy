package nl.decoy.core.order

import nl.decoy.core.ResolvedSession
import nl.decoy.core.identity.ClassifiedSubmission
import nl.decoy.core.identity.FieldClassifier
import nl.decoy.core.payment.PaymentPort
import nl.decoy.core.payment.PaymentRequest
import nl.decoy.core.payment.PaymentResult
import nl.decoy.core.payment.PaymentStatus
import nl.decoy.plugins.api.PluginUiState

data class TimedStartOutcome(
    val payment: PaymentResult,
    val submission: ClassifiedSubmission?,
    /** True when payment succeeded and session should stay active until stop. */
    val running: Boolean,
)

/**
 * Pay/start a TimedSession without wiping; caller wipes on stop.
 */
class TimedSessionFlow(
    private val classifier: FieldClassifier = FieldClassifier(),
    private val payments: PaymentPort,
) {
    fun start(
        session: ResolvedSession,
        contextualValues: Map<String, String>,
    ): TimedStartOutcome {
        val ui = session.ui as? PluginUiState.TimedSession
            ?: return TimedStartOutcome(
                payment = PaymentResult(PaymentStatus.Failed, message = "Not a timed session"),
                submission = null,
                running = false,
            )

        val submission = classifier.classify(ui.fields, contextualValues)
        classifier.assertNoSecrets(submission.outbound)

        val payment = payments.pay(
            PaymentRequest(
                amountCents = ui.amountCents,
                description = ui.title,
            ),
        )
        return TimedStartOutcome(
            payment = payment,
            submission = submission,
            running = payment.status == PaymentStatus.Success,
        )
    }
}
