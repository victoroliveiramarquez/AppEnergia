package com.aplicacion2.appenergia.domain.usecase

import com.aplicacion2.appenergia.data.repository.FacturaRepository
import com.aplicacion2.appenergia.domain.model.FacturaBDD
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FiltrarFacturasUseCaseTest {

    private lateinit var repository: FacturaRepository
    private lateinit var filtrarFacturasUseCase: FiltrarFacturasUseCase

    @Before
    fun onBefore() {
        repository = mock()
        filtrarFacturasUseCase = FiltrarFacturasUseCase(repository)
    }

    @Test
    fun `cuando se aplican todos los filtros, retorna facturas filtradas`(): Unit = runBlocking {
        // Configuración del mock
        val facturasFiltradas = listOf(FacturaBDD(1, "Pagada", 100.0, 1635692400000))
        whenever(repository.filtrarFacturas(listOf("Pagada"), 100, 1635692400000, 1635792400000)).thenReturn(facturasFiltradas)

        // Ejecución del caso de uso
        val resultado = filtrarFacturasUseCase(listOf("Pagada"), 100, 1635692400000, 1635792400000)

        // Verificación
        assertEquals(facturasFiltradas, resultado)
        verify(repository).filtrarFacturas(listOf("Pagada"), 100, 1635692400000, 1635792400000)
    }

    @Test
    fun `cuando se filtra solo por estado, retorna facturas filtradas por estado`(): Unit = runBlocking {
        val facturasFiltradas = listOf(FacturaBDD(2, "Pendiente", 50.0, 1635692400000))
        whenever(repository.filtrarFacturas(listOf("Pendiente"), Int.MAX_VALUE, null, null)).thenReturn(facturasFiltradas)

        val resultado = filtrarFacturasUseCase(listOf("Pendiente"), Int.MAX_VALUE, null, null)

        assertEquals(facturasFiltradas, resultado)
        verify(repository).filtrarFacturas(listOf("Pendiente"), Int.MAX_VALUE, null, null)
    }

    @Test
    fun `cuando se filtra solo por valor maximo, retorna facturas debajo del valor maximo`(): Unit = runBlocking {
        val facturasFiltradas = listOf(FacturaBDD(3, "Pagada", 80.0, 1635692400000))
        whenever(repository.filtrarFacturas(emptyList(), 80, null, null)).thenReturn(facturasFiltradas)

        val resultado = filtrarFacturasUseCase(emptyList(), 80, null, null)

        assertEquals(facturasFiltradas, resultado)
        verify(repository).filtrarFacturas(emptyList(), 80, null, null)
    }

    @Test
    fun `cuando se filtra solo por fecha desde, retorna facturas desde esa fecha`(): Unit = runBlocking {
        val facturasFiltradas = listOf(FacturaBDD(4, "Pagada", 100.0, 1635792400000))
        whenever(repository.filtrarFacturas(emptyList(), Int.MAX_VALUE, 1635792400000, null)).thenReturn(facturasFiltradas)

        val resultado = filtrarFacturasUseCase(emptyList(), Int.MAX_VALUE, 1635792400000, null)

        assertEquals(facturasFiltradas, resultado)
        verify(repository).filtrarFacturas(emptyList(), Int.MAX_VALUE, 1635792400000, null)
    }

    @Test
    fun `cuando se filtra solo por fecha hasta, retorna facturas hasta esa fecha`(): Unit = runBlocking {
        val facturasFiltradas = listOf(FacturaBDD(5, "Pendiente", 60.0, 1635692400000))
        whenever(repository.filtrarFacturas(emptyList(), Int.MAX_VALUE, null, 1635692400000)).thenReturn(facturasFiltradas)

        val resultado = filtrarFacturasUseCase(emptyList(), Int.MAX_VALUE, null, 1635692400000)

        assertEquals(facturasFiltradas, resultado)
        verify(repository).filtrarFacturas(emptyList(), Int.MAX_VALUE, null, 1635692400000)
    }

    @Test
    fun `cuando no se aplican filtros, retorna todas las facturas`(): Unit = runBlocking {
        val todasLasFacturas = listOf(
            FacturaBDD(1, "Pagada", 100.0, 1635692400000),
            FacturaBDD(2, "Pendiente", 50.0, 1635692400000)
        )
        whenever(repository.filtrarFacturas(emptyList(), Int.MAX_VALUE, null, null)).thenReturn(todasLasFacturas)

        val resultado = filtrarFacturasUseCase(emptyList(), Int.MAX_VALUE, null, null)

        assertEquals(todasLasFacturas, resultado)
        verify(repository).filtrarFacturas(emptyList(), Int.MAX_VALUE, null, null)
    }

    @Test
    fun `cuando no hay coincidencias en los filtros, retorna lista vacia`(): Unit = runBlocking {
        whenever(repository.filtrarFacturas(any(), any(), any(), any())).thenReturn(emptyList())

        val resultado = filtrarFacturasUseCase(listOf("Inexistente"), 9999, 9999999999999, 9999999999999)

        assertEquals(emptyList<FacturaBDD>(), resultado)
        verify(repository).filtrarFacturas(listOf("Inexistente"), 9999, 9999999999999, 9999999999999)
    }
}





