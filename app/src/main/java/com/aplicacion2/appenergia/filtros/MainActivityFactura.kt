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

        // Verificar si hay filtros en el Intent
        val importe = intent.getDoubleExtra("importe", -1.0)
        val estado = intent.getStringExtra("estado")
        val fechaDesde = intent.getStringExtra("fechaDesde")
        val fechaHasta = intent.getStringExtra("fechaHasta")

        if (importe != -1.0 || !estado.isNullOrEmpty() || !fechaDesde.isNullOrEmpty() || !fechaHasta.isNullOrEmpty()) {
            applyFilters(importe, estado, fechaDesde, fechaHasta)
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

    // Función para aplicar filtros exactos y mostrar solo las facturas filtradas
    private fun applyFilters(importe: Double, estado: String?, fechaDesde: String?, fechaHasta: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            val filteredFacturas = facturaDao.filterFacturasExact(
                estado = estado ?: "%",
                fechaDesde = fechaDesde ?: "01/01/2000",
                fechaHasta = fechaHasta ?: "31/12/2999",
                minImporte = if (importe != -1.0) importe else 0.0,
                maxImporte = if (importe != -1.0) importe else Double.MAX_VALUE
            )

            withContext(Dispatchers.Main) {
                facturaAdapter.updateData(filteredFacturas)
                displayNoFacturasMessage(filteredFacturas.isEmpty()) // Mostrar mensaje si no hay facturas
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
        finish() // Destruir la Activity al presionar el botón "Atrás"
    }
}










