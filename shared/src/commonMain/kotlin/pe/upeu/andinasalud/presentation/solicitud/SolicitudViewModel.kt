package pe.upeu.andinasalud.presentation.solicitud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import pe.upeu.andinasalud.domain.model.*
import pe.upeu.andinasalud.domain.usecase.SolicitarCitaUseCase

data class FormularioUiState(val solicitud: Solicitud = Solicitud(), val errores: Map<String, String> = emptyMap(), val guardando: Boolean = false, val creada: Long? = null)
class SolicitudViewModel(private val solicitar: SolicitarCitaUseCase) : ViewModel() {
    private val mutable = MutableStateFlow(FormularioUiState())
    val estado = mutable.asStateFlow()
    fun cambiar(s: Solicitud) { if(!mutable.value.guardando) mutable.update { it.copy(solicitud = s, errores = emptyMap()) } }
    fun guardar() {
        if(mutable.value.guardando || mutable.value.creada != null) return
        viewModelScope.launch {
            mutable.update { it.copy(guardando = true, errores = emptyMap()) }
            try {
                val id = solicitar(mutable.value.solicitud)
                mutable.update { it.copy(guardando = false, creada = id) }
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { mutable.update { it.copy(guardando = false, errores = (e as? ValidacionException)?.errores ?: mapOf("general" to (e.message ?: "No se pudo guardar"))) } }
        }
    }
}
