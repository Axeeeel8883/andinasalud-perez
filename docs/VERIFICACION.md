# Verificación y pendientes

Fecha: 22 de septiembre de 2026. Equipo: Windows; Android SDK 36; emulador Pixel 9, Android 17, 1080 x 2424.

## Resultados reales

| Comprobación | Resultado |
| --- | --- |
| Compilación `:androidApp:assembleDebug` | Correcta; APK instalado y abierto en emulador |
| Análisis `:androidApp:lintDebug` | 0 errores, 1 advertencia `OldTargetApi` (target 35 frente a SDK 36) |
| Pruebas de dominio y ViewModel | 32 aprobadas mediante JUnitCore |
| Ejecutor estándar `:shared:testDebugUnitTest` | Clases compiladas; ejecución bloqueada por error local de sockets de Windows |
| Inicio | Saludo y próxima cita visibles; accesos a lista y solicitud |
| Cancelación | Confirmación, cambio a Cancelada y actualización del inicio |
| Solicitud | Campos vacíos producen errores propios; registro completo crea una cita y abre su detalle |
| Lista y búsqueda | Búsqueda `IVAN` encuentra `Iván`; filtro de estado y orden cubiertos además por pruebas |
| Perfil y tema | Datos visibles; modo oscuro aplicado a toda la aplicación |
| Navegación | Inicio, Citas, Perfil, Detalle, Solicitud y Ajustes accesibles; Atrás regresa correctamente |
| Estados de interfaz | Carga, normal, vacío, error y recuperación comprobados |
| iOS | Configurado, **no compilado ni ejecutado localmente** |
| GitHub | Destino pendiente de confirmación del usuario; no se afirma haber publicado |
| SC-A/B/C/D | Pendiente de conocer la solicitud asignada; no se presume una asignación |

## Evidencias

- `COMPILACION_ANDROID.txt`: salida final de compilación y Lint.
- `JUNIT_RESULTADO.txt`: salida real de las 32 pruebas ejecutadas directamente.
- `LINT_ANDROID.txt`: advertencia restante completa.
- `CAPTURAS/`: capturas reales del emulador Android, no simulaciones del ZIP original.
- `EVIDENCIAS_ANDROID.pdf`: seis pantallas, operaciones y estados. Solo Android; no se incluyen capturas iOS ficticias.
- `GIT_HISTORIAL.txt`: transcripción del historial local; no acredita un segundo autor.

## Repetir las pruebas

Comando habitual:

```sh
./gradlew :shared:testDebugUnitTest
```

Si el entorno Windows falla con `Unable to establish loopback connection` en el Test Worker, esta alternativa ejecuta las mismas clases JUnit mediante JavaExec, conservando las aserciones y el código de salida:

```sh
./gradlew -I tools/junit-directo.init.gradle :shared:verificarJUnitDirecto
```

El proyecto no depende del comando `testAndroidHostTest` citado en el ZIP de ClinicaMobil: ese paquete corresponde a otro caso. Aquí se usa el target Android de KMP con la tarea real `testDebugUnitTest`.

## Para cerrar la entrega del examen

1. Confirmar repositorio, apellido y solicitud SC asignada.
2. Ejecutar en macOS/Xcode o verificar el job iOS de Actions; recorrer y capturar las seis pantallas reales en iOS.
3. Cada integrante realiza sus cambios propios y abre las solicitudes de incorporación correspondientes; el compañero revisa realmente.
4. Integrar en develop y después en main mediante el flujo aprobado del equipo. Etiquetar el commit evaluado cuando ambas plataformas estén verificadas.
5. Completar el PDF con las evidencias iOS, el gráfico y las revisiones auténticas del equipo.
6. Estudiar `DEFENSA.md`, contrastando cada respuesta con el código que debes poder explicar.

La rúbrica incluye defensa individual y trabajo colaborativo histórico. El producto técnico por sí solo no garantiza 20 puntos.
