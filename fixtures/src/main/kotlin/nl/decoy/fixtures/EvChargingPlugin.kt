package nl.decoy.fixtures

import nl.decoy.plugins.api.CapabilityPlugin
import nl.decoy.plugins.api.CompletionResult
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.FieldKind
import nl.decoy.plugins.api.FormField
import nl.decoy.plugins.api.PluginContext
import nl.decoy.plugins.api.PluginUiState

/**
 * EV charging start/stop fixture.
 */
class EvChargingPlugin : CapabilityPlugin {
    override val id: String = "fixture.ev.charging"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr ->
            input.payload == FIXTURE_QR || input.payload.startsWith("decoy://ev/")
        is DiscoveryInput.Url -> input.url.contains("ev") || input.url.contains("charge")
        is DiscoveryInput.Nfc ->
            input.payload == FIXTURE_NFC || input.payload.startsWith("decoy://ev/")
        is DiscoveryInput.ZoneCode ->
            input.code.equals("EV1", ignoreCase = true)
        else -> false
    }

    override fun resolve(input: DiscoveryInput): PluginContext = PluginContext(
        contextId = "ev-charger-1",
        input = input,
        attributes = mapOf(
            "chargerId" to "EV-1",
            "amountCents" to AMOUNT_CENTS.toString(),
        ),
    )

    override fun render(context: PluginContext): PluginUiState = PluginUiState.TimedSession(
        title = "EV charger ${context.attributes["chargerId"]}",
        subtitle = "Fixture charge session. Pay to start; stop when unplugged.",
        tariffLabel = "€0.45 / kWh (fixture pre-auth €5.00)",
        amountCents = AMOUNT_CENTS,
        fields = listOf(
            FormField(id = "email", label = "Receipt email", kind = FieldKind.ContactAlias),
            FormField(id = "operator_account", label = "Operator account", kind = FieldKind.Omit),
            FormField(id = "iban", label = "IBAN", kind = FieldKind.PayElsewhere),
        ),
        startLabel = "Pay & start charging",
        stopLabel = "Stop charging & wipe",
        activeHint = "Charging session active. Stop when done.",
    )

    override fun complete(context: PluginContext, result: CompletionResult) = Unit

    override fun dispose(context: PluginContext) = Unit

    companion object {
        const val FIXTURE_QR = "decoy://ev/charger=EV-1?fixture=1"
        const val FIXTURE_NFC = "decoy://ev/charger=EV-1?nfc=1"
        const val AMOUNT_CENTS = 500
    }
}
