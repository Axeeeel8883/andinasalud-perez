package pe.upeu.andinasalud.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.upeu.andinasalud.data.local.CitasSimuladas
import pe.upeu.andinasalud.domain.model.*
import pe.upeu.andinasalud.domain.repository.CitaRepository

class CitaRepositoryFake(private val retardo: Long = 800) : CitaRepository {
    private var citas = CitasSimuladas.crear()
    private val mutex = Mutex()
    private val revision = MutableStateFlow(0L)
    override val cambios = revision.asStateFlow()
    override suspend fun obtenerCitas(): List<Cita> { delay(retardo); return mutex.withLock { citas.toList() } }
    override suspend fun obtenerCatalogo(): Catalogo { delay(retardo); return CitasSimuladas.catalogo }
    override suspend fun modificar(transformar: (List<Cita>) -> List<Cita>) {
        delay(retardo)
        mutex.withLock { citas = transformar(citas); revision.value++ }
    }
}
