package pe.upeu.andinasalud.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import pe.upeu.andinasalud.data.repository.CitaRepositoryFake
import pe.upeu.andinasalud.domain.usecase.*
import pe.upeu.andinasalud.presentation.citas.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CitasViewModelTest {
    @Test fun cargaContenidoErrorVacioYReintento() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo = CitaRepositoryFake()
            val vm = CitasViewModel(repo, ObtenerCitasUseCase(repo), ObtenerCatalogoUseCase(repo), PoliticaCitas())
            assertIs<UiState.Cargando>(vm.estado.value)
            advanceUntilIdle()
            assertEquals(6, assertIs<UiState.Contenido<Panel>>(vm.estado.value).datos.citas.size)
            vm.simular("Error"); advanceUntilIdle(); assertIs<UiState.Error>(vm.estado.value)
            vm.simular("Vacío"); advanceUntilIdle(); assertIs<UiState.Vacio>(vm.estado.value)
            vm.simular("Normal"); advanceUntilIdle(); assertIs<UiState.Contenido<Panel>>(vm.estado.value)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun filtroSeCombinaYActualizaTrasCancelar() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo = CitaRepositoryFake()
            val politica = PoliticaCitas()
            val vm = CitasViewModel(repo, ObtenerCitasUseCase(repo), ObtenerCatalogoUseCase(repo), politica)
            advanceUntilIdle()
            vm.filtrar("Programada"); vm.buscar("ivan"); advanceUntilIdle()
            assertEquals(1, vm.visibles.value.size)
            CancelarCitaUseCase(repo, politica)(1); advanceUntilIdle()
            assertTrue(vm.visibles.value.isEmpty())
            vm.filtrar("Cancelada"); advanceUntilIdle()
            assertEquals(2, vm.visibles.value.size)
        } finally { Dispatchers.resetMain() }
    }
}
