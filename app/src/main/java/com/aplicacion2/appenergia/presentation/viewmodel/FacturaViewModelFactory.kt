package com.aplicacion2.appenergia.presentation.viewmodel

import FacturaViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aplicacion2.appenergia.domain.usecase.GetFacturasUseCase
import com.aplicacion2.appenergia.domain.usecase.FiltrarFacturasUseCase

class FacturaViewModelFactory(
    private val getFacturasUseCase: GetFacturasUseCase,
    private val filtrarFacturasUseCase: FiltrarFacturasUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FacturaViewModel::class.java)) {
            return FacturaViewModel(getFacturasUseCase, filtrarFacturasUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

