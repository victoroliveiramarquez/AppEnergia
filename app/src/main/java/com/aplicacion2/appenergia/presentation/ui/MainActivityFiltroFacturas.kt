package com.aplicacion2.appenergia.presentation.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
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
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
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
        binding = ActivityMainFiltroFacturasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("FiltroFacturasPrefs", Context.MODE_PRIVATE)

        // Configurar el rango de fechas de acuerdo con la lista actual de facturas
        lifecycleScope.launch {
            val facturas = obtenerFacturasActuales()
            val (fechaDesde, fechaHasta) = obtenerFechasDesdeYHasta(facturas)

            binding.buttonDesde.setOnClickListener {
                showMaterialDatePicker(fechaDesde, fechaHasta) { date -> binding.buttonDesde.text = date }
            }
            binding.buttonHasta.setOnClickListener {
                showMaterialDatePicker(fechaDesde, System.currentTimeMillis()) { date -> binding.buttonHasta.text = date }
            }
        }

        // Inicializar SeekBar, botones y filtros
        initializeSeekBar()
        setupButtons()
        loadFilters()
        actualizarImporteMaximoSeekBarDesdeFacturas()
    }

    private suspend fun obtenerFacturasActuales(): List<FacturaBDD> {
        // Obtener la lista de facturas activas o filtradas desde la base de datos
        val facturaDao = FacturaDatabase.getDatabase(this).facturaDao()
        return facturaDao.getAllFacturas() // Aplica filtros si estás trabajando con una lista filtrada
    }

    private fun obtenerFechasDesdeYHasta(facturas: List<FacturaBDD>): Pair<Long, Long> {
        val fechaMinima = facturas.minOfOrNull { it.fecha } ?: System.currentTimeMillis()
        val fechaMaxima = System.currentTimeMillis()
        return Pair(fechaMinima, fechaMaxima)
    }

    private fun showMaterialDatePicker(minDate: Long, maxDate: Long, onDateSelected: (String) -> Unit) {
        val originalLocale = Locale.getDefault()
        val spanishLocale = Locale("es", "ES")
        Locale.setDefault(spanishLocale)
        val config = Configuration(resources.configuration)
        config.setLocale(spanishLocale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(minDate)
            .setEnd(maxDate)
            .setValidator(CustomDateValidator.from(minDate, maxDate)) // Usar validador personalizado

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Calendario")
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", spanishLocale)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val formattedDate = dateFormat.format(selection)
            onDateSelected(formattedDate)
        }

        datePicker.addOnDismissListener {
            Locale.setDefault(originalLocale)
            config.setLocale(originalLocale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }

        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

    // Validador personalizado que permite solo fechas desde `minDate` hasta `maxDate`
    class CustomDateValidator(private val minDate: Long, private val maxDate: Long) :
        CalendarConstraints.DateValidator {
        override fun isValid(date: Long): Boolean {
            return date in minDate..maxDate // Solo permite fechas dentro del rango
        }

        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) {}

        companion object {
            fun from(minDate: Long, maxDate: Long) = CustomDateValidator(minDate, maxDate)
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
            val importeMaximo = facturas.maxOfOrNull { it.importeOrdenacion }?.let { ceil(it).toInt() } ?: 300

            binding.seekBar.max = importeMaximo
            binding.tvMaxImporte.text = getString(R.string.importeMaximoTV, importeMaximo.toString())

            val savedProgress = sharedPreferences.getInt("seekBarProgress", 0).coerceIn(0, importeMaximo)
            binding.seekBar.progress = savedProgress
            binding.textView5.text = getString(R.string.saved_progress_text, decimalFormat.format(savedProgress))

        }

        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textView5.text = getString(R.string.progress_text, decimalFormat.format(progress))

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
        binding.button.setOnClickListener { applyFilters() }  // Aplica los filtros cuando se selecciona el botón de aplicar
        binding.button3.setOnClickListener {
            clearFilters()
            Toast.makeText(this, "Filtros eliminados", Toast.LENGTH_SHORT).show()
        }
        binding.imClose.setOnClickListener {
            onBackPressed()
        }
    }


    private fun obtenerEstadosSeleccionados(): List<String> {
        val estadosSeleccionados = mutableListOf<String>()
        if (binding.chkPagadas.isChecked) estadosSeleccionados.add("Pagada")
        if (binding.chkPendientesPago.isChecked) estadosSeleccionados.add("Pendiente de pago")
        if (binding.chkAnuladas.isChecked) estadosSeleccionados.add("Anulada")
        if (binding.chkCuotaFija.isChecked) estadosSeleccionados.add("Cuota Fija")
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
        binding.chkCuotaFija.isChecked = estados.contains("Cuota Fija")
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
        binding.buttonDesde.text = getString(R.string.date_format_text)
        binding.buttonHasta.text = getString(R.string.date_format_text)

        sharedPreferences.edit().clear().apply()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isApplyingFilters) 1 else 0)
    }

    override fun describeContents(): Int = 0
}















