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

        // Obtener la fecha mínima desde las facturas (puede provenir de la base de datos)
        minDate = getMinFechaFactura()

        // Inicializar el SeekBar y configurarlo
        initializeSeekBar()

        // Configurar los botones y los CheckBox
        setupButtons()
    }

    private fun initializeSeekBar() {
        // Configurar el formato para mostrar decimales con coma
        val decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = ','
            groupingSeparator = '.' // Separador de miles, opcional
        }
        val decimalFormat = DecimalFormat("#,##0.00", decimalFormatSymbols)

        // Establecer el valor inicial del SeekBar a 1 (equivalente a 0,01)
        binding.seekBar.max = 10000 // Máximo valor ajustable a 100,00
        binding.seekBar.progress = 100 // Valor inicial ajustado a 1,00

        // Mostrar el valor inicial del SeekBar en el TextView
        binding.textView5.text = "${decimalFormat.format(1.0)} €"

        // Configurar el listener para el SeekBar
        binding.seekBar.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                // Convertir el progreso a valor decimal (equivalente a dividir por 100)
                val decimalValue = progress / 100.0
                binding.textView5.text = "${decimalFormat.format(decimalValue)} €"
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
        // Obtener la fecha mínima de las facturas
        val minFecha = "07/08/2018"
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.parse(minFecha)?.time ?: 0
    }

    private fun applyFilters() {
        val importeSeleccionado = binding.seekBar.progress / 100.0 // Convertir a valor decimal
        val estadoSeleccionado = when {
            binding.chkPagadas.isChecked -> "Pagada"
            binding.chkPendientesPago.isChecked -> "Pendientes de pago"
            else -> ""
        }
        val fechaDesde = binding.button2.text.toString()
        val fechaHasta = binding.button4.text.toString()

        // Crear un Intent para pasar los filtros a MainActivityFactura
        val intent = Intent(this, MainActivityFactura::class.java)
        intent.putExtra("importe", importeSeleccionado)
        intent.putExtra("estado", estadoSeleccionado)
        intent.putExtra("fechaDesde", fechaDesde)
        intent.putExtra("fechaHasta", fechaHasta)
        startActivity(intent)
        finish() // Finalizar la Activity actual para destruirla
    }

    private fun clearFilters() {
        binding.seekBar.progress = 100
        binding.textView5.text = "1,00 €"

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

