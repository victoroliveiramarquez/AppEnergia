package com.aplicacion2.appenergia.data.api

import retrofit2.http.GET

interface FacturaService {
    @GET("facturas")
    suspend fun getFacturas(): FacturaResponse
}