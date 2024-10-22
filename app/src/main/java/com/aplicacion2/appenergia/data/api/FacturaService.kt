package com.aplicacion2.appenergia.data.api

import co.infinum.retromock.meta.MockCircular
import retrofit2.http.GET

interface FacturaService {
    @MockCircular
    @GET("facturas")
    suspend fun getFacturas(): FacturaResponse
}