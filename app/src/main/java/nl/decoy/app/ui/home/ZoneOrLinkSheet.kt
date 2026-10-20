package nl.decoy.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ZoneOrLinkSheet(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var value by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Zone or link", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Enter a painted zone code, or paste a URL / decoy link from a public QR.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CAMP42", "4021", "WIFI", "STAY42", "CABIN3", "PASS1").forEach { code ->
                    OutlinedButton(
                        onClick = { value = code },
                        modifier = Modifier.testTag("chip_$code"),
                    ) {
                        Text(code)
                    }
                }
            }
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Code or URL") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("zone_or_link_field"),
            )
            Button(
                onClick = { onSubmit(value) },
                enabled = value.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_zone_or_link"),
            ) {
                Text("Continue")
            }
        }
    }
}
