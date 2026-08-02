package com.example.acae30.ui.pedidos

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.domain.usecase.SincronizarPedidosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * REFACTORIZACIÓN MVVM: ViewModel para la pantalla de Pedidos.
 * Gestiona el estado de la UI y coordina la lógica con los Casos de Uso.
 */
class PedidosViewModel(
    private val repository: PedidosRepository,
    private val sincronizarUseCase: SincronizarPedidosUseCase
) : ViewModel() {

    // REFACTORIZACIÓN MVVM: Lista de pedidos observada directamente desde Room
    val pedidos = repository.obtenerTodosLosPedidosFlow()

    // Estado de la sincronización expuesto a la UI
    private val _syncStatus = MutableStateFlow<SincronizarPedidosUseCase.SyncProgress?>(null)
    val syncStatus = _syncStatus.asStateFlow()

    // Variable para controlar si el proceso es visible o silencioso (segundo plano)
    private val _esSegundoPlano = MutableStateFlow(false)
    val esSegundoPlano = _esSegundoPlano.asStateFlow()

    /**
     * Inicia el proceso de sincronización.
     * @param context Contexto necesario para verificar red y acceder a SharedPreferences en el repositorio.
     * @param esSilencioso Si es true, la UI no mostrará diálogos de carga.
     * @param eliminarAutomaticos Preferencia del usuario para limpiar pedidos antiguos.
     * @param tipoVentaLocal Indica si la app está en modo venta local.
     */
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

    /**
     * Limpia el estado de sincronización después de procesarlo en la UI.
     */
    fun resetSyncStatus() {
        _syncStatus.value = null
    }
}
