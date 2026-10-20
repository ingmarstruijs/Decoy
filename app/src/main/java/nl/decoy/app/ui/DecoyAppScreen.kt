package nl.decoy.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import nl.decoy.core.ResolvedSession
import nl.decoy.plugins.api.PluginUiState

@Composable
fun DecoyAppScreen(
    session: ResolvedSession?,
    onInjectHelloQr: () -> Unit,
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Decoy",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("app_title"),
        )
        Text(
            text = "Phase 0 — privacy shell + fixture plugin",
            style = MaterialTheme.typography.bodyMedium,
        )

        if (session == null) {
            Text(
                text = "No active session. Inject a fixture QR (emulator, no camera).",
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
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
                    Text(
                        text = "Session: ${session.session.sessionId.take(8)}…",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    val actionLabel = ui.primaryActionLabel
                    if (actionLabel != null) {
                        Button(
                            onClick = onComplete,
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
                is PluginUiState.Error -> {
                    Text(ui.message, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = onDismiss) { Text("Dismiss") }
                }
            }
        }
    }
}
