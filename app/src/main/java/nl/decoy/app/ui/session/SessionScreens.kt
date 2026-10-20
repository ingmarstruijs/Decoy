package nl.decoy.app.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import nl.decoy.core.ResolvedSession
import nl.decoy.plugins.api.FieldKind
import nl.decoy.plugins.api.MenuItem
import nl.decoy.plugins.api.PluginUiState

@Composable
private fun sessionFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    cursorColor = MaterialTheme.colorScheme.primary,
)

@Composable
fun SimpleSessionScreen(
    session: ResolvedSession,
    ui: PluginUiState.Screen,
    onConfirmMultiDay: () -> Unit,
    onCompleteSimple: () -> Unit,
    onDismiss: () -> Unit,
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val isMultiDay = session.context.attributes["retention"] == "multi-day"
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            ui.title,
            style = MaterialTheme.typography.headlineMedium,
            color = onBg,
            modifier = Modifier.testTag("plugin_title"),
        )
        Text(
            ui.body,
            style = MaterialTheme.typography.bodyLarge,
            color = muted,
            modifier = Modifier.testTag("plugin_body"),
        )
        Text(
            text = "Session ${session.session.sessionId.take(8)}…",
            style = MaterialTheme.typography.labelMedium,
            color = muted,
        )
        Spacer(Modifier.height(8.dp))
        ui.primaryActionLabel?.let { label ->
            Button(
                onClick = if (isMultiDay) onConfirmMultiDay else onCompleteSimple,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(if (isMultiDay) "confirm_stay" else "complete_session"),
            ) {
                Text(label)
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().testTag("dismiss_session")) {
            Text("Dismiss", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun OrderSessionScreen(
    session: ResolvedSession,
    form: PluginUiState.OrderForm,
    onPlaceOrder: (MenuItem, Map<String, String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    var selected by remember { mutableStateOf(form.menu.firstOrNull()) }
    var pitch by remember {
        mutableStateOf(
            session.context.attributes["pitchDefault"]
                ?: session.context.attributes["pitch"]
                ?: session.context.attributes["table"]
                ?: "",
        )
    }
    var notes by remember { mutableStateOf("") }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            form.title,
            style = MaterialTheme.typography.headlineMedium,
            color = onBg,
            modifier = Modifier.testTag("plugin_title"),
        )
        Text(form.subtitle, style = MaterialTheme.typography.bodyMedium, color = muted)
        Text("Choose", style = MaterialTheme.typography.titleMedium, color = onBg)
        form.menu.forEach { item ->
            val mark = if (selected?.id == item.id) "✓ " else ""
            OutlinedButton(
                onClick = { selected = item },
                modifier = Modifier.fillMaxWidth().testTag("menu_${item.id}"),
            ) {
                Text("$mark${item.name} — €${"%.2f".format(item.priceCents / 100.0)}")
            }
        }
        Text("What Decoy sends", style = MaterialTheme.typography.titleMedium, color = onBg)
        form.fields.forEach { field ->
            when (field.kind) {
                FieldKind.Needed -> {
                    if (field.id == "pitch" || field.id == "table") {
                        OutlinedTextField(
                            value = pitch,
                            onValueChange = { pitch = it },
                            label = { Text(field.label) },
                            colors = sessionFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("field_${field.id}"),
                        )
                    } else if (field.id == "notes") {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(field.label) },
                            colors = sessionFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("field_notes"),
                        )
                    } else {
                        Text("• ${field.label}", style = MaterialTheme.typography.bodySmall, color = muted)
                    }
                }
                FieldKind.ContactAlias ->
                    Text(
                        "• ${field.label}: one-time alias",
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                    )
                FieldKind.PayElsewhere ->
                    Text(
                        "• ${field.label}: paid separately — never typed here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                FieldKind.Omit ->
                    Text(
                        "• ${field.label}: omitted",
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                    )
                FieldKind.ConsentRequired ->
                    Text(
                        "• ${field.label}: only with your consent",
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                    )
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
            modifier = Modifier.fillMaxWidth().testTag("place_order"),
        ) {
            Text(form.primaryActionLabel)
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().testTag("dismiss_session")) {
            Text("Dismiss", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun TimedSessionScreen(
    session: ResolvedSession,
    timed: PluginUiState.TimedSession,
    onStartTimed: (Map<String, String>) -> Boolean,
    onStopTimed: () -> Unit,
    onDismiss: () -> Unit,
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    var running by remember(session.session.sessionId) { mutableStateOf(false) }
    var plate by remember(session.session.sessionId) { mutableStateOf("") }
    var plateConsent by remember(session.session.sessionId) { mutableStateOf(false) }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            timed.title,
            style = MaterialTheme.typography.headlineMedium,
            color = onBg,
            modifier = Modifier.testTag("plugin_title"),
        )
        Text(timed.subtitle, style = MaterialTheme.typography.bodyMedium, color = muted)
        Text(
            text = "${timed.tariffLabel} · €${"%.2f".format(timed.amountCents / 100.0)}",
            style = MaterialTheme.typography.bodySmall,
            color = muted,
            modifier = Modifier.testTag("tariff_label"),
        )
        if (!running) {
            timed.fields.forEach { field ->
                when (field.kind) {
                    FieldKind.ConsentRequired -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = plateConsent,
                                onCheckedChange = { plateConsent = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = muted,
                                    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                modifier = Modifier.testTag("consent_${field.id}"),
                            )
                            Text(
                                "I consent to send my ${field.label.lowercase()}",
                                color = onBg,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        OutlinedTextField(
                            value = plate,
                            onValueChange = { plate = it },
                            enabled = plateConsent,
                            label = { Text(field.label) },
                            colors = sessionFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("field_${field.id}"),
                        )
                    }
                    FieldKind.ContactAlias ->
                        Text(
                            "• ${field.label}: one-time alias",
                            style = MaterialTheme.typography.bodySmall,
                            color = muted,
                        )
                    FieldKind.PayElsewhere ->
                        Text(
                            "• ${field.label}: paid separately",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    FieldKind.Omit ->
                        Text(
                            "• ${field.label}: omitted",
                            style = MaterialTheme.typography.bodySmall,
                            color = muted,
                        )
                    FieldKind.Needed ->
                        Text(
                            "• ${field.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = muted,
                        )
                }
            }
            Button(
                onClick = {
                    val contextual = buildMap {
                        if (plateConsent && plate.isNotBlank()) {
                            put("plate", plate)
                            put("consent_plate", "true")
                        }
                    }
                    if (onStartTimed(contextual)) running = true
                },
                modifier = Modifier.fillMaxWidth().testTag("start_timed"),
            ) {
                Text(timed.startLabel)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().testTag("dismiss_session")) {
                Text("Dismiss", color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Text(
                timed.activeHint,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("timed_active_hint"),
            )
            Button(
                onClick = onStopTimed,
                modifier = Modifier.fillMaxWidth().testTag("stop_timed"),
            ) {
                Text(timed.stopLabel)
            }
        }
    }
}
