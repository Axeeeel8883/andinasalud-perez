package pe.upeu.andinasalud.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler

@Composable actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) { BackHandler(enabled, onBack) }
