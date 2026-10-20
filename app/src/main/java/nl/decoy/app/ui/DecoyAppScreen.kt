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
    onInjectHelloQr: () -> Unit,
    onInjectCampsiteQr: () -> Unit,
    onInjectTerraceQr: () -> Unit,
    onPlaceOrder: (MenuItem, Map<String, String>) -> Unit,
    onCompleteSimple: () -> Unit,
    onDismiss: () -> Unit,
) {
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
            text = "Phase 1 — hero order fixtures (stubs only)",
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

        if (session == null) {
            Text(
                text = "No active session. Inject a fixture QR (emulator, no camera).",
                style = MaterialTheme.typography.bodyLarge,
            )
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
                    val actionLabel = ui.primaryActionLabel
                    if (actionLabel != null) {
                        Button(
                            onClick = onCompleteSimple,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("complete_session"),
                        ) {
                            Text(actionLabel)
                        }
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
