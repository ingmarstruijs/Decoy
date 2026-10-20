package nl.decoy.core

import nl.decoy.core.retention.InMemorySessionStore
import nl.decoy.core.retention.RetentionController
import nl.decoy.fixtures.FixtureCatalog
import nl.decoy.plugins.api.DiscoveryInput
import nl.decoy.plugins.api.PluginRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionLifecycleTest {
    @Test
    fun walkAwayWipesOneShotSession() {
        val store = InMemorySessionStore()
        val retention = RetentionController(store)
        val resolver = ContextResolver(PluginRegistry(FixtureCatalog.allPlugins()), retention)
        val lifecycle = SessionLifecycle(retention)
        val session = resolver.resolve(DiscoveryInput.Qr(FixtureCatalog.injectedCampsiteQr))!!
        assertEquals(1, store.size())
        lifecycle.walkAway(session)
        assertEquals(0, store.size())
    }

    @Test
    fun dismissWipesSession() {
        val store = InMemorySessionStore()
        val retention = RetentionController(store)
        val resolver = ContextResolver(PluginRegistry(FixtureCatalog.allPlugins()), retention)
        val lifecycle = SessionLifecycle(retention)
        val session = resolver.resolve(DiscoveryInput.Nfc(FixtureCatalog.injectedCampsiteNfc))!!
        lifecycle.dismiss(session)
        assertEquals(0, store.size())
    }
}
