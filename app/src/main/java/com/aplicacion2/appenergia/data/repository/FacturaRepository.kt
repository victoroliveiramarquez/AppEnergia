package com.aplicacion2.appenergia.data.repository


import com.aplicacion2.appenergia.domain.model.FacturaBDD


interface FacturaRepository {
    suspend fun getFacturas(): List<FacturaBDD>
    suspend fun filtrarFacturas(
        estados: List<String>,
        valorMaximo: Int,
        fechaDesde: Long?,
        fechaHasta: Long?
    ): List<FacturaBDD>
}