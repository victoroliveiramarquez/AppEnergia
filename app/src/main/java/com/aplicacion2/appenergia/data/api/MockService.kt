package com.aplicacion2.appenergia.data.api

import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockCircular
import co.infinum.retromock.meta.MockResponse
import retrofit2.http.GET

interface MockService {
    @Mock
    @MockCircular
    @MockResponse(body = "facturasParcialmentePagadas.json")
    @MockResponse(body = "facturasSinPagar.json")
    @MockResponse(body = "facturasTodasPagadas.json")
    @GET("/")
    suspend fun getFacturas(): FacturaResponse
}
