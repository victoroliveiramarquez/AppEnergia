package com.aplicacion2.appenergia.domain.usecase

import com.aplicacion2.appenergia.data.repository.FacturaRepository
import com.aplicacion2.appenergia.domain.model.FacturaBDD


class GetFacturasUseCase(
    private val repository: FacturaRepository
) {
    suspend operator fun invoke(): List<FacturaBDD> {
        // Implementación para cargar facturas
        return repository.getFacturas()
    }
}

