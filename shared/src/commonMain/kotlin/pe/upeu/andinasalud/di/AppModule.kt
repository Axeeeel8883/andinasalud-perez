package pe.upeu.andinasalud.di

import org.koin.core.context.startKoin
import org.koin.dsl.module
import pe.upeu.andinasalud.data.repository.CitaRepositoryFake
import pe.upeu.andinasalud.domain.repository.CitaRepository
import pe.upeu.andinasalud.domain.usecase.*
import pe.upeu.andinasalud.presentation.citas.CitasViewModel
import pe.upeu.andinasalud.presentation.detalle.DetalleCitaViewModel
import pe.upeu.andinasalud.presentation.solicitud.SolicitudViewModel

val appModule = module {
    single<CitaRepository> { CitaRepositoryFake() }
    single { PoliticaCitas() }
    factory { ObtenerCitasUseCase(get()) }
    factory { ObtenerCatalogoUseCase(get()) }
    factory { SolicitarCitaUseCase(get(), get()) }
    factory { CancelarCitaUseCase(get(), get()) }
    factory { CitasViewModel(get(), get(), get(), get()) }
    factory { SolicitudViewModel(get()) }
    factory { DetalleCitaViewModel(get()) }
}
private var iniciado = false
fun iniciarKoin() { if (!iniciado) { startKoin { modules(appModule) }; iniciado = true } }
