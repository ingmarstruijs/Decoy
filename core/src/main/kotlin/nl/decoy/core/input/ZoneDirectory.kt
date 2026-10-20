package nl.decoy.core.input

import nl.decoy.plugins.api.DiscoveryInput

/**
 * Maps human zone / short codes (keypad) to a discovery payload plugins understand.
 */
class ZoneDirectory(
    private val mappings: Map<String, DiscoveryInput> = DEFAULT,
) {
    fun resolve(code: String): DiscoveryInput? {
        val normalized = code.trim().uppercase()
        if (normalized.isEmpty()) return null
        return mappings[normalized]
    }

    companion object {
        val DEFAULT = mapOf(
            "CAMP42" to DiscoveryInput.Qr("decoy://campsite/order?fixture=1&via=keypad"),
            "STAY42" to DiscoveryInput.Qr("decoy://campsite/stay?pitch=42&fixture=1&via=keypad"),
            "T7" to DiscoveryInput.Qr("decoy://terrace/table=7?fixture=1&via=keypad"),
            "4021" to DiscoveryInput.Qr("decoy://campsite/order?fixture=1&zone=4021"),
        )
    }
}
