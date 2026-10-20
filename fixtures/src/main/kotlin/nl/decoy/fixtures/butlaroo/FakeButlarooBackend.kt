package nl.decoy.fixtures.butlaroo

import nl.decoy.plugins.api.MenuItem

/**
 * Emulator/CI twin — never hits Butlaroo hosts.
 */
class FakeButlarooBackend : ButlarooBackend {
    override fun resolveTable(rawPayload: String): ButlarooTableRef {
        val table = Regex("table[=/]?(\\w+)", RegexOption.IGNORE_CASE)
            .find(rawPayload)
            ?.groupValues
            ?.getOrNull(1)
            ?: "12"
        return ButlarooTableRef(
            venueHint = "Fixture Butlaroo venue",
            tableId = table,
            sourceUrl = rawPayload.takeIf { it.startsWith("http", ignoreCase = true) },
        )
    }

    override fun fetchMenu(table: ButlarooTableRef): ButlarooMenu = ButlarooMenu(
        title = "Butlaroo — table ${table.tableId}",
        items = listOf(
            MenuItem(id = "cola", name = "Cola", priceCents = 325),
            MenuItem(id = "fries", name = "Fries", priceCents = 450),
            MenuItem(id = "burger", name = "Burger", priceCents = 1250),
        ),
    )

    override fun submitOrder(request: ButlarooOrderRequest): ButlarooOrderResult =
        ButlarooOrderResult(
            accepted = true,
            ticketHint = "fake-${request.table.tableId}-${request.itemId}",
            message = "Fixture order accepted (no live kitchen)",
        )
}
