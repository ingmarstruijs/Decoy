package nl.decoy.app

import android.app.Application
import nl.decoy.core.ContextResolver
import nl.decoy.core.order.OrderFlow
import nl.decoy.core.payment.FakePaymentAdapter
import nl.decoy.core.retention.InMemorySessionStore
import nl.decoy.core.retention.RetentionController
import nl.decoy.fixtures.FixtureCatalog
import nl.decoy.plugins.api.PluginRegistry

class DecoyApp : Application() {
    lateinit var resolver: ContextResolver
        private set
    lateinit var orderFlow: OrderFlow
        private set
    lateinit var sessionStore: InMemorySessionStore
        private set

    override fun onCreate() {
        super.onCreate()
        sessionStore = InMemorySessionStore()
        val registry = PluginRegistry(FixtureCatalog.allPlugins())
        val retention = RetentionController(sessionStore)
        resolver = ContextResolver(registry, retention)
        orderFlow = OrderFlow(
            payments = FakePaymentAdapter(),
            endSession = { session, ok -> resolver.complete(session, ok) },
        )
    }
}
