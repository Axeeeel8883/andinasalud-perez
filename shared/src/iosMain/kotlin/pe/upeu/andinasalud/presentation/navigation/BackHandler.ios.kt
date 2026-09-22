package pe.upeu.andinasalud.presentation.navigation

import androidx.compose.runtime.Composable

// iOS vuelve mediante el botón de la barra superior; no dispone de botón Atrás del sistema.
@Composable actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
