package com.aplicacion2.appenergia.samartsolar

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.aplicacion2.appenergia.presentation.ui.MainActivityPortada
import com.aplicacion2.appenergia.samartsolar.fragments.DetallesFragment
import com.aplicacion2.appenergia.samartsolar.fragments.EnergiaFragment
import com.aplicacion2.appenergia.samartsolar.fragments.InstalacionFragment
import com.example.facturas_tfc.R
import com.example.facturas_tfc.databinding.ActivityMainSmartSolarBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivitySmartSolar : AppCompatActivity() {

    // Utilizar ViewBinding para enlazar las vistas
    private lateinit var binding: ActivityMainSmartSolarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityMainSmartSolarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        // Configurar el botón "Atrás" para que navegue a la Activity MainActivityPortada
        binding.ibAtras.setOnClickListener {
            val intent = Intent(this, MainActivityPortada::class.java)
            startActivity(intent)
        }

        // Configurar el ViewPager con su adaptador
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Vincular el TabLayout con el ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.mi_instalaci_n)
                1 -> getString(R.string.energ_a)
                2 -> getString(R.string.detalles)
                else -> null
            }
        }.attach()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Destruir la Activity al presionar el botón "Atrás"
    }
}
