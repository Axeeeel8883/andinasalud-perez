package pe.upeu.andinasalud.domain.usecase

import kotlinx.datetime.*
import pe.upeu.andinasalud.domain.model.*
import pe.upeu.andinasalud.domain.repository.CitaRepository
import kotlin.time.Duration.Companion.hours

class PoliticaCitas(private val reloj: Clock = Clock.System) {
    val limiteProgramadas = 3
    fun puedeSolicitar(citas: List<Cita>, pacienteId: String) = citas.count { it.pacienteId == pacienteId && it.estado is EstadoCita.Programada } < limiteProgramadas
    fun puedeCancelar(cita: Cita) = cita.estado is EstadoCita.Programada && cita.momento - reloj.now() > 24.hours
    fun validar(s: Solicitud, catalogo: Catalogo, citas: List<Cita>, citaIgnorada: Long? = null): Instant {
        val errores = mutableMapOf<String, String>()
        if (s.especialidad !in catalogo.especialidades) errores["especialidad"] = "Selecciona una especialidad"
        if (catalogo.sedes.none { it.nombre == s.sede }) errores["sede"] = "Selecciona una sede"
        if (catalogo.medicos.none { it.id == s.medicoId && it.especialidad == s.especialidad && s.sede in it.sedes }) errores["medicoId"] = "Selecciona un médico disponible en esta sede"
        val fecha = runCatching { LocalDate.parse(s.fecha) }.getOrNull()
        val hora = runCatching { LocalTime.parse(s.hora) }.getOrNull()
        if (fecha == null) errores["fecha"] = "Ingresa una fecha válida (AAAA-MM-DD)"
        if (hora == null) errores["hora"] = "Ingresa una hora válida (HH:MM)"
        val momento = if(fecha != null && hora != null) LocalDateTime(fecha, hora).toInstant(TimeZone.currentSystemDefault()) else null
        if (momento != null && momento <= reloj.now()) errores["fecha"] = "La fecha y hora deben ser futuras"
        if (s.motivo.trim().length !in 10..200) errores["motivo"] = "El motivo debe tener entre 10 y 200 caracteres"
        if (citaIgnorada == null && !puedeSolicitar(citas, catalogo.paciente.id)) errores["general"] = "Ya tienes tres citas Programadas. Cancela una antes de solicitar otra."
        if (momento != null && citas.any { it.id != citaIgnorada && it.pacienteId == catalogo.paciente.id && it.estado is EstadoCita.Programada && it.momento == momento }) errores["hora"] = "Ya tienes una cita Programada en ese día y hora"
        if (errores.isNotEmpty()) throw ValidacionException(errores)
        return requireNotNull(momento)
    }
}
fun normalizar(texto: String): String = texto.lowercase().map { c ->
    when(c) { 'á','à','ä','â' -> 'a'; 'é','è','ë','ê' -> 'e'; 'í','ì','ï','î' -> 'i'; 'ó','ò','ö','ô' -> 'o'; 'ú','ù','ü','û' -> 'u'; 'ñ' -> 'n'; else -> c }
}.filter { it !in '\u0300'..'\u036f' }.joinToString("")
class ObtenerCitasUseCase(private val repository: CitaRepository, private val reloj: Clock = Clock.System) {
    suspend operator fun invoke() = repository.obtenerCitas().sortedBy { it.momento }
    fun filtrar(citas: List<Cita>, estado: String, busqueda: String, soloHoy: Boolean = false): List<Cita> {
        val q = normalizar(busqueda.trim())
        val hoy = reloj.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return citas.filter { (estado == "Todas" || it.estado.etiqueta == estado) &&
            (!soloHoy || it.momento.toLocalDateTime(TimeZone.currentSystemDefault()).date == hoy) &&
            (normalizar(it.medico.nombre).contains(q) || normalizar(it.medico.especialidad).contains(q)) }.sortedBy { it.momento }
    }
}
class ObtenerCatalogoUseCase(private val repository: CitaRepository) { suspend operator fun invoke() = repository.obtenerCatalogo() }
class SolicitarCitaUseCase(private val repository: CitaRepository, private val politica: PoliticaCitas) {
    suspend operator fun invoke(s: Solicitud): Long {
        val catalogo = repository.obtenerCatalogo()
        var id = 0L
        repository.modificar { citas ->
            val momento = politica.validar(s, catalogo, citas)
            id = (citas.maxOfOrNull { it.id } ?: 0L) + 1
            citas + Cita(id, catalogo.paciente.id, catalogo.medicos.first { it.id == s.medicoId }, s.sede, momento, s.motivo.trim(), EstadoCita.Programada(true), s.modalidad)
        }
        return id
    }
}
class ReprogramarCitaUseCase(private val repository: CitaRepository, private val politica: PoliticaCitas) {
    suspend operator fun invoke(id: Long, solicitud: Solicitud) {
        val catalogo = repository.obtenerCatalogo()
        repository.modificar { citas ->
            val cita = citas.firstOrNull { it.id == id } ?: throw IllegalArgumentException("No se encontró la cita")
            require(cita.estado is EstadoCita.Programada) { "Solo puedes reprogramar una cita Programada" }
            val nuevoMomento = politica.validar(solicitud, catalogo, citas, citaIgnorada = id)
            citas.map { actual -> if (actual.id == id) actual.copy(
                medico = catalogo.medicos.first { it.id == solicitud.medicoId }, sede = solicitud.sede,
                momento = nuevoMomento, motivo = solicitud.motivo.trim(), modalidad = solicitud.modalidad,
                reprogramaciones = actual.reprogramaciones + Reprogramacion(actual.momento, nuevoMomento)
            ) else actual }
        }
    }
}
class CancelarCitaUseCase(private val repository: CitaRepository, private val politica: PoliticaCitas) {
    suspend operator fun invoke(id: Long) {
        repository.modificar { citas ->
            val cita = citas.firstOrNull { it.id == id } ?: throw IllegalArgumentException("No se encontró la cita")
            require(politica.puedeCancelar(cita)) { "Solo puedes cancelar citas Programadas con más de 24 horas de anticipación" }
            citas.map { if(it.id == id) it.copy(estado = EstadoCita.Cancelada("Cancelada por el paciente", true)) else it }
        }
    }
}

