package pe.upeu.andinasalud.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import pe.upeu.andinasalud.domain.model.*
import pe.upeu.andinasalud.presentation.citas.UiState

fun Cita.fechaHora(): String {
    val local = momento.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.date} · ${local.time.toString().take(5)}"
}
@Composable fun CitaCard(cita: Cita, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(cita.medico.especialidad, style = MaterialTheme.typography.titleMedium)
            Text(cita.medico.nombre, style = MaterialTheme.typography.bodyLarge)
            Text("${cita.sede}  •  ${cita.fechaHora()}", style = MaterialTheme.typography.bodyMedium)
            Text(cita.estado.etiqueta, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}
@Composable fun Mensaje(titulo: String, texto: String, accion: String? = null, onAccion: () -> Unit = {}) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleLarge)
        Text(texto, style = MaterialTheme.typography.bodyMedium)
        if(accion != null) Button(onClick = onAccion) { Text(accion) }
    }
}
@Composable fun <T> EstadoContenido(estado: UiState<T>, reintentar: () -> Unit, contenido: @Composable (T) -> Unit) {
    when(estado) {
        UiState.Cargando -> Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(); Text("Cargando tu información…")
        }
        UiState.Vacio -> Mensaje("Sin información", "No hay datos disponibles para mostrar.", "Volver a cargar", reintentar)
        is UiState.Error -> Mensaje("No pudimos cargar", estado.mensaje, "Reintentar", reintentar)
        is UiState.Contenido -> contenido(estado.datos)
    }
}
@Composable fun Selector(etiqueta: String, valor: String, opciones: List<String>, error: String?, enabled: Boolean = true, elegir: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { abierto = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text(valor.ifBlank { "Seleccionar $etiqueta" }) }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                opciones.forEach { opcion -> DropdownMenuItem(text = { Text(opcion) }, onClick = { elegir(opcion); abierto = false }) }
                if(opciones.isEmpty()) DropdownMenuItem(text = { Text("Primero selecciona especialidad y sede") }, onClick = { abierto = false }, enabled = false)
            }
        }
        if(error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}
