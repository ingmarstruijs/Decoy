package nl.decoy.fixtures.butlaroo

import nl.decoy.privacy.net.PrivacyHttpClient
import okhttp3.OkHttpClient
import java.net.URI

/**
 * Skeleton for real Butlaroo guest HTTP after a captured table QR session.
 * Until capture: resolve/allowlist only; menu/order fail loudly (no silent Unresolved).
 */
class LiveButlarooBackend(
    private val client: OkHttpClient = PrivacyHttpClient.create(),
) : ButlarooBackend {
    override fun resolveTable(rawPayload: String): ButlarooTableRef {
        val trimmed = rawPayload.trim()
        val host = try {
            URI(trimmed).host
        } catch (_: Exception) {
            null
        }
        if (host == null || !ButlarooHosts.isAllowedHost(host)) {
            throw ButlarooCaptureNeededException(
                "URL host is not an allowlisted Butlaroo host: ${host ?: "(none)"}",
            )
        }
        val table = Regex("table[=/]?(\\w+)", RegexOption.IGNORE_CASE)
            .find(trimmed)
            ?.groupValues
            ?.getOrNull(1)
            ?: "unknown"
        return ButlarooTableRef(
            venueHint = host,
            tableId = table,
            sourceUrl = trimmed,
        )
    }

    override fun fetchMenu(table: ButlarooTableRef): ButlarooMenu {
        // Touch client so the dependency stays wired; no production call until capture.
        check(client.connectTimeoutMillis > 0)
        throw ButlarooCaptureNeededException(
            "Live menu fetch not captured yet. Provide a real guest QR URL + optional HAR " +
                "(see docs/butlaroo-adapter-spike.md). Table=${table.tableId} host=${table.venueHint}",
        )
    }

    override fun submitOrder(request: ButlarooOrderRequest): ButlarooOrderResult {
        throw ButlarooCaptureNeededException(
            "Live order submit not captured yet. See docs/butlaroo-adapter-spike.md.",
        )
    }
}

class ButlarooCaptureNeededException(message: String) : Exception(message)
