package pe.upeu.andinasalud.domain

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import pe.upeu.andinasalud.data.local.CitasSimuladas
import pe.upeu.andinasalud.data.repository.CitaRepositoryFake
import pe.upeu.andinasalud.domain.model.*
import pe.upeu.andinasalud.domain.repository.CitaRepository
import pe.upeu.andinasalud.domain.usecase.*
import kotlin.test.*
import kotlin.time.Duration.Companion.hours

class ReglasCitasTest {
    private val ahora = Instant.parse("2026-09-22T12:00:00Z")
    private val reloj = object : Clock { override fun now() = ahora }
    private val politica = PoliticaCitas(reloj)
    private val catalogo = CitasSimuladas.catalogo
    private fun cita(id: Long = 1, horas: Int = 48, estado: EstadoCita = EstadoCita.Programada(true)) =
        Cita(id, catalogo.paciente.id, catalogo.medicos.first(), "Ñaña", ahora + horas.hours, "Consulta general", estado)
    private fun solicitud(horas: Int = 48, motivo: String = "Consulta general"): Solicitud {
        val fecha = (ahora + horas.hours).toLocalDateTime(TimeZone.currentSystemDefault())
        return Solicitud("Medicina General", "Ñaña", "M-0", fecha.date.toString(), fecha.time.toString(), motivo)
    }
    @Test fun permiteUnaSolicitudValida() { assertEquals(ahora + 48.hours, politica.validar(solicitud(), catalogo, emptyList())) }
    @Test fun rechazaFechaPasada() { assertTrue(assertFailsWith<ValidacionException> { politica.validar(solicitud(-1), catalogo, emptyList()) }.errores.containsKey("fecha")) }
    @Test fun rechazaInstanteActual() { assertFailsWith<ValidacionException> { politica.validar(solicitud(0), catalogo, emptyList()) } }
    @Test fun rechazaCuartaProgramada() {
        val citas = (1L..3L).map { cita(it, (it * 72).toInt()) }
        assertFalse(politica.puedeSolicitar(citas, catalogo.paciente.id))
        assertTrue(assertFailsWith<ValidacionException> { politica.validar(solicitud(), catalogo, citas) }.errores.containsKey("general"))
    }
    @Test fun noCuentaAtendidasNiCanceladasEnLimite() { assertTrue(politica.puedeSolicitar(listOf(cita(), cita(2, estado = EstadoCita.Atendida("Control")), cita(3, estado = EstadoCita.Cancelada("Viaje", true))), catalogo.paciente.id)) }
    @Test fun noCuentaCitasDeOtroPaciente() { assertTrue(politica.puedeSolicitar((1L..3L).map { cita(it).copy(pacienteId = "otro") }, catalogo.paciente.id)) }
    @Test fun cancelarConMasDe24Horas() { assertTrue(politica.puedeCancelar(cita(horas = 25))) }
    @Test fun noCancelarCon24HorasExactas() { assertFalse(politica.puedeCancelar(cita(horas = 24))) }
    @Test fun noCancelarConMenosDe24Horas() { assertFalse(politica.puedeCancelar(cita(horas = 23))) }
    @Test fun noCancelarAtendida() { assertFalse(politica.puedeCancelar(cita(estado = EstadoCita.Atendida("Control")))) }
    @Test fun noCancelarCancelada() { assertFalse(politica.puedeCancelar(cita(estado = EstadoCita.Cancelada("Viaje", true)))) }
    @Test fun aceptaDiezCaracteres() { politica.validar(solicitud(motivo = "a".repeat(10)), catalogo, emptyList()) }
    @Test fun aceptaDoscientosCaracteres() { politica.validar(solicitud(motivo = "a".repeat(200)), catalogo, emptyList()) }
    @Test fun rechazaNueveCaracteres() { assertFailsWith<ValidacionException> { politica.validar(solicitud(motivo = "a".repeat(9)), catalogo, emptyList()) } }
    @Test fun rechazaDoscientosUno() { assertFailsWith<ValidacionException> { politica.validar(solicitud(motivo = "a".repeat(201)), catalogo, emptyList()) } }
    @Test fun espaciosNoCumplenMotivo() { assertFailsWith<ValidacionException> { politica.validar(solicitud(motivo = " ".repeat(10)), catalogo, emptyList()) } }
    @Test fun rechazaHorarioDuplicado() { assertTrue(assertFailsWith<ValidacionException> { politica.validar(solicitud(), catalogo, listOf(cita())) }.errores.containsKey("hora")) }
    @Test fun permiteHorarioDeCitaCancelada() { politica.validar(solicitud(), catalogo, listOf(cita(estado = EstadoCita.Cancelada("Viaje", true)))) }
    @Test fun validaTodosLosCamposVacios() { assertEquals(setOf("especialidad", "sede", "medicoId", "fecha", "hora", "motivo"), assertFailsWith<ValidacionException> { politica.validar(Solicitud(), catalogo, emptyList()) }.errores.keys) }
    @Test fun rechazaFechaInexistente() { assertFailsWith<ValidacionException> { politica.validar(solicitud().copy(fecha = "2026-02-30"), catalogo, emptyList()) } }
    @Test fun rechazaHoraInexistente() { assertFailsWith<ValidacionException> { politica.validar(solicitud().copy(hora = "25:00"), catalogo, emptyList()) } }
    @Test fun rechazaMedicoDeOtraEspecialidad() { assertFailsWith<ValidacionException> { politica.validar(solicitud().copy(medicoId = "M-4"), catalogo, emptyList()) } }
    @Test fun buscaSinMayusculasNiTildes() { assertEquals("ivan nunez odontologia", normalizar("IVÁN NÚÑEZ Odontología")); assertEquals("ivan", normalizar("IVA\u0301N")) }
    @Test fun combinaBusquedaYEstadoYOrdena() {
        val useCase = ObtenerCitasUseCase(RepositorioPrueba())
        val citas = listOf(cita(3, 72), cita(2, 24), cita(1, 48, EstadoCita.Atendida("Control")))
        assertEquals(listOf(2L, 3L), useCase.filtrar(citas, "Programada", "IVAN").map { it.id })
        assertTrue(useCase.filtrar(citas, "Atendida", "Pediatria").isEmpty())
    }
    @Test fun semillasCumplenCantidadesYFechas() {
        val citas = CitasSimuladas.crear(reloj)
        assertEquals(6, citas.size); assertEquals(4, catalogo.sedes.size); assertEquals(5, catalogo.especialidades.size)
        assertTrue(catalogo.especialidades.all { e -> catalogo.medicos.count { it.especialidad == e } >= 2 })
        assertEquals(3, citas.count { it.estado is EstadoCita.Programada && it.momento > ahora })
        assertEquals(2, citas.count { it.estado is EstadoCita.Atendida }); assertEquals(1, citas.count { it.estado is EstadoCita.Cancelada })
    }
    @Test fun cancelarActualizaRepositorio() = runTest {
        val repo = RepositorioPrueba(listOf(cita()))
        CancelarCitaUseCase(repo, politica)(1)
        assertIs<EstadoCita.Cancelada>(repo.obtenerCitas().single().estado)
    }
    @Test fun cancelacionRechazadaNoMuta() = runTest {
        val repo = RepositorioPrueba(listOf(cita(horas = 24)))
        assertFailsWith<IllegalArgumentException> { CancelarCitaUseCase(repo, politica)(1) }
        assertIs<EstadoCita.Programada>(repo.obtenerCitas().single().estado)
    }
    @Test fun registroGeneraIdYNotifica() = runTest {
        val repo = RepositorioPrueba(listOf(cita(8, 72)))
        assertEquals(9L, SolicitarCitaUseCase(repo, politica)(solicitud()))
        assertEquals(2, repo.obtenerCitas().size)
        assertEquals(1L, repo.cambios.value)
    }
    @Test fun transaccionEvitaCuartaCitaConcurrente() = runTest {
        val repo = CitaRepositoryFake(0)
        repo.modificar { listOf(cita(1, 72), cita(2, 96)) }
        val useCase = SolicitarCitaUseCase(repo, politica)
        val resultados = listOf(async { runCatching { useCase(solicitud(120)) } }, async { runCatching { useCase(solicitud(144)) } }).awaitAll()
        assertEquals(1, resultados.count { it.isSuccess })
        assertEquals(3, repo.obtenerCitas().size)
    }
    @Test fun retardoEsOchocientosMilisegundos() = runTest {
        val repo = CitaRepositoryFake()
        val inicio = testScheduler.currentTime
        repo.obtenerCitas()
        assertEquals(800, testScheduler.currentTime - inicio)
    }
    private class RepositorioPrueba(var citas: List<Cita> = emptyList()) : CitaRepository {
        override val cambios = MutableStateFlow(0L)
        override suspend fun obtenerCitas() = citas
        override suspend fun obtenerCatalogo() = CitasSimuladas.catalogo
        override suspend fun modificar(transformar: (List<Cita>) -> List<Cita>) { citas = transformar(citas); cambios.value++ }
    }
}
