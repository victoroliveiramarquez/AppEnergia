package com.aplicacion2.appenergia.service

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "facturas")
data class Factura(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Hacer que el id se autogenere
    val descEstado: String,
    val importeOrdenacion: Double,
    val fecha: String
)
