package com.aplicacion2.appenergia.domain.usecase

import com.aplicacion2.appenergia.data.repository.FacturaRepository
import com.aplicacion2.appenergia.domain.model.Factura


class GetFacturasUseCase(
    private val repository: FacturaRepository
) {
    suspend operator fun invoke(): List<Factura> {
        // Implementación para cargar facturas
        return repository.getFacturas()
    }
}

