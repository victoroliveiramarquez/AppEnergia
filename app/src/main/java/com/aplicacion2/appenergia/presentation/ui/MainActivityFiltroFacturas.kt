package com.aplicacion2.appenergia.presentation.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aplicacion2.appenergia.data.api.FacturaDatabase
import com.aplicacion2.appenergia.domain.model.FacturaBDD
import com.example.facturas_tfc.R
import com.example.facturas_tfc.databinding.ActivityMainFiltroFacturasBinding
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

@SuppressLint("ParcelCreator")
@Suppress("DEPRECATION")
class MainActivityFiltroFactura() : AppCompatActivity(), Parcelable {

    private lateinit var binding: ActivityMainFiltroFacturasBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var isApplyingFilters: Boolean = false

    constructor(parcel: Parcel) : this() {
        isApplyingFilters = parcel.readByte() != 0.toByte()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityMainFiltroFacturasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("FiltroFacturasPrefs", Context.MODE_PRIVATE)

        // Configurar fechas mínima y máxima para DatePicker
        lifecycleScope.launch {
            val facturas = FacturaDatabase.getDatabase(this@MainActivityFiltroFactura).facturaDao().getAllFacturas()
            val (fechaDesde, fechaHasta) = obtenerFechasDesdeYHasta(facturas)

            binding.buttonDesde.setOnClickListener {
                showDatePicker(fechaDesde, fechaHasta) { date -> binding.buttonDesde.text = date }
            }
            binding.buttonHasta.setOnClickListener {
                showDatePicker(fechaDesde, System.currentTimeMillis()) { date -> binding.buttonHasta.text = date }
            }
        }

        // Inicializar SeekBar
        initializeSeekBar()

        // Configurar botones y CheckBoxes
        setupButtons()

        // Cargar filtros guardados
        loadFilters()

        actualizarImporteMaximoSeekBarDesdeFacturas()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isApplyingFilters) {
            clearFilters()
        }
    }

    private fun initializeSeekBar() {
        val decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        }
        val decimalFormat = DecimalFormat("#,##0", decimalFormatSymbols)

        val facturaDao = FacturaDatabase.getDatabase(this).facturaDao()
        lifecycleScope.launch {
            val facturas = facturaDao.getAllFacturas()
            val importeMaximo = facturas.maxOfOrNull { it.importeOrdenacion }
                ?.let { ceil(it).toInt() } ?: 300

            binding.seekBar.max = importeMaximo
            binding.tvMaxImporte.text = getString(R.string.importeMaximoTV, importeMaximo.toString())

            val savedProgress = sharedPreferences.getInt("seekBarProgress", 0).coerceIn(0, importeMaximo)
            binding.seekBar.progress = savedProgress
            binding.textView5.text = "${decimalFormat.format(savedProgress)} €"
        }

        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textView5.text = "${decimalFormat.format(progress)} €"
                sharedPreferences.edit().putInt("seekBarProgress", progress).apply()
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun actualizarImporteMaximoSeekBarDesdeFacturas() {
        val facturaDao = FacturaDatabase.getDatabase(this).facturaDao()
        lifecycleScope.launch {
            val facturas = facturaDao.getAllFacturas()
            val maxImporte = facturas.maxOfOrNull { it.importeOrdenacion } ?: 0.0
            val maxSeekBarValue = ceil(maxImporte).toInt()
            binding.seekBar.max = maxSeekBarValue
            val savedProgress = sharedPreferences.getInt("seekBarProgress", 0).coerceIn(0, maxSeekBarValue)
            binding.seekBar.progress = savedProgress
        }
    }

    private fun setupButtons() {
        binding.button.setOnClickListener { applyFilters() }
        binding.button3.setOnClickListener {
            clearFilters()
            Toast.makeText(this, "Filtros eliminados", Toast.LENGTH_SHORT).show()
        }
        binding.imClose.setOnClickListener {
            val intent = Intent(this, MainActivityPortada::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showDatePicker(minDate: Long, maxDate: Long, onDateSelected: (String) -> Unit) {
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
        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.datePicker.maxDate = maxDate
        datePickerDialog.show()
    }

    // Función para obtener la fecha mínima y máxima basadas en la lista de facturas
    private fun obtenerFechasDesdeYHasta(facturas: List<FacturaBDD>): Pair<Long, Long> {
        val fechaMinima = facturas.minOfOrNull { it.fecha } ?: System.currentTimeMillis()
        val fechaMaxima = System.currentTimeMillis()
        return Pair(fechaMinima, fechaMaxima)
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
        val valorMaximo = binding.seekBar.progress.toDouble()
        val fechaDesde = obtenerFechaDesde()
        val fechaHasta = obtenerFechaHasta()

        if (estadosSeleccionados.isEmpty() && valorMaximo == Double.MAX_VALUE && fechaDesde == 0L && fechaHasta == Long.MAX_VALUE) {
            Toast.makeText(this, "Por favor selecciona al menos un filtro", Toast.LENGTH_SHORT).show()
        } else {
            saveFilters(estadosSeleccionados, valorMaximo, fechaDesde, fechaHasta)

            isApplyingFilters = true
            val intent = Intent(this, MainActivityFactura::class.java)
            intent.putStringArrayListExtra("estados", ArrayList(estadosSeleccionados))
            intent.putExtra("valorMaximo", valorMaximo)
            intent.putExtra("fechaDesde", fechaDesde)
            intent.putExtra("fechaHasta", fechaHasta)
            startActivity(intent)
            finish()
        }
    }

    private fun obtenerFechaDesde(): Long {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return if (binding.buttonDesde.text.toString() != "día/mes/año") {
            simpleDateFormat.parse(binding.buttonDesde.text.toString())?.time ?: 0L
        } else {
            0L
        }
    }

    private fun obtenerFechaHasta(): Long {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return if (binding.buttonHasta.text.toString() != "día/mes/año") {
            simpleDateFormat.parse(binding.buttonHasta.text.toString())?.time ?: Long.MAX_VALUE
        } else {
            Long.MAX_VALUE
        }
    }

    private fun saveFilters(estadosSeleccionados: List<String>, valorMaximo: Double, fechaDesde: Long, fechaHasta: Long) {
        with(sharedPreferences.edit()) {
            putStringSet("estados", estadosSeleccionados.toSet())
            putInt("valorMaximo", valorMaximo.toInt())
            putLong("fechaDesde", fechaDesde)
            putLong("fechaHasta", fechaHasta)
            apply()
        }
    }

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
        sharedPreferences.edit().clear().apply()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isApplyingFilters) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }
}














