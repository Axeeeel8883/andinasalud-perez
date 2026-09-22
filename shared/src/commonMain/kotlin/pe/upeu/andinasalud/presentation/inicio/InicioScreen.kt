package pe.upeu.andinasalud.presentation.inicio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.andinasalud.domain.model.*
import pe.upeu.andinasalud.presentation.citas.Panel
import pe.upeu.andinasalud.presentation.components.*

@Composable fun InicioScreen(panel: Panel, siguiente: Cita?, detalle: (Long) -> Unit, citas: () -> Unit, solicitar: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("TU SALUD, MÁS CERCA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text("Hola, ${panel.catalogo.paciente.nombre.substringBefore(' ')}", style = MaterialTheme.typography.headlineLarge)
        Text("Estamos contigo en cada consulta.", style = MaterialTheme.typography.bodyLarge)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tu próxima cita", style = MaterialTheme.typography.titleLarge)
                if(siguiente != null) CitaCard(siguiente) { detalle(siguiente.id) }
                else Text("No tienes próximas citas programadas.")
            }
        }
        Text("¿Qué necesitas hoy?", style = MaterialTheme.typography.titleLarge)
        Button(onClick = solicitar, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Solicitar cita") }
        OutlinedButton(onClick = citas, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Ver mis citas") }
        Text("4 sedes · 5 especialidades", style = MaterialTheme.typography.titleMedium)
        Text("Ñaña · Chosica · Chaclacayo · Santa Anita", style = MaterialTheme.typography.bodyMedium)
    }
}
