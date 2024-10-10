package com.aplicacion2.appenergia.service

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Factura::class], version = 1)
abstract class FacturaDatabase : RoomDatabase() {
    abstract fun facturaDao(): FacturaDao

    companion object {
        @Volatile
        private var INSTANCE: FacturaDatabase? = null

        fun getDatabase(context: Context): FacturaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FacturaDatabase::class.java,
                    "factura_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}