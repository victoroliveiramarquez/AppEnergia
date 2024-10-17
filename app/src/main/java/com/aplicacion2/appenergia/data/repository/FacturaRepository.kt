package com.aplicacion2.appenergia.data.repository


import com.aplicacion2.appenergia.domain.model.Factura


interface FacturaRepository {
    suspend fun getFacturas(): List<Factura>
    suspend fun filtrarFacturas(
        estados: List<String>,
        valorMaximo: Int,
        fechaDesde: Long?,
        fechaHasta: Long?
    ): List<Factura>
}