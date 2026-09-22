package pe.upeu.andinasalud.presentation.detalle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.andinasalud.domain.model.*
import pe.upeu.andinasalud.presentation.components.*

@Composable fun DetalleCitaScreen(cita: Cita, puedeCancelar: Boolean, accion: AccionUiState, cancelar: () -> Unit, reprogramar: () -> Unit) {
    var confirmar by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Detalle de cita", style = MaterialTheme.typography.headlineMedium)
        CitaCard(cita) {}
        Text("Motivo", style = MaterialTheme.typography.titleMedium)
        Text(cita.motivo)
        Text("Indicaciones", style = MaterialTheme.typography.titleMedium)
        Text((cita.estado as? EstadoCita.Atendida)?.indicaciones ?: cita.indicaciones)
        when(val estado = cita.estado) {
            is EstadoCita.Cancelada -> Text("Motivo de cancelación: ${estado.motivo}")
            is EstadoCita.Programada -> {
                Text(if(estado.recordatorioActivo) "Recordatorio activo" else "Recordatorio desactivado")
                OutlinedButton(onClick = reprogramar, enabled = !accion.procesando, modifier = Modifier.fillMaxWidth()) { Text("Reprogramar cita") }
                Button(onClick = { confirmar = true }, enabled = puedeCancelar && !accion.procesando, modifier = Modifier.fillMaxWidth()) { Text(if(accion.procesando) "Cancelando…" else "Cancelar cita") }
                Text("Puedes cancelar con más de 24 horas de anticipación.", style = MaterialTheme.typography.bodySmall)
            }
            is EstadoCita.Atendida -> Unit
        }
        if (cita.reprogramaciones.isNotEmpty()) {
            Text("Historial de reprogramaciones", style = MaterialTheme.typography.titleMedium)
            cita.reprogramaciones.forEach { Text("${it.momentoAnterior} → ${it.momentoNuevo}", style = MaterialTheme.typography.bodySmall) }
        }
        accion.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
    if(confirmar) AlertDialog(onDismissRequest = { confirmar = false }, title = { Text("¿Cancelar esta cita?") },
        text = { Text("Se cancelará tu cita de ${cita.medico.especialidad} del ${cita.fechaHora()}.") },
        confirmButton = { TextButton(onClick = { confirmar = false; cancelar() }) { Text("Sí, cancelar") } },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Conservar cita") } })
}

