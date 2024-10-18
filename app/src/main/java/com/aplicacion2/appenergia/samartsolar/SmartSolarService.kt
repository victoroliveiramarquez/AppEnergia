package com.aplicacion2.appenergia.samartsolar

import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockResponse
import retrofit2.http.GET

interface SmartSolarService {
    @Mock
    @MockResponse(body = "detalles.json")
    @GET("/")
    suspend fun getSmartSolarDetails(): SmartSolarDetails
}