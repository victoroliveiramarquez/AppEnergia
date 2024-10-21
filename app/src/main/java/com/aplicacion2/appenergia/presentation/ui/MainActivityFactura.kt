package com.aplicacion2.appenergia.presentation.ui

import FacturaViewModel
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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

@Suppress("DEPRECATION")
class MainActivityFactura : AppCompatActivity() {

    private lateinit var binding: ActivityMainFacturaBinding
    private lateinit var facturaAdapter: FacturaAdapter
    private lateinit var facturaViewModel: FacturaViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainFacturaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SharedPreferences para controlar la primera carga
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Inicializar el RecyclerView
        facturaAdapter = FacturaAdapter(emptyList(), this)
        binding.rvFacturas.layoutManager = LinearLayoutManager(this)
        binding.rvFacturas.adapter = facturaAdapter

        // Crear el repositorio, casos de uso y el ViewModel usando el Factory
        val api = RetrofitClient.facturaService
        val facturaDao = FacturaDatabase.getDatabase(this).facturaDao()
        val repository = FacturaRepositoryImpl(api, facturaDao)

        val getFacturasUseCase = GetFacturasUseCase(repository)
        val filtrarFacturasUseCase = FiltrarFacturasUseCase(repository)

        val factory = FacturaViewModelFactory(getFacturasUseCase, filtrarFacturasUseCase)
        facturaViewModel = ViewModelProvider(this, factory)[FacturaViewModel::class.java]

        // Observa los cambios en las facturas filtradas
        facturaViewModel.facturasBDD.observe(this) { facturas ->
            val listaPasada: MutableList<Factura> = mutableListOf()
            for (i in facturas) {
                listaPasada.add(i.toApi())
            }
            facturaAdapter.updateData(listaPasada)
            displayNoFacturasMessage(facturas.isEmpty())
        }

        // Verificar si es la primera vez que se cargan las facturas desde la API
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
        } else if (estados.isNotEmpty() || valorMaximo.toDouble() != Double.MAX_VALUE || fechaDesdeMillis > 0 || fechaHastaMillis < Long.MAX_VALUE) {

            // Aplicar filtros desde el ViewModel
            facturaViewModel.aplicarFiltros(
                estados,
                valorMaximo,
                fechaDesdeMillis,
                fechaHastaMillis
            )
        } else {
            // Si no hay filtros, cargar todas las facturas desde Room
            facturaViewModel.cargarFacturas()
        }

        // Botón para navegar a la Activity de filtros
        binding.imageView.setOnClickListener {
            // Navegar de regreso a los filtros
            val intent = Intent(this, MainActivityFiltroFactura::class.java)
            startActivity(intent)
            finish()
        }

        // Botón para navegar a la SmartaSolar
        binding.ibAtras.setOnClickListener {
            // Navegar de regreso a los filtros
            val intent = Intent(this, MainActivitySmartSolar::class.java)
            startActivity(intent)
            finish()
        }
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





















