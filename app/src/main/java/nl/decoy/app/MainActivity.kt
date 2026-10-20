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
import nl.decoy.core.DisposeReason
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
                    var vaultStatus by remember { mutableStateOf<String?>(null) }

                    fun resolve(input: DiscoveryInput) {
                        session = app.resolver.resolve(input)
                        lastOutcome = null
                    }

                    DecoyAppScreen(
                        session = session,
                        lastOutcome = lastOutcome,
                        vaultStatus = vaultStatus,
                        onInjectHelloQr = {
                            resolve(DiscoveryInput.Qr(FixtureCatalog.injectedHelloQr))
                        },
                        onInjectCampsiteQr = {
                            resolve(DiscoveryInput.Qr(FixtureCatalog.injectedCampsiteQr))
                        },
                        onInjectTerraceQr = {
                            resolve(DiscoveryInput.Qr(FixtureCatalog.injectedTerraceQr))
                        },
                        onInjectCampsiteNfc = {
                            resolve(DiscoveryInput.Nfc(FixtureCatalog.injectedCampsiteNfc))
                        },
                        onInjectStayNfc = {
                            resolve(DiscoveryInput.Nfc(FixtureCatalog.injectedStayNfc))
                        },
                        onInjectStayQr = {
                            resolve(DiscoveryInput.Qr(FixtureCatalog.injectedStayQr))
                        },
                        onInjectParkingQr = {
                            resolve(DiscoveryInput.Qr(FixtureCatalog.injectedParkingQr))
                        },
                        onInjectPortalQr = {
                            resolve(DiscoveryInput.Qr(FixtureCatalog.injectedPortalQr))
                        },
                        onInjectEvQr = {
                            resolve(DiscoveryInput.Qr(FixtureCatalog.injectedEvQr))
                        },
                        onInjectVendingQr = {
                            resolve(DiscoveryInput.Qr(FixtureCatalog.injectedVendingQr))
                        },
                        onSubmitZone = { code ->
                            val mapped = app.zones.resolve(code)
                                ?: DiscoveryInput.ZoneCode(code.trim())
                            resolve(mapped)
                        },
                        onPlaceOrder = { item, contextual ->
                            val current = session ?: return@DecoyAppScreen
                            val outcome = app.orderFlow.placeOrder(current, item, contextual)
                            lastOutcome = outcome
                            if (outcome.completed) {
                                session = null
                            }
                        },
                        onStartTimed = { contextual ->
                            val current = session ?: return@DecoyAppScreen false
                            val outcome = app.timedFlow.start(current, contextual)
                            lastOutcome = OrderOutcome(
                                payment = outcome.payment,
                                submission = outcome.submission,
                                completed = false,
                            )
                            outcome.running
                        },
                        onStopTimed = {
                            val current = session ?: return@DecoyAppScreen
                            app.lifecycle.dispose(current, DisposeReason.Completed, success = true)
                            lastOutcome = lastOutcome?.copy(completed = true)
                            session = null
                        },
                        onConfirmStay = {
                            val current = session ?: return@DecoyAppScreen
                            val stayId = current.context.attributes["stayId"] ?: current.context.contextId
                            val pitch = current.context.attributes["pitch"] ?: "?"
                            app.multiDay.startStay(
                                id = stayId,
                                kind = "campsite",
                                payload = """{"pitch":"$pitch"}""",
                            )
                            vaultStatus = "Vault: stay $stayId pitch=$pitch (encrypted, Multi-day TTL)"
                            app.lifecycle.dispose(current, DisposeReason.Completed, success = true)
                            session = null
                        },
                        onCompleteSimple = {
                            val current = session
                            if (current != null) {
                                app.lifecycle.dispose(current, DisposeReason.Completed, success = true)
                                session = null
                            }
                        },
                        onWalkAway = {
                            val current = session
                            if (current != null) {
                                app.lifecycle.walkAway(current)
                                session = null
                            }
                        },
                        onDismiss = {
                            val current = session
                            if (current != null) {
                                app.lifecycle.dismiss(current)
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
        val qr = intent?.getStringExtra(EXTRA_QR_PAYLOAD)
        if (qr != null) return app.resolver.resolve(DiscoveryInput.Qr(qr))
        val nfc = intent?.getStringExtra(EXTRA_NFC_PAYLOAD)
        if (nfc != null) return app.resolver.resolve(DiscoveryInput.Nfc(nfc))
        val zone = intent?.getStringExtra(EXTRA_ZONE_CODE)
        if (zone != null) {
            val mapped = app.zones.resolve(zone) ?: DiscoveryInput.ZoneCode(zone)
            return app.resolver.resolve(mapped)
        }
        return null
    }

    companion object {
        const val EXTRA_QR_PAYLOAD = "nl.decoy.app.extra.QR_PAYLOAD"
        const val EXTRA_NFC_PAYLOAD = "nl.decoy.app.extra.NFC_PAYLOAD"
        const val EXTRA_ZONE_CODE = "nl.decoy.app.extra.ZONE_CODE"
    }
}
