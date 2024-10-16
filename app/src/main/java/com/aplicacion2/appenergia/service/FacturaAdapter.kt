package com.aplicacion2.appenergia.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.facturas_tfc.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale

// Función de extensión para formatear la fecha
fun String.toFormattedDate(): String {
    val originalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val targetFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val date = originalFormat.parse(this)
    return date?.let { targetFormat.format(it) } ?: this
}

// Adaptador del RecyclerView para mostrar las facturas
class FacturaAdapter(private var facturas: List<Factura>, private val context: Context) :
    RecyclerView.Adapter<FacturaAdapter.FacturaViewHolder>() {

    // ViewHolder para cada elemento de la lista de facturas
    class FacturaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fecha: TextView = view.findViewById(R.id.tvFecha)
        val estado: TextView = view.findViewById(R.id.tvEstado)
        val importe: TextView = view.findViewById(R.id.tvImporte)
        val icon: ImageView = view.findViewById(R.id.ivIcon) // Referencia al ImageView del icono
        val divider: View = view.findViewById(R.id.divider)  // Referencia al Divider
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_factura, parent, false)
        return FacturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {
        val factura = facturas[position]

        // Formatear la fecha con letras y mostrarla
        holder.fecha.text = factura.fecha.toFormattedDate()

        // Mostrar u ocultar el estado dependiendo de si es "Pendiente de pago"
        if (factura.descEstado == "Pendiente de pago") {
            holder.estado.text = factura.descEstado
            holder.estado.visibility = View.VISIBLE
            holder.estado.setTextColor(Color.RED)
        } else if (factura.descEstado == "Pagada") {
            holder.estado.visibility = View.INVISIBLE
        } else {
            holder.estado.visibility = View.GONE
        }

        // Formatear el importe para mostrar coma en lugar de punto
        val decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        }
        val decimalFormat = DecimalFormat("###,##0.00", decimalFormatSymbols)
        holder.importe.text = "${decimalFormat.format(factura.importeOrdenacion)} €"
        holder.importe.gravity = Gravity.END

        // Ajustar la posición del divider en función de la visibilidad de `tvEstado`
        holder.divider.visibility = View.VISIBLE
        val layoutParams = holder.divider.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = if (holder.estado.visibility == View.VISIBLE) 16 else 32
        holder.divider.layoutParams = layoutParams

        // Configurar el icono de la flecha hacia la derecha
        holder.icon.setImageResource(R.drawable.ic_chevron_right_24)

        // Configurar el clic en la celda para mostrar el popup de información
        holder.itemView.setOnClickListener {
            showInfoPopup()
        }
    }

    override fun getItemCount(): Int = facturas.size

    // Función para mostrar el AlertDialog
    private fun showInfoPopup() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_info_recyclerview, null)

        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        // Configurar la acción del botón "Aceptar" dentro del dialogView
        val btnAceptar: TextView = dialogView.findViewById(R.id.btnCerrar)
        btnAceptar.setOnClickListener {
            dialogBuilder.dismiss() // Cierra el dialog cuando se pulsa el botón "Aceptar"
        }

        // Mostrar el AlertDialog con el diseño personalizado
        dialogBuilder.show()
    }

    // Función para actualizar los datos del adaptador
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFacturas: List<Factura>) {
        facturas = newFacturas
        notifyDataSetChanged()
    }
}







