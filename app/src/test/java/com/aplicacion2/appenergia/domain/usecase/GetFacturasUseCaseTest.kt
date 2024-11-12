package com.aplicacion2.appenergia.domain.usecase

import com.aplicacion2.appenergia.data.repository.FacturaRepositoryImpl
import com.aplicacion2.appenergia.domain.model.FacturaBDD
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetFacturasUseCaseTest {

    private lateinit var facturaRepository: FacturaRepositoryImpl
    private lateinit var getFacturasUseCase: GetFacturasUseCase

    @Before
    fun onBefore() {
        // Crear el mock del repositorio
        facturaRepository = mock()
        getFacturasUseCase = GetFacturasUseCase(facturaRepository)
    }

    @Test
    fun `cuando forceApi es true, retorna facturas desde la API`(): Unit = runBlocking {
        // Given: Configuro el mock para devolver una lista desde la API
        val facturasFromApi = listOf(
            FacturaBDD(1, "Pagada", 20.0, 1635692400000),
            FacturaBDD(2, "Pendiente de pago", 50.0, 1635792400000)
        )
        whenever(facturaRepository.getFacturasFromApi()).thenReturn(facturasFromApi)

        // When: Llamo al caso de uso con forceApi = true
        val resultado = getFacturasUseCase(forceApi = true)

        // Then: Verifico que el resultado sea el esperado y que se llame al método de la API
        assertEquals(facturasFromApi, resultado)
        verify(facturaRepository).getFacturasFromApi()
    }

    @Test
    fun `cuando forceApi es false, retorna facturas desde Room`(): Unit = runBlocking {
        // Given: Configuro el mock para devolver una lista desde Room
        val facturasFromRoom = listOf(
            FacturaBDD(3, "Pagada", 30.0, 1635892400000),
            FacturaBDD(4, "Pendiente de pago", 40.0, 1635992400000)
        )
        whenever(facturaRepository.getFacturasFromRoom()).thenReturn(facturasFromRoom)

        // When: Llamo al caso de uso con forceApi = false
        val resultado = getFacturasUseCase(forceApi = false)

        // Then: Verifico que el resultado sea el esperado y que se llame al método de Room
        assertEquals(facturasFromRoom, resultado)
        verify(facturaRepository).getFacturasFromRoom()
    }

}
