package nl.decoy.fixtures.butlaroo

import nl.decoy.fixtures.ButlarooOrderPlugin
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ButlarooOrderPluginTest {
    private val plugin = ButlarooOrderPlugin()

    @Test
    fun fixtureQrResolvesFakeOrderForm() {
        assertTrue(plugin.matches(DiscoveryInput.Qr(ButlarooOrderPlugin.FIXTURE_QR)))
        val ctx = plugin.resolve(DiscoveryInput.Qr(ButlarooOrderPlugin.FIXTURE_QR))
        assertEquals("fake", ctx.attributes["backend"])
        assertEquals("12", ctx.attributes["table"])
        val ui = plugin.render(ctx)
        assertTrue(ui is PluginUiState.OrderForm)
        val form = ui as PluginUiState.OrderForm
        assertTrue(form.menu.isNotEmpty())
        assertTrue(form.subtitle.contains("UI overwrite", ignoreCase = true))
    }

    @Test
    fun liveHostMatchesButShowsCaptureNeededError() {
        val url = ButlarooOrderPlugin.SAMPLE_LIVE_URL
        assertTrue(plugin.matches(DiscoveryInput.Url(url)))
        val ctx = plugin.resolve(DiscoveryInput.Url(url))
        assertEquals("live", ctx.attributes["backend"])
        val ui = plugin.render(ctx)
        assertTrue(ui is PluginUiState.Error)
        assertTrue((ui as PluginUiState.Error).message.contains("not captured", ignoreCase = true))
    }

    @Test
    fun fakeBackendNeverNeedsNetwork() {
        val fake = FakeButlarooBackend()
        val table = fake.resolveTable(ButlarooOrderPlugin.FIXTURE_QR)
        val menu = fake.fetchMenu(table)
        assertEquals(3, menu.items.size)
        val result = fake.submitOrder(
            ButlarooOrderRequest(
                table = table,
                itemId = menu.items.first().id,
                itemName = menu.items.first().name,
                amountCents = menu.items.first().priceCents,
                contextual = mapOf("table" to table.tableId),
            ),
        )
        assertTrue(result.accepted)
    }

    @Test
    fun hostAllowlist() {
        assertTrue(ButlarooHosts.isAllowedHost("order.butlaroo.app"))
        assertTrue(ButlarooHosts.isAllowedHost("v2.api.butlaroo.com"))
        assertTrue(ButlarooHosts.looksLikeButlarooUrl("https://guest.butlaroo.com/t/1"))
        assertFalse(ButlarooHosts.isAllowedHost("evil.example.com"))
        assertFalse(ButlarooHosts.looksLikeButlarooUrl("https://example.com/butlaroo-phishing"))
    }
}
