package com.aplicacion2.appenergia.data.repository

import com.aplicacion2.appenergia.data.api.FacturaService
import com.aplicacion2.appenergia.domain.model.Factura
import com.aplicacion2.appenergia.data.db.FacturaDao
import com.aplicacion2.appenergia.domain.model.FacturaBDD

class FacturaRepositoryImpl(
    private val api: FacturaService,
    private val facturaDao: FacturaDao
) : FacturaRepository {

    override suspend fun getFacturas(): List<FacturaBDD> {
        val facturasDesdeApi = api.getFacturas().facturas
        val listaPAsada : MutableList<FacturaBDD> = mutableListOf()
        facturaDao.deleteAll()
        for(i in facturasDesdeApi){
            listaPAsada.add(i.toEntity())
        }
        facturaDao.insertAll(listaPAsada)
        return facturaDao.getAllFacturas()
    }

    override suspend fun filtrarFacturas(
        estados: List<String>,
        valorMaximo: Int,
        fechaDesde: Long?,
        fechaHasta: Long?
    ): List<FacturaBDD> {

        // Si la lista de estados está vacía o nula, asignar un valor por defecto
        val estadosFiltrados = if (estados.isEmpty()) {
            listOf("Pagada", "Pendiente de pago", "Anulada", "Cuota fija", "Plan de pago") // Lista por defecto
        } else {
            estados
        }

        // Si el valorMaximo es 0 o negativo, asignar un valor máximo por defecto
        val valorMaximoFiltrado = if (valorMaximo <= 0) {
            Int.MAX_VALUE // O cualquier valor máximo que quieras usar
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