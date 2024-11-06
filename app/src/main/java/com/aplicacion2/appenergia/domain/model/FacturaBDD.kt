package com.aplicacion2.appenergia.domain.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "facturasBDD")
data class FacturaBDD(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val descEstado: String,
    val importeOrdenacion: Double,
    val fecha: Long
) : Parcelable {
    fun toApi(): Factura {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = Date(fecha)
        return Factura(descEstado, importeOrdenacion, dateFormat.format(date))
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(descEstado)
        parcel.writeDouble(importeOrdenacion)
        parcel.writeLong(fecha)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FacturaBDD> {
        override fun createFromParcel(parcel: Parcel): FacturaBDD = FacturaBDD(parcel)
        override fun newArray(size: Int): Array<FacturaBDD?> = arrayOfNulls(size)
    }
}
