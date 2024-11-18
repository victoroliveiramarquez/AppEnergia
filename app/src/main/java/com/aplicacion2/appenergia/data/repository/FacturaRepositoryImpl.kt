package com.aplicacion2.appenergia.data.repository

import android.util.Log
import com.aplicacion2.appenergia.data.api.FacturaService
import com.aplicacion2.appenergia.data.db.FacturaDao
import com.aplicacion2.appenergia.domain.model.FacturaBDD

class FacturaRepositoryImpl(
    private val api: FacturaService,
    private val facturaDao: FacturaDao
) : FacturaRepository {

    override suspend fun getFacturas(): List<FacturaBDD> {
        // Verifico si hay facturas almacenadas en Room
        val facturasLocal = facturaDao.getAllFacturas()

        return facturasLocal.ifEmpty {
            // Si no hay facturas en Room, obtener desde la API
            getFacturasFromApi()
        }
    }

    suspend fun getFacturasFromApi(): List<FacturaBDD> {
        val facturasDesdeApi = api.getFacturas().facturas
        val listaPasada: MutableList<FacturaBDD> = mutableListOf()

        // Verifico cuántas facturas se obtienen de la API con un LOG
        Log.d("API_DEBUG", "Facturas obtenidas desde la API: ${facturasDesdeApi.size}")

        for (i in facturasDesdeApi) {
            listaPasada.add(i.toEntity())
        }

        // Verifico que la conversión a FacturaBDD es correcta con un LOG
        Log.d("CONVERSION_DEBUG", "Facturas convertidas a FacturaBDD: ${listaPasada.size}")

        // Almaceno las facturas en Room
        facturaDao.deleteAll()
        facturaDao.insertAll(listaPasada)

        // Verifico cuántas facturas se almacenaron en Room con un LOG
        Log.d("DB_DEBUG", "Facturas almacenadas en Room: ${listaPasada.size}")

        return listaPasada // Devuelvo las facturas obtenidas desde la API
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

        // Si la lista de estados está vacía o nula, asigno un valor por defecto
        val estadosFiltrados = if (estados.isEmpty()) {
            listOf("Pagada", "Pendiente de pago", "Anulada", "Cuota Fija", "Plan de pago")
        } else {
            estados
        }

        // Si el valorMaximo es 0 o negativo, asigno un valor máximo por defecto
        val valorMaximoFiltrado = if (valorMaximo <= 0) {
            Int.MAX_VALUE
        } else {
            valorMaximo
        }

        // Si la fechaDesde es nula, usar el valor por defecto (0L)
        val fechaDesdeFiltrada = fechaDesde ?: 0L

        // Si la fechaHasta es nula, usar el valor máximo por defecto (Long.MAX_VALUE)
        val fechaHastaFiltrada = fechaHasta ?: Long.MAX_VALUE

        // Llamo al filtro de la base de datos usando los valores filtrados
        return facturaDao.filterFacturasByEstadoYValorYFechas(
            estadosFiltrados,
            valorMaximoFiltrado,
            fechaDesdeFiltrada,
            fechaHastaFiltrada
        )
    }
}
