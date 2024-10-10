package com.aplicacion2.appenergia.filtros

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facturas_tfc.databinding.ActivityMainFiltroFacturasBinding
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

        // Obtener la fecha mínima desde las facturas (puede provenir de la base de datos)
        minDate = getMinFechaFactura()

        // Inicializar el SeekBar y configurarlo
        initializeSeekBar()

        // Configurar los botones y los CheckBox
        setupButtons()
    }

    private fun initializeSeekBar() {
        // Establecer el valor inicial del SeekBar a 1
        binding.seekBar.progress = 1
        binding.textView5.text = "1 €"

        // Configurar el listener para el SeekBar
        binding.seekBar.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                // Asegurarse de que el progreso mínimo sea 1
                val adjustedProgress = if (progress < 1) 1 else progress
                binding.textView5.text = "$adjustedProgress €"
                binding.seekBar.progress = adjustedProgress // Asegurar que el SeekBar nunca sea menor a 1
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun setupButtons() {
        // Configurar botón Aplicar
        binding.button.setOnClickListener {
            applyFilters()
            Toast.makeText(this, "Filtros aplicados", Toast.LENGTH_SHORT).show()
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

    // Función para obtener la fecha mínima de las facturas (ajustar según sea necesario)
    private fun getMinFechaFactura(): Long {
        // Obtener la fecha mínima de las facturas (puede ser una consulta a la base de datos o lista de facturas)
        val minFecha = "01/01/2018" // Ejemplo: Cambiar por la fecha mínima real
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.parse(minFecha)?.time ?: 0
    }

    private fun applyFilters() {
        val importeSeleccionado = binding.seekBar.progress
        val estadosSeleccionados = mutableListOf<String>()

        if (binding.chkPagadas.isChecked) estadosSeleccionados.add("Pagadas")
        if (binding.chkAnuladas.isChecked) estadosSeleccionados.add("Anuladas")
        if (binding.chkCuotaFija.isChecked) estadosSeleccionados.add("Cuota Fija")
        if (binding.chkPendientesPago.isChecked) estadosSeleccionados.add("Pendientes de pago")
        if (binding.chkPlanPago.isChecked) estadosSeleccionados.add("Plan de pago")

        Toast.makeText(
            this,
            "Importe seleccionado: $importeSeleccionado €, Estados: $estadosSeleccionados",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun clearFilters() {
        binding.seekBar.progress = 1
        binding.textView5.text = "1 €"

        binding.chkPagadas.isChecked = false
        binding.chkAnuladas.isChecked = false
        binding.chkCuotaFija.isChecked = false
        binding.chkPendientesPago.isChecked = false
        binding.chkPlanPago.isChecked = false

        binding.button2.text = "día/mes/año"
        binding.button4.text = "días/mes/año"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Destruir la Activity al presionar el botón "Atrás"
    }
}
