package com.aplicacion2.appenergia.filtros

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.facturas_tfc.databinding.ActivityMainFiltroFacturasBinding
import java.util.Calendar

@Suppress("DEPRECATION")
class MainActivityFiltroFactura : AppCompatActivity() {

    private lateinit var binding: ActivityMainFiltroFacturasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityMainFiltroFacturasBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                binding.seekBar.progress =
                    adjustedProgress // Asegurar que el SeekBar nunca sea menor a 1
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                // Acciones opcionales al iniciar el toque en el SeekBar
            }

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                // Acciones opcionales al detener el toque en el SeekBar
            }
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
            // Crear un Intent para regresar a la actividad principal de Facturas
            val intent = Intent(this, MainActivityPortada::class.java)
            startActivity(intent)
            finish() // Cerrar la actividad actual
        }

        // Configurar los botones de selección de fecha
        binding.button2.setOnClickListener {
            showDatePicker { date ->
                binding.button2.text = date
                Toast.makeText(this, "Fecha seleccionada: $date", Toast.LENGTH_SHORT).show()
            }
        }

        binding.button4.setOnClickListener {
            showDatePicker { date ->
                binding.button4.text = date
                Toast.makeText(this, "Fecha seleccionada: $date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Mostrar un DatePickerDialog y obtener la fecha seleccionada.
     * @param onDateSelected Función lambda para retornar la fecha seleccionada en formato "dd/MM/yyyy".
     */
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        // Fecha actual
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Crear un DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Formatear la fecha seleccionada y pasarla a la función lambda
                val formattedDate =
                    String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                onDateSelected(formattedDate)
            },
            year,
            month,
            day
        )

        // Mostrar el selector de fecha
        datePickerDialog.show()
    }

    private fun applyFilters() {
        // Lógica para aplicar los filtros seleccionados
        val importeSeleccionado = binding.seekBar.progress
        val estadosSeleccionados = mutableListOf<String>()

        // Agregar los estados seleccionados a la lista
        if (binding.chkPagadas.isChecked) estadosSeleccionados.add("Pagadas")
        if (binding.chkAnuladas.isChecked) estadosSeleccionados.add("Anuladas")
        if (binding.chkCuotaFija.isChecked) estadosSeleccionados.add("Cuota Fija")
        if (binding.chkPendientesPago.isChecked) estadosSeleccionados.add("Pendientes de pago")
        if (binding.chkPlanPago.isChecked) estadosSeleccionados.add("Plan de pago")

        // Aquí puedes agregar la lógica para procesar los filtros aplicados
        Toast.makeText(
            this,
            "Importe seleccionado: $importeSeleccionado €, Estados: $estadosSeleccionados",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun clearFilters() {
        // Restablecer el valor del SeekBar a 1
        binding.seekBar.progress = 1
        binding.textView5.text = "1 €"

        // Desmarcar todos los CheckBox
        binding.chkPagadas.isChecked = false
        binding.chkAnuladas.isChecked = false
        binding.chkCuotaFija.isChecked = false
        binding.chkPendientesPago.isChecked = false
        binding.chkPlanPago.isChecked = false

        // Restablecer texto de los botones de fecha
        binding.button2.text = "día/mes/año"
        binding.button4.text = "días/mes/año"
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish() // Destruir la Activity al presionar el botón "Atrás"
    }
}