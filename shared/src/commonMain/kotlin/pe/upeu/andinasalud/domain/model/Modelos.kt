package pe.upeu.andinasalud.domain.model

import kotlinx.datetime.Instant

data class Paciente(val id: String, val nombre: String, val documento: String, val correo: String, val telefono: String)
data class Sede(val nombre: String)
data class Medico(val id: String, val nombre: String, val especialidad: String, val sedes: List<String>)
sealed class EstadoCita {
    data class Programada(val recordatorioActivo: Boolean) : EstadoCita()
    data class Atendida(val indicaciones: String) : EstadoCita()
    data class Cancelada(val motivo: String, val canceladaPorPaciente: Boolean) : EstadoCita()
}
val EstadoCita.etiqueta: String get() = when(this) {
    is EstadoCita.Programada -> "Programada"
    is EstadoCita.Atendida -> "Atendida"
    is EstadoCita.Cancelada -> "Cancelada"
}
data class Cita(val id: Long, val pacienteId: String, val medico: Medico, val sede: String,
    val momento: Instant, val motivo: String, val estado: EstadoCita,
    val indicaciones: String = "Llega 15 minutos antes y presenta tu documento de identidad.")
data class Catalogo(val paciente: Paciente, val sedes: List<Sede>, val especialidades: List<String>, val medicos: List<Medico>)
data class Solicitud(val especialidad: String = "", val sede: String = "", val medicoId: String = "",
    val fecha: String = "", val hora: String = "", val motivo: String = "")
class ValidacionException(val errores: Map<String, String>) : Exception(errores.values.firstOrNull() ?: "Datos inválidos")
