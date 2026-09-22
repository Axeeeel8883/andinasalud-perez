# Guía de defensa técnica

Esta guía facilita estudiar el código; no sustituye tu explicación individual.

1. **RN-02.** `domain/usecase/CasosDeUso.kt`, clase `PoliticaCitas`, método `puedeSolicitar`. Cuenta únicamente Programadas del paciente. `validar` la aplica dentro de la transacción del repositorio. No depende de Compose.
2. **API futura.** Crear `data/remote/` con transporte y DTO, un mapper hacia entidades y `data/repository/CitaRepositoryRemoto`. Sustituir el binding de `CitaRepository` en `di/AppModule.kt`. La API deberá garantizar la operación atómica de solicitud/cancelación: una validación solo en cliente no protege contra otros dispositivos. Manteniendo el contrato, los casos de uso y pantallas no cambian. El transporte no se incluye en esta versión.
3. **Sealed class.** `EstadoCita` conserva información diferente por variante: recordatorio, indicaciones o motivo de cancelación y autor. `when` exhaustivo obliga a contemplarlas. Un enum no expresa esos datos por instancia.
4. **Recorrido.** `CitasSimuladas.crear` → `CitaRepositoryFake.obtenerCitas` → `ObtenerCitasUseCase` → `CitasViewModel.estado` → `collectAsState` en `AppNavHost` → `CitasScreen` → `CitaCard`.
5. **UiState y dominio.** `Cita` representa una cita clínica. `UiState` describe si la pantalla está cargando, tiene datos, está vacía o falló. `FormularioUiState` contiene además errores y estado de envío.
6. **Corrutina.** `viewModelScope.launch` arranca la consulta. `delay(800)` está en la fuente. Al limpiar el ViewModel se cancela su scope. `CancellationException` se propaga; no se transforma en un falso error.
7. **StateFlow.** El consumidor observa el último estado sin poder modificar el `MutableStateFlow` privado. Los eventos públicos del ViewModel controlan las transiciones.
8. **Reutilización.** `CitaCard(cita, onClick)` muestra un modelo y emite un clic; desconoce repositorios y navegación. `EstadoContenido` centraliza la presentación de carga/vacío/error.
9. **Tema.** `CitasViewModel.oscuro` cambia desde un Switch. `AndinaSaludTheme` envuelve el Scaffold en `AppNavHost`, con paletas propias. No hay persistencia por diseño.
10. **Especialidad adicional.** En el prototipo se amplía `CitasSimuladas.especialidades` y su catálogo de médicos. Los selectores derivan sus opciones del catálogo. El dominio no contiene una lista fija de especialidades permitidas aparte del catálogo.
11. **Aporte individual y conflictos.** Mostrar solamente tu historial real. Este trabajo del asistente no demuestra contribuciones del compañero ni un conflicto que no ocurrió. Explica qué revisaste, cambiaste y probaste personalmente.

## Límites que conviene poder explicar

- Reiniciar el proceso restaura los datos iniciales. Es una fuente en memoria, no un sistema clínico productivo.
- Se usan fechas relativas a hoy, evitando semillas vencidas el día de la evaluación.
- Una cita a exactamente 24 horas no se puede cancelar: la regla exige **más** de 24.
- Se valida el motivo después de `trim`: espacios exteriores no sirven para cumplir el mínimo.
- Los errores se pueden simular desde Ajustes sin destruir los datos. Las excepciones reales también producen `UiState.Error`.
- El formulario añade selección de médico para asegurar que corresponde a la especialidad y sede.
