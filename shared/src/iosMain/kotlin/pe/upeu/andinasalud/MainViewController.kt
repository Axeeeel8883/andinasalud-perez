package pe.upeu.andinasalud

import androidx.compose.ui.window.ComposeUIViewController
import pe.upeu.andinasalud.di.iniciarKoin
import pe.upeu.andinasalud.presentation.navigation.AppNavHost

fun MainViewController() = ComposeUIViewController { iniciarKoin(); AppNavHost() }
