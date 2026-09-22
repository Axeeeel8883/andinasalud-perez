package pe.upeu.andinasalud.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import pe.upeu.andinasalud.domain.usecase.CancelarCitaUseCase
import pe.upeu.andinasalud.domain.usecase.ReprogramarCitaUseCase
import pe.upeu.andinasalud.domain.model.*

data class AccionUiState(val procesando: Boolean = false, val error: String? = null, val completada: Boolean = false)
class DetalleCitaViewModel(private val cancelar: CancelarCitaUseCase, private val reprogramar: ReprogramarCitaUseCase) : ViewModel() {
    private val mutable = MutableStateFlow(AccionUiState())
    val estado = mutable.asStateFlow()
    fun cancelar(id: Long) {
        if(mutable.value.procesando) return
        viewModelScope.launch {
            mutable.value = AccionUiState(procesando = true)
            try { cancelar.invoke(id); mutable.value = AccionUiState() }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { mutable.value = AccionUiState(error = e.message ?: "No se pudo cancelar") }
        }
    }
    fun reprogramar(id: Long, solicitud: Solicitud) {
        if(mutable.value.procesando) return
        viewModelScope.launch {
            mutable.value = AccionUiState(procesando = true)
            try { reprogramar.invoke(id, solicitud); mutable.value = AccionUiState(completada = true) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { mutable.value = AccionUiState(error = e.message ?: "No se pudo reprogramar") }
        }
    }
}

