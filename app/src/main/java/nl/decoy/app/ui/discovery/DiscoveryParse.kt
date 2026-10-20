package nl.decoy.app.ui.discovery

import nl.decoy.core.input.ZoneDirectory
import nl.decoy.plugins.api.DiscoveryInput

/**
 * Parse user-entered or scanned text into a [DiscoveryInput].
 */
object DiscoveryParse {
    fun fromScannedOrPasted(raw: String, zones: ZoneDirectory): DiscoveryInput {
        val trimmed = raw.trim()
        require(trimmed.isNotEmpty()) { "empty payload" }
        return when {
            trimmed.startsWith("decoy://", ignoreCase = true) -> DiscoveryInput.Qr(trimmed)
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> DiscoveryInput.Url(trimmed)
            zones.resolve(trimmed) != null -> zones.resolve(trimmed)!!
            trimmed.length <= 24 && trimmed.all { it.isLetterOrDigit() || it == '-' || it == '_' } ->
                DiscoveryInput.ZoneCode(trimmed)
            else -> DiscoveryInput.Qr(trimmed)
        }
    }
}
