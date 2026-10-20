package nl.decoy.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DecoyColors = lightColorScheme(
    primary = Color(0xFF2F5D50),
    onPrimary = Color(0xFFF7F4EF),
    secondary = Color(0xFF4A6670),
    background = Color(0xFFF7F4EF),
    surface = Color(0xFFF7F4EF),
    onBackground = Color(0xFF1C1B19),
    onSurface = Color(0xFF1C1B19),
)

@Composable
fun DecoyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DecoyColors,
        content = content,
    )
}
