package pe.upeu.andinasalud.presentation.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import pe.upeu.andinasalud.domain.model.Paciente

@Composable fun PerfilScreen(paciente: Paciente, oscuro: Boolean, tema: (Boolean) -> Unit, ajustes: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Mi perfil", style = MaterialTheme.typography.headlineMedium)
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(paciente.nombre, style = MaterialTheme.typography.titleLarge)
            Text("Documento: ${paciente.documento}")
            Text(paciente.correo)
            Text("Teléfono: ${paciente.telefono}")
        } }
        Text("Preferencias", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Tema oscuro"); Switch(checked = oscuro, onCheckedChange = tema, modifier = Modifier.semantics { contentDescription = "Cambiar tema oscuro" }) }
        OutlinedButton(onClick = ajustes, modifier = Modifier.fillMaxWidth()) { Text("Ajustes y demostración") }
        Text("AndinaSalud · Prototipo académico\nLos datos se mantienen en memoria durante esta sesión.", style = MaterialTheme.typography.bodySmall)
    }
}
