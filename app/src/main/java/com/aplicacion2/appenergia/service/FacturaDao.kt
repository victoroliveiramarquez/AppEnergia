package com.aplicacion2.appenergia.service
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FacturaDao {

    @Query("SELECT * FROM facturas WHERE descEstado LIKE :estado AND fecha BETWEEN :fechaDesde AND :fechaHasta AND importeOrdenacion BETWEEN :minImporte AND :maxImporte")
    suspend fun filterFacturasExact(
        estado: String,
        fechaDesde: String,
        fechaHasta: String,
        minImporte: Double,
        maxImporte: Double
    ): List<Factura>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(facturas: List<Factura>)

    @Query("SELECT * FROM facturas")
    suspend fun getAllFacturas(): List<Factura>

    @Query("DELETE FROM facturas")
    suspend fun deleteAll()
}