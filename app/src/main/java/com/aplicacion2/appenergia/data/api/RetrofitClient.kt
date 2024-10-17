
package com.aplicacion2.appenergia.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://viewnextandroid.wiremockapi.cloud/"

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val facturaService: FacturaService by lazy {
        instance.create(FacturaService::class.java)
    }
}
