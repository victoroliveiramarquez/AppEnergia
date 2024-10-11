package com.aplicacion2.appenergia.filtros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
        facturaAdapter = FacturaAdapter(emptyList(), this)
        binding.rvFacturas.layoutManager = LinearLayoutManager(this)
        binding.rvFacturas.adapter = facturaAdapter

        // Configurar el botón "Consumo" para que navegue a la Activity SmartSolar
        binding.ibAtras.setOnClickListener {
            val intent = Intent(this, MainActivitySmartSolar::class.java)
            startActivity(intent)
            finish()
        }

        // Configurar el botón "Filtros" para que navegue a la Activity MainActivityFiltroFactura
        binding.imageView.setOnClickListener {
            val intent = Intent(this, MainActivityFiltroFactura::class.java)
            startActivity(intent)
            finish()
        }

        // Cargar y mostrar las facturas desde Room y la API
        loadFacturasFromDatabase()

        // Verificar si hay filtros en el Intent
        val estado = intent.getStringExtra("estado")

        if (!estado.isNullOrEmpty()) {
            applyFilters(estado)
        } else {
            loadFacturasFromApi() // Si no hay filtros, cargar desde la API
        }
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
                displayNoFacturasMessage(facturasFromDb.isEmpty())
            }
        }
    }

    private fun applyFilters(estado: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Filtrar solo si el estado es "Pagada" o "Pendiente de pago".
            val filteredFacturas = if (estado == "Pagada" || estado == "Pendiente de pago") {
                facturaAdapter.updateData(emptyList())
                facturaDao.filterFacturasByEstado(estado)
            } else {
                emptyList() // Si no es "Pagada" o "Pendiente de pago", no hay facturas disponibles.
            }

            withContext(Dispatchers.Main) {
                if (filteredFacturas.isNotEmpty()) {
                    facturaAdapter.updateData(filteredFacturas)
                    displayNoFacturasMessage(false) // Ocultar mensaje de "No hay facturas"
                } else {
                    facturaAdapter.updateData(emptyList())
                    displayNoFacturasMessage(true) // Mostrar mensaje de "No hay facturas"
                }
            }
        }

    }

    // Función para mostrar/ocultar el mensaje de "No hay facturas"
    private fun displayNoFacturasMessage(isVisible: Boolean) {
        binding.tvNoFacturas.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.rvFacturas.visibility = if (isVisible) View.GONE else View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivityPortada::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}











