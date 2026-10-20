package nl.decoy.plugins.api

/**
 * Capability plugin contract:
 * discover match → resolve → render state → complete → dispose.
 */
interface CapabilityPlugin {
    val id: String

    /** Whether this plugin can handle the raw discovery input. */
    fun matches(input: DiscoveryInput): Boolean

    /** Build a typed context after a match (may parse IDs, zones, etc.). */
    fun resolve(input: DiscoveryInput): PluginContext

    /** Initial UI for the Compose shell. */
    fun render(context: PluginContext): PluginUiState

    /** Finish the flow (order placed, unlock done, …). */
    fun complete(context: PluginContext, result: CompletionResult)

    /** Release any plugin-local resources for this context. */
    fun dispose(context: PluginContext)
}
