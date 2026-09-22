package pe.upeu.andinasalud.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
import org.koin.mp.KoinPlatform
import pe.upeu.andinasalud.domain.model.*
import pe.upeu.andinasalud.presentation.citas.*
import pe.upeu.andinasalud.presentation.components.*
import pe.upeu.andinasalud.presentation.detalle.*
import pe.upeu.andinasalud.presentation.inicio.InicioScreen
import pe.upeu.andinasalud.presentation.perfil.PerfilScreen
import pe.upeu.andinasalud.presentation.solicitud.*
import pe.upeu.andinasalud.presentation.theme.AndinaSaludTheme

object Destinos { const val INICIO = "inicio"; const val CITAS = "citas"; const val PERFIL = "perfil"; const val SOLICITUD = "solicitud"; const val AJUSTES = "ajustes"; const val DETALLE = "detalle/" }
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun AppNavHost() {
    val vm = viewModel { KoinPlatform.getKoin().get<CitasViewModel>() }
    val estado by vm.estado.collectAsState()
    val oscuro by vm.oscuro.collectAsState()
    val busqueda by vm.busqueda.collectAsState()
    val filtro by vm.filtro.collectAsState()
    val visibles by vm.visibles.collectAsState()
    var pila by rememberSaveable { mutableStateOf(listOf(Destinos.INICIO)) }
    var numeroFormulario by rememberSaveable { mutableStateOf(0) }
    val ruta = pila.last()
    val volver: () -> Unit = { if(pila.size > 1) pila = pila.dropLast(1) else if(ruta != Destinos.INICIO) pila = listOf(Destinos.INICIO) }
    val ir: (String) -> Unit = { if(it != ruta) pila = pila + it }
    val solicitar: () -> Unit = { numeroFormulario++; ir(Destinos.SOLICITUD) }
    SystemBackHandler(pila.size > 1 || ruta != Destinos.INICIO, volver)
    AndinaSaludTheme(oscuro) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("AndinaSalud") }, navigationIcon = {
                if(pila.size > 1) IconButton(onClick = volver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
            }, actions = { IconButton(onClick = { ir(Destinos.AJUSTES) }) { Icon(Icons.Default.Settings, "Ajustes") } })
        }, bottomBar = {
            NavigationBar {
                listOf(Triple(Destinos.INICIO, "Inicio", Icons.Default.Home), Triple(Destinos.CITAS, "Citas", Icons.Default.DateRange), Triple(Destinos.PERFIL, "Perfil", Icons.Default.Person)).forEach { (destino, texto, icono) ->
                    NavigationBarItem(selected = pila.first() == destino, onClick = { pila = listOf(destino) }, icon = { Icon(icono, texto) }, label = { Text(texto) })
                }
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if(ruta == Destinos.AJUSTES) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Tema oscuro"); Switch(oscuro, vm::tema, modifier = Modifier.semantics { contentDescription = "Cambiar tema oscuro" }) }
                        Text("Demostración de estados", style = MaterialTheme.typography.titleLarge)
                        Text("Simula carga, contenido vacío y error sin alterar las citas guardadas. La carga dura 800 ms.")
                        listOf("Normal", "Vacío", "Error").forEach { opcion -> OutlinedButton(onClick = { vm.simular(opcion); volver() }, modifier = Modifier.fillMaxWidth()) { Text(opcion) } }
                    }
                } else EstadoContenido(estado, { vm.simular("Normal") }) { panel ->
                    when {
                        ruta == Destinos.INICIO -> InicioScreen(panel, panel.citas.filter { it.estado is EstadoCita.Programada && it.momento > Clock.System.now() }.minByOrNull { it.momento }, { ir(Destinos.DETALLE + it) }, { pila = listOf(Destinos.CITAS) }, solicitar)
                        ruta == Destinos.CITAS -> CitasScreen(visibles, busqueda, filtro, vm::buscar, vm::filtrar, { ir(Destinos.DETALLE + it) }, solicitar)
                        ruta == Destinos.PERFIL -> PerfilScreen(panel.catalogo.paciente, oscuro, vm::tema) { ir(Destinos.AJUSTES) }
                        ruta == Destinos.SOLICITUD -> {
                            val formulario = viewModel(key = "solicitud-$numeroFormulario") { KoinPlatform.getKoin().get<SolicitudViewModel>() }
                            val form by formulario.estado.collectAsState()
                            LaunchedEffect(form.creada) { form.creada?.let { pila = pila.dropLast(1) + (Destinos.DETALLE + it) } }
                            SolicitudScreen(panel.catalogo, form, formulario::cambiar, formulario::guardar)
                        }
                        ruta.startsWith(Destinos.DETALLE) -> {
                            val id = ruta.removePrefix(Destinos.DETALLE).toLongOrNull()
                            val cita = panel.citas.firstOrNull { it.id == id }
                            val detalle = viewModel(key = "detalle-$id") { KoinPlatform.getKoin().get<DetalleCitaViewModel>() }
                            val accion by detalle.estado.collectAsState()
                            if(cita == null) Mensaje("Cita no encontrada", "Regresa a la lista para consultar tus citas.")
                            else DetalleCitaScreen(cita, vm.politica.puedeCancelar(cita), accion) { detalle.cancelar(cita.id) }
                        }
                    }
                }
            }
        }
    }
}
