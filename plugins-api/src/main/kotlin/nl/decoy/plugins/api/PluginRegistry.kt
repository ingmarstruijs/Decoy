package nl.decoy.plugins.api

class PluginRegistry(
    private val plugins: List<CapabilityPlugin>,
) {
    fun resolve(input: DiscoveryInput): Pair<CapabilityPlugin, PluginContext>? {
        val plugin = plugins.firstOrNull { it.matches(input) } ?: return null
        return plugin to plugin.resolve(input)
    }

    fun all(): List<CapabilityPlugin> = plugins
}
