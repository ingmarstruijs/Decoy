package nl.decoy.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import nl.decoy.core.ResolvedSession
import nl.decoy.core.order.OrderOutcome
import nl.decoy.plugins.api.FieldKind
import nl.decoy.plugins.api.MenuItem
import nl.decoy.plugins.api.PluginUiState

@Composable
fun DecoyAppScreen(
    session: ResolvedSession?,
    lastOutcome: OrderOutcome?,
    vaultStatus: String?,
    onInjectHelloQr: () -> Unit,
    onInjectCampsiteQr: () -> Unit,
    onInjectTerraceQr: () -> Unit,
    onInjectCampsiteNfc: () -> Unit,
    onInjectStayNfc: () -> Unit,
    onInjectStayQr: () -> Unit,
    onSubmitZone: (String) -> Unit,
    onPlaceOrder: (MenuItem, Map<String, String>) -> Unit,
    onConfirmStay: () -> Unit,
    onCompleteSimple: () -> Unit,
    onWalkAway: () -> Unit,
    onDismiss: () -> Unit,
) {
    var zoneCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Decoy",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("app_title"),
        )
        Text(
            text = "Phase 2 — NFC, keypad, Multi-day vault",
            style = MaterialTheme.typography.bodyMedium,
        )

        if (lastOutcome != null) {
            Text(
                text = "Last payment: ${lastOutcome.payment.status}" +
                    (lastOutcome.payment.message?.let { " — $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("last_payment"),
            )
        }
        if (vaultStatus != null) {
            Text(
                text = vaultStatus,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("vault_status"),
            )
        }

        if (session == null) {
            Text(
                text = "No active session. Use keypad, NFC inject, or QR fixtures.",
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedTextField(
                value = zoneCode,
                onValueChange = { zoneCode = it },
                label = { Text("Zone / short code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("zone_keypad"),
            )
            Button(
                onClick = { onSubmitZone(zoneCode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_zone"),
            ) {
                Text("Resolve zone (CAMP42 / STAY42 / T7 / 4021)")
            }
            Button(
                onClick = onInjectCampsiteNfc,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inject_campsite_nfc"),
            ) {
                Text("Inject campsite NFC")
            }
            Button(
                onClick = onInjectStayNfc,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inject_stay_nfc"),
            ) {
                Text("Inject stay NFC (Multi-day)")
            }
            Button(
                onClick = onInjectCampsiteQr,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inject_campsite_qr"),
            ) {
                Text("Inject campsite order QR")
            }
            Button(
                onClick = onInjectTerraceQr,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inject_terrace_qr"),
            ) {
                Text("Inject terrace table QR")
            }
            Button(
                onClick = onInjectStayQr,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inject_stay_qr"),
            ) {
                Text("Inject campsite stay QR")
            }
            OutlinedButton(
                onClick = onInjectHelloQr,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inject_hello_qr"),
            ) {
                Text("Inject hello fixture QR")
            }
        } else {
            when (val ui = session.ui) {
                is PluginUiState.Screen -> {
                    Text(
                        text = ui.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.testTag("plugin_title"),
                    )
                    Text(
                        text = ui.body,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag("plugin_body"),
                    )
                    SessionFooter(session.session.sessionId)
                    Spacer(Modifier.height(8.dp))
                    val isStay = session.context.attributes["retention"] == "multi-day"
                    val actionLabel = ui.primaryActionLabel
                    if (actionLabel != null) {
                        Button(
                            onClick = if (isStay) onConfirmStay else onCompleteSimple,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(if (isStay) "confirm_stay" else "complete_session"),
                        ) {
                            Text(actionLabel)
                        }
                    }
                    OutlinedButton(
                        onClick = onWalkAway,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("walk_away"),
                    ) {
                        Text("Walk away (wipe)")
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dismiss_session"),
                    ) {
                        Text("Dismiss / wipe")
                    }
                }
                is PluginUiState.OrderForm -> {
                    OrderFormContent(
                        sessionId = session.session.sessionId,
                        form = ui,
                        defaults = session.context.attributes,
                        onPlaceOrder = onPlaceOrder,
                        onWalkAway = onWalkAway,
                        onDismiss = onDismiss,
                    )
                }
                is PluginUiState.Error -> {
                    Text(ui.message, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onDismiss) { Text("Dismiss") }
                }
            }
        }
    }
}

@Composable
private fun OrderFormContent(
    sessionId: String,
    form: PluginUiState.OrderForm,
    defaults: Map<String, String>,
    onPlaceOrder: (MenuItem, Map<String, String>) -> Unit,
    onWalkAway: () -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(form.menu.firstOrNull()) }
    var pitch by remember {
        mutableStateOf(defaults["pitchDefault"] ?: defaults["pitch"] ?: defaults["table"] ?: "")
    }
    var notes by remember { mutableStateOf("") }

    Text(
        text = form.title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.testTag("plugin_title"),
    )
    Text(form.subtitle, style = MaterialTheme.typography.bodyMedium)
    SessionFooter(sessionId)

    Text("Menu", style = MaterialTheme.typography.titleMedium)
    form.menu.forEach { item ->
        val selectedMark = if (selected?.id == item.id) "✓ " else ""
        OutlinedButton(
            onClick = { selected = item },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("menu_${item.id}"),
        ) {
            Text("$selectedMark${item.name} — €${"%.2f".format(item.priceCents / 100.0)}")
        }
    }

    Text("Fields (policy)", style = MaterialTheme.typography.titleMedium)
    form.fields.forEach { field ->
        when (field.kind) {
            FieldKind.Needed -> {
                if (field.id == "pitch" || field.id == "table") {
                    OutlinedTextField(
                        value = pitch,
                        onValueChange = { pitch = it },
                        label = { Text(field.label) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("field_${field.id}"),
                    )
                } else if (field.id == "notes") {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(field.label) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("field_notes"),
                    )
                } else {
                    Text("• ${field.label}: (needed)", style = MaterialTheme.typography.bodySmall)
                }
            }
            FieldKind.ContactAlias -> {
                Text(
                    "• ${field.label}: → single-use alias (not your inbox)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("policy_alias_${field.id}"),
                )
            }
            FieldKind.PayElsewhere -> {
                Text(
                    "• ${field.label}: blocked — pay out-of-band (fake adapter)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("policy_pay_${field.id}"),
                )
            }
            FieldKind.Omit -> {
                Text(
                    "• ${field.label}: omitted",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("policy_omit_${field.id}"),
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    Button(
        onClick = {
            val item = selected ?: return@Button
            val contextual = buildMap {
                if (pitch.isNotBlank()) {
                    put("pitch", pitch)
                    put("table", pitch)
                }
                if (notes.isNotBlank()) put("notes", notes)
            }
            onPlaceOrder(item, contextual)
        },
        enabled = selected != null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("place_order"),
    ) {
        Text(form.primaryActionLabel)
    }
    OutlinedButton(
        onClick = onWalkAway,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("walk_away"),
    ) {
        Text("Walk away (wipe)")
    }
    OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dismiss_session"),
    ) {
        Text("Dismiss / wipe")
    }
}

@Composable
private fun SessionFooter(sessionId: String) {
    Text(
        text = "Session: ${sessionId.take(8)}…",
        style = MaterialTheme.typography.labelMedium,
    )
}
