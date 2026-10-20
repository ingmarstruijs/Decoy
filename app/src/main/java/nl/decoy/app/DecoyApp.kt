package nl.decoy.app

import android.app.Application
import nl.decoy.core.ContextResolver
import nl.decoy.core.SessionLifecycle
import nl.decoy.core.input.ZoneDirectory
import nl.decoy.core.order.OrderFlow
import nl.decoy.core.order.TimedSessionFlow
import nl.decoy.core.payment.FakePaymentAdapter
import nl.decoy.core.payment.PaymentPort
import nl.decoy.core.retention.AesGcmMultiDayVault
import nl.decoy.core.retention.InMemorySessionStore
import nl.decoy.core.retention.MultiDayService
import nl.decoy.core.retention.RetentionController
import nl.decoy.fixtures.FixtureCatalog
import nl.decoy.plugins.api.PluginRegistry

class DecoyApp : Application() {
    lateinit var resolver: ContextResolver
        private set
    lateinit var orderFlow: OrderFlow
        private set
    lateinit var timedFlow: TimedSessionFlow
        private set
    lateinit var sessionStore: InMemorySessionStore
        private set
    lateinit var lifecycle: SessionLifecycle
        private set
    lateinit var multiDay: MultiDayService
        private set
    lateinit var zones: ZoneDirectory
        private set

    override fun onCreate() {
        super.onCreate()
        sessionStore = InMemorySessionStore()
        val retention = RetentionController(sessionStore)
        resolver = ContextResolver(PluginRegistry(FixtureCatalog.allPlugins()), retention)
        lifecycle = SessionLifecycle(retention)
        val payments: PaymentPort = FakePaymentAdapter()
        orderFlow = OrderFlow(
            payments = payments,
            endSession = { session, ok ->
                if (ok) {
                    lifecycle.dispose(session, nl.decoy.core.DisposeReason.Completed, success = true)
                } else {
                    lifecycle.dismiss(session)
                }
            },
        )
        timedFlow = TimedSessionFlow(payments = payments)
        multiDay = MultiDayService(AesGcmMultiDayVault(AesGcmMultiDayVault.generateKey()))
        zones = ZoneDirectory()
    }
}
