package com.aplicacion2.appenergia.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.facturas_tfc.databinding.ActivityMainPortadaBinding
import com.aplicacion2.appenergia.data.api.FacturaService
import com.aplicacion2.appenergia.samartsolar.MainActivitySmartSolar
import com.aplicacion2.appenergia.data.api.RetrofitClient

@Suppress("DEPRECATION")
class MainActivityPortada : AppCompatActivity() {

    private lateinit var binding: ActivityMainPortadaBinding
    private var mocksEnabled = false // Variable para controlar el estado de los mocks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // Inicializa ViewBinding
        binding = ActivityMainPortadaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el botón "Ver Facturas" para que navegue a MainActivityFactura
        binding.btnFacturas.setOnClickListener {
            val intent = Intent(this, MainActivityFactura::class.java)
            intent.putExtra("MOCKS_ENABLED", mocksEnabled) // Pasar el estado de mocks a MainActivityFactura
            startActivity(intent)
        }

        // Configurar el botón "Smart Solar" para que navegue a MainActivitySmartSolar
        binding.btnSmartSolar.setOnClickListener {
            val intent = Intent(this, MainActivitySmartSolar::class.java)
            startActivity(intent)
        }

        // Configurar el Switch para activar/desactivar mocks
        binding.switchMocks.setOnCheckedChangeListener { _, isChecked ->
            mocksEnabled = isChecked // Actualiza el estado de los mocks

            // Mostrar mensaje Toast
            val message = if (mocksEnabled) {
                "Mocks activados"
            } else {
                "Mocks desactivados"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            // Llamar a la función para activar o desactivar el sistema de mocks
            toggleMocksSystem(mocksEnabled)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Cierra todas las actividades y finaliza la aplicación cuando se presiona "Atrás"
        finishAffinity()
    }

    // Función para activar o desactivar el sistema de mocks
    private fun toggleMocksSystem(enable: Boolean) {
        if (enable) {
            // Activa Retromock
            RetrofitClient.facturaService = RetrofitClient.retromock.create(FacturaService::class.java)
        } else {
            // Usa Retrofit normal
            RetrofitClient.facturaService = RetrofitClient.instance.create(FacturaService::class.java)
        }
    }
}


