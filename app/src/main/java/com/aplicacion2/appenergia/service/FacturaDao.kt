package com.aplicacion2.appenergia.service
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FacturaDao {

    @Query("""
    SELECT * FROM facturas 
    WHERE 
        (:estado IS NULL OR descEstado = :estado) AND
        (:minImporte IS NULL OR :maxImporte IS NULL OR importeOrdenacion BETWEEN :minImporte AND :maxImporte) AND
        (:fechaDesde IS NULL OR fecha >= :fechaDesde) AND
        (:fechaHasta IS NULL OR fecha <= :fechaHasta)
    """)
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