package com.aplicacion2.appenergia.samartsolar

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.aplicacion2.appenergia.presentation.ui.MainActivityPortada
import com.aplicacion2.appenergia.samartsolar.fragments.DetallesFragment
import com.aplicacion2.appenergia.samartsolar.fragments.EnergiaFragment
import com.aplicacion2.appenergia.samartsolar.fragments.InstalacionFragment
import com.example.facturas_tfc.R
import com.example.facturas_tfc.databinding.ActivityMainSmartSolarBinding
import com.google.android.material.tabs.TabLayout

@Suppress("DEPRECATION")
class MainActivitySmartSolar : AppCompatActivity() {

    // Utilizar ViewBinding para enlazar las vistas
    private lateinit var binding: ActivityMainSmartSolarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityMainSmartSolarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        // Configurar el botón "Atrás" para que navegue a la Activity MainActivityFactura
        binding.ibAtras.setOnClickListener {
            val intent = Intent(this, MainActivityPortada ::class.java)
            startActivity(intent)
        }

        fun replaceFragment(fragment: Fragment) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.flContainer,
                    fragment
                ) // Reemplazar el contenedor del container con el nuevo fragmento
                .commit()
        }

        // Inicializar el TabLayout
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)

        // Configurar el fragmento inicial al cargar la Activity
        replaceFragment(InstalacionFragment())

        // Agregar listener para manejar los eventos de clic en las pestañas
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Cambiar el fragmento según la pestaña seleccionada
                val selectedFragment: Fragment = when (tab?.position) {
                    0 -> InstalacionFragment()
                    1 -> EnergiaFragment()
                    2 -> DetallesFragment()
                    else -> InstalacionFragment()
                }
                replaceFragment(selectedFragment)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Destruir la Activity al presionar el botón "Atrás"
    }
}