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
 * Detects decoy portal URIs, zone codes, BLE beacons, and HTML marker pages.
 */
class CaptivePortalPlugin : CapabilityPlugin {
    override val id: String = "fixture.captive.portal"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr ->
            input.payload == FIXTURE_QR || input.payload.startsWith("decoy://portal/")
        is DiscoveryInput.Url ->
            input.url.contains("captive") ||
                input.url.contains("portal") ||
                input.url.startsWith("decoy://portal/") ||
                input.url.contains(HTML_MARKER)
        is DiscoveryInput.ZoneCode ->
            input.code.equals("WIFI", ignoreCase = true) ||
                input.code.equals("PORTAL", ignoreCase = true)
        is DiscoveryInput.Ble ->
            AmbientFixtures.matchesAmbient(input.advertisement, "portal")
        is DiscoveryInput.Uwb ->
            AmbientFixtures.matchesAmbient(input.advertisement, "portal")
        else -> false
    }

    override fun resolve(input: DiscoveryInput): PluginContext {
        val raw = when (input) {
            is DiscoveryInput.Ble -> input.advertisement
            is DiscoveryInput.Uwb -> input.advertisement
            is DiscoveryInput.Url -> input.url
            is DiscoveryInput.Qr -> input.payload
            else -> ""
        }
        val ssid = Regex("ssid=([^|&\\s]+)").find(raw)?.groupValues?.getOrNull(1)
            ?: "Fixture-Guest-WiFi"
        return PluginContext(
            contextId = "portal-1",
            input = input,
            attributes = mapOf(
                "ssid" to ssid,
                "portalDetect" to "local-markers",
                "trackers" to "stripped",
            ),
        )
    }

    override fun render(context: PluginContext): PluginUiState = PluginUiState.OrderForm(
        title = "Guest Wi-Fi",
        subtitle = "Captive portal (polished fixture). Detected locally — no vendor WebView, " +
            "no marketing dossier. Alias contact; omit account / home.",
        menu = listOf(
            MenuItem(id = "connect", name = "Connect to ${context.attributes["ssid"]}", priceCents = 0),
        ),
        fields = listOf(
            FormField(id = "email", label = "Email", kind = FieldKind.ContactAlias),
            FormField(id = "phone", label = "Phone", kind = FieldKind.ContactAlias),
            FormField(id = "marketing", label = "Send offers", kind = FieldKind.Omit),
            FormField(id = "create_account", label = "Create account", kind = FieldKind.Omit),
            FormField(id = "home_address", label = "Home address", kind = FieldKind.Omit),
            FormField(id = "newsletter", label = "Newsletter", kind = FieldKind.Omit),
        ),
        primaryActionLabel = "Accept & connect",
    )

    override fun complete(context: PluginContext, result: CompletionResult) = Unit

    override fun dispose(context: PluginContext) = Unit

    companion object {
        const val FIXTURE_QR = "decoy://portal/guest?fixture=1"
        /** Marker string as if scraped from captive portal HTML without loading trackers. */
        const val HTML_MARKER = "decoy-captive-fixture"
        const val FIXTURE_HTML_URL =
            "http://192.0.2.1/generate_204?decoy-captive-fixture=1"
    }
}
