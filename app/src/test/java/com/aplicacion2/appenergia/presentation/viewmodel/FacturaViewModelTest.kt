package com.aplicacion2.appenergia.presentation.viewmodel

import FacturaViewModel
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.aplicacion2.appenergia.domain.model.Factura
import com.aplicacion2.appenergia.domain.model.FacturaBDD
import com.aplicacion2.appenergia.domain.usecase.FiltrarFacturasUseCase
import com.aplicacion2.appenergia.domain.usecase.GetFacturasUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class FacturaViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule() // Para ejecutar LiveData inmediatamente

    @Mock
    private lateinit var getFacturasUseCase: GetFacturasUseCase

    @Mock
    private lateinit var filtrarFacturasUseCase: FiltrarFacturasUseCase

    @Mock
    private lateinit var observer: Observer<List<FacturaBDD>> // Observador para verificar cambios en LiveData

    private lateinit var viewModel: FacturaViewModel

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    @Before
    fun onBefore() {
        Dispatchers.setMain(testDispatcher) // Configura el Dispatcher Main para pruebas
        viewModel = FacturaViewModel(getFacturasUseCase, filtrarFacturasUseCase)
        viewModel.facturasBDD.observeForever(observer) // Observa los cambios en LiveData
    }

    @After
    fun tearDown() {
        viewModel.facturasBDD.removeObserver(observer)
        Dispatchers.resetMain() // Restablece el Dispatcher Main
    }

    @Test
    fun `cuando cargarFacturas se llama, se cargan facturas desde Room`() = runTest(testDispatcher) {
        // Given
        val facturas = listOf(FacturaBDD(1, "Pagada", 100.0, 1635692400000))
        whenever(getFacturasUseCase()).thenReturn(facturas)

        // When
        viewModel.cargarFacturas()
        testScheduler.advanceUntilIdle() // Avanza el tiempo de la corrutina

        // Then
        verify(observer).onChanged(facturas) // Verifica que LiveData se actualizó con las facturas
        verify(getFacturasUseCase).invoke() // Verifica que se llamó a getFacturasUseCase sin forzar la API
    }

    @Test
    fun `cuando cargarFacturas se llama y no hay facturas, actualiza LiveData con lista vacia`() = runTest(testDispatcher) {
        // Given
        whenever(getFacturasUseCase()).thenReturn(emptyList())

        // When
        viewModel.cargarFacturas()
        testScheduler.advanceUntilIdle()

        // Then
        verify(observer).onChanged(emptyList())
        verify(getFacturasUseCase).invoke()
    }

    @Test
    fun `cuando aplicarFiltros se llama con filtros, retorna facturas que cumplen los criterios`() = runTest(testDispatcher) {
        // Given
        val facturasFiltradas = listOf(FacturaBDD(2, "Pendiente", 50.0, 1635692400000))
        whenever(filtrarFacturasUseCase(listOf("Pendiente"), 100, null, null)).thenReturn(facturasFiltradas)

        // When
        viewModel.aplicarFiltros(listOf("Pendiente"), 100, null, null)
        testScheduler.advanceUntilIdle()

        // Then
        verify(observer).onChanged(facturasFiltradas)
        verify(filtrarFacturasUseCase).invoke(listOf("Pendiente"), 100, null, null)
    }

    @Test
    fun `cuando aplicarFiltros se llama y no hay coincidencias, actualiza LiveData con lista vacia`() = runTest(testDispatcher) {
        // Given
        whenever(filtrarFacturasUseCase(any(), any(), anyOrNull(), anyOrNull())).thenReturn(emptyList())

        // When
        viewModel.aplicarFiltros(listOf("Inexistente"), 9999, null, null)
        testScheduler.advanceUntilIdle()

        // Then
        verify(observer).onChanged(emptyList())
        verify(filtrarFacturasUseCase).invoke(listOf("Inexistente"), 9999, null, null)
    }

    @Test
    fun `cuando cargarFacturasPorPrimeraVez se llama, se cargan facturas desde la API`() = runTest(testDispatcher) {
        // Given
        val facturasApi = listOf(FacturaBDD(3, "Pagada", 75.0, 1635692400000))
        whenever(getFacturasUseCase(forceApi = true)).thenReturn(facturasApi)

        // When
        viewModel.cargarFacturasPorPrimeraVez()
        testScheduler.advanceUntilIdle()

        // Then
        verify(observer).onChanged(facturasApi)
        verify(getFacturasUseCase).invoke(true) // Verifica que se forzó la carga desde la API
    }

    @Test
    fun `cuando cargarFacturasPorPrimeraVez se llama y la API devuelve lista vacia, actualiza LiveData con lista vacia`() = runTest(testDispatcher) {
        // Given
        whenever(getFacturasUseCase(forceApi = true)).thenReturn(emptyList())

        // When
        viewModel.cargarFacturasPorPrimeraVez()
        testScheduler.advanceUntilIdle()

        // Then
        verify(observer).onChanged(emptyList())
        verify(getFacturasUseCase).invoke(true)
    }

    @Test
    fun `cuando cargarFacturasDesdeMock se llama con lista de facturas, actualiza LiveData con esta lista`() = runTest(testDispatcher) {
        // Given
        val facturas = listOf(
            Factura("Pendiente", 30.0, "01/01/2022"),
            Factura("Pagada", 50.0, "01/02/2022")
        )
        val facturasBDD = facturas.map { it.toEntity() } // Convierte a FacturaBDD

        // When
        viewModel.cargarFacturasDesdeMock(facturas)
        testScheduler.advanceUntilIdle()

        // Then
        verify(observer).onChanged(facturasBDD)
    }

    @Test
    fun `cuando cargarFacturasDesdeMock se llama con lista vacia, actualiza LiveData con lista vacia`() = runTest(testDispatcher) {
        // When
        viewModel.cargarFacturasDesdeMock(emptyList())
        testScheduler.advanceUntilIdle()

        // Then
        verify(observer).onChanged(emptyList())
    }
}
