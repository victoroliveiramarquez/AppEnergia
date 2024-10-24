package com.aplicacion2.appenergia.data.api

import android.content.Context
import co.infinum.retromock.Retromock
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://viewnextandroid.wiremockapi.cloud/"

    val gson = GsonBuilder()
        .setLenient() // Permite JSON malformado
        .create()

    // Instancia de Retrofit
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Retromock necesita el contexto para acceder a los assets
    private var context: Context? = null

    // Función para inicializar el contexto desde la actividad o fragmento
    fun initContext(context: Context) {
        this.context = context
    }

    // Instancia de Retromock que usa el contexto para abrir archivos de assets
    val retromock: Retromock by lazy {
        Retromock.Builder()
            .retrofit(instance)
            .defaultBodyFactory { fileName ->
                val ctx = context ?: throw IllegalStateException("Context no inicializado en RetrofitClient")
                ctx.assets.open("facturasParcialmentePagadas.json") // Abrir el archivo usando el contexto
            }
            .build()
    }

    var facturaService: FacturaService = instance.create(FacturaService::class.java)
}


