package com.aplicacion2.appenergia.data.api

import com.aplicacion2.appenergia.domain.model.Factura

data class FacturaResponse(
    val numFacturas: Int,
    val facturas: List<Factura>
)

