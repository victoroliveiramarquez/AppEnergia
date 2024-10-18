package com.aplicacion2.appenergia.domain.model

import java.text.SimpleDateFormat
import java.util.Locale


data class Factura(
    val descEstado: String,
    val importeOrdenacion: Double,
    val fecha: String
){

    fun toEntity() : FacturaBDD{
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaLong = simpleDateFormat.parse(fecha)?.time ?: Long.MAX_VALUE
        return FacturaBDD(0, descEstado, importeOrdenacion, fechaLong)
    }
}

