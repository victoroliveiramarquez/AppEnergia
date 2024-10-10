package com.aplicacion2.appenergia.service

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.facturas_tfc.R
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
class FacturaAdapter(private var facturas: List<Factura>) : RecyclerView.Adapter<FacturaAdapter.FacturaViewHolder>() {

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
        } else {
            holder.estado.visibility = View.GONE
        }

        // Mostrar el importe alineado a la derecha
        holder.importe.text = String.format("%.2f €", factura.importeOrdenacion)
        holder.importe.gravity = Gravity.END

        // Configurar el divider para que se muestre entre los elementos
        holder.divider.visibility = View.VISIBLE

        // Configurar el icono de la flecha hacia la derecha
        holder.icon.setImageResource(R.drawable.ic_chevron_right_24)
    }

    override fun getItemCount(): Int = facturas.size

    // Función para actualizar los datos del adaptador
    fun updateData(newFacturas: List<Factura>) {
        facturas = newFacturas
        notifyDataSetChanged()
    }
}




