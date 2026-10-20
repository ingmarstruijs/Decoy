package nl.decoy.fixtures

import nl.decoy.plugins.api.CapabilityPlugin
import nl.decoy.plugins.api.CompletionResult
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.FieldKind
import nl.decoy.plugins.api.FormField
import nl.decoy.plugins.api.PluginContext
import nl.decoy.plugins.api.PluginUiState

/**
 * Parking One-shot fixture — zone 4021 / QR / NFC.
 * Plate only with explicit consent; never fabricated.
 */
class ParkingSessionPlugin : CapabilityPlugin {
    override val id: String = "fixture.parking.session"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr ->
            input.payload == FIXTURE_QR ||
                input.payload.startsWith("decoy://parking/") ||
                input.payload.contains("zone=4021")
        is DiscoveryInput.Url ->
            input.url.contains("parking") || input.url.contains("zone=4021")
        is DiscoveryInput.Nfc ->
            input.payload == FIXTURE_NFC || input.payload.startsWith("decoy://parking/")
        is DiscoveryInput.ZoneCode ->
            input.code == "4021" || input.code.equals("PARK4021", ignoreCase = true)
        else -> false
    }

    override fun resolve(input: DiscoveryInput): PluginContext {
        val zone = when (input) {
            is DiscoveryInput.ZoneCode -> input.code.filter { it.isDigit() }.ifBlank { "4021" }
            else -> Regex("zone[=/]?(\\d+)").find(
                when (input) {
                    is DiscoveryInput.Qr -> input.payload
                    is DiscoveryInput.Url -> input.url
                    is DiscoveryInput.Nfc -> input.payload
                    else -> "4021"
                },
            )?.groupValues?.getOrNull(1) ?: "4021"
        }
        return PluginContext(
            contextId = "parking-$zone",
            input = input,
            attributes = mapOf(
                "zone" to zone,
                "tariff" to TARIFF_LABEL,
                "amountCents" to AMOUNT_CENTS.toString(),
            ),
        )
    }

    override fun render(context: PluginContext): PluginUiState {
        val zone = context.attributes["zone"] ?: "4021"
        return PluginUiState.TimedSession(
            title = "Parking — zone $zone",
            subtitle = "Fixture parking. Pay to start; stop when you leave. Plate only if you consent.",
            tariffLabel = TARIFF_LABEL,
            amountCents = AMOUNT_CENTS,
            fields = listOf(
                FormField(
                    id = "plate",
                    label = "License plate (optional)",
                    kind = FieldKind.ConsentRequired,
                ),
                FormField(
                    id = "email",
                    label = "Email receipt",
                    kind = FieldKind.ContactAlias,
                ),
                FormField(
                    id = "account_signup",
                    label = "Create parking account",
                    kind = FieldKind.Omit,
                ),
                FormField(
                    id = "iban",
                    label = "IBAN",
                    kind = FieldKind.PayElsewhere,
                ),
            ),
            startLabel = "Pay & start parking",
            stopLabel = "Stop parking & wipe",
            activeHint = "Parking session active (One-shot). Stop when you leave the zone.",
        )
    }

    override fun complete(context: PluginContext, result: CompletionResult) = Unit

    override fun dispose(context: PluginContext) = Unit

    companion object {
        const val FIXTURE_QR = "decoy://parking/zone=4021?fixture=1"
        const val FIXTURE_NFC = "decoy://parking/zone=4021?nfc=1"
        const val TARIFF_LABEL = "€2.50 / hour (fixture tariff)"
        const val AMOUNT_CENTS = 250
    }
}
