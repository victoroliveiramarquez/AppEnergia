package com.aplicacion2.appenergia.filtros

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aplicacion2.appenergia.samartsolar.MainActivitySmartSolar
import com.example.facturas_tfc.databinding.ActivityMainFacturaBinding

@Suppress("DEPRECATION")
class MainActivityFactura : AppCompatActivity() {

    private lateinit var binding: ActivityMainFacturaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityMainFacturaBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Destruir la Activity al presionar el botón "Atrás"
    }
}