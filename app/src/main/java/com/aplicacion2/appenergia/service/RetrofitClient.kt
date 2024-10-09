
package com.aplicacion2.appenergia.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://viewnextandroid.wiremockapi.cloud/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val facturaService: FacturaService = retrofit.create(FacturaService::class.java)
}

interface FacturaService {
    @GET("facturas") // Endpoint de la API
    suspend fun getFacturas(): FacturaResponse
}
