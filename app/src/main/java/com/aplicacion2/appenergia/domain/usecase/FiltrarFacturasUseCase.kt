package com.aplicacion2.appenergia.domain.usecase

import com.aplicacion2.appenergia.data.repository.FacturaRepository
import com.aplicacion2.appenergia.domain.model.Factura


class FiltrarFacturasUseCase(
    private val repository: FacturaRepository
) {
    suspend operator fun invoke(
        estados: List<String>,
        valorMaximo: Int,
        fechaDesde: Long?,
        fechaHasta: Long?
    ): List<Factura> {
        // Filtrar facturas en base a los criterios
        return repository.filtrarFacturas(estados, valorMaximo, fechaDesde, fechaHasta)
    }
}