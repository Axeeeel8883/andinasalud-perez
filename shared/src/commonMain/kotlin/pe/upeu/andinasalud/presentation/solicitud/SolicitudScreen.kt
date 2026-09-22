package pe.upeu.andinasalud.presentation.solicitud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.andinasalud.domain.model.*
import pe.upeu.andinasalud.presentation.components.Selector

@Composable fun SolicitudScreen(catalogo: Catalogo, estado: FormularioUiState, cambiar: (Solicitud) -> Unit, guardar: () -> Unit,
    titulo: String = "Solicitar cita", boton: String = "Confirmar solicitud") {
    val s = estado.solicitud
    val e = estado.errores
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(titulo, style = MaterialTheme.typography.headlineMedium)
        Text("Elige dónde y cuándo quieres atenderte.")
        Selector("Modalidad", s.modalidad.etiqueta, ModalidadAtencion.entries.map { it.etiqueta }, null, !estado.guardando) { etiqueta -> cambiar(s.copy(modalidad = ModalidadAtencion.entries.first { it.etiqueta == etiqueta })) }
        Selector("Especialidad", s.especialidad, catalogo.especialidades, e["especialidad"], !estado.guardando) { cambiar(s.copy(especialidad = it, medicoId = "")) }
        Selector("Sede", s.sede, catalogo.sedes.map { it.nombre }, e["sede"], !estado.guardando) { cambiar(s.copy(sede = it, medicoId = "")) }
        val medicos = catalogo.medicos.filter { it.especialidad == s.especialidad && s.sede in it.sedes }
        Selector("Médico", medicos.firstOrNull { it.id == s.medicoId }?.nombre ?: "", medicos.map { it.nombre }, e["medicoId"], !estado.guardando) { nombre -> cambiar(s.copy(medicoId = medicos.first { it.nombre == nombre }.id)) }
        OutlinedTextField(s.fecha, { cambiar(s.copy(fecha = it)) }, label = { Text("Fecha (AAAA-MM-DD)") }, placeholder = { Text("2026-10-15") }, isError = e["fecha"] != null, supportingText = { e["fecha"]?.let { Text(it) } }, enabled = !estado.guardando, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(s.hora, { cambiar(s.copy(hora = it)) }, label = { Text("Hora (HH:MM)") }, placeholder = { Text("09:30") }, isError = e["hora"] != null, supportingText = { e["hora"]?.let { Text(it) } }, enabled = !estado.guardando, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(s.motivo, { cambiar(s.copy(motivo = it)) }, label = { Text("Motivo de consulta") }, isError = e["motivo"] != null, supportingText = { Text(e["motivo"] ?: "${s.motivo.length}/200 · mínimo 10 caracteres") }, enabled = !estado.guardando, minLines = 3, modifier = Modifier.fillMaxWidth())
        e["general"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = guardar, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(if(estado.guardando) "Guardando…" else boton) }
    }
}

