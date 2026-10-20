package nl.decoy.app.ui.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

data class DemoActions(
    val paymentBackendLabel: String,
    val onCyclePaymentBackend: () -> Unit,
    val onInjectHelloQr: () -> Unit,
    val onInjectCampsiteQr: () -> Unit,
    val onInjectTerraceQr: () -> Unit,
    val onInjectCampsiteNfc: () -> Unit,
    val onInjectStayNfc: () -> Unit,
    val onInjectStayQr: () -> Unit,
    val onInjectParkingQr: () -> Unit,
    val onInjectPortalQr: () -> Unit,
    val onInjectEvQr: () -> Unit,
    val onInjectVendingQr: () -> Unit,
    val onInjectCabinQr: () -> Unit,
    val onInjectFestivalQr: () -> Unit,
    val onInjectBleCampsite: () -> Unit,
    val onInjectBleStay: () -> Unit,
    val onInjectBlePortal: () -> Unit,
    val onInjectUwbCampsite: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoSheet(
    actions: DemoActions,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Demo fixtures", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Debug only. Inject discovery without camera or radio.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Payment: ${actions.paymentBackendLabel}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("payment_backend_label"),
            )
            OutlinedButton(
                onClick = actions.onCyclePaymentBackend,
                modifier = Modifier.fillMaxWidth().testTag("cycle_payment_backend"),
            ) {
                Text("Switch payment backend")
            }

            Section("One-shot")
            DemoBtn("Campsite order", "inject_campsite_qr", actions.onInjectCampsiteQr)
            DemoBtn("Terrace order", "inject_terrace_qr", actions.onInjectTerraceQr)
            DemoBtn("Parking", "inject_parking_qr", actions.onInjectParkingQr)
            DemoBtn("Captive portal", "inject_portal_qr", actions.onInjectPortalQr)
            DemoBtn("EV charging", "inject_ev_qr", actions.onInjectEvQr)
            DemoBtn("Vending", "inject_vending_qr", actions.onInjectVendingQr)

            Section("Multi-day")
            DemoBtn("Campsite stay", "inject_stay_qr", actions.onInjectStayQr)
            DemoBtn("Cabin stay", "inject_cabin_qr", actions.onInjectCabinQr)
            DemoBtn("Festival pass", "inject_festival_qr", actions.onInjectFestivalQr)

            Section("Ambient / NFC")
            DemoBtn("BLE campsite order", "inject_ble_campsite", actions.onInjectBleCampsite)
            DemoBtn("BLE stay", "inject_ble_stay", actions.onInjectBleStay)
            DemoBtn("BLE portal", "inject_ble_portal", actions.onInjectBlePortal)
            DemoBtn("UWB campsite", "inject_uwb_campsite", actions.onInjectUwbCampsite)
            DemoBtn("NFC campsite order", "inject_campsite_nfc", actions.onInjectCampsiteNfc)
            DemoBtn("NFC stay", "inject_stay_nfc", actions.onInjectStayNfc)
            OutlinedButton(
                onClick = actions.onInjectHelloQr,
                modifier = Modifier.fillMaxWidth().testTag("inject_hello_qr"),
            ) {
                Text("Hello smoke")
            }
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 12.dp),
    )
}

@Composable
private fun DemoBtn(label: String, tag: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
    ) {
        Text(label)
    }
}
