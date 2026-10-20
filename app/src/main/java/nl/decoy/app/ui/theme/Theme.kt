package nl.decoy.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nl.decoy.app.R

private val Ink = Color(0xFF0E1A22)
private val InkLift = Color(0xFF1A2F38)
private val Mist = Color(0xFFF2F6F7)
private val MistDim = Color(0xFFC5D0D5)
private val Copper = Color(0xFFE0B686)
private val CopperDeep = Color(0xFFB8895A)
private val Danger = Color(0xFFFFA59A)

val DecoyInk = Ink
val DecoyInkLift = InkLift
val DecoyCopper = Copper

private val Fraunces = FontFamily(Font(R.font.fraunces_variable, FontWeight.Normal))
private val SourceSans = FontFamily(Font(R.font.source_sans3_variable, FontWeight.Normal))

private val DecoyDarkColors = darkColorScheme(
    primary = Copper,
    onPrimary = Ink,
    secondary = MistDim,
    onSecondary = Ink,
    background = Ink,
    onBackground = Mist,
    surface = InkLift,
    onSurface = Mist,
    surfaceVariant = Color(0xFF243B45),
    onSurfaceVariant = MistDim,
    error = Danger,
    onError = Ink,
    outline = Color(0xFF6A8590),
)

private val DecoyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 56.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.5).sp,
        color = Mist,
    ),
    headlineMedium = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = Mist,
    ),
    titleLarge = TextStyle(
        fontFamily = Fraunces,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = Mist,
    ),
    titleMedium = TextStyle(
        fontFamily = SourceSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = Mist,
    ),
    bodyLarge = TextStyle(
        fontFamily = SourceSans,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        color = Mist,
    ),
    bodyMedium = TextStyle(
        fontFamily = SourceSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = MistDim,
    ),
    bodySmall = TextStyle(
        fontFamily = SourceSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = MistDim,
    ),
    labelLarge = TextStyle(
        fontFamily = SourceSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        letterSpacing = 0.3.sp,
        color = Mist,
    ),
    labelMedium = TextStyle(
        fontFamily = SourceSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp,
        color = MistDim,
    ),
)

@Composable
fun DecoyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DecoyDarkColors,
        typography = DecoyTypography,
        content = content,
    )
}
