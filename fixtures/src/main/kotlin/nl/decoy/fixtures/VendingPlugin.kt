package nl.decoy.fixtures

import nl.decoy.plugins.api.CapabilityPlugin
import nl.decoy.plugins.api.CompletionResult
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.FieldKind
import nl.decoy.plugins.api.FormField
import nl.decoy.plugins.api.MenuItem
import nl.decoy.plugins.api.PluginContext
import nl.decoy.plugins.api.PluginUiState

/**
 * Vending / locker / laundry QR pay fixture.
 */
class VendingPlugin : CapabilityPlugin {
    override val id: String = "fixture.vending.pay"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr ->
            input.payload == FIXTURE_QR || input.payload.startsWith("decoy://vending/")
        is DiscoveryInput.Url ->
            input.url.contains("vending") || input.url.contains("locker") ||
                input.url.contains("laundry")
        is DiscoveryInput.Nfc ->
            input.payload.startsWith("decoy://vending/")
        is DiscoveryInput.ZoneCode ->
            input.code.equals("VEND1", ignoreCase = true)
        else -> false
    }

    override fun resolve(input: DiscoveryInput): PluginContext = PluginContext(
        contextId = "vending-1",
        input = input,
        attributes = mapOf("machine" to "Locker A3"),
    )

    override fun render(context: PluginContext): PluginUiState = PluginUiState.OrderForm(
        title = context.attributes["machine"] ?: "Vending",
        subtitle = "Fixture machine pay. No account, no home address.",
        menu = listOf(
            MenuItem(id = "cycle30", name = "Laundry 30 min", priceCents = 400),
            MenuItem(id = "locker", name = "Open locker A3", priceCents = 150),
            MenuItem(id = "snack", name = "Snack", priceCents = 200),
        ),
        fields = listOf(
            FormField(id = "loyalty", label = "Loyalty signup", kind = FieldKind.Omit),
            FormField(id = "home_address", label = "Home address", kind = FieldKind.Omit),
            FormField(id = "iban", label = "IBAN", kind = FieldKind.PayElsewhere),
        ),
        primaryActionLabel = "Pay & dispense",
    )

    override fun complete(context: PluginContext, result: CompletionResult) = Unit

    override fun dispose(context: PluginContext) = Unit

    companion object {
        const val FIXTURE_QR = "decoy://vending/locker=A3?fixture=1"
    }
}
