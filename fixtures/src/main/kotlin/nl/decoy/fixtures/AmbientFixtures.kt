package nl.decoy.fixtures

import nl.decoy.plugins.api.DiscoveryInput

/**
 * Injected ambient advertisements for emulator / CI.
 * Local-only — never sent as cloud geolocation telemetry.
 */
object AmbientFixtures {
    const val BLE_CAMPSITE_ORDER = "decoy|ble|campsite.order|fixture=1"
    const val BLE_CAMPSITE_STAY = "decoy|ble|campsite.stay|pitch=42|fixture=1"
    const val BLE_PARKING = "decoy|ble|parking|zone=4021|fixture=1"
    const val BLE_PORTAL = "decoy|ble|portal|ssid=Fixture-Guest-WiFi|fixture=1"
    const val BLE_CABIN = "decoy|ble|cabin.stay|unit=3|fixture=1"
    const val BLE_FESTIVAL = "decoy|ble|festival.pass|event=FX26|fixture=1"

    /** UWB uses the same decoy payload contract as BLE. */
    const val UWB_CAMPSITE_ORDER = "decoy|uwb|campsite.order|fixture=1"

    fun ble(ad: String = BLE_CAMPSITE_ORDER): DiscoveryInput.Ble = DiscoveryInput.Ble(ad)

    fun uwb(ad: String = UWB_CAMPSITE_ORDER): DiscoveryInput.Uwb = DiscoveryInput.Uwb(ad)

    fun matchesAmbient(raw: String, marker: String): Boolean =
        raw.contains(marker, ignoreCase = true)
}
