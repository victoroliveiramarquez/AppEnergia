package com.aplicacion2.appenergia.data.db

import androidx.room.*
import com.aplicacion2.appenergia.domain.model.Factura

@Dao
interface FacturaDao {

    @Query("SELECT * FROM facturas WHERE importeOrdenacion <= :valorMaximo")
    suspend fun filterFacturasByValorMaximo(valorMaximo: Int): List<Factura>

    @Query("SELECT * FROM facturas WHERE descEstado IN (:estados) AND importeOrdenacion <= :valorMaximo AND (:fechaDesde IS NULL OR fecha >= :fechaDesde) AND (:fechaHasta IS NULL OR fecha <= :fechaHasta)")
    suspend fun filterFacturasByEstadoYValorYFechas(
        estados: List<String>,
        valorMaximo: Int,
        fechaDesde: Long?,
        fechaHasta: Long?
    ): List<Factura>

    @Query("SELECT * FROM facturas")
    suspend fun getAllFacturas(): List<Factura>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(facturas: List<Factura>)

    @Query("DELETE FROM facturas")
    suspend fun deleteAll()
}









