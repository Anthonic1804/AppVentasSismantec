package com.example.acae30.ui.pedidos

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.domain.usecase.ObtenerDatosReporteUseCase
import com.example.acae30.domain.usecase.SincronizarPedidosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PedidosViewModel(
    private val repository: PedidosRepository,
    private val sincronizarUseCase: SincronizarPedidosUseCase,
    private val obtenerReporteUseCase: ObtenerDatosReporteUseCase
) : ViewModel() {

    // REFACTORIZACIÓN MVVM: Lista de pedidos observada directamente desde Room
    val pedidos = repository.obtenerTodosLosPedidosFlow()

    // Estado de la sincronización expuesto a la UI
    private val _syncStatus = MutableStateFlow<SincronizarPedidosUseCase.SyncProgress?>(null)
    val syncStatus = _syncStatus.asStateFlow()

    // Variable para controlar si el proceso es visible o silencioso (segundo plano)
    private val _esSegundoPlano = MutableStateFlow(false)
    val esSegundoPlano = _esSegundoPlano.asStateFlow()

    // REFACTORIZACIÓN MVVM: Estado de generación del reporte PDF
    private val _reportStatus = MutableStateFlow<ObtenerDatosReporteUseCase.ReportStatus?>(null)
    val reportStatus = _reportStatus.asStateFlow()


     //Inicia el proceso de sincronización.
    fun sincronizarPedidos(
        context: Context,
        esSilencioso: Boolean,
        eliminarAutomaticos: Boolean,
        tipoVentaLocal: Boolean
    ) {
        _esSegundoPlano.value = esSilencioso

        viewModelScope.launch {
            sincronizarUseCase.ejecutar(context, eliminarAutomaticos, tipoVentaLocal).collect { progreso ->
                _syncStatus.value = progreso
            }
        }
    }

     //Obtiene los datos del servidor y los guarda localmente para generar el PDF.
    fun obtenerDatosReporte(idVendedor: Int, fecha: String, context: Context) {
        viewModelScope.launch {
            obtenerReporteUseCase.ejecutar(idVendedor, fecha, context).collect { status ->
                _reportStatus.value = status
            }
        }
    }

     //Limpia el estado de sincronización después de procesarlo en la UI.
    fun resetSyncStatus() {
        _syncStatus.value = null
    }

     //Limpia el estado del reporte.
    fun resetReportStatus() {
        _reportStatus.value = null
    }
}
