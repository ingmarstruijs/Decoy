package nl.decoy.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import nl.decoy.app.ui.DecoyAppScreen
import nl.decoy.app.ui.theme.DecoyTheme
import nl.decoy.core.ResolvedSession
import nl.decoy.core.order.OrderOutcome
import nl.decoy.fixtures.FixtureCatalog
import nl.decoy.plugins.api.DiscoveryInput

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as DecoyApp
        val initial = resolveFromIntent(intent, app)

        setContent {
            DecoyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    var session by remember { mutableStateOf(initial) }
                    var lastOutcome by remember { mutableStateOf<OrderOutcome?>(null) }
                    DecoyAppScreen(
                        session = session,
                        lastOutcome = lastOutcome,
                        onInjectHelloQr = {
                            session = app.resolver.resolve(
                                DiscoveryInput.Qr(FixtureCatalog.injectedHelloQr),
                            )
                            lastOutcome = null
                        },
                        onInjectCampsiteQr = {
                            session = app.resolver.resolve(
                                DiscoveryInput.Qr(FixtureCatalog.injectedCampsiteQr),
                            )
                            lastOutcome = null
                        },
                        onInjectTerraceQr = {
                            session = app.resolver.resolve(
                                DiscoveryInput.Qr(FixtureCatalog.injectedTerraceQr),
                            )
                            lastOutcome = null
                        },
                        onPlaceOrder = { item, contextual ->
                            val current = session ?: return@DecoyAppScreen
                            val outcome = app.orderFlow.placeOrder(current, item, contextual)
                            lastOutcome = outcome
                            if (outcome.completed) {
                                session = null
                            }
                        },
                        onCompleteSimple = {
                            val current = session
                            if (current != null) {
                                app.resolver.complete(current, success = true)
                                session = null
                            }
                        },
                        onDismiss = {
                            val current = session
                            if (current != null) {
                                app.orderFlow.dismiss(current)
                                session = null
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recreate()
    }

    private fun resolveFromIntent(intent: Intent?, app: DecoyApp): ResolvedSession? {
        val payload = intent?.getStringExtra(EXTRA_QR_PAYLOAD) ?: return null
        return app.resolver.resolve(DiscoveryInput.Qr(payload))
    }

    companion object {
        const val EXTRA_QR_PAYLOAD = "nl.decoy.app.extra.QR_PAYLOAD"
    }
}
