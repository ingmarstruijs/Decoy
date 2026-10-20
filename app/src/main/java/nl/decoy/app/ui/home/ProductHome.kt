package nl.decoy.app.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.decoy.app.ui.theme.DecoyCopper
import nl.decoy.app.ui.theme.DecoyInk
import nl.decoy.app.ui.theme.DecoyInkLift
import nl.decoy.core.retention.HistoryEndReason
import nl.decoy.core.retention.HistoryEntry
import nl.decoy.core.retention.HistoryRetentionClass
import nl.decoy.core.retention.MultiDayRecord

@Composable
fun ProductHome(
    vaultRecords: List<MultiDayRecord>,
    historyRecords: List<HistoryEntry>,
    statusLine: String?,
    showDemoEntry: Boolean,
    onScan: () -> Unit,
    onZoneOrLink: () -> Unit,
    onOpenDemo: () -> Unit,
    onEndStay: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    val infinite = rememberInfiniteTransition(label = "scanPulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val fadeIn by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fade",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(DecoyInk, DecoyInkLift, DecoyInk)),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            Text(
                text = "Decoy",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .testTag("app_title")
                    .alpha(0.5f + fadeIn * 0.5f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "A context appears when you need it.\nIt leaves when you walk away.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("home_tagline"),
            )
            Spacer(Modifier.height(48.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulse)
                    .border(1.5.dp, DecoyCopper.copy(alpha = 0.65f), CircleShape)
                    .testTag("scan_ring"),
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(1.dp, DecoyCopper.copy(alpha = 0.35f), CircleShape),
                )
            }

            Spacer(Modifier.height(40.dp))
            Button(
                onClick = onScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scan_qr"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DecoyCopper,
                    contentColor = DecoyInk,
                ),
            ) {
                Text("Scan")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onZoneOrLink,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("zone_or_link"),
            ) {
                Text("Zone or link")
            }

            if (statusLine != null) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = statusLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("status_line"),
                )
            }

            if (vaultRecords.isNotEmpty()) {
                Spacer(Modifier.height(40.dp))
                Text(
                    text = "Active stays",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_section_title"),
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    vaultRecords.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vault_${record.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "${record.kind} · ${record.id}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                onClick = { onEndStay(record.id) },
                                modifier = Modifier.testTag("end_stay_${record.id}"),
                            ) {
                                Text("End")
                            }
                        }
                    }
                }
            }

            if (historyRecords.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Recent",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("history_section_title"),
                    )
                    TextButton(
                        onClick = onClearHistory,
                        modifier = Modifier.testTag("clear_history"),
                    ) {
                        Text("Clear")
                    }
                }
                Spacer(Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    historyRecords.forEach { entry ->
                        Text(
                            text = formatHistoryLine(entry),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("history_${entry.id}"),
                        )
                    }
                }
            }

            if (showDemoEntry) {
                Spacer(Modifier.height(48.dp))
                TextButton(
                    onClick = onOpenDemo,
                    modifier = Modifier.testTag("open_demo"),
                ) {
                    Text("Demo fixtures")
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun formatHistoryLine(entry: HistoryEntry): String {
    val retention = when (entry.retention) {
        HistoryRetentionClass.OneShot -> "one-shot"
        HistoryRetentionClass.MultiDay -> "stay"
    }
    val reason = when (entry.endReason) {
        HistoryEndReason.Completed -> "done"
        HistoryEndReason.Dismissed -> "dismissed"
        HistoryEndReason.Ended -> "ended"
    }
    return "${entry.kind} · ${entry.label} · $retention · $reason"
}
