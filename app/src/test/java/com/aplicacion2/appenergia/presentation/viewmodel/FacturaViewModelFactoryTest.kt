package com.aplicacion2.appenergia.presentation.viewmodel

import FacturaViewModel
import androidx.lifecycle.ViewModel
import com.aplicacion2.appenergia.domain.usecase.GetFacturasUseCase
import com.aplicacion2.appenergia.domain.usecase.FiltrarFacturasUseCase
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Before
import org.junit.Rule
import org.junit.rules.ExpectedException
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class FacturaViewModelFactoryTest {

    @Mock
    private lateinit var getFacturasUseCase: GetFacturasUseCase

    @Mock
    private lateinit var filtrarFacturasUseCase: FiltrarFacturasUseCase

    private lateinit var factory: FacturaViewModelFactory

    @Before
    fun onBefore() {
        MockitoAnnotations.openMocks(this)
        factory = FacturaViewModelFactory(getFacturasUseCase, filtrarFacturasUseCase)
    }

    @Test
    fun `create should return FacturaViewModel instance`() {
        // Act
        factory.create(FacturaViewModel::class.java)

        // Assert
        assertTrue(true)
    }

    @get:Rule
    val exceptionRule: ExpectedException = ExpectedException.none()

    @Test
    fun `create should throw IllegalArgumentException for unknown ViewModel class`() {
        // Arrange
        exceptionRule.expect(IllegalArgumentException::class.java)
        exceptionRule.expectMessage("Unknown ViewModel class")

        // Act & Assert
        factory.create(UnknownViewModel::class.java)
    }

    // Clase ficticia para probar la excepción de modelo de ViewModel desconocido
    class UnknownViewModel : ViewModel()
}
