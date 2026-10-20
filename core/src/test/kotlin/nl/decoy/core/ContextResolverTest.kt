package nl.decoy.core

import nl.decoy.core.retention.InMemorySessionStore
import nl.decoy.core.retention.RetentionController
import nl.decoy.fixtures.HelloFixturePlugin
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginRegistry
import nl.decoy.plugins.api.PluginUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextResolverTest {
    private fun resolver(): ContextResolver {
        val registry = PluginRegistry(listOf(HelloFixturePlugin()))
        return ContextResolver(registry, RetentionController(InMemorySessionStore()))
    }

    @Test
    fun injectedQrResolvesHelloPlugin() {
        val resolved = resolver().resolve(DiscoveryInput.Qr(HelloFixturePlugin.FIXTURE_QR))
        assertNotNull(resolved)
        assertEquals("fixture.hello", resolved!!.plugin.id)
        assertTrue(resolved.ui is PluginUiState.Screen)
        assertEquals("Hello Decoy", (resolved.ui as PluginUiState.Screen).title)
    }

    @Test
    fun unknownQrReturnsNull() {
        assertNull(resolver().resolve(DiscoveryInput.Qr("https://unknown.example/x")))
    }

    @Test
    fun completeWipesSession() {
        val store = InMemorySessionStore()
        val resolver = ContextResolver(
            PluginRegistry(listOf(HelloFixturePlugin())),
            RetentionController(store),
        )
        val resolved = resolver.resolve(DiscoveryInput.Qr(HelloFixturePlugin.FIXTURE_QR))!!
        assertEquals(1, store.size())
        resolver.complete(resolved, success = true)
        assertEquals(0, store.size())
    }
}
