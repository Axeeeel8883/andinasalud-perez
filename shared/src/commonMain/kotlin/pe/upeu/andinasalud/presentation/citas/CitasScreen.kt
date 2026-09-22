package pe.upeu.andinasalud.presentation.citas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.upeu.andinasalud.domain.model.Cita
import pe.upeu.andinasalud.presentation.components.*

@Composable fun CitasScreen(citas: List<Cita>, busqueda: String, filtro: String, soloHoy: Boolean,
    puedeSolicitar: Boolean, buscar: (String) -> Unit, filtrar: (String) -> Unit,
    filtrarHoy: (Boolean) -> Unit, detalle: (Long) -> Unit, solicitar: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Mis citas", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(value = busqueda, onValueChange = buscar, label = { Text("Especialidad o médico") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = soloHoy, onClick = { filtrarHoy(!soloHoy) }, label = { Text("Hoy") })
            listOf("Todas", "Programada", "Atendida", "Cancelada").forEach { FilterChip(selected = filtro == it, onClick = { filtrar(it) }, label = { Text(it) }) }
        }
        Button(onClick = solicitar, enabled = puedeSolicitar, modifier = Modifier.fillMaxWidth()) { Text(if (puedeSolicitar) "Solicitar cita" else "Límite de 3 citas alcanzado") }
        if(citas.isEmpty()) Mensaje("No hay citas", "No encontramos citas con estos filtros. Prueba otra búsqueda.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(citas, key = { it.id }) { CitaCard(it) { detalle(it.id) } }
        }
    }
}

