package com.aplicacion2.appenergia.filtros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aplicacion2.appenergia.samartsolar.MainActivitySmartSolar
import com.aplicacion2.appenergia.service.FacturaAdapter
import com.aplicacion2.appenergia.service.FacturaDao
import com.aplicacion2.appenergia.service.FacturaDatabase
import com.aplicacion2.appenergia.service.RetrofitClient
import com.example.facturas_tfc.databinding.ActivityMainFacturaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class MainActivityFactura : AppCompatActivity() {

    private lateinit var binding: ActivityMainFacturaBinding
    private lateinit var facturaAdapter: FacturaAdapter
    private lateinit var facturaDao: FacturaDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityMainFacturaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar la base de datos y DAO de Room
        val db = FacturaDatabase.getDatabase(this)
        facturaDao = db.facturaDao()

        // Configurar RecyclerView
        facturaAdapter = FacturaAdapter(emptyList(), this) // Pasar el contexto actual
        binding.rvFacturas.layoutManager = LinearLayoutManager(this)
        binding.rvFacturas.adapter = facturaAdapter

        // Configurar el botón "Consumo" para que navegue a la Activity SmartSolar
        binding.ibAtras.setOnClickListener {
            val intent = Intent(this, MainActivitySmartSolar::class.java)
            startActivity(intent)
            finish() // Finalizar la Activity actual para destruirla
        }

        // Configurar el botón "Filtros" para que navegue a la Activity MainActivityFiltroFactura
        binding.imageView.setOnClickListener {
            val intent = Intent(this, MainActivityFiltroFactura::class.java)
            startActivity(intent)
            finish() // Finalizar la Activity actual para destruirla
        }

        // Cargar y mostrar las facturas desde Room y la API
        loadFacturasFromDatabase()
        loadFacturasFromApi()
    }

    // Función para cargar las facturas desde la API y almacenarlas en Room
    private fun loadFacturasFromApi() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val facturasService = RetrofitClient.facturaService
                val response = facturasService.getFacturas() // Llamada a la API

                // Insertar los datos en Room
                facturaDao.deleteAll()
                facturaDao.insertAll(response.facturas)

                // Actualizar la UI con los datos de la base de datos
                loadFacturasFromDatabase()
            } catch (e: Exception) {
                Log.e("MainActivityFactura", "Error al cargar las facturas desde la API: ${e.message}")
            }
        }
    }

    // Función para cargar las facturas desde la base de datos Room
    private fun loadFacturasFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val facturasFromDb = facturaDao.getAllFacturas()
            withContext(Dispatchers.Main) {
                facturaAdapter.updateData(facturasFromDb)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivityPortada::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish() // Destruir la Activity al presionar el botón "Atrás"
    }
}





