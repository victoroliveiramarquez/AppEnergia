package com.aplicacion2.appenergia.data.repository

import android.util.Log
import com.aplicacion2.appenergia.data.api.FacturaService
import com.aplicacion2.appenergia.data.db.FacturaDao
import com.aplicacion2.appenergia.domain.model.FacturaBDD

class FacturaRepositoryImpl(
    private val api: FacturaService,
    private val facturaDao: FacturaDao
) : FacturaRepository {

    // Implementar el método getFacturas definido en la interfaz FacturaRepository
    override suspend fun getFacturas(): List<FacturaBDD> {
        // Verificar si hay facturas almacenadas en Room
        val facturasLocal = facturaDao.getAllFacturas()

        return if (facturasLocal.isEmpty()) {
            // Si no hay facturas en Room, obtener desde la API
            getFacturasFromApi()
        } else {
            // Si ya hay facturas en Room, devolverlas
            facturasLocal
        }
    }

    suspend fun getFacturasFromApi(): List<FacturaBDD> {
        val facturasDesdeApi = api.getFacturas().facturas
        val listaPasada: MutableList<FacturaBDD> = mutableListOf()

        // Verificar cuántas facturas se obtienen de la API
        Log.d("API_DEBUG", "Facturas obtenidas desde la API: ${facturasDesdeApi.size}")

        // Convertir facturas de la API a entidades de Room
        for (i in facturasDesdeApi) {
            listaPasada.add(i.toEntity())
        }

        // Verificar que la conversión a FacturaBDD es correcta
        Log.d("CONVERSION_DEBUG", "Facturas convertidas a FacturaBDD: ${listaPasada.size}")

        // Almacenar las facturas en Room
        facturaDao.deleteAll()
        facturaDao.insertAll(listaPasada)

        // Verificar cuántas facturas se almacenaron en Room
        Log.d("DB_DEBUG", "Facturas almacenadas en Room: ${listaPasada.size}")

        return listaPasada // Devuelve las facturas obtenidas desde la API
    }

    // Obtener facturas desde Room (localmente)
    suspend fun getFacturasFromRoom(): List<FacturaBDD> {
        return facturaDao.getAllFacturas()
    }

    // Filtrar facturas almacenadas en Room
    override suspend fun filtrarFacturas(
        estados: List<String>,
        valorMaximo: Int,
        fechaDesde: Long?,
        fechaHasta: Long?
    ): List<FacturaBDD> {

        // Si la lista de estados está vacía o nula, asignar un valor por defecto
        val estadosFiltrados = if (estados.isEmpty()) {
            listOf("Pagada", "Pendiente de pago", "Anulada", "Cuota fija", "Plan de pago")
        } else {
            estados
        }

        // Si el valorMaximo es 0 o negativo, asignar un valor máximo por defecto
        val valorMaximoFiltrado = if (valorMaximo <= 0) {
            Int.MAX_VALUE
        } else {
            valorMaximo
        }

        // Si la fechaDesde es nula, usar el valor por defecto (0L)
        val fechaDesdeFiltrada = fechaDesde ?: 0L

        // Si la fechaHasta es nula, usar el valor máximo por defecto (Long.MAX_VALUE)
        val fechaHastaFiltrada = fechaHasta ?: Long.MAX_VALUE

        // Llamar al filtro de la base de datos usando los valores filtrados
        return facturaDao.filterFacturasByEstadoYValorYFechas(
            estadosFiltrados,
            valorMaximoFiltrado,
            fechaDesdeFiltrada,
            fechaHastaFiltrada
        )
    }
}
