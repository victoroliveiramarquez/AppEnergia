import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aplicacion2.appenergia.domain.model.FacturaBDD
import com.aplicacion2.appenergia.domain.usecase.FiltrarFacturasUseCase
import com.aplicacion2.appenergia.domain.usecase.GetFacturasUseCase
import kotlinx.coroutines.launch

class FacturaViewModel(
    private val getFacturasUseCase: GetFacturasUseCase,
    private val filtrarFacturasUseCase: FiltrarFacturasUseCase
) : ViewModel() {

    private val _facturasBDD = MutableLiveData<List<FacturaBDD>>()
    val facturasBDD: LiveData<List<FacturaBDD>> get() = _facturasBDD

    // Cargar todas las facturas desde la API y almacenarlas en Room
    fun cargarFacturas() {
        viewModelScope.launch {
            val facturas = getFacturasUseCase() // Obtener todas las facturas desde Room
            _facturasBDD.value = facturas
        }
    }

    // Aplicar filtros por estado, valor y fechas
    fun aplicarFiltros(
        estados: List<String>,
        valorMaximo: Int,
        fechaDesde: Long?,
        fechaHasta: Long?
    ) {
        viewModelScope.launch {
            val facturasFiltradas: List<FacturaBDD> = filtrarFacturasUseCase(estados, valorMaximo, fechaDesde, fechaHasta)
            _facturasBDD.value = facturasFiltradas
        }
    }

    // Cargar facturas desde la API por primera vez y almacenarlas en Room
    fun cargarFacturasPorPrimeraVez() {
        viewModelScope.launch {
            val facturas = getFacturasUseCase(forceApi = true) // Forzar la obtención desde la API
            _facturasBDD.value = facturas
        }
    }
}



