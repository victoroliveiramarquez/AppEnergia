package com.aplicacion2.appenergia.service

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FacturaDao {

    @Query("""
    SELECT * FROM facturas 
    WHERE importeOrdenacion <= :valorMaximo
    """)
    suspend fun filterFacturasByValorMaximo(valorMaximo: Int): List<Factura>

    @Query("""
    SELECT * FROM facturas 
    WHERE descEstado IN (:estados)
    """)
    suspend fun filterFacturasByEstados(estados: List<String>): List<Factura>

    @Query("""
    SELECT * FROM facturas 
    WHERE descEstado IN (:estados)
    AND importeOrdenacion <= :valorMaximo
    """)
    suspend fun filterFacturasByEstadoYValor(estados: List<String>, valorMaximo: Int): List<Factura>




    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(facturas: List<Factura>)

    @Query("SELECT * FROM facturas")
    suspend fun getAllFacturas(): List<Factura>

    @Query("DELETE FROM facturas")
    suspend fun deleteAll()

}







