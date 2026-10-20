package nl.decoy.fixtures

import nl.decoy.plugins.api.CapabilityPlugin
import nl.decoy.plugins.api.CompletionResult
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginContext
import nl.decoy.plugins.api.PluginUiState

/**
 * Multi-day short-stay apartment / cabin fixture — local access context only.
 */
class ShortStayCabinPlugin : CapabilityPlugin {
    override val id: String = "fixture.cabin.stay"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr ->
            input.payload == FIXTURE_QR || input.payload.startsWith("decoy://cabin/")
        is DiscoveryInput.Url ->
            input.url.contains("cabin") || input.url.contains("short-stay")
        is DiscoveryInput.Nfc ->
            input.payload == FIXTURE_NFC || input.payload.startsWith("decoy://cabin/")
        is DiscoveryInput.ZoneCode ->
            input.code.equals("CABIN3", ignoreCase = true) ||
                input.code.equals("CABIN1", ignoreCase = true)
        is DiscoveryInput.Ble ->
            AmbientFixtures.matchesAmbient(input.advertisement, "cabin.stay")
        is DiscoveryInput.Uwb ->
            AmbientFixtures.matchesAmbient(input.advertisement, "cabin.stay")
    }

    override fun resolve(input: DiscoveryInput): PluginContext {
        val raw = ambientRaw(input)
        val unit = Regex("unit=(\\w+)").find(raw)?.groupValues?.getOrNull(1) ?: "3"
        return PluginContext(
            contextId = "cabin-$unit",
            input = input,
            attributes = mapOf(
                "unit" to unit,
                "stayId" to "cabin-$unit",
                "kind" to "cabin",
                "retention" to "multi-day",
                "vaultPayload" to """{"unit":"$unit","doorToken":"local-only"}""",
            ),
        )
    }

    override fun render(context: PluginContext): PluginUiState {
        val unit = context.attributes["unit"] ?: "?"
        return PluginUiState.Screen(
            title = "Cabin stay — unit $unit",
            body = "Multi-day short-stay. Door/access token stays in the encrypted vault.\n" +
                "No host surveillance app; wipe after stay TTL.",
            primaryActionLabel = "Confirm cabin stay (save vault)",
        )
    }

    override fun complete(context: PluginContext, result: CompletionResult) = Unit

    override fun dispose(context: PluginContext) = Unit

    companion object {
        const val FIXTURE_QR = "decoy://cabin/unit=3?fixture=1"
        const val FIXTURE_NFC = "decoy://cabin/unit=3?nfc=1"

        private fun ambientRaw(input: DiscoveryInput): String = when (input) {
            is DiscoveryInput.Qr -> input.payload
            is DiscoveryInput.Url -> input.url
            is DiscoveryInput.Nfc -> input.payload
            is DiscoveryInput.ZoneCode -> "decoy://cabin/unit=3"
            is DiscoveryInput.Ble -> input.advertisement
            is DiscoveryInput.Uwb -> input.advertisement
        }
    }
}
