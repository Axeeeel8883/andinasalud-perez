package pe.upeu.andinasalud.presentation.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import pe.upeu.andinasalud.domain.model.*
import pe.upeu.andinasalud.domain.repository.CitaRepository
import pe.upeu.andinasalud.domain.usecase.*

sealed interface UiState<out T> {
    data object Cargando : UiState<Nothing>
    data object Vacio : UiState<Nothing>
    data class Contenido<T>(val datos: T) : UiState<T>
    data class Error(val mensaje: String) : UiState<Nothing>
}
data class Panel(val catalogo: Catalogo, val citas: List<Cita>)
class CitasViewModel(private val repository: CitaRepository, private val obtener: ObtenerCitasUseCase,
    private val catalogo: ObtenerCatalogoUseCase, val politica: PoliticaCitas) : ViewModel() {
    private val mutable = MutableStateFlow<UiState<Panel>>(UiState.Cargando)
    val estado = mutable.asStateFlow()
    private val mutableBusqueda = MutableStateFlow("")
    val busqueda = mutableBusqueda.asStateFlow()
    private val mutableFiltro = MutableStateFlow("Todas")
    val filtro = mutableFiltro.asStateFlow()
    private val mutableSoloHoy = MutableStateFlow(false)
    val soloHoy = mutableSoloHoy.asStateFlow()
    private val mutableOscuro = MutableStateFlow(false)
    val oscuro = mutableOscuro.asStateFlow()
    private var carga: Job? = null
    private var escenario = "Normal"
    val visibles = combine(estado, busqueda, filtro, soloHoy) { e, b, f, hoy ->
        if(e is UiState.Contenido) obtener.filtrar(e.datos.citas, f, b, hoy) else emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    init { viewModelScope.launch { repository.cambios.collect { recargar() } } }
    fun buscar(valor: String) { mutableBusqueda.value = valor }
    fun filtrar(valor: String) { mutableFiltro.value = valor }
    fun filtrarHoy(valor: Boolean) { mutableSoloHoy.value = valor }
    fun tema(valor: Boolean) { mutableOscuro.value = valor }
    fun simular(valor: String) { escenario = valor; recargar() }
    fun recargar() {
        carga?.cancel()
        carga = viewModelScope.launch {
            mutable.value = UiState.Cargando
            try {
                coroutineScope {
                    val citas = async { obtener() }
                    val datos = async { catalogo() }
                    val panel = Panel(datos.await(), citas.await())
                    mutable.value = when(escenario) {
                        "Error" -> UiState.Error("No se pudieron cargar los datos simulados. Vuelve a intentar.")
                        "Vacío" -> UiState.Vacio
                        else -> UiState.Contenido(panel)
                    }
                }
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { mutable.value = UiState.Error(e.message ?: "No se pudieron cargar los datos") }
        }
    }
}

