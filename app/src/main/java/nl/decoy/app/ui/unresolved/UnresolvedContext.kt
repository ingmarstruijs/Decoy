package nl.decoy.app.ui.unresolved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import nl.decoy.app.ui.theme.DecoyInk
import nl.decoy.app.ui.theme.DecoyInkLift
import java.net.URI

@Composable
fun UnresolvedContextScreen(
    payload: String,
    onDismiss: () -> Unit,
) {
    val summary = summarizePayload(payload)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DecoyInk, DecoyInkLift)))
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            text = "No Decoy capability yet",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.testTag("unresolved_title"),
        )
        Text(
            text = "This code was read on your device. Nothing was sent to the vendor.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("unresolved_summary"),
        )
        Spacer(Modifier.weight(1f))
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("unresolved_dismiss"),
        ) {
            Text("Back")
        }
    }
}

internal fun summarizePayload(payload: String): String {
    val trimmed = payload.trim()
    return try {
        if (trimmed.startsWith("http", ignoreCase = true)) {
            val uri = URI(trimmed)
            val host = uri.host ?: "link"
            val path = uri.path?.take(48).orEmpty()
            "$host$path".take(80)
        } else {
            trimmed.take(80)
        }
    } catch (_: Exception) {
        trimmed.take(80)
    }
}
