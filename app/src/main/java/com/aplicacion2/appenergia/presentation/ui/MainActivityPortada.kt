package com.aplicacion2.appenergia.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.aplicacion2.appenergia.samartsolar.MainActivitySmartSolar
import com.example.facturas_tfc.databinding.ActivityMainPortadaBinding

@Suppress("DEPRECATION")
class MainActivityPortada : AppCompatActivity() {

    private lateinit var binding: ActivityMainPortadaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Inicializa ViewBinding
        binding = ActivityMainPortadaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el botón "Ver Facturas" para que navegue a MainActivityFactura
        binding.btnFacturas.setOnClickListener {
            val intent = Intent(this, MainActivityFactura::class.java)
            startActivity(intent)
        }

        // Configurar el botón "Smart Solar" para que navegue a MainActivitySmartSolar
        binding.btnSmartSolar.setOnClickListener {
            val intent = Intent(this, MainActivitySmartSolar::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Cierra todas las actividades y finaliza la aplicación cuando se presiona "Atrás"
        finishAffinity()
    }
}
