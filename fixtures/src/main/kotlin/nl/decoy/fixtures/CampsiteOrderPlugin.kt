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
 * Hero fixture: campsite sandwich order with surplus PII theater.
 */
class CampsiteOrderPlugin : CapabilityPlugin {
    override val id: String = "fixture.campsite.order"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr -> input.payload == FIXTURE_QR || input.payload.startsWith("decoy://campsite/order")
        is DiscoveryInput.Url -> input.url.contains("campsite") && input.url.contains("order")
        is DiscoveryInput.Nfc ->
            input.payload == FIXTURE_NFC || input.payload.startsWith("decoy://campsite/order")
        is DiscoveryInput.ZoneCode ->
            input.code.equals("CAMP42", ignoreCase = true)
        is DiscoveryInput.Ble ->
            AmbientFixtures.matchesAmbient(input.advertisement, "campsite.order")
        is DiscoveryInput.Uwb ->
            AmbientFixtures.matchesAmbient(input.advertisement, "campsite.order")
    }

    override fun resolve(input: DiscoveryInput): PluginContext = PluginContext(
        contextId = "campsite-order-1",
        input = input,
        attributes = mapOf(
            "venue" to "Fixture Campsite",
            "pitchDefault" to "42",
        ),
    )

    override fun render(context: PluginContext): PluginUiState = PluginUiState.OrderForm(
        title = "Campsite kitchen",
        subtitle = "Fixture venue — stubs only. Pitch kept; home/IBAN stripped.",
        menu = listOf(
            MenuItem(id = "sandwich", name = "Sandwich", priceCents = 650),
            MenuItem(id = "soup", name = "Tomato soup", priceCents = 450),
        ),
        fields = VENDOR_FORM_SCHEMA,
    )

    override fun complete(context: PluginContext, result: CompletionResult) = Unit

    override fun dispose(context: PluginContext) = Unit

    companion object {
        const val FIXTURE_QR = "decoy://campsite/order?fixture=1"
        const val FIXTURE_NFC = "decoy://campsite/order?nfc=1"

        /** Mirrors a hostile vendor form asking for everything. */
        val VENDOR_FORM_SCHEMA = listOf(
            FormField(id = "pitch", label = "Pitch number", kind = FieldKind.Needed, vendorRequestedValue = "42"),
            FormField(id = "notes", label = "Order notes", kind = FieldKind.Needed),
            FormField(id = "email", label = "Email", kind = FieldKind.ContactAlias),
            FormField(id = "phone", label = "Phone", kind = FieldKind.ContactAlias),
            FormField(id = "iban", label = "IBAN", kind = FieldKind.PayElsewhere, vendorRequestedValue = "NL00BANK0123456789"),
            FormField(id = "home_address", label = "Home address", kind = FieldKind.Omit, vendorRequestedValue = "Should never send"),
            FormField(id = "marketing_opt_in", label = "Marketing emails", kind = FieldKind.Omit),
        )
    }
}
