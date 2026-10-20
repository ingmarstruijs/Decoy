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
 * Captive portal Wi-Fi fixture — connect without marketing dossier.
 * Uses OrderForm with a single "Connect" "product" and field policy.
 */
class CaptivePortalPlugin : CapabilityPlugin {
    override val id: String = "fixture.captive.portal"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr ->
            input.payload == FIXTURE_QR || input.payload.startsWith("decoy://portal/")
        is DiscoveryInput.Url ->
            input.url.contains("captive") || input.url.contains("portal") ||
                input.url.startsWith("decoy://portal/")
        is DiscoveryInput.ZoneCode ->
            input.code.equals("WIFI", ignoreCase = true) ||
                input.code.equals("PORTAL", ignoreCase = true)
        else -> false
    }

    override fun resolve(input: DiscoveryInput): PluginContext = PluginContext(
        contextId = "portal-1",
        input = input,
        attributes = mapOf("ssid" to "Fixture-Guest-WiFi"),
    )

    override fun render(context: PluginContext): PluginUiState = PluginUiState.OrderForm(
        title = "Guest Wi-Fi",
        subtitle = "Captive portal fixture. Alias contact; omit marketing / account.",
        menu = listOf(
            MenuItem(id = "connect", name = "Connect to ${context.attributes["ssid"]}", priceCents = 0),
        ),
        fields = listOf(
            FormField(id = "email", label = "Email", kind = FieldKind.ContactAlias),
            FormField(id = "marketing", label = "Send offers", kind = FieldKind.Omit),
            FormField(id = "create_account", label = "Create account", kind = FieldKind.Omit),
            FormField(id = "home_address", label = "Home address", kind = FieldKind.Omit),
        ),
        primaryActionLabel = "Accept & connect",
    )

    override fun complete(context: PluginContext, result: CompletionResult) = Unit

    override fun dispose(context: PluginContext) = Unit

    companion object {
        const val FIXTURE_QR = "decoy://portal/guest?fixture=1"
    }
}
