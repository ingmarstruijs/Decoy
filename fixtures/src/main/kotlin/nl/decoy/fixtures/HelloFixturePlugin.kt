package nl.decoy.fixtures

import nl.decoy.plugins.api.CapabilityPlugin
import nl.decoy.plugins.api.CompletionResult
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginContext
import nl.decoy.plugins.api.PluginUiState

/**
 * Deterministic hello plugin for emulator / CI.
 * Matches injected QR payload [FIXTURE_QR] or decoy://hello
 */
class HelloFixturePlugin : CapabilityPlugin {
    override val id: String = "fixture.hello"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr -> input.payload == FIXTURE_QR || input.payload.startsWith("decoy://hello")
        is DiscoveryInput.Url -> input.url.startsWith("decoy://hello")
        else -> false
    }

    override fun resolve(input: DiscoveryInput): PluginContext {
        val label = when (input) {
            is DiscoveryInput.Qr -> input.payload
            is DiscoveryInput.Url -> input.url
            else -> "unknown"
        }
        return PluginContext(
            contextId = "hello-1",
            input = input,
            attributes = mapOf("label" to label),
        )
    }

    override fun render(context: PluginContext): PluginUiState =
        PluginUiState.Screen(
            title = "Hello Decoy",
            body = "Fixture plugin resolved.\nInput: ${context.attributes["label"]}\n" +
                "No WebView. Stubs only.",
            primaryActionLabel = "Complete session",
        )

    override fun complete(context: PluginContext, result: CompletionResult) {
        // no-op for fixture
    }

    override fun dispose(context: PluginContext) {
        // no-op for fixture
    }

    companion object {
        const val FIXTURE_QR = "decoy://hello?fixture=1"
    }
}

object FixtureCatalog {
    val injectedHelloQr = HelloFixturePlugin.FIXTURE_QR
    val injectedCampsiteQr = CampsiteOrderPlugin.FIXTURE_QR
    val injectedTerraceQr = TerraceOrderPlugin.FIXTURE_QR

    fun allPlugins() = listOf(
        HelloFixturePlugin(),
        CampsiteOrderPlugin(),
        TerraceOrderPlugin(),
    )
}
