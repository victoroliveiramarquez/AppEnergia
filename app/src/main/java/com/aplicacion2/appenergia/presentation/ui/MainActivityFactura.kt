package com.aplicacion2.appenergia.presentation.ui

import FacturaViewModel
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
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

        // Inicializar el ViewModel antes de cualquier acceso a él
        val api = RetrofitClient.facturaService
        val facturaDao = FacturaDatabase.getDatabase(this).facturaDao()
        val repository = FacturaRepositoryImpl(api, facturaDao)

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

        if (mocksEnabled) {
            // Si los mocks están activados, no mostrar ninguna factura
            displayNoFacturasMessage(true) // Mostrar mensaje de que no hay facturas
        } else {
            // Si los mocks están desactivados, carga las facturas desde Room o la API
            gestionarCargarFacturas()
        }

        // Inicializar el RecyclerView
        facturaAdapter = FacturaAdapter(emptyList(), this)
        binding.rvFacturas.layoutManager = LinearLayoutManager(this)
        binding.rvFacturas.adapter = facturaAdapter

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

    // Función para gestionar la carga de facturas con o sin filtros
    private fun gestionarCargarFacturas() {
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
                // Si no hay filtros, cargar todas las facturas desde Room
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























