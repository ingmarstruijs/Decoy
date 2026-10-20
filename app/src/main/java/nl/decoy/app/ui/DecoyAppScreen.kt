package nl.decoy.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
    onInjectParkingQr: () -> Unit,
    onInjectPortalQr: () -> Unit,
    onInjectEvQr: () -> Unit,
    onInjectVendingQr: () -> Unit,
    onSubmitZone: (String) -> Unit,
    onPlaceOrder: (MenuItem, Map<String, String>) -> Unit,
    onStartTimed: (Map<String, String>) -> Boolean,
    onStopTimed: () -> Unit,
    onConfirmStay: () -> Unit,
    onCompleteSimple: () -> Unit,
    onWalkAway: () -> Unit,
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

        if (session == null) {
            DebugHome(
                lastOutcome = lastOutcome,
                vaultStatus = vaultStatus,
                onInjectHelloQr = onInjectHelloQr,
                onInjectCampsiteQr = onInjectCampsiteQr,
                onInjectTerraceQr = onInjectTerraceQr,
                onInjectCampsiteNfc = onInjectCampsiteNfc,
                onInjectStayNfc = onInjectStayNfc,
                onInjectStayQr = onInjectStayQr,
                onInjectParkingQr = onInjectParkingQr,
                onInjectPortalQr = onInjectPortalQr,
                onInjectEvQr = onInjectEvQr,
                onInjectVendingQr = onInjectVendingQr,
                onSubmitZone = onSubmitZone,
            )
        } else {
            ActiveSessionBanner(session)
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
                    Text(
                        text = if (isStay) {
                            "Next: tap Confirm stay — pitch goes into the encrypted Multi-day vault, then this session ends."
                        } else {
                            "Next: tap the primary action, or Walk away to wipe without finishing."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
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
                        Text("Walk away — wipe session, do nothing else")
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dismiss_session"),
                    ) {
                        Text("Dismiss")
                    }
                }
                is PluginUiState.OrderForm -> {
                    Text(
                        text = "Next: pick a menu item → Pay & order. Watch field policy (alias / IBAN blocked / home omitted).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    OrderFormContent(
                        sessionId = session.session.sessionId,
                        form = ui,
                        defaults = session.context.attributes,
                        onPlaceOrder = onPlaceOrder,
                        onWalkAway = onWalkAway,
                        onDismiss = onDismiss,
                    )
                }
                is PluginUiState.TimedSession -> {
                    TimedSessionContent(
                        sessionId = session.session.sessionId,
                        timed = ui,
                        onStartTimed = onStartTimed,
                        onStopTimed = onStopTimed,
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
private fun DebugHome(
    lastOutcome: OrderOutcome?,
    vaultStatus: String?,
    onInjectHelloQr: () -> Unit,
    onInjectCampsiteQr: () -> Unit,
    onInjectTerraceQr: () -> Unit,
    onInjectCampsiteNfc: () -> Unit,
    onInjectStayNfc: () -> Unit,
    onInjectStayQr: () -> Unit,
    onInjectParkingQr: () -> Unit,
    onInjectPortalQr: () -> Unit,
    onInjectEvQr: () -> Unit,
    onInjectVendingQr: () -> Unit,
    onSubmitZone: (String) -> Unit,
) {
    var zoneCode by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Debug test lab", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "There is no real camera or NFC here. Each scenario below fakes a scan " +
                    "and opens the matching Decoy flow. Pick one story and follow the on-screen “Next” hint.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    if (lastOutcome != null || vaultStatus != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Last result", style = MaterialTheme.typography.titleSmall)
                if (lastOutcome != null) {
                    Text(
                        text = "Payment: ${lastOutcome.payment.status}" +
                            (lastOutcome.payment.message?.let { " — $it" } ?: "") +
                            if (lastOutcome.completed) " · session wiped" else " · session still open",
                        modifier = Modifier.testTag("last_payment"),
                    )
                    lastOutcome.submission?.let { sub ->
                        Text("Kept: ${sub.outbound.keys.joinToString()}")
                        Text("Aliased: ${sub.aliasedFieldIds.joinToString()}")
                        Text("Blocked pay fields: ${sub.payElsewhereFieldIds.joinToString()}")
                        Text("Omitted: ${sub.omittedFieldIds.joinToString()}")
                    }
                }
                if (vaultStatus != null) {
                    Text(vaultStatus, modifier = Modifier.testTag("vault_status"))
                }
            }
        }
    }

    Text("Scenarios", style = MaterialTheme.typography.titleMedium)

    ScenarioCard(
        title = "1. Campsite sandwich (hero)",
        goal = "Order food without giving home address or IBAN.",
        steps = "Simulates QR → order form → fake pay → session wipe.",
        buttonLabel = "Start campsite order",
        testTag = "inject_campsite_qr",
        onClick = onInjectCampsiteQr,
    )
    ScenarioCard(
        title = "2. Terrace table 7",
        goal = "Same idea at a terrace: keep table id, alias email, block IBAN.",
        steps = "Simulates table QR → menu → fake pay → wipe.",
        buttonLabel = "Start terrace order",
        testTag = "inject_terrace_qr",
        onClick = onInjectTerraceQr,
    )
    ScenarioCard(
        title = "3. Multi-day campsite stay",
        goal = "Save pitch in encrypted vault for several days.",
        steps = "Simulates stay QR → Confirm stay → vault holds pitch; One-shot UI session ends.",
        buttonLabel = "Start campsite stay",
        testTag = "inject_stay_qr",
        onClick = onInjectStayQr,
    )
    ScenarioCard(
        title = "4. Parking zone 4021",
        goal = "Timed One-shot: pay/start, optional plate with consent, stop to wipe.",
        steps = "Simulates parking QR → Pay & start → Stop parking & wipe.",
        buttonLabel = "Start parking session",
        testTag = "inject_parking_qr",
        onClick = onInjectParkingQr,
    )
    ScenarioCard(
        title = "5. Guest Wi-Fi (captive portal)",
        goal = "Connect without marketing dossier or forced account.",
        steps = "Simulates portal QR → Accept & connect → wipe.",
        buttonLabel = "Start captive portal",
        testTag = "inject_portal_qr",
        onClick = onInjectPortalQr,
    )
    ScenarioCard(
        title = "6. EV charging",
        goal = "Timed charge session without operator app account.",
        steps = "Simulates charger QR → Pay & start → Stop & wipe.",
        buttonLabel = "Start EV charging",
        testTag = "inject_ev_qr",
        onClick = onInjectEvQr,
    )
    ScenarioCard(
        title = "7. Vending / locker / laundry",
        goal = "Machine pay without loyalty or home address.",
        steps = "Simulates machine QR → pick item → Pay & dispense → wipe.",
        buttonLabel = "Start vending pay",
        testTag = "inject_vending_qr",
        onClick = onInjectVendingQr,
    )
    ScenarioCard(
        title = "8. NFC instead of QR",
        goal = "Same plugins, different input channel (still faked).",
        steps = "Use when you want to verify NFC discovery without hardware.",
        buttonLabel = "Fake NFC: campsite order",
        testTag = "inject_campsite_nfc",
        onClick = onInjectCampsiteNfc,
        secondaryLabel = "Fake NFC: campsite stay",
        secondaryTag = "inject_stay_nfc",
        onSecondary = onInjectStayNfc,
    )

    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("9. Zone keypad (no tag)", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "Type a short code as if it were painted on a pillar. Tap a chip to fill, then Resolve.",
                style = MaterialTheme.typography.bodySmall,
            )
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CAMP42", "STAY42", "T7", "4021", "WIFI", "EV1", "VEND1").forEach { code ->
                    OutlinedButton(
                        onClick = { zoneCode = code },
                        modifier = Modifier.testTag("chip_$code"),
                    ) {
                        Text(code)
                    }
                }
            }
            OutlinedTextField(
                value = zoneCode,
                onValueChange = { zoneCode = it },
                label = { Text("Zone code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("zone_keypad"),
            )
            Button(
                onClick = { onSubmitZone(zoneCode) },
                enabled = zoneCode.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_zone"),
            ) {
                Text("Resolve zone code")
            }
        }
    }

    TextButton(
        onClick = onInjectHelloQr,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inject_hello_qr"),
    ) {
        Text("Smoke test: Hello Decoy (Phase 0)")
    }
}

@Composable
private fun ScenarioCard(
    title: String,
    goal: String,
    steps: String,
    buttonLabel: String,
    testTag: String,
    onClick: () -> Unit,
    secondaryLabel: String? = null,
    secondaryTag: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Card(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text("Goal: $goal", style = MaterialTheme.typography.bodySmall)
            Text(steps, style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
            ) {
                Text(buttonLabel)
            }
            if (secondaryLabel != null && onSecondary != null && secondaryTag != null) {
                OutlinedButton(
                    onClick = onSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(secondaryTag),
                ) {
                    Text(secondaryLabel)
                }
            }
        }
    }
}

@Composable
private fun ActiveSessionBanner(session: ResolvedSession) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_session_banner"),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Active test session", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "Plugin: ${session.plugin.id}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "You are inside a faked scan. Finish the flow or Walk away to wipe.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun TimedSessionContent(
    sessionId: String,
    timed: PluginUiState.TimedSession,
    onStartTimed: (Map<String, String>) -> Boolean,
    onStopTimed: () -> Unit,
    onWalkAway: () -> Unit,
    onDismiss: () -> Unit,
) {
    var running by remember(sessionId) { mutableStateOf(false) }
    var plate by remember(sessionId) { mutableStateOf("") }
    var plateConsent by remember(sessionId) { mutableStateOf(false) }

    Text(
        text = timed.title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.testTag("plugin_title"),
    )
    Text(timed.subtitle, style = MaterialTheme.typography.bodyMedium)
    Text(
        text = "Tariff: ${timed.tariffLabel} · charge €${"%.2f".format(timed.amountCents / 100.0)}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag("tariff_label"),
    )
    SessionFooter(sessionId)

    if (!running) {
        Text(
            text = "Next: review field policy → ${timed.startLabel}. Session stays open until you stop.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text("Field policy", style = MaterialTheme.typography.titleMedium)
        timed.fields.forEach { field ->
            when (field.kind) {
                FieldKind.ConsentRequired -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = plateConsent,
                                onCheckedChange = { plateConsent = it },
                                modifier = Modifier.testTag("consent_${field.id}"),
                            )
                            Text(
                                "I consent to send my real ${field.label.lowercase()}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        OutlinedTextField(
                            value = plate,
                            onValueChange = { plate = it },
                            enabled = plateConsent,
                            label = { Text(field.label) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("field_${field.id}"),
                        )
                    }
                }
                FieldKind.ContactAlias -> {
                    Text(
                        "• ${field.label}: replaced with one-time alias",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag("policy_alias_${field.id}"),
                    )
                }
                FieldKind.PayElsewhere -> {
                    Text(
                        "• ${field.label}: NEVER typed here — fake payment runs separately",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("policy_pay_${field.id}"),
                    )
                }
                FieldKind.Omit -> {
                    Text(
                        "• ${field.label}: dropped (not sent)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag("policy_omit_${field.id}"),
                    )
                }
                FieldKind.Needed -> {
                    Text("• ${field.label}: kept if filled", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val contextual = buildMap {
                    if (plateConsent && plate.isNotBlank()) {
                        put("plate", plate)
                        put("consent_plate", "true")
                    }
                }
                if (onStartTimed(contextual)) {
                    running = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("start_timed"),
        ) {
            Text(timed.startLabel)
        }
        OutlinedButton(
            onClick = onWalkAway,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("walk_away"),
        ) {
            Text("Walk away — wipe without starting")
        }
        TextButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dismiss_session"),
        ) {
            Text("Dismiss")
        }
    } else {
        Text(
            text = timed.activeHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("timed_active_hint"),
        )
        Button(
            onClick = onStopTimed,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("stop_timed"),
        ) {
            Text(timed.stopLabel)
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

    Text("1. Choose menu item", style = MaterialTheme.typography.titleMedium)
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

    Text("2. Field policy (what Decoy does with vendor form fields)", style = MaterialTheme.typography.titleMedium)
    form.fields.forEach { field ->
        when (field.kind) {
            FieldKind.Needed -> {
                if (field.id == "pitch" || field.id == "table") {
                    OutlinedTextField(
                        value = pitch,
                        onValueChange = { pitch = it },
                        label = { Text("${field.label} (kept)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("field_${field.id}"),
                    )
                } else if (field.id == "notes") {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("${field.label} (kept)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("field_notes"),
                    )
                } else {
                    Text("• ${field.label}: kept if filled", style = MaterialTheme.typography.bodySmall)
                }
            }
            FieldKind.ContactAlias -> {
                Text(
                    "• ${field.label}: replaced with one-time alias (not your real inbox)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("policy_alias_${field.id}"),
                )
            }
            FieldKind.PayElsewhere -> {
                Text(
                    "• ${field.label}: NEVER typed here — fake payment runs separately",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("policy_pay_${field.id}"),
                )
            }
            FieldKind.Omit -> {
                Text(
                    "• ${field.label}: dropped (not sent)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("policy_omit_${field.id}"),
                )
            }
            FieldKind.ConsentRequired -> {
                Text(
                    "• ${field.label}: only if you explicitly consent",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("policy_consent_${field.id}"),
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    Text("3. Finish", style = MaterialTheme.typography.titleMedium)
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
        Text("Walk away — wipe without ordering")
    }
    TextButton(
        onClick = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dismiss_session"),
    ) {
        Text("Dismiss")
    }
}

@Composable
private fun SessionFooter(sessionId: String) {
    Text(
        text = "Session: ${sessionId.take(8)}…",
        style = MaterialTheme.typography.labelMedium,
    )
}
