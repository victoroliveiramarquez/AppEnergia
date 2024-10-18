package com.aplicacion2.appenergia.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "facturasBDD")
data class FacturaBDD(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val descEstado: String,
    val importeOrdenacion: Double,
    val fecha: Long
){
    fun toApi() : Factura{
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Define el formato que quieras
        val date = Date(fecha)
        return Factura(descEstado, importeOrdenacion, dateFormat.format(date))
    }
}