package com.aplicacion2.appenergia.domain.usecase

import com.aplicacion2.appenergia.data.repository.FacturaRepositoryImpl
import com.aplicacion2.appenergia.domain.model.FacturaBDD


class GetFacturasUseCase(
    private val facturaRepository: FacturaRepositoryImpl
) {
    suspend operator fun invoke(forceApi: Boolean = false): List<FacturaBDD> {
        return if (forceApi) {
            // Forzar la llamada a la API
            facturaRepository.getFacturasFromApi()
        } else {
            // Obtener las facturas desde Room
            facturaRepository.getFacturasFromRoom()
        }
    }
}


