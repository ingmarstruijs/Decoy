package nl.decoy.fixtures

import nl.decoy.plugins.api.CapabilityPlugin
import nl.decoy.plugins.api.CompletionResult
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginContext
import nl.decoy.plugins.api.PluginUiState

/**
 * Deterministic hello plugin for emulator / CI.
 * Matches injected QR payload [FIXTURE_QR] or decoy://hello
 */
class HelloFixturePlugin : CapabilityPlugin {
    override val id: String = "fixture.hello"

    override fun matches(input: DiscoveryInput): Boolean = when (input) {
        is DiscoveryInput.Qr -> input.payload == FIXTURE_QR || input.payload.startsWith("decoy://hello")
        is DiscoveryInput.Url -> input.url.startsWith("decoy://hello")
        else -> false
    }

    override fun resolve(input: DiscoveryInput): PluginContext {
        val label = when (input) {
            is DiscoveryInput.Qr -> input.payload
            is DiscoveryInput.Url -> input.url
            else -> "unknown"
        }
        return PluginContext(
            contextId = "hello-1",
            input = input,
            attributes = mapOf("label" to label),
        )
    }

    override fun render(context: PluginContext): PluginUiState =
        PluginUiState.Screen(
            title = "Hello Decoy",
            body = "Fixture plugin resolved.\nInput: ${context.attributes["label"]}\n" +
                "No WebView. Stubs only.",
            primaryActionLabel = "Complete session",
        )

    override fun complete(context: PluginContext, result: CompletionResult) {
        // no-op for fixture
    }

    override fun dispose(context: PluginContext) {
        // no-op for fixture
    }

    companion object {
        const val FIXTURE_QR = "decoy://hello?fixture=1"
    }
}

object FixtureCatalog {
    val injectedHelloQr = HelloFixturePlugin.FIXTURE_QR
    val injectedCampsiteQr = CampsiteOrderPlugin.FIXTURE_QR
    val injectedCampsiteNfc = CampsiteOrderPlugin.FIXTURE_NFC
    val injectedTerraceQr = TerraceOrderPlugin.FIXTURE_QR
    val injectedStayQr = CampsiteStayPlugin.FIXTURE_QR
    val injectedStayNfc = CampsiteStayPlugin.FIXTURE_NFC
    val injectedParkingQr = ParkingSessionPlugin.FIXTURE_QR
    val injectedParkingNfc = ParkingSessionPlugin.FIXTURE_NFC
    val injectedPortalQr = CaptivePortalPlugin.FIXTURE_QR
    val injectedEvQr = EvChargingPlugin.FIXTURE_QR
    val injectedVendingQr = VendingPlugin.FIXTURE_QR
    val injectedCabinQr = ShortStayCabinPlugin.FIXTURE_QR
    val injectedFestivalQr = FestivalPassPlugin.FIXTURE_QR
    val injectedBleCampsiteOrder = AmbientFixtures.BLE_CAMPSITE_ORDER
    val injectedBleStay = AmbientFixtures.BLE_CAMPSITE_STAY
    val injectedBlePortal = AmbientFixtures.BLE_PORTAL
    val injectedBleCabin = AmbientFixtures.BLE_CABIN
    val injectedBleFestival = AmbientFixtures.BLE_FESTIVAL
    val injectedUwbCampsiteOrder = AmbientFixtures.UWB_CAMPSITE_ORDER
    val injectedButlarooQr = ButlarooOrderPlugin.FIXTURE_QR

    fun allPlugins() = listOf(
        HelloFixturePlugin(),
        CampsiteOrderPlugin(),
        TerraceOrderPlugin(),
        CampsiteStayPlugin(),
        ParkingSessionPlugin(),
        CaptivePortalPlugin(),
        EvChargingPlugin(),
        VendingPlugin(),
        ShortStayCabinPlugin(),
        FestivalPassPlugin(),
        ButlarooOrderPlugin(),
    )
}
