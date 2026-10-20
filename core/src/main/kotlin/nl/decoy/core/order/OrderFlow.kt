package nl.decoy.core.order

import nl.decoy.core.ResolvedSession
import nl.decoy.core.identity.ClassifiedSubmission
import nl.decoy.core.identity.FieldClassifier
import nl.decoy.core.payment.PaymentPort
import nl.decoy.core.payment.PaymentRequest
import nl.decoy.core.payment.PaymentResult
import nl.decoy.core.payment.PaymentStatus
import nl.decoy.plugins.api.MenuItem
import nl.decoy.plugins.api.PluginUiState

data class OrderOutcome(
    val payment: PaymentResult,
    val submission: ClassifiedSubmission?,
    val completed: Boolean,
)

/**
 * Pay out-of-band, apply field policy, complete plugin, wipe One-shot session.
 */
class OrderFlow(
    private val classifier: FieldClassifier = FieldClassifier(),
    private val payments: PaymentPort,
    private val endSession: (ResolvedSession, Boolean) -> Unit,
) {
    fun placeOrder(
        session: ResolvedSession,
        selected: MenuItem,
        contextualValues: Map<String, String>,
    ): OrderOutcome {
        val form = session.ui as? PluginUiState.OrderForm
            ?: return OrderOutcome(
                payment = PaymentResult(
                    status = PaymentStatus.Failed,
                    message = "Not an order form session",
                ),
                submission = null,
                completed = false,
            )

        val submission = classifier.classify(form.fields, contextualValues)
        classifier.assertNoSecrets(submission.outbound)

        val payment = payments.pay(
            PaymentRequest(
                amountCents = selected.priceCents,
                description = "${form.title}: ${selected.name}",
            ),
        )

        return when (payment.status) {
            PaymentStatus.Success -> {
                endSession(session, true)
                OrderOutcome(payment, submission, completed = true)
            }
            PaymentStatus.Failed, PaymentStatus.Cancelled -> {
                OrderOutcome(payment, submission, completed = false)
            }
        }
    }

    fun dismiss(session: ResolvedSession) {
        endSession(session, false)
    }
}
