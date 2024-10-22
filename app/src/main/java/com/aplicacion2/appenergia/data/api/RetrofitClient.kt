package com.aplicacion2.appenergia.data.api

import co.infinum.retromock.Retromock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://viewnextandroid.wiremockapi.cloud/"
    private const val ASSETS_PATH = "src/main/assets/"

    // Instancia de Retrofit
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Instancia de Retromock que usa Retrofit
    val retromock: Retromock by lazy {
        Retromock.Builder()
            .retrofit(instance) // Tu instancia de Retrofit correctamente inicializada
            .build()
    }

    // Cambiamos `val` por `var` para permitir la reasignación de facturaService
    var facturaService: FacturaService = instance.create(FacturaService::class.java)

    // Función opcional para alternar entre Retromock y Retrofit según lo que se necesite
    fun toggleMocks(enable: Boolean) {
        facturaService = if (enable) {
            retromock.create(FacturaService::class.java)
        } else {
            instance.create(FacturaService::class.java)
        }
    }
}

