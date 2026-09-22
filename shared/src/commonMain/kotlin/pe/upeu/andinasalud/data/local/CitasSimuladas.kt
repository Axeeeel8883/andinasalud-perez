package pe.upeu.andinasalud.data.local

import kotlinx.datetime.*
import pe.upeu.andinasalud.domain.model.*

object CitasSimuladas {
    val paciente = Paciente("P-0417", "Lucía Quispe Mamani", "70154823", "lucia.quispe@correo.pe", "987 654 321")
    val sedes = listOf("Ñaña", "Chosica", "Chaclacayo", "Santa Anita")
    val especialidades = listOf("Medicina General", "Odontología", "Pediatría", "Nutrición", "Psicología")
    private val nombres = listOf("Dr. Iván Rojas", "Dra. Elena Soto", "Dra. Rosa Flores", "Dr. José León", "Dra. Carla Núñez", "Dr. Miguel Díaz", "Lic. Ana Bermúdez", "Lic. Diego Ramos", "Ps. Luis Tapia", "Ps. María Torres")
    val medicos = nombres.mapIndexed { i, nombre -> Medico("M-$i", nombre, especialidades[i / 2], sedes) }
    val catalogo = Catalogo(paciente, sedes.map(::Sede), especialidades, medicos)
    fun crear(reloj: Clock = Clock.System): List<Cita> {
        val zona = TimeZone.currentSystemDefault()
        val hoy = reloj.now().toLocalDateTime(zona).date
        val dias = listOf(2, 4, 7, -20, -10, -5)
        val doctores = listOf(0, 2, 6, 4, 8, 0)
        return dias.mapIndexed { i, diasDesdeHoy ->
            val estado = when(i) {
                0,1,2 -> EstadoCita.Programada(i != 1)
                3 -> EstadoCita.Atendida("Control en tres meses")
                4 -> EstadoCita.Atendida("Continuar sesiones quincenales")
                else -> EstadoCita.Cancelada("Viaje del paciente", true)
            }
            Cita(i + 1L, paciente.id, medicos[doctores[i]], sedes[i % 4],
                LocalDateTime(hoy.plus(diasDesdeHoy, DateTimeUnit.DAY), LocalTime(9 + i, 0)).toInstant(zona), "Consulta de seguimiento", estado)
        }
    }
}
