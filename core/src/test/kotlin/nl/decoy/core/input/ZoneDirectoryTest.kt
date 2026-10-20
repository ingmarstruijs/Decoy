package nl.decoy.core.input

import nl.decoy.core.ContextResolver
import nl.decoy.core.retention.InMemorySessionStore
import nl.decoy.core.retention.RetentionController
import nl.decoy.fixtures.FixtureCatalog
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginRegistry
import nl.decoy.plugins.api.PluginUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ZoneDirectoryTest {
    private val zones = ZoneDirectory()
    private val resolver = ContextResolver(
        PluginRegistry(FixtureCatalog.allPlugins()),
        RetentionController(InMemorySessionStore()),
    )

    @Test
    fun keypadCamp42ResolvesCampsiteOrder() {
        val input = zones.resolve("camp42")
        assertNotNull(input)
        val session = resolver.resolve(input!!)
        assertEquals("fixture.campsite.order", session!!.plugin.id)
    }

    @Test
    fun zoneCodeDirectlyMatchesPlugin() {
        val session = resolver.resolve(DiscoveryInput.ZoneCode("T7"))
        assertTrue(session!!.ui is PluginUiState.OrderForm)
        assertEquals("fixture.terrace.order", session.plugin.id)
    }

    @Test
    fun nfcStayResolvesMultiDayPlugin() {
        val session = resolver.resolve(DiscoveryInput.Nfc(FixtureCatalog.injectedStayNfc))
        assertEquals("fixture.campsite.stay", session!!.plugin.id)
        assertEquals("multi-day", session.context.attributes["retention"])
    }
}
