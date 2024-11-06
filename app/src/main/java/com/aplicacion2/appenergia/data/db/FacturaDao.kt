package com.aplicacion2.appenergia.data.db

import androidx.room.*
import com.aplicacion2.appenergia.domain.model.FacturaBDD

@Dao
interface FacturaDao {


    @Query("SELECT * FROM facturasBDD WHERE descEstado IN (:estados) AND importeOrdenacion <= :valorMaximo AND (:fechaDesde IS NULL OR fecha >= :fechaDesde) AND (:fechaHasta IS NULL OR fecha <= :fechaHasta)")
    suspend fun filterFacturasByEstadoYValorYFechas(
        estados: List<String>,
        valorMaximo: Int,
        fechaDesde: Long?,
        fechaHasta: Long?
    ): List<FacturaBDD>

    @Query("SELECT * FROM facturasBDD")
    suspend fun getAllFacturas(): List<FacturaBDD>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(facturas: List<FacturaBDD>)

    @Query("DELETE FROM facturasBDD")
    suspend fun deleteAll()

}









