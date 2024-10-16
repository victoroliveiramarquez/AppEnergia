package com.aplicacion2.appenergia.filtros

import android.app.DatePickerDialog
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityMainFiltroFacturasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener la fecha mínima desde las facturas
        minDate = getMinFechaFactura()

        // Inicializar el SeekBar y configurarlo
        initializeSeekBar()

        // Configurar los botones y los CheckBoxes
        setupButtons()
    }

    private fun initializeSeekBar() {
        val decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        }
        val decimalFormat = DecimalFormat("#,##0", decimalFormatSymbols)

        binding.seekBar.max = 300
        binding.seekBar.progress = 0
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

    // Método para obtener el valor del SeekBar
    private fun obtenerValorSeekBar(): Int {
        return binding.seekBar.progress
    }

    // Aplicar filtros al hacer clic en "Aplicar"
    private fun applyFilters() {
        val estadosSeleccionados = obtenerEstadosSeleccionados()
        val valorMaximo: Double = if (binding.seekBar.progress == 1) {
            Double.MAX_VALUE // Esto implica que el usuario no tocó el SeekBar
        } else {
            binding.seekBar.progress.toDouble() // Tomar el valor si el usuario lo cambió
        }

        // Verifica si hay estados seleccionados o el SeekBar fue modificado
        if (estadosSeleccionados.isEmpty() && valorMaximo == Double.MAX_VALUE) {
            Toast.makeText(this, "Por favor selecciona al menos un filtro", Toast.LENGTH_SHORT)
                .show()
        } else {
            // Crear un Intent para pasar los filtros a MainActivityFactura
            val intent = Intent(this, MainActivityFactura::class.java)
            intent.putStringArrayListExtra("estados", ArrayList(estadosSeleccionados))
            intent.putExtra("valorMaximo", valorMaximo) // Pasar el valor del SeekBar
            startActivity(intent)
            finish()
        }
    }

    // Limpiar filtros
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
    }
}







