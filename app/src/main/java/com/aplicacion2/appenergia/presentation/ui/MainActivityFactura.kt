package com.aplicacion2.appenergia.presentation.ui

import FacturaViewModel
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
import com.aplicacion2.appenergia.data.api.RetrofitClient
import com.aplicacion2.appenergia.data.repository.FacturaRepositoryImpl
import com.aplicacion2.appenergia.domain.model.Factura
import com.aplicacion2.appenergia.domain.usecase.FiltrarFacturasUseCase
import com.aplicacion2.appenergia.domain.usecase.GetFacturasUseCase
import com.aplicacion2.appenergia.presentation.viewmodel.FacturaViewModelFactory
import com.aplicacion2.appenergia.samartsolar.MainActivitySmartSolar
import com.example.facturas_tfc.databinding.ActivityMainFacturaBinding
import com.aplicacion2.appenergia.data.api.FacturaService
import com.aplicacion2.appenergia.data.api.MockService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivityFactura : AppCompatActivity() {

    private lateinit var binding: ActivityMainFacturaBinding
    private lateinit var facturaAdapter: FacturaAdapter
    private lateinit var facturaViewModel: FacturaViewModel
    private lateinit var sharedPreferences: SharedPreferences

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
        val repository = FacturaRepositoryImpl(apiFacturaService, facturaDao)

        val getFacturasUseCase = GetFacturasUseCase(repository)
        val filtrarFacturasUseCase = FiltrarFacturasUseCase(repository)

        val factory = FacturaViewModelFactory(getFacturasUseCase, filtrarFacturasUseCase)
        facturaViewModel = ViewModelProvider(this, factory)[FacturaViewModel::class.java]

        // Inicializar SharedPreferences antes de su uso
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Restablecer filtros a los valores por defecto al iniciar
        restablecerFiltrosPorDefecto()

        // Obtener el estado de mocks desde el Intent
        val mocksEnabled = intent.getBooleanExtra("MOCKS_ENABLED", false)


        // Inicializar el RecyclerView
        facturaAdapter = FacturaAdapter(emptyList(), this)
        binding.rvFacturas.layoutManager = LinearLayoutManager(this)
        binding.rvFacturas.adapter = facturaAdapter



        // Cargar facturas de acuerdo con el estado del switch
        if (mocksEnabled) {
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
            finish()
        }

        // Botón para navegar a la SmartaSolar
        binding.ibAtras.setOnClickListener {
            val intent = Intent(this, MainActivitySmartSolar::class.java)
            startActivity(intent)
            finish()
        }

    }
    private fun limpiarBaseDeDatos() {
        val facturaDao = FacturaDatabase.getDatabase(this).facturaDao()
        lifecycleScope.launch {
            facturaDao.deleteAll()  // Método para eliminar todas las facturas de la base de datos
        }
    }

    private fun gestionarCargarFacturasDesdeMock() {
        try {
            lifecycleScope.launch {
                // Llama al servicio de mocks
                val response = mockFacturaService.getFacturas()
                val facturasMock = response.facturas.toMutableList()

                // Obtener filtros desde el Intent o SharedPreferences
                val estados = intent.getStringArrayListExtra("estados") ?: emptyList()
                val valorMaximo = intent.getDoubleExtra("valorMaximo", Double.MAX_VALUE).toInt()
                val fechaDesdeMillis = intent.getLongExtra("fechaDesde", 0L)
                val fechaHastaMillis = intent.getLongExtra("fechaHasta", Long.MAX_VALUE)

                // Crear un SimpleDateFormat para analizar las fechas en los mocks
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                // Aplicar filtros a las facturas de mock
                val facturasFiltradas = facturasMock.filter { factura ->
                    val fechaFactura = dateFormat.parse(factura.fecha)
                    (estados.isEmpty() || estados.contains(factura.descEstado)) &&
                            (factura.importeOrdenacion <= valorMaximo) &&
                            ((fechaFactura?.time ?: 0L) >= fechaDesdeMillis) &&
                            ((fechaFactura?.time ?: Long.MAX_VALUE) <= fechaHastaMillis)
                }

                // Actualizar la lista del adaptador con las facturas filtradas de mocks
                facturaAdapter.updateData(facturasFiltradas)
                displayNoFacturasMessage(facturasFiltradas.isEmpty())

                // Almacenar solo el mock mostrado en la base de datos
                guardarFacturasEnBaseDeDatos(facturasFiltradas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar facturas desde Mock: ${e.message}", Toast.LENGTH_LONG).show()
        }
        limpiarBaseDeDatos()
    }

    private fun guardarFacturasEnBaseDeDatos(facturas: List<Factura>) {
        val facturaDao = FacturaDatabase.getDatabase(this).facturaDao()
        lifecycleScope.launch {
            val facturasBDD = facturas.map { it.toEntity() }  // Convierte las facturas mostradas a FacturaBDD
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
                facturaViewModel.aplicarFiltros(
                    estados,
                    valorMaximo,
                    fechaDesdeMillis,
                    fechaHastaMillis
                )
            } else {
                // Si no hay filtros, cargar todas las facturas desde Room o desde la API
                facturaViewModel.cargarFacturas()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar facturas: ${e.message}", Toast.LENGTH_LONG).show()
        }
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
        super.onBackPressed()
        finish()
    }
}


























