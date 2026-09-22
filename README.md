# AndinaSalud

Aplicación de citas para el caso del Examen Parcial U1. Kotlin Multiplatform, Compose Multiplatform, Material 3, Clean + MVVM y Koin. Los datos son simulados **exclusivamente en memoria**; no hay cliente HTTP, base de datos ni permisos de Internet.

## Abrir en Android Studio

1. Abre esta carpeta, donde está `settings.gradle.kts`.
2. Selecciona JDK 17 o el JBR de Android Studio en la configuración de Gradle.
3. Instala Android SDK 36 y Build Tools 35.0.0 desde SDK Manager.
4. Sincroniza Gradle, selecciona la configuración `androidApp` y un emulador/dispositivo API 24 o superior.
5. Pulsa Run. No necesita claves, servidor ni cuentas.

```sh
./gradlew :androidApp:assembleDebug :shared:testDebugUnitTest :androidApp:lintDebug
```

En Windows utiliza `gradlew.bat`. La primera sincronización descarga dependencias. El APK aparece en `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

Si el Test Worker de Windows falla al abrir una conexión local, consulta la alternativa JUnit directa y los resultados en [docs/VERIFICACION.md](docs/VERIFICACION.md).

## Ejecutar iOS (requiere macOS y Xcode)

1. Instala JDK 17 y Xcode; abre Xcode una vez para aceptar la licencia e instalar un simulador iOS.
2. Ejecuta `chmod +x gradlew` en la raíz del proyecto.
3. Abre `iosApp/iosApp.xcodeproj`, selecciona el esquema **AndinaSalud** y un simulador iPhone.
4. Pulsa Run. La fase de compilación genera `Shared.framework` mediante `:shared:embedAndSignAppleFrameworkForXcode`.
5. Para un iPhone físico, selecciona tu equipo de firma en Xcode.

```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme AndinaSalud \
  -configuration Debug -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build
```

Los targets `iosArm64`, `iosSimulatorArm64` e `iosX64` comparten el mismo `commonMain`. El flujo `.github/workflows/verificar.yml` prepara compilación Android/pruebas y compilación/arranque en simulador iOS. Que exista el flujo **no significa que ya se haya ejecutado correctamente**; consulta `docs/VERIFICACION.md` y los resultados de Actions.

Integración iOS según [la documentación oficial de Kotlin](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html). AGP 8.8.2 advierte que fue probado hasta API 35; este entorno dispone de SDK 36. La [documentación de AGP](https://developer.android.com/build/releases/agp-8-8-0-release-notes) explica esta compatibilidad. La compilación efectiva se registra en las evidencias.

## Estructura

```text
shared/src/commonMain/kotlin/pe/upeu/andinasalud/
  domain/model/          entidades, EstadoCita y Solicitud
  domain/repository/     contrato CitaRepository
  domain/usecase/        reglas, consulta, solicitud y cancelación
  data/local/           catálogo y seis citas relativas al día actual
  data/repository/      memoria, retardo suspendido y transacciones
  presentation/         ViewModels, UiState, pantallas y componentes
  presentation/theme/   paleta Material 3 clara y oscura
  presentation/navigation/ rutas, pila de navegación y Atrás
  di/                   módulos comunes de Koin
shared/src/commonTest/   pruebas de reglas y repositorio
shared/src/androidMain/  adaptación del botón Atrás
shared/src/iosMain/      controlador Compose para iOS
androidApp/             entrada Android
iosApp/                 aplicación y proyecto Xcode
```

## Decisiones de arquitectura

- `CitaRepository` está en dominio. La implementación `CitaRepositoryFake` se registra como `single` en Koin para compartir la misma lista de citas.
- `PoliticaCitas` contiene RN-01 a RN-05. Los casos de uso consultan esa política; la pantalla presenta sus errores sin repetir las reglas.
- `modificar` ejecuta la validación y escritura en una sección protegida por `Mutex`. Dos solicitudes simultáneas no pueden superar el límite ni crear un horario duplicado.
- Las fechas usan `Instant`; el formulario y las pantallas convierten a la zona horaria del dispositivo. El reloj se puede sustituir en las pruebas.
- Los ViewModels exponen `StateFlow` de solo lectura. Las operaciones se ejecutan en `viewModelScope` y propagan cancelación correctamente.
- La fuente retrasa cada operación 800 ms con `delay`, sin bloquear el hilo principal. Las consultas de catálogo y citas se realizan concurrentemente.
- La navegación conserva una pila de rutas con `rememberSaveable`. Android devuelve al destino previo con Atrás; iOS usa la flecha superior. La barra inferior tiene Inicio, Citas y Perfil.
- Los componentes reciben datos y eventos; no acceden a la fuente simulada. `LazyColumn` dibuja las listas con claves estables.
- El tema se aplica alrededor de todo el `Scaffold`. Las preferencias y los datos duran la sesión; no se persisten por la restricción del caso.

## Recorrido de demostración

1. Inicio: saludo, próxima cita y dos accesos rápidos.
2. Citas: seis registros, orden cronológico y chips de estado. Busca `IVAN` u `odontologia` para comprobar búsqueda sin tildes.
3. Abre una Programada, cancela y confirma. Comprueba el nuevo estado en detalle, lista e inicio.
4. Solicita otra cita: especialidad, sede, médico, fecha futura, hora y motivo. La semilla comienza con tres Programadas: primero cancela una para registrar otra.
5. Envía el formulario vacío para ver errores debajo de cada campo. Prueba fecha pasada, motivo corto y horario duplicado.
6. Perfil: datos del paciente y tema oscuro. El cambio se aplica de inmediato.
7. Ajustes: elige Normal, Vacío o Error para demostrar los estados de todas las pantallas de datos. Reintentar restaura Normal sin borrar citas.
8. Recorre detalle/formulario y usa Atrás. Ajustes es la sexta pantalla, accesible desde el icono superior y desde Perfil.

## Estado de la evaluación y autoría

Este proyecto fue elaborado con asistencia de Codex. Los commits generados por el asistente se identifican como tales. No representan aportes personales de dos estudiantes, revisiones de un compañero ni sesiones de trabajo pasadas. No se han fabricado autores, fechas, capturas iOS ni aprobaciones.

La solicitud individual SC-A/B/C/D, el apellido para la rama y el repositorio de destino requieren confirmación del estudiante. La aplicación base no presupone una solicitud asignada. Cada integrante debe explicar y justificar el código, realizar sus aportes auténticos y seguir el flujo de revisión del curso.

Antes de etiquetar `v1.0-unidad1`, hace falta verificar iOS, completar las evidencias de ambas plataformas y las revisiones reales del equipo. La defensa y el historial colaborativo no se pueden garantizar mediante el producto técnico.
