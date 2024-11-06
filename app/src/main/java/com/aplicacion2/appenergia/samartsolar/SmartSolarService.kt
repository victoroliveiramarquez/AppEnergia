package com.aplicacion2.appenergia.samartsolar

import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockRandom
import co.infinum.retromock.meta.MockResponse
import retrofit2.http.GET

interface SmartSolarService {

    @Mock
    @MockRandom
    @MockResponse(body = "detalles.json")
    @MockResponse(body = "detalles2.json")
    @MockResponse(body = "detalles3.json")
    @MockResponse(body = "detalles4.json")
    @MockResponse(body = "detalles5.json")
    @GET("/")
    suspend fun getSmartSolarDetails(): SmartSolarDetails
}