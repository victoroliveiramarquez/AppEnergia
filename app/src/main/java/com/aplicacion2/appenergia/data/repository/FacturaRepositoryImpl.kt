package com.aplicacion2.appenergia.data.repository

import com.aplicacion2.appenergia.data.api.FacturaService
import com.aplicacion2.appenergia.domain.model.Factura
import com.aplicacion2.appenergia.data.db.FacturaDao

class FacturaRepositoryImpl(
    private val api: FacturaService,
    private val facturaDao: FacturaDao
) : FacturaRepository {

    override suspend fun getFacturas(): List<Factura> {
        val facturasDesdeApi = api.getFacturas().facturas
        facturaDao.deleteAll()
        facturaDao.insertAll(facturasDesdeApi)
        return facturaDao.getAllFacturas()
    }

    override suspend fun filtrarFacturas(
        estados: List<String>,
        valorMaximo: Int,
        fechaDesde: Long?,
        fechaHasta: Long?
    ): List<Factura> {

        val estadosFiltrados = if (estados.isEmpty()) {
            listOf("Pagada", "Pendiente de pago", "Anulada", "Cuota fija", "Plan de pago") // Lista por defecto
        } else {
            estados
        }

        val valorMaximoFiltrado = if (valorMaximo <= 0) {
            Int.MAX_VALUE // O cualquier valor máximo que quieras usar
        } else {
            valorMaximo
        }

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