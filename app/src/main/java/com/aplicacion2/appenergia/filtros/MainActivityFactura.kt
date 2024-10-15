package com.aplicacion2.appenergia.filtros

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aplicacion2.appenergia.samartsolar.MainActivitySmartSolar
import com.aplicacion2.appenergia.service.Factura
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

    // Contenedor intermediario para almacenar las facturas filtradas
    private var filteredFacturasContainer: MutableList<Factura> = mutableListOf()

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

        // Botón "Consumo" para navegar a la Activity SmartSolar
        binding.ibAtras.setOnClickListener {
            val intent = Intent(this, MainActivitySmartSolar::class.java)
            startActivity(intent)
            finish()
        }

        // Botón "Filtros" para navegar a la Activity MainActivityFiltroFactura
        binding.imageView.setOnClickListener {
            val intent = Intent(this, MainActivityFiltroFactura::class.java)
            startActivity(intent)
            finish()
        }


        // Recuperar los estados y el valor del SeekBar del Intent
        val estados = intent.getStringArrayListExtra("estados") ?: emptyList()
        val valorMaximo = intent.getDoubleExtra("maxValueSlider", Double.MAX_VALUE)

        // Aplicar filtros a las facturas en función de los estados seleccionados y el valor del SeekBar
        if (estados.isNotEmpty()) {
            applyFilters(estados, valorMaximo)
        } else {
            // Si no hay filtros, cargar las facturas desde la base de datos
            loadFacturasFromDatabase()
        }
    }


    // Función para cargar las facturas desde la base de datos Room
    private fun loadFacturasFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val facturasFromDb = facturaDao.getAllFacturas()
            withContext(Dispatchers.Main) {
                // Almacenar las facturas obtenidas
                filteredFacturasContainer.clear()
                filteredFacturasContainer.addAll(facturasFromDb)

                // Actualizar el RecyclerView
                facturaAdapter.updateData(filteredFacturasContainer)
                displayNoFacturasMessage(filteredFacturasContainer.isEmpty())
            }
        }
    }

    // Función para aplicar filtros
    private fun applyFilters(estados: List<String>, valorMaximo: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            val estadosValidos = estados.filter { it == "Pagada" || it == "Pendiente de pago" }

            if (estadosValidos.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivityFactura, "No hay facturas disponibles para los filtros seleccionados", Toast.LENGTH_SHORT).show()
                    displayNoFacturasMessage(true)
                }
                return@launch
            }

            // Filtrar las facturas por estado y valor del SeekBar (importe máximo)
            val filteredFacturas = facturaDao.filterFacturasByEstadoYValor(estadosValidos, valorMaximo.toInt())

            withContext(Dispatchers.Main) {
                // Llenar `filteredFacturasContainer` con las facturas filtradas
                filteredFacturasContainer.clear()
                filteredFacturasContainer.addAll(filteredFacturas)

                // Actualizar el RecyclerView con las facturas filtradas
                facturaAdapter.updateData(filteredFacturasContainer)
                displayNoFacturasMessage(filteredFacturasContainer.isEmpty())
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




















