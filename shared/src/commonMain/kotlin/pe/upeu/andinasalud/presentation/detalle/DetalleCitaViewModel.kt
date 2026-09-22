package pe.upeu.andinasalud.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import pe.upeu.andinasalud.domain.usecase.CancelarCitaUseCase

data class AccionUiState(val procesando: Boolean = false, val error: String? = null)
class DetalleCitaViewModel(private val cancelar: CancelarCitaUseCase) : ViewModel() {
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
}
