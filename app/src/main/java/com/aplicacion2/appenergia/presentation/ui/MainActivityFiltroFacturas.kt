package com.aplicacion2.appenergia.presentation.ui

import android.app.DatePickerDialog
import android.content.Context
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
    private var minDate: Long = 0
    private lateinit var sharedPreferences: SharedPreferences
    private var isApplyingFilters: Boolean = false // Indicador de si se está aplicando un filtro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityMainFiltroFacturasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("FiltroFacturasPrefs", Context.MODE_PRIVATE)

        // Obtener la fecha mínima desde las facturas
        minDate = getMinFechaFactura()

        // Inicializar el SeekBar y configurarlo
        initializeSeekBar()

        // Configurar los botones y los CheckBoxes
        setupButtons()

        // Cargar los filtros guardados
        loadFilters()
    }

    // Este método ya no limpiará los filtros, para que no se limpien al mover la app al fondo
    override fun onStop() {
        super.onStop()
        // No se limpian los filtros aquí
    }

    // Limpiar los filtros solo si la actividad se destruye por completo (cierre de la app)
    override fun onDestroy() {
        super.onDestroy()
        if (!isApplyingFilters) {
            clearFilters() // Solo limpiar los filtros si no se están aplicando
        }
    }

    private fun initializeSeekBar() {
        val decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        }
        val decimalFormat = DecimalFormat("#,##0", decimalFormatSymbols)

        binding.seekBar.max = 300
        binding.seekBar.progress = sharedPreferences.getInt("valorMaximo", 0)
        binding.textView5.text = "${decimalFormat.format(binding.seekBar.progress)} €"

        binding.seekBar.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
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
        binding.imClose.setOnClickListener {
            val intent = Intent(this, MainActivityPortada::class.java) // Navegar a la actividad de portada
            startActivity(intent)
            finish() // Finalizar la actividad actual para eliminarla de la pila
        }

        // Configurar los botones de selección de fecha
        binding.buttonDesde.setOnClickListener {
            showDatePickerWithMinDate { date ->
                binding.buttonDesde.text = date
                Toast.makeText(this, "Fecha seleccionada: $date", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonHasta.setOnClickListener {
            showDatePickerWithMinDate { date ->
                binding.buttonHasta.text = date
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
                val formattedDate =
                    String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                onDateSelected(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.show()
    }

    // Función para obtener la fecha mínima de las facturas
    private fun getMinFechaFactura(): Long {
        val minFecha = "07/08/2018"
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.parse(minFecha)?.time ?: 0
    }

    // Método para obtener los estados seleccionados
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
        // Obtener el valor actual del SeekBar, sea cual sea el valor
        val valorMaximo = binding.seekBar.progress.toDouble()
        // Convertir las fechas de los botones a Long
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Fechas seleccionadas en los botones
        val fechaDesde = if (binding.buttonDesde.text.toString() != "día/mes/año") {
            simpleDateFormat.parse(binding.buttonDesde.text.toString())?.time ?: 0L
        } else {
            0L // Si no se selecciona una fecha, usa 0L como valor por defecto
        }

        val fechaHasta = if (binding.buttonHasta.text.toString() != "día/mes/año") {
            simpleDateFormat.parse(binding.buttonHasta.text.toString())?.time ?: Long.MAX_VALUE
        } else {
            Long.MAX_VALUE // Si no se selecciona una fecha, usa Long.MAX_VALUE como valor por defecto
        }

        // Verifica si hay estados seleccionados, el SeekBar ha sido modificado, o si hay fechas
        if (estadosSeleccionados.isEmpty() && valorMaximo == Double.MAX_VALUE && fechaDesde == 0L && fechaHasta == Long.MAX_VALUE) {
            Toast.makeText(this, "Por favor selecciona al menos un filtro", Toast.LENGTH_SHORT).show()
        } else {
            // Guardar los filtros seleccionados en SharedPreferences
            saveFilters(estadosSeleccionados, valorMaximo, fechaDesde, fechaHasta)

            isApplyingFilters = true // Indicar que se están aplicando filtros

            // Crear un Intent para pasar los filtros a MainActivityFactura
            val intent = Intent(this, MainActivityFactura::class.java)
            intent.putStringArrayListExtra("estados", ArrayList(estadosSeleccionados))
            intent.putExtra("valorMaximo", valorMaximo) // Pasar el valor del SeekBar
            intent.putExtra("fechaDesde", fechaDesde)   // Pasar la fecha desde
            intent.putExtra("fechaHasta", fechaHasta)   // Pasar la fecha hasta
            startActivity(intent)
            finish()
        }
    }

    // Guardar los filtros en SharedPreferences
    private fun saveFilters(
        estadosSeleccionados: List<String>,
        valorMaximo: Double,
        fechaDesde: Long,
        fechaHasta: Long
    ) {
        with(sharedPreferences.edit()) {
            putStringSet("estados", estadosSeleccionados.toSet())
            putInt("valorMaximo", valorMaximo.toInt())
            putLong("fechaDesde", fechaDesde)
            putLong("fechaHasta", fechaHasta)
            apply()
        }
    }

    // Cargar los filtros desde SharedPreferences
    private fun loadFilters() {
        val estados = sharedPreferences.getStringSet("estados", emptySet()) ?: emptySet()
        binding.chkPagadas.isChecked = estados.contains("Pagada")
        binding.chkPendientesPago.isChecked = estados.contains("Pendiente de pago")
        binding.chkAnuladas.isChecked = estados.contains("Anulada")
        binding.chkCuotaFija.isChecked = estados.contains("Cuota fija")
        binding.chkPlanPago.isChecked = estados.contains("Plan de pago")

        binding.seekBar.progress = sharedPreferences.getInt("valorMaximo", 0)
        binding.buttonDesde.text = if (sharedPreferences.getLong("fechaDesde", 0L) != 0L) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(sharedPreferences.getLong("fechaDesde", 0L))
        } else {
            "día/mes/año"
        }

        binding.buttonHasta.text = if (sharedPreferences.getLong("fechaHasta", Long.MAX_VALUE) != Long.MAX_VALUE) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(sharedPreferences.getLong("fechaHasta", Long.MAX_VALUE))
        } else {
            "día/mes/año"
        }
    }

    // Limpiar filtros y SharedPreferences
    private fun clearFilters() {
        binding.seekBar.progress = 0
        binding.textView5.text = "0 €"
        binding.chkPagadas.isChecked = false
        binding.chkAnuladas.isChecked = false
        binding.chkCuotaFija.isChecked = false
        binding.chkPendientesPago.isChecked = false
        binding.chkPlanPago.isChecked = false
        binding.buttonDesde.text = "día/mes/año"
        binding.buttonHasta.text = "día/mes/año"

        // Limpiar SharedPreferences
        sharedPreferences.edit().clear().apply()
    }
}












