package com.aplicacion2.appenergia.presentation.ui

import FacturaViewModel
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aplicacion2.appenergia.data.api.FacturaAdapter
import com.aplicacion2.appenergia.data.api.FacturaDatabase
import com.aplicacion2.appenergia.data.api.FacturaService
import com.aplicacion2.appenergia.data.api.MockService
import com.aplicacion2.appenergia.data.api.RetrofitClient
import com.aplicacion2.appenergia.data.repository.FacturaRepositoryImpl
import com.aplicacion2.appenergia.domain.model.Factura
import com.aplicacion2.appenergia.domain.usecase.FiltrarFacturasUseCase
import com.aplicacion2.appenergia.domain.usecase.GetFacturasUseCase
import com.aplicacion2.appenergia.presentation.viewmodel.FacturaViewModelFactory
import com.example.facturas_tfc.databinding.ActivityMainFacturaBinding
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivityFactura : AppCompatActivity() {

    private lateinit var binding: ActivityMainFacturaBinding
    private lateinit var facturaAdapter: FacturaAdapter
    private lateinit var facturaViewModel: FacturaViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var repository : FacturaRepositoryImpl

    // Instancia del servicio de mock (Retromock) usando tu MockService
    private val mockFacturaService: MockService by lazy {
        RetrofitClient.retromock.create(MockService::class.java)
    }

    // Instancia del servicio real (Retrofit) usando el servicio real
    private val apiFacturaService: FacturaService by lazy {
        RetrofitClient.instance.create(FacturaService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainFacturaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar el contexto en RetrofitClient para usar Retromock correctamente
        RetrofitClient.initContext(this)

        // Inicializar el ViewModel antes de cualquier acceso a él
        val facturaDao = FacturaDatabase.getDatabase(this).facturaDao()

        repository = FacturaRepositoryImpl(apiFacturaService, facturaDao)

        val getFacturasUseCase = GetFacturasUseCase(repository)
        val filtrarFacturasUseCase = FiltrarFacturasUseCase(repository)

        val factory = FacturaViewModelFactory(getFacturasUseCase, filtrarFacturasUseCase)
        facturaViewModel = ViewModelProvider(this, factory)[FacturaViewModel::class.java]

        // Inicializar SharedPreferences antes de su uso
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Restablecer filtros a los valores por defecto al iniciar
        restablecerFiltrosPorDefecto()


        // Inicializar el RecyclerView
        facturaAdapter = FacturaAdapter(emptyList(), this)
        binding.rvFacturas.layoutManager = LinearLayoutManager(this)
        binding.rvFacturas.adapter = facturaAdapter

        // Cargar facturas de acuerdo con el estado del switch
        if (MainActivityPortada.mocksEnabled) {
            // Si los mocks están activados, usar el servicio simulado
            gestionarCargarFacturasDesdeMock()
        } else {
            // Si los mocks están desactivados, usar el servicio real (API)
            gestionarCargarFacturasDesdeApi()
        }

        // Observa los cambios en las facturas filtradas
        facturaViewModel.facturasBDD.observe(this) { facturas ->
            val listaPasada: MutableList<Factura> = mutableListOf()
            for (i in facturas) {
                listaPasada.add(i.toApi())
            }
            facturaAdapter.updateData(listaPasada)
            displayNoFacturasMessage(facturas.isEmpty())
        }

        // Botón para navegar a la Activity de filtros
        binding.imageView.setOnClickListener {
            val intent = Intent(this, MainActivityFiltroFactura::class.java)
            startActivity(intent)
        }

        // Botón para navegar atrás (portada)
        binding.ibAtras.setOnClickListener {
            val intent = Intent(this, MainActivityPortada::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun gestionarCargarFacturasDesdeMock() {
        try {
            lifecycleScope.launch {
                // Obtener los filtros del Intent
                val estados = intent.getStringArrayListExtra("estados") ?: emptyList()
                val valorMaximo = intent.getDoubleExtra("valorMaximo", Double.MAX_VALUE).toInt()
                val fechaDesdeMillis = intent.getLongExtra("fechaDesde", 0L)
                val fechaHastaMillis = intent.getLongExtra("fechaHasta", Long.MAX_VALUE)

                if (estados.isNotEmpty() || valorMaximo != Double.MAX_VALUE.toInt() || fechaDesdeMillis > 0 || fechaHastaMillis < Long.MAX_VALUE) {
                    // Aplicar filtros desde el ViewModel
                    filtrado(estados, valorMaximo, fechaDesdeMillis, fechaHastaMillis)
                } else {
                    val response = mockFacturaService.getFacturas()
                    val facturasMock = response.facturas.toMutableList()
                    // Actualizar la lista del adaptador con las facturas filtradas de mocks
                    facturaAdapter.updateData(facturasMock)
                    displayNoFacturasMessage(facturasMock.isEmpty())

                    // Almacenar solo el mock mostrado en la base de datos
                    guardarFacturasEnBaseDeDatos(facturasMock)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar facturas desde Mock: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun guardarFacturasEnBaseDeDatos(facturas: List<Factura>) {
        val facturaDao = FacturaDatabase.getDatabase(this).facturaDao()
        lifecycleScope.launch {
            val facturasBDD = facturas.map { it.toEntity() }  // Convierte las facturas mostradas a FacturaBDD
            facturaDao.deleteAll()
            facturaDao.insertAll(facturasBDD)  // Inserta solo el mock mostrado en la base de datos
        }
    }

    // Función para gestionar la carga de facturas desde la API real
    private fun gestionarCargarFacturasDesdeApi() {
        try {
            val esPrimeraCarga = sharedPreferences.getBoolean("primeraCarga", true)

            // Obtener los filtros del Intent
            val estados = intent.getStringArrayListExtra("estados") ?: emptyList()
            val valorMaximo = intent.getDoubleExtra("valorMaximo", Double.MAX_VALUE).toInt()
            val fechaDesdeMillis = intent.getLongExtra("fechaDesde", 0L)
            val fechaHastaMillis = intent.getLongExtra("fechaHasta", Long.MAX_VALUE)

            if (esPrimeraCarga) {
                // Cargar facturas desde la API por primera vez
                facturaViewModel.cargarFacturasPorPrimeraVez()
                // Guardar en SharedPreferences que ya no es la primera carga
                sharedPreferences.edit().putBoolean("primeraCarga", false).commit()
            } else if (estados.isNotEmpty() || valorMaximo != Double.MAX_VALUE.toInt() || fechaDesdeMillis > 0 || fechaHastaMillis < Long.MAX_VALUE) {
                // Aplicar filtros desde el ViewModel
                filtrado(estados, valorMaximo, fechaDesdeMillis, fechaHastaMillis)
            } else {
                // Si no hay filtros, cargar todas las facturas desde Room o desde la API
                facturaViewModel.cargarFacturas()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar facturas: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun filtrado(
        estados: List<String>,
        valorMaximo: Int,
        fechaDesdeMillis: Long,
        fechaHastaMillis: Long
    ) {
        facturaViewModel.aplicarFiltros(
            estados,
            valorMaximo,
            fechaDesdeMillis,
            fechaHastaMillis
        )
    }

    // Restablecer los filtros a sus valores por defecto
    private fun restablecerFiltrosPorDefecto() {
        val editor = sharedPreferences.edit()
        editor.putStringSet("estados", emptySet()) // Restablecer estados
        editor.putInt("valorMaximo", 0) // Restablecer valor máximo
        editor.putLong("fechaDesde", 0L) // Restablecer fecha desde
        editor.putLong("fechaHasta", Long.MAX_VALUE) // Restablecer fecha hasta
        editor.apply()
    }

    // Mostrar/ocultar el mensaje de "No hay facturas"
    private fun displayNoFacturasMessage(isVisible: Boolean) {
        binding.tvNoFacturas.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.rvFacturas.visibility = if (isVisible) View.GONE else View.VISIBLE
    }

    override fun onBackPressed() {
        val sharedPreferences = getSharedPreferences("FiltroFacturasPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply() // Limpia los filtros
        super.onBackPressed() // Regresa a la actividad anterior
    }

}


























