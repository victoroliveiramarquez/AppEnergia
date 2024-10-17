package com.aplicacion2.appenergia.data.api

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aplicacion2.appenergia.data.db.FacturaDao
import com.aplicacion2.appenergia.domain.model.Factura

@Database(entities = [Factura::class], version = 2)
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
                    "factura-database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}