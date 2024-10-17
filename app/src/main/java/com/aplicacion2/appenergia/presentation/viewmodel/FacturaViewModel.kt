package com.aplicacion2.appenergia.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacion2.appenergia.domain.model.Factura
import com.aplicacion2.appenergia.domain.usecase.GetFacturasUseCase
import com.aplicacion2.appenergia.domain.usecase.FiltrarFacturasUseCase
import kotlinx.coroutines.launch

class FacturaViewModel(
    private val getFacturasUseCase: GetFacturasUseCase,
    private val filtrarFacturasUseCase: FiltrarFacturasUseCase
) : ViewModel() {

    private val _facturas = MutableLiveData<List<Factura>>()
    val facturas: LiveData<List<Factura>> get() = _facturas

    // Cargar todas las facturas sin filtros
    fun cargarFacturas() {
        viewModelScope.launch {
            val facturas = getFacturasUseCase() // Obtener todas las facturas
            _facturas.value = facturas
        }
    }

    // Aplicar filtros localmente
    fun aplicarFiltros(
        estados: List<String>,
        valorMaximo: Int,
        fechaDesde: Long?,
        fechaHasta: Long?
    ) {
        viewModelScope.launch {
            val facturasFiltradas = filtrarFacturasUseCase(estados, valorMaximo, fechaDesde, fechaHasta)
            _facturas.value = facturasFiltradas
        }
    }
}



