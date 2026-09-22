package pe.upeu.andinasalud.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Claro = lightColorScheme(primary = Color(0xFF006B60), onPrimary = Color.White,
    primaryContainer = Color(0xFFB2F1DF), onPrimaryContainer = Color(0xFF00382F), secondary = Color(0xFF49655F),
    secondaryContainer = Color(0xFFCDE8DD), onSecondaryContainer = Color(0xFF12362B),
    background = Color(0xFFF5FAF7), surface = Color(0xFFF5FAF7), surfaceVariant = Color(0xFFDCEAE3),
    surfaceContainer = Color(0xFFE8F2EC), surfaceContainerHighest = Color(0xFFDCEAE3))
private val Oscuro = darkColorScheme(primary = Color(0xFF80D5C3), onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005047), onPrimaryContainer = Color(0xFFB2F1DF), secondary = Color(0xFFB0CCC3),
    secondaryContainer = Color(0xFF304D42), onSecondaryContainer = Color(0xFFCDE8DD),
    background = Color(0xFF101C19), surface = Color(0xFF101C19), surfaceVariant = Color(0xFF30443C),
    surfaceContainer = Color(0xFF1C2D25), surfaceContainerHighest = Color(0xFF30443C))
@Composable fun AndinaSaludTheme(oscuro: Boolean, contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if(oscuro) Oscuro else Claro, typography = Typography(), content = contenido)
}
