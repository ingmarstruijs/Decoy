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
 * Terrace table-QR order fixture.
 */
class TerraceOrderPlugin : CapabilityPlugin {
    override val id: String = "fixture.terrace.order"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr -> input.payload == FIXTURE_QR || input.payload.startsWith("decoy://terrace/table")
        is DiscoveryInput.Url -> input.url.contains("terrace") && input.url.contains("table")
        is DiscoveryInput.Nfc -> input.payload.startsWith("decoy://terrace/table")
        is DiscoveryInput.ZoneCode -> input.code.equals("T7", ignoreCase = true)
        else -> false
    }

    override fun resolve(input: DiscoveryInput): PluginContext {
        val table = Regex("table[=/](\\w+)").find(
            when (input) {
                is DiscoveryInput.Qr -> input.payload
                is DiscoveryInput.Url -> input.url
                else -> ""
            },
        )?.groupValues?.getOrNull(1) ?: "7"
        return PluginContext(
            contextId = "terrace-table-$table",
            input = input,
            attributes = mapOf("table" to table),
        )
    }

    override fun render(context: PluginContext): PluginUiState {
        val table = context.attributes["table"] ?: "?"
        return PluginUiState.OrderForm(
            title = "Terrace — table $table",
            subtitle = "Fixture menu. Keep table id; no account / home address.",
            menu = listOf(
                MenuItem(id = "beer", name = "Beer", priceCents = 350),
                MenuItem(id = "bitterballen", name = "Bitterballen", priceCents = 700),
            ),
            fields = listOf(
                FormField(id = "table", label = "Table", kind = FieldKind.Needed, vendorRequestedValue = table),
                FormField(id = "email", label = "Email for receipt", kind = FieldKind.ContactAlias),
                FormField(id = "iban", label = "IBAN", kind = FieldKind.PayElsewhere),
                FormField(id = "create_account", label = "Create account", kind = FieldKind.Omit),
            ),
        )
    }

    override fun complete(context: PluginContext, result: CompletionResult) = Unit

    override fun dispose(context: PluginContext) = Unit

    companion object {
        const val FIXTURE_QR = "decoy://terrace/table=7?fixture=1"
    }
}
