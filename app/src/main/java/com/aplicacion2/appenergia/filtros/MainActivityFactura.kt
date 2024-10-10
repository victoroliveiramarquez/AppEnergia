package com.aplicacion2.appenergia.filtros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aplicacion2.appenergia.samartsolar.MainActivitySmartSolar
import com.aplicacion2.appenergia.service.FacturaAdapter
import com.aplicacion2.appenergia.service.Factura
import com.aplicacion2.appenergia.service.RetrofitClient
import com.example.facturas_tfc.databinding.ActivityMainFacturaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class MainActivityFactura : AppCompatActivity() {

    private lateinit var binding: ActivityMainFacturaBinding
    private lateinit var facturaAdapter: FacturaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityMainFacturaBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Cargar y mostrar las facturas
        loadFacturasFromApi()
    }

    // Función para cargar las facturas desde la API simulada usando Retromock
    private fun loadFacturasFromApi() {
        lifecycleScope.launch {
            try {
                val facturasService = RetrofitClient.facturaService
                val response = withContext(Dispatchers.IO) {
                    facturasService.getFacturas()
                }
                response?.let {
                    // Actualizar el RecyclerView con los datos obtenidos
                    facturaAdapter.updateData(it.facturas)
                }
            } catch (e: Exception) {
                Log.e("MainActivityFactura", "Error al cargar las facturas: ${e.message}")
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






