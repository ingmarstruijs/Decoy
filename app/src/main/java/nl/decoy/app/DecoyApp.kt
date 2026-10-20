package nl.decoy.app

import android.app.Application
import nl.decoy.core.ContextResolver
import nl.decoy.core.retention.InMemorySessionStore
import nl.decoy.core.retention.RetentionController
import nl.decoy.fixtures.HelloFixturePlugin
import nl.decoy.plugins.api.PluginRegistry

class DecoyApp : Application() {
    lateinit var resolver: ContextResolver
        private set

    override fun onCreate() {
        super.onCreate()
        val registry = PluginRegistry(listOf(HelloFixturePlugin()))
        val retention = RetentionController(InMemorySessionStore())
        resolver = ContextResolver(registry, retention)
    }
}
