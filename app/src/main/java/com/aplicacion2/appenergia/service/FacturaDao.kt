package com.aplicacion2.appenergia.service
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FacturaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(facturas: List<Factura>)

    @Query("SELECT * FROM facturas")
    suspend fun getAllFacturas(): List<Factura>

    @Query("DELETE FROM facturas")
    suspend fun deleteAll()
}