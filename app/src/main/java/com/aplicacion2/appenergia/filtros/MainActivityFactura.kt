package com.aplicacion2.appenergia.filtros

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aplicacion2.appenergia.samartsolar.MainActivitySmartSolar
import com.aplicacion2.appenergia.service.*
import com.example.facturas_tfc.databinding.ActivityMainFacturaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class MainActivityFactura : AppCompatActivity() {

    private lateinit var binding: ActivityMainFacturaBinding
    private lateinit var facturaAdapter: FacturaAdapter
    private lateinit var facturaDao: FacturaDao

    // Contenedor para almacenar las facturas filtradas
    private var filteredFacturasContainer: MutableList<Factura> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainFacturaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener la instancia de la base de datos
        val db = FacturaDatabase.getDatabase(this)
        facturaDao = db.facturaDao()

        // Configurar el RecyclerView
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

        // Obtener los filtros del Intent
        val estados = intent.getStringArrayListExtra("estados") ?: emptyList()
        val valorMaximo = intent.getDoubleExtra("valorMaximo", Double.MAX_VALUE)

        // Si hay filtros, aplicarlos
        if (estados.isNotEmpty() || valorMaximo != Double.MAX_VALUE) {
            applyFilters(estados, valorMaximo)
        } else {
            // Cargar todas las facturas desde la base de datos si no hay filtros
            loadFacturasFromDatabase()
        }
    }

    // Función para cargar las facturas desde la API solo si la base de datos está vacía
    private fun cargarFacturasSiEsNecesario() {
        lifecycleScope.launch(Dispatchers.IO) {
            val facturasCount = facturaDao.getCountFacturas()

            if (facturasCount == 0) {
                // Si no hay facturas en la base de datos, cargarlas desde la API
                cargarFacturasDesdeAPI()
            } else {
                // Si ya hay facturas, solo cargarlas desde la base de datos local
                loadFacturasFromDatabase()
            }
        }
    }

    // Función para cargar las facturas desde la API y almacenarlas en Room
    private fun cargarFacturasDesdeAPI() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.facturaService.getFacturas()

                // Guardar las facturas en la base de datos local
                facturaDao.deleteAll() // Limpiar la base de datos antes de insertar nuevas facturas
                facturaDao.insertAll(response.facturas)

                // Una vez almacenadas las facturas, cargarlas desde la base de datos local
                withContext(Dispatchers.Main) {
                    loadFacturasFromDatabase()
                    Toast.makeText(this@MainActivityFactura, "Facturas cargadas desde la API", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivityFactura, "Error al cargar las facturas desde la API", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // Función para cargar las facturas desde la base de datos Room
    private fun loadFacturasFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val facturasFromDb = facturaDao.getAllFacturas()
            withContext(Dispatchers.Main) {
                filteredFacturasContainer.clear()
                filteredFacturasContainer.addAll(facturasFromDb)
                facturaAdapter.updateData(filteredFacturasContainer)
                displayNoFacturasMessage(filteredFacturasContainer.isEmpty())
            }
        }
    }

    // Aplicar los filtros por estados y valor del SeekBar localmente
    private fun applyFilters(estados: List<String>, valorMaximo: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Filtrar los datos de Room en función de los filtros seleccionados
            val filteredFacturas = when {
                // Caso 1: Filtrar por estado y valor máximo
                estados.isNotEmpty() && valorMaximo != Double.MAX_VALUE -> {
                    facturaDao.filterFacturasByEstadoYValor(estados, valorMaximo.toInt())
                }
                // Caso 2: Filtrar solo por estado
                estados.isNotEmpty() -> {
                    facturaDao.filterFacturasByEstados(estados)
                }
                // Caso 3: Filtrar solo por valor máximo
                valorMaximo != Double.MAX_VALUE -> {
                    facturaDao.filterFacturasByValorMaximo(valorMaximo.toInt())
                }
                // Caso 4: Si no hay filtros, mostrar todas las facturas
                else -> {
                    facturaDao.getAllFacturas()
                }
            }

            // Actualizar la UI con las facturas filtradas
            withContext(Dispatchers.Main) {
                filteredFacturasContainer.clear()
                filteredFacturasContainer.addAll(filteredFacturas)
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



















