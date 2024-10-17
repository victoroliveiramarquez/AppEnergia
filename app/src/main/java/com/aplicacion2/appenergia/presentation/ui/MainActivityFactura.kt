package com.aplicacion2.appenergia.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aplicacion2.appenergia.presentation.viewmodel.FacturaViewModel
import com.aplicacion2.appenergia.presentation.viewmodel.FacturaViewModelFactory
import com.aplicacion2.appenergia.data.api.FacturaAdapter
import com.aplicacion2.appenergia.data.api.FacturaDatabase
import com.aplicacion2.appenergia.domain.usecase.GetFacturasUseCase
import com.aplicacion2.appenergia.domain.usecase.FiltrarFacturasUseCase
import com.aplicacion2.appenergia.data.repository.FacturaRepositoryImpl
import com.aplicacion2.appenergia.data.api.RetrofitClient
import com.example.facturas_tfc.databinding.ActivityMainFacturaBinding

@Suppress("DEPRECATION")
class MainActivityFactura : AppCompatActivity() {

    private lateinit var binding: ActivityMainFacturaBinding
    private lateinit var facturaAdapter: FacturaAdapter
    private lateinit var facturaViewModel: FacturaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainFacturaBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        facturaViewModel = ViewModelProvider(this, factory).get(FacturaViewModel::class.java)

        // Observa los cambios en las facturas filtradas
        facturaViewModel.facturas.observe(this, { facturas ->
            facturaAdapter.updateData(facturas)
            displayNoFacturasMessage(facturas.isEmpty())
        })

        // Obtener los filtros del Intent
        val estados = intent.getStringArrayListExtra("estados") ?: emptyList()
        val valorMaximo = intent.getDoubleExtra("valorMaximo", Double.MAX_VALUE).toInt()
        val fechaDesdeMillis = intent.getLongExtra("fechaDesde", 0L)
        val fechaHastaMillis = intent.getLongExtra("fechaHasta", Long.MAX_VALUE)

        // Cargar las facturas aplicando los filtros
        if (estados.isNotEmpty() || valorMaximo.toDouble() != Double.MAX_VALUE || fechaDesdeMillis > 0 || fechaHastaMillis < Long.MAX_VALUE) {
            aplicarFiltros(estados, valorMaximo, fechaDesdeMillis, fechaHastaMillis)
        } else {
            // Si no hay filtros, cargar todas las facturas
            facturaViewModel.cargarFacturas()
        }

        // Botón para navegar a la Activity de filtros
        binding.imageView.setOnClickListener {
            // Navegar de regreso a los filtros
            val intent = Intent(this, MainActivityFiltroFactura::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Aplicar los filtros usando el ViewModel
    private fun aplicarFiltros(estados: List<String>, valorMaximo: Int, fechaDesde: Long, fechaHasta: Long) {
        facturaViewModel.aplicarFiltros(estados, valorMaximo, fechaDesde, fechaHasta)
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



















