package com.aplicacion2.appenergia.samartsolar.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import co.infinum.retromock.Retromock
import com.aplicacion2.appenergia.samartsolar.SmartSolarDetails
import com.aplicacion2.appenergia.samartsolar.SmartSolarService
import com.example.facturas_tfc.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DetallesFragment : Fragment() {

    // Declarar las vistas que se actualizarán
    private lateinit var tvCau: TextView
    private lateinit var tvEstadoSolicitud: TextView
    private lateinit var tvTipoAutoconsumo: TextView
    private lateinit var tvCompensacion: TextView
    private lateinit var tvPotencia: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_detalles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar las vistas
        tvCau = view.findViewById(R.id.mock1)
        tvEstadoSolicitud = view.findViewById(R.id.mock2)
        tvTipoAutoconsumo = view.findViewById(R.id.mock3)
        tvCompensacion = view.findViewById(R.id.mock4)
        tvPotencia = view.findViewById(R.id.mock5)

        // Inicializar el botón de información y configurar su listener
        val infoButton: ImageView = view.findViewById(R.id.ibInfo)
        infoButton.setOnClickListener {
            infoDialog() // Mostrar el diálogo de información al hacer clic
        }
    }

    override fun onResume() {
        super.onResume()
        // Llamada simulada a la API con Retromock para actualizar los mocks
        loadSmartSolarDetails()
    }

    private fun loadSmartSolarDetails() {
        // Crear cliente Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://appenergia.api/") // URL base ficticia
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Crear cliente Retromock para simular llamadas a la API
        val retromock = Retromock.Builder()
            .retrofit(retrofit)
            .defaultBodyFactory { context?.assets?.open(it) } // Pasar InputStream directamente
            .build()

        val service = retromock.create(SmartSolarService::class.java)

        // Llamar al servicio usando coroutines para operaciones asincrónicas
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val details = service.getSmartSolarDetails()
                updateUI(details)
            } catch (e: Exception) {
                Log.e("DetallesFragment", "Error al cargar los detalles: ${e.message}")
            }
        }
    }

    // Actualizar la UI con los datos obtenidos
    private fun updateUI(details: SmartSolarDetails) {
        tvCau.text = details.cau
        tvEstadoSolicitud.text = details.estadoSolicitud
        tvTipoAutoconsumo.text = details.tipoAutoconsumo
        tvCompensacion.text = details.compensacion
        tvPotencia.text = details.potencia
    }

    // Función para mostrar el diálogo de información
    private fun infoDialog() {
        // Inflar el layout personalizado para el popup
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_info_autoconsumo, null)

        // Crear el AlertDialog con el layout personalizado
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Configurar la acción del botón "Aceptar" dentro del dialogView
        val btnAceptar: Button = dialogView.findViewById(R.id.btnAceptar)
        btnAceptar.setOnClickListener {
            dialogBuilder.dismiss() // Cierra el dialog cuando se pulsa el botón "Aceptar"
        }

        // Mostrar el AlertDialog
        dialogBuilder.show()
    }
}

