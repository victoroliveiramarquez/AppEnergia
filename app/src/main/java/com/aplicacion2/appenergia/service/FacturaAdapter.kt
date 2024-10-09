
package com.aplicacion2.appenergia.service

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.facturas_tfc.R

class FacturaAdapter(private var facturas: List<Factura>) : RecyclerView.Adapter<FacturaAdapter.FacturaViewHolder>() {

    class FacturaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fecha: TextView = view.findViewById(R.id.tvFecha)
        val estado: TextView = view.findViewById(R.id.tvEstado)
        val importe: TextView = view.findViewById(R.id.tvImporte)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_factura, parent, false)
        return FacturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {
        val factura = facturas[position]
        holder.fecha.text = factura.fecha
        holder.estado.text = factura.descEstado
        holder.importe.text = "${factura.importeOrdenacion} €"
    }

    override fun getItemCount(): Int = facturas.size

    fun updateData(newFacturas: List<Factura>) {
        facturas = newFacturas
        notifyDataSetChanged()
    }
}
