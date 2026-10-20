package nl.decoy.fixtures.butlaroo

import nl.decoy.plugins.api.MenuItem

/**
 * Minimal Butlaroo guest backend for UI overwrite (no vendor WebView).
 * CI uses [FakeButlarooBackend]; [LiveButlarooBackend] waits on a captured guest session.
 */
data class ButlarooTableRef(
    val venueHint: String,
    val tableId: String,
    val sourceUrl: String?,
)

data class ButlarooMenu(
    val title: String,
    val items: List<MenuItem>,
)

data class ButlarooOrderRequest(
    val table: ButlarooTableRef,
    val itemId: String,
    val itemName: String,
    val amountCents: Int,
    /** Contextual fields only — never IBAN/home/account. */
    val contextual: Map<String, String>,
)

data class ButlarooOrderResult(
    val accepted: Boolean,
    val ticketHint: String?,
    val message: String,
)

interface ButlarooBackend {
    fun resolveTable(rawPayload: String): ButlarooTableRef

    fun fetchMenu(table: ButlarooTableRef): ButlarooMenu

    fun submitOrder(request: ButlarooOrderRequest): ButlarooOrderResult
}

object ButlarooHosts {
    /** Hosts Decoy may contact for Butlaroo UI overwrite (privacy-net allowlist). */
    val ALLOWED_SUFFIXES = listOf(
        "butlaroo.com",
        "butlaroo.app",
    )

    fun isAllowedHost(host: String): Boolean {
        val h = host.lowercase().trimEnd('.')
        return ALLOWED_SUFFIXES.any { suffix ->
            h == suffix || h.endsWith(".$suffix")
        }
    }

    fun looksLikeButlarooUrl(raw: String): Boolean {
        val trimmed = raw.trim()
        try {
            val host = java.net.URI(trimmed).host
            if (host != null) return isAllowedHost(host)
        } catch (_: Exception) {
            // fall through for non-URI payloads
        }
        val lower = trimmed.lowercase()
        return ALLOWED_SUFFIXES.any { lower.contains(it) }
    }
}
