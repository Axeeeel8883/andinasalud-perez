package pe.upeu.andinasalud.domain.repository

import kotlinx.coroutines.flow.StateFlow
import pe.upeu.andinasalud.domain.model.*

interface CitaRepository {
    val cambios: StateFlow<Long>
    suspend fun obtenerCitas(): List<Cita>
    suspend fun obtenerCatalogo(): Catalogo
    // La transacción evita que dos solicitudes simultáneas eludan RN-02 o RN-05.
    suspend fun modificar(transformar: (List<Cita>) -> List<Cita>)
}
