package com.aplicacion2.appenergia.service

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "facturas")
data class Factura(
    @PrimaryKey val id: Int,
    val descEstado: String,
    val importeOrdenacion: Double,
    val fecha: String
)
