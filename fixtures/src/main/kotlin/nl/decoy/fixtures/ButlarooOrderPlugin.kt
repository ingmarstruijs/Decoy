package nl.decoy.fixtures

import nl.decoy.fixtures.butlaroo.ButlarooBackend
import nl.decoy.fixtures.butlaroo.ButlarooCaptureNeededException
import nl.decoy.fixtures.butlaroo.ButlarooHosts
import nl.decoy.fixtures.butlaroo.FakeButlarooBackend
import nl.decoy.fixtures.butlaroo.LiveButlarooBackend
import nl.decoy.plugins.api.CapabilityPlugin
import nl.decoy.plugins.api.CompletionResult
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.FieldKind
import nl.decoy.plugins.api.FormField
import nl.decoy.plugins.api.PluginContext
import nl.decoy.plugins.api.PluginUiState

/**
 * Butlaroo UI overwrite: native Compose order UI + minimal backend.
 * Fixture / `decoy://butlaroo/` → [FakeButlarooBackend]. Real hosts → [LiveButlarooBackend]
 * (capture-needed Error until guest API paths are recorded).
 */
class ButlarooOrderPlugin(
    private val fakeBackend: ButlarooBackend = FakeButlarooBackend(),
    private val liveBackend: ButlarooBackend = LiveButlarooBackend(),
) : CapabilityPlugin {
    override val id: String = "fixture.butlaroo.order"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr ->
            input.payload == FIXTURE_QR ||
                input.payload.startsWith("decoy://butlaroo/") ||
                ButlarooHosts.looksLikeButlarooUrl(input.payload)
        is DiscoveryInput.Url ->
            ButlarooHosts.looksLikeButlarooUrl(input.url)
        is DiscoveryInput.Nfc ->
            input.payload.startsWith("decoy://butlaroo/") ||
                ButlarooHosts.looksLikeButlarooUrl(input.payload)
        is DiscoveryInput.ZoneCode ->
            input.code.equals("BUTLAROO", ignoreCase = true)
        else -> false
    }

    override fun resolve(input: DiscoveryInput): PluginContext {
        val raw = when (input) {
            is DiscoveryInput.Qr -> input.payload
            is DiscoveryInput.Url -> input.url
            is DiscoveryInput.Nfc -> input.payload
            is DiscoveryInput.ZoneCode -> FIXTURE_QR
            else -> FIXTURE_QR
        }
        val useFake = shouldUseFake(raw, input)
        val active = if (useFake) fakeBackend else liveBackend
        val table = active.resolveTable(raw)
        return PluginContext(
            contextId = "butlaroo-${table.tableId}",
            input = input,
            attributes = mapOf(
                "venue" to table.venueHint,
                "table" to table.tableId,
                "backend" to if (useFake) "fake" else "live",
                "sourceUrl" to (table.sourceUrl ?: raw),
            ),
        )
    }

    override fun render(context: PluginContext): PluginUiState {
        val tableId = context.attributes["table"] ?: "?"
        val mode = context.attributes["backend"] ?: "fake"
        val source = context.attributes["sourceUrl"] ?: FIXTURE_QR
        return try {
            val active = if (mode == "fake") fakeBackend else liveBackend
            val table = active.resolveTable(source)
            val menu = active.fetchMenu(table)
            PluginUiState.OrderForm(
                title = menu.title,
                subtitle = if (mode == "fake") {
                    "UI overwrite fixture — Decoy Compose, no Butlaroo WebView. Stubs only."
                } else {
                    "UI overwrite — native menu via allowlisted Butlaroo backend."
                },
                menu = menu.items,
                fields = listOf(
                    FormField(
                        id = "table",
                        label = "Table",
                        kind = FieldKind.Needed,
                        vendorRequestedValue = tableId,
                    ),
                    FormField(id = "email", label = "Email for receipt", kind = FieldKind.ContactAlias),
                    FormField(id = "iban", label = "IBAN", kind = FieldKind.PayElsewhere),
                    FormField(id = "create_account", label = "Create account", kind = FieldKind.Omit),
                    FormField(id = "marketing", label = "Marketing emails", kind = FieldKind.Omit),
                ),
            )
        } catch (e: ButlarooCaptureNeededException) {
            PluginUiState.Error(
                "Butlaroo venue recognized, but live menu is not captured yet.\n\n${e.message}",
            )
        }
    }

    override fun complete(context: PluginContext, result: CompletionResult) = Unit

    override fun dispose(context: PluginContext) = Unit

    private fun shouldUseFake(raw: String, input: DiscoveryInput): Boolean =
        input is DiscoveryInput.ZoneCode ||
            raw == FIXTURE_QR ||
            raw.startsWith("decoy://butlaroo/")

    companion object {
        const val FIXTURE_QR = "decoy://butlaroo/order?table=12&fixture=1"
        /** Example host shape for paste tests (no live menu until capture). */
        const val SAMPLE_LIVE_URL = "https://order.butlaroo.app/venue/demo?table=12"
    }
}
