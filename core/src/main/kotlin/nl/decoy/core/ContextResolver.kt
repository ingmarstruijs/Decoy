package nl.decoy.core

import nl.decoy.core.retention.RetentionController
import nl.decoy.core.retention.SessionRecord
import nl.decoy.plugins.api.CapabilityPlugin
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginContext
import nl.decoy.plugins.api.PluginRegistry
import nl.decoy.plugins.api.PluginUiState

data class ResolvedSession(
    val plugin: CapabilityPlugin,
    val context: PluginContext,
    val session: SessionRecord,
    val ui: PluginUiState,
)

/**
 * Maps discovery input → plugin → One-shot session → UI state.
 */
class ContextResolver(
    private val registry: PluginRegistry,
    private val retention: RetentionController,
) {
    fun resolve(input: DiscoveryInput): ResolvedSession? {
        val (plugin, context) = registry.resolve(input) ?: return null
        val session = retention.startOneShot(
            attributes = mapOf(
                "pluginId" to plugin.id,
                "contextId" to context.contextId,
            ),
        )
        val ui = plugin.render(context)
        return ResolvedSession(plugin, context, session, ui)
    }

    fun complete(resolved: ResolvedSession, success: Boolean) {
        resolved.plugin.complete(
            resolved.context,
            if (success) {
                nl.decoy.plugins.api.CompletionResult.Success
            } else {
                nl.decoy.plugins.api.CompletionResult.Cancelled
            },
        )
        resolved.plugin.dispose(resolved.context)
        retention.endSession(resolved.session.sessionId)
    }
}
