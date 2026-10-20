package nl.decoy.core

import nl.decoy.core.retention.RetentionController
import nl.decoy.plugins.api.CompletionResult

enum class DisposeReason {
    Completed,
    Dismissed,
    SafetyTtl,
}

/**
 * Dispose active One-shot contexts on complete or dismiss.
 */
class SessionLifecycle(
    private val retention: RetentionController,
    private val onDisposed: (sessionId: String, reason: DisposeReason) -> Unit = { _, _ -> },
) {
    fun dispose(session: ResolvedSession, reason: DisposeReason, success: Boolean = false) {
        val result = when (reason) {
            DisposeReason.Completed ->
                if (success) CompletionResult.Success else CompletionResult.Failed
            DisposeReason.Dismissed, DisposeReason.SafetyTtl ->
                CompletionResult.Cancelled
        }
        session.plugin.complete(session.context, result)
        session.plugin.dispose(session.context)
        retention.endSession(session.session.sessionId)
        onDisposed(session.session.sessionId, reason)
    }

    fun dismiss(session: ResolvedSession) = dispose(session, DisposeReason.Dismissed)
}
