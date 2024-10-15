package com.aplicacion2.appenergia.filtros

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facturas_tfc.databinding.ActivityMainFiltroFacturasBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivityFiltroFactura : AppCompatActivity() {

    private lateinit var binding: ActivityMainFiltroFacturasBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var minDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding y SharedPreferences
        binding = ActivityMainFiltroFacturasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("filtros_facturas", MODE_PRIVATE)

        // Obtener la fecha mínima desde las facturas (puede provenir de la base de datos)
        minDate = getMinFechaFactura()

        // Inicializar el SeekBar y configurarlo
        initializeSeekBar()

        // Configurar los botones y los CheckBox
        setupButtons()

        // Cargar los filtros almacenados y aplicarlos a la UI
        loadSavedFilters()
    }

    private fun initializeSeekBar() {
        // Configurar el formato para mostrar números enteros
        val decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = ',' // Separador decimal
            groupingSeparator = '.' // Separador de miles, opcional
        }
        val decimalFormat = DecimalFormat("#,##0", decimalFormatSymbols)

        // Establecer el valor inicial del SeekBar
        binding.seekBar.max = 300 // Máximo valor ajustable a 300
        binding.seekBar.progress = 1 // Valor inicial ajustado a 1

        // Mostrar el valor inicial del SeekBar en el TextView
        binding.textView5.text = "${decimalFormat.format(binding.seekBar.progress)} €"

        // Configurar el listener para el SeekBar
        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                // Mostrar el valor actual del SeekBar como número entero
                binding.textView5.text = "${decimalFormat.format(progress)} €"
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun setupButtons() {
        // Configurar botón Aplicar
        binding.button.setOnClickListener {
            applyFilters()
        }

        // Configurar botón Eliminar Filtros
        binding.button3.setOnClickListener {
            clearFilters()
            Toast.makeText(this, "Filtros eliminados", Toast.LENGTH_SHORT).show()
        }

        // Configurar botón Cerrar (Cerrar la Activity y regresar a MainActivityFactura)
        binding.imClose.setOnClickListener {
            val intent = Intent(this, MainActivityPortada::class.java)
            startActivity(intent)
            finish()
        }

        // Configurar los botones de selección de fecha con fecha mínima
        binding.button2.setOnClickListener {
            showDatePickerWithMinDate { date ->
                binding.button2.text = date
                Toast.makeText(this, "Fecha seleccionada: $date", Toast.LENGTH_SHORT).show()
            }
        }

        binding.button4.setOnClickListener {
            showDatePickerWithMinDate { date ->
                binding.button4.text = date
                Toast.makeText(this, "Fecha seleccionada: $date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerWithMinDate(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                onDateSelected(formattedDate)
            },
            year,
            month,
            day
        )

        // Configurar la fecha mínima en el DatePicker
        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.show()
    }

    // Función para obtener la fecha mínima de las facturas
    private fun getMinFechaFactura(): Long {
        val minFecha = "07/08/2018" // Puedes cambiar esta fecha por la que necesites
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.parse(minFecha)?.time ?: 0
    }


    private fun obtenerEstadosSeleccionados(): List<String> {
        val estadosSeleccionados = mutableListOf<String>()

        if (binding.chkPagadas.isChecked) estadosSeleccionados.add("Pagada")
        if (binding.chkPendientesPago.isChecked) estadosSeleccionados.add("Pendiente de pago")
        if (binding.chkAnuladas.isChecked) estadosSeleccionados.add("Anulada")
        if (binding.chkCuotaFija.isChecked) estadosSeleccionados.add("Cuota fija")
        if (binding.chkPlanPago.isChecked) estadosSeleccionados.add("Plan de pago")

        return estadosSeleccionados
    }


    private fun applyFilters() {
        val estadosSeleccionados = obtenerEstadosSeleccionados()

        if (estadosSeleccionados.isEmpty()) {
            Toast.makeText(this, "No hay facturas disponibles para el estado seleccionado", Toast.LENGTH_SHORT).show()
        } else {
            // Pasar los filtros a la MainActivityFactura usando un Intent
            val intent = Intent(this, MainActivityFactura::class.java)
            intent.putStringArrayListExtra("estados", ArrayList(estadosSeleccionados))
            startActivity(intent)
            finish()
        }
    }

    private fun clearFilters() {
        // Restablecer todos los filtros a sus valores predeterminados
        binding.seekBar.progress = 1
        binding.textView5.text = "100 €"

        binding.chkPagadas.isChecked = false
        binding.chkAnuladas.isChecked = false
        binding.chkCuotaFija.isChecked = false
        binding.chkPendientesPago.isChecked = false
        binding.chkPlanPago.isChecked = false

        binding.button2.text = "día/mes/año"
        binding.button4.text = "días/mes/año"

        // Eliminar los filtros de SharedPreferences
        sharedPreferences.edit().clear().apply()
    }

    private fun loadSavedFilters() {
        // Cargar los filtros guardados en SharedPreferences y aplicarlos a la UI
        val savedEstados = sharedPreferences.getStringSet("estados", emptySet()) ?: emptySet()

        // Aplicar los estados guardados a los CheckBox
        binding.chkPagadas.isChecked = savedEstados.contains("Pagada")
        binding.chkPendientesPago.isChecked = savedEstados.contains("Pendiente de pago")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Destruir la Activity al presionar el botón "Atrás"
    }
}





