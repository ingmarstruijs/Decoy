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
import nl.decoy.app.ui.demo.DemoActions
import nl.decoy.app.ui.discovery.DiscoveryParse
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
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) {
                    var session by remember { mutableStateOf(initial) }
                    var unresolved by remember { mutableStateOf<String?>(null) }
                    var lastOutcome by remember { mutableStateOf<OrderOutcome?>(null) }
                    var statusLine by remember { mutableStateOf<String?>(null) }
                    var paymentLabel by remember { mutableStateOf(app.paymentBackendLabel()) }
                    var vaultTick by remember { mutableStateOf(0) }

                    fun resolve(input: DiscoveryInput) {
                        val resolved = app.resolver.resolve(input)
                        if (resolved == null) {
                            unresolved = when (input) {
                                is DiscoveryInput.Qr -> input.payload
                                is DiscoveryInput.Url -> input.url
                                is DiscoveryInput.ZoneCode -> input.code
                                is DiscoveryInput.Nfc -> input.payload
                                is DiscoveryInput.Ble -> input.advertisement
                                is DiscoveryInput.Uwb -> input.advertisement
                            }
                            session = null
                        } else {
                            unresolved = null
                            session = resolved
                            lastOutcome = null
                        }
                    }

                    fun discover(raw: String) {
                        try {
                            resolve(DiscoveryParse.fromScannedOrPasted(raw, app.zones))
                        } catch (_: Exception) {
                            unresolved = raw
                            session = null
                        }
                    }

                    val vaultRecords = remember(vaultTick, session) {
                        app.multiDay.listActive()
                    }

                    val outcomeStatus = lastOutcome?.let { o ->
                        buildString {
                            append(o.payment.status)
                            o.payment.adapterId?.let { append(" via $it") }
                            if (o.completed) append(" · wiped")
                        }
                    }
                    val combinedStatus = listOfNotNull(statusLine, outcomeStatus).joinToString(" · ")
                        .ifBlank { null }

                    DecoyAppScreen(
                        session = session,
                        unresolvedPayload = unresolved,
                        vaultRecords = vaultRecords,
                        statusLine = combinedStatus,
                        showDemoEntry = BuildConfig.DEBUG,
                        demoActions = DemoActions(
                            paymentBackendLabel = paymentLabel,
                            onCyclePaymentBackend = {
                                app.cyclePaymentBackend()
                                paymentLabel = app.paymentBackendLabel()
                            },
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
                            onInjectCabinQr = {
                                resolve(DiscoveryInput.Qr(FixtureCatalog.injectedCabinQr))
                            },
                            onInjectFestivalQr = {
                                resolve(DiscoveryInput.Qr(FixtureCatalog.injectedFestivalQr))
                            },
                            onInjectBleCampsite = {
                                resolve(DiscoveryInput.Ble(FixtureCatalog.injectedBleCampsiteOrder))
                            },
                            onInjectBleStay = {
                                resolve(DiscoveryInput.Ble(FixtureCatalog.injectedBleStay))
                            },
                            onInjectBlePortal = {
                                resolve(DiscoveryInput.Ble(FixtureCatalog.injectedBlePortal))
                            },
                            onInjectUwbCampsite = {
                                resolve(DiscoveryInput.Uwb(FixtureCatalog.injectedUwbCampsiteOrder))
                            },
                        ),
                        onDiscover = { discover(it) },
                        onClearUnresolved = { unresolved = null },
                        onPlaceOrder = { item, contextual ->
                            val current = session ?: return@DecoyAppScreen
                            val outcome = app.orderFlow.placeOrder(current, item, contextual)
                            lastOutcome = outcome
                            statusLine = null
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
                            val kind = current.context.attributes["kind"] ?: "campsite"
                            val payload = current.context.attributes["vaultPayload"]
                                ?: """{"pitch":"${current.context.attributes["pitch"] ?: "?"}"}"""
                            app.multiDay.startStay(
                                id = stayId,
                                kind = kind,
                                payload = payload,
                            )
                            statusLine = "Saved $kind · $stayId"
                            vaultTick++
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
        intent?.getStringExtra(EXTRA_PAYMENT_BACKEND)?.let { app.setPaymentBackendId(it) }
        val qr = intent?.getStringExtra(EXTRA_QR_PAYLOAD)
        if (qr != null) return app.resolver.resolve(DiscoveryInput.Qr(qr))
        val nfc = intent?.getStringExtra(EXTRA_NFC_PAYLOAD)
        if (nfc != null) return app.resolver.resolve(DiscoveryInput.Nfc(nfc))
        val zone = intent?.getStringExtra(EXTRA_ZONE_CODE)
        if (zone != null) {
            val mapped = app.zones.resolve(zone) ?: DiscoveryInput.ZoneCode(zone)
            return app.resolver.resolve(mapped)
        }
        val ble = intent?.getStringExtra(EXTRA_BLE_AD)
        if (ble != null) return app.resolver.resolve(DiscoveryInput.Ble(ble))
        val uwb = intent?.getStringExtra(EXTRA_UWB_AD)
        if (uwb != null) return app.resolver.resolve(DiscoveryInput.Uwb(uwb))
        return null
    }

    companion object {
        const val EXTRA_QR_PAYLOAD = "nl.decoy.app.extra.QR_PAYLOAD"
        const val EXTRA_NFC_PAYLOAD = "nl.decoy.app.extra.NFC_PAYLOAD"
        const val EXTRA_ZONE_CODE = "nl.decoy.app.extra.ZONE_CODE"
        const val EXTRA_PAYMENT_BACKEND = "nl.decoy.app.extra.PAYMENT_BACKEND"
        const val EXTRA_BLE_AD = "nl.decoy.app.extra.BLE_AD"
        const val EXTRA_UWB_AD = "nl.decoy.app.extra.UWB_AD"
    }
}
