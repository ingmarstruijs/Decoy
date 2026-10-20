package nl.decoy.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import nl.decoy.app.ui.demo.DemoActions
import nl.decoy.app.ui.demo.DemoSheet
import nl.decoy.app.ui.home.ProductHome
import nl.decoy.app.ui.home.ZoneOrLinkSheet
import nl.decoy.app.ui.scan.QrScanScreen
import nl.decoy.app.ui.session.OrderSessionScreen
import nl.decoy.app.ui.session.SimpleSessionScreen
import nl.decoy.app.ui.session.TimedSessionScreen
import nl.decoy.app.ui.theme.DecoyInk
import nl.decoy.app.ui.theme.DecoyInkLift
import nl.decoy.app.ui.unresolved.UnresolvedContextScreen
import nl.decoy.core.ResolvedSession
import nl.decoy.core.retention.MultiDayRecord
import nl.decoy.plugins.api.MenuItem
import nl.decoy.plugins.api.PluginUiState

private enum class ShellRoute {
    Home,
    Scanning,
    Unresolved,
}

@Composable
fun DecoyAppScreen(
    session: ResolvedSession?,
    unresolvedPayload: String?,
    vaultRecords: List<MultiDayRecord>,
    statusLine: String?,
    showDemoEntry: Boolean,
    demoActions: DemoActions,
    onDiscover: (String) -> Unit,
    onClearUnresolved: () -> Unit,
    onPlaceOrder: (MenuItem, Map<String, String>) -> Unit,
    onStartTimed: (Map<String, String>) -> Boolean,
    onStopTimed: () -> Unit,
    onConfirmStay: () -> Unit,
    onCompleteSimple: () -> Unit,
    onDismiss: () -> Unit,
) {
    var route by remember { mutableStateOf(ShellRoute.Home) }
    var showZoneSheet by remember { mutableStateOf(false) }
    var showDemo by remember { mutableStateOf(false) }

    LaunchedEffect(unresolvedPayload, session) {
        when {
            session != null -> route = ShellRoute.Home
            unresolvedPayload != null -> route = ShellRoute.Unresolved
        }
    }

    fun closeDemoAnd(block: () -> Unit) {
        showDemo = false
        block()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DecoyInk, DecoyInkLift))),
    ) {
        when {
            session != null -> {
                when (val ui = session.ui) {
                    is PluginUiState.Screen -> SimpleSessionScreen(
                        session = session,
                        ui = ui,
                        onConfirmMultiDay = onConfirmStay,
                        onCompleteSimple = onCompleteSimple,
                        onDismiss = onDismiss,
                    )
                    is PluginUiState.OrderForm -> OrderSessionScreen(
                        session = session,
                        form = ui,
                        onPlaceOrder = onPlaceOrder,
                        onDismiss = onDismiss,
                    )
                    is PluginUiState.TimedSession -> TimedSessionScreen(
                        session = session,
                        timed = ui,
                        onStartTimed = onStartTimed,
                        onStopTimed = onStopTimed,
                        onDismiss = onDismiss,
                    )
                    is PluginUiState.Error -> {
                        Text(ui.message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onDismiss) { Text("Dismiss") }
                    }
                }
            }
            route == ShellRoute.Scanning -> {
                QrScanScreen(
                    onDecoded = {
                        route = ShellRoute.Home
                        onDiscover(it)
                    },
                    onCancel = { route = ShellRoute.Home },
                    onUsePasteInstead = {
                        route = ShellRoute.Home
                        showZoneSheet = true
                    },
                )
            }
            route == ShellRoute.Unresolved && unresolvedPayload != null -> {
                UnresolvedContextScreen(
                    payload = unresolvedPayload,
                    onDismiss = {
                        onClearUnresolved()
                        route = ShellRoute.Home
                    },
                )
            }
            else -> {
                ProductHome(
                    vaultRecords = vaultRecords,
                    statusLine = statusLine,
                    showDemoEntry = showDemoEntry,
                    onScan = { route = ShellRoute.Scanning },
                    onZoneOrLink = { showZoneSheet = true },
                    onOpenDemo = { showDemo = true },
                )
            }
        }

        if (showZoneSheet) {
            ZoneOrLinkSheet(
                onDismiss = { showZoneSheet = false },
                onSubmit = { value ->
                    showZoneSheet = false
                    onDiscover(value)
                },
            )
        }
        if (showDemo && showDemoEntry) {
            DemoSheet(
                actions = demoActions.copy(
                    onInjectHelloQr = { closeDemoAnd(demoActions.onInjectHelloQr) },
                    onInjectCampsiteQr = { closeDemoAnd(demoActions.onInjectCampsiteQr) },
                    onInjectTerraceQr = { closeDemoAnd(demoActions.onInjectTerraceQr) },
                    onInjectCampsiteNfc = { closeDemoAnd(demoActions.onInjectCampsiteNfc) },
                    onInjectStayNfc = { closeDemoAnd(demoActions.onInjectStayNfc) },
                    onInjectStayQr = { closeDemoAnd(demoActions.onInjectStayQr) },
                    onInjectParkingQr = { closeDemoAnd(demoActions.onInjectParkingQr) },
                    onInjectPortalQr = { closeDemoAnd(demoActions.onInjectPortalQr) },
                    onInjectEvQr = { closeDemoAnd(demoActions.onInjectEvQr) },
                    onInjectVendingQr = { closeDemoAnd(demoActions.onInjectVendingQr) },
                    onInjectCabinQr = { closeDemoAnd(demoActions.onInjectCabinQr) },
                    onInjectFestivalQr = { closeDemoAnd(demoActions.onInjectFestivalQr) },
                    onInjectBleCampsite = { closeDemoAnd(demoActions.onInjectBleCampsite) },
                    onInjectBleStay = { closeDemoAnd(demoActions.onInjectBleStay) },
                    onInjectBlePortal = { closeDemoAnd(demoActions.onInjectBlePortal) },
                    onInjectUwbCampsite = { closeDemoAnd(demoActions.onInjectUwbCampsite) },
                ),
                onDismiss = { showDemo = false },
            )
        }
    }
}
