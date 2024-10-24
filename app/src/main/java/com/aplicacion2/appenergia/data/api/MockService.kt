package com.aplicacion2.appenergia.data.api

import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockResponse
import retrofit2.http.GET

interface MockService {
    @Mock
    @MockResponse(body = "facturasParcialmentePagadas.json")
    @GET("/")
    suspend fun getFacturas(): FacturaResponse
}
