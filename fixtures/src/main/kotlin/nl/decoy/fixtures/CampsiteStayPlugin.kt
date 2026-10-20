package nl.decoy.fixtures

import nl.decoy.plugins.api.CapabilityPlugin
import nl.decoy.plugins.api.CompletionResult
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginContext
import nl.decoy.plugins.api.PluginUiState

/**
 * Multi-day campsite stay fixture — pitch stored in encrypted vault by the app shell.
 */
class CampsiteStayPlugin : CapabilityPlugin {
    override val id: String = "fixture.campsite.stay"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr ->
            input.payload == FIXTURE_QR || input.payload.startsWith("decoy://campsite/stay")
        is DiscoveryInput.Url ->
            input.url.contains("campsite") && input.url.contains("stay")
        is DiscoveryInput.Nfc ->
            input.payload.startsWith("decoy://campsite/stay") || input.payload == FIXTURE_NFC
        is DiscoveryInput.ZoneCode ->
            input.code.equals("STAY42", ignoreCase = true)
        is DiscoveryInput.Ble ->
            AmbientFixtures.matchesAmbient(input.advertisement, "campsite.stay")
        is DiscoveryInput.Uwb ->
            AmbientFixtures.matchesAmbient(input.advertisement, "campsite.stay")
    }

    override fun resolve(input: DiscoveryInput): PluginContext {
        val raw = when (input) {
            is DiscoveryInput.Qr -> input.payload
            is DiscoveryInput.Url -> input.url
            is DiscoveryInput.Nfc -> input.payload
            is DiscoveryInput.ZoneCode -> "decoy://campsite/stay?pitch=42"
            is DiscoveryInput.Ble -> input.advertisement
            is DiscoveryInput.Uwb -> input.advertisement
        }
        val pitch = Regex("pitch=(\\w+)").find(raw)?.groupValues?.getOrNull(1) ?: "42"
        return PluginContext(
            contextId = "campsite-stay-$pitch",
            input = input,
            attributes = mapOf(
                "pitch" to pitch,
                "stayId" to "stay-$pitch",
                "kind" to "campsite",
                "retention" to "multi-day",
                "vaultPayload" to """{"pitch":"$pitch"}""",
            ),
        )
    }

    override fun render(context: PluginContext): PluginUiState {
        val pitch = context.attributes["pitch"] ?: "?"
        return PluginUiState.Screen(
            title = "Campsite stay — pitch $pitch",
            body = "Multi-day context. Pitch stored in encrypted vault with auto-destroy TTL.\n" +
                "Recurring orders can reuse this pitch without re-entering PII.",
            primaryActionLabel = "Confirm stay (save vault)",
        )
    }

    override fun complete(context: PluginContext, result: CompletionResult) = Unit

    override fun dispose(context: PluginContext) = Unit

    companion object {
        const val FIXTURE_QR = "decoy://campsite/stay?pitch=42&fixture=1"
        const val FIXTURE_NFC = "decoy://campsite/stay?pitch=42&nfc=1"
    }
}
