package nl.decoy.fixtures

import nl.decoy.plugins.api.CapabilityPlugin
import nl.decoy.plugins.api.CompletionResult
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginContext
import nl.decoy.plugins.api.PluginUiState

/**
 * Multi-day festival / venue pass — local event-bounded credential.
 * Not a generic ticket import-and-show wallet.
 */
class FestivalPassPlugin : CapabilityPlugin {
    override val id: String = "fixture.festival.pass"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr ->
            input.payload == FIXTURE_QR || input.payload.startsWith("decoy://festival/")
        is DiscoveryInput.Url ->
            input.url.contains("festival") || input.url.contains("venue-pass")
        is DiscoveryInput.Nfc ->
            input.payload == FIXTURE_NFC || input.payload.startsWith("decoy://festival/")
        is DiscoveryInput.ZoneCode ->
            input.code.equals("PASS1", ignoreCase = true) ||
                input.code.equals("FX26", ignoreCase = true)
        is DiscoveryInput.Ble ->
            AmbientFixtures.matchesAmbient(input.advertisement, "festival.pass")
        is DiscoveryInput.Uwb ->
            AmbientFixtures.matchesAmbient(input.advertisement, "festival.pass")
    }

    override fun resolve(input: DiscoveryInput): PluginContext {
        val raw = ambientRaw(input)
        val event = Regex("event=(\\w+)").find(raw)?.groupValues?.getOrNull(1) ?: "FX26"
        return PluginContext(
            contextId = "pass-$event",
            input = input,
            attributes = mapOf(
                "event" to event,
                "stayId" to "pass-$event",
                "kind" to "festival-pass",
                "retention" to "multi-day",
                "vaultPayload" to """{"event":"$event","credential":"local-pass","topupCents":0}""",
            ),
        )
    }

    override fun render(context: PluginContext): PluginUiState {
        val event = context.attributes["event"] ?: "?"
        return PluginUiState.Screen(
            title = "Festival pass — $event",
            body = "Multi-day event pass stored locally for the festival window.\n" +
                "Re-entry / top-up without a permanent festival account. Auto-destroy after TTL.",
            primaryActionLabel = "Activate pass (save vault)",
        )
    }

    override fun complete(context: PluginContext, result: CompletionResult) = Unit

    override fun dispose(context: PluginContext) = Unit

    companion object {
        const val FIXTURE_QR = "decoy://festival/pass?event=FX26&fixture=1"
        const val FIXTURE_NFC = "decoy://festival/pass?event=FX26&nfc=1"

        private fun ambientRaw(input: DiscoveryInput): String = when (input) {
            is DiscoveryInput.Qr -> input.payload
            is DiscoveryInput.Url -> input.url
            is DiscoveryInput.Nfc -> input.payload
            is DiscoveryInput.ZoneCode -> "decoy://festival/pass?event=FX26"
            is DiscoveryInput.Ble -> input.advertisement
            is DiscoveryInput.Uwb -> input.advertisement
        }
    }
}
