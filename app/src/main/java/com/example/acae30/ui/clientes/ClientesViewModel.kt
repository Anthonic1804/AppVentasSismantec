package com.example.acae30.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.domain.usecase.CrearPedidoDirectoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ClientesViewModel(
    private val repository: ClientesRepository,
    private val crearPedidoDirectoUseCase: CrearPedidoDirectoUseCase
) : ViewModel() {

    // Eventos de navegación
    sealed class Navegacion {
        data class IrADetallePedido(val idCliente: Int, val nombre: String, val codigo: String, val idPedido: Int) : Navegacion()
        data class IrAVisita(val idCliente: Int, val nombre: String, val codigo: String) : Navegacion()
    }

    private val _eventoNavegacion = MutableStateFlow<Navegacion?>(null)
    val eventoNavegacion = _eventoNavegacion.asStateFlow()

    //-------------------------------------------------------------
    // Procesa la selección de un cliente y procesa según el tipo de venta.
    //-------------------------------------------------------------
    fun seleccionarCliente(
        idCliente: Int,
        nombre: String,
        codigo: String,
        tipoVentaLocal: Boolean
    ) {
        viewModelScope.launch {
            if (tipoVentaLocal) {
                // PROCESO LOCAL: Crear pedido directamente
                try {
                    val nuevoIdPedido = crearPedidoDirectoUseCase.ejecutar(idCliente)
                    _eventoNavegacion.value = Navegacion.IrADetallePedido(
                        idCliente, nombre, codigo, nuevoIdPedido
                    )
                } catch (e: Exception) {
                    Timber.e(e, "[CLIENTES_VIEW_MODEL] ERROR EN PROCESO DE TIPOVENTALOCA = TRUE")
                }
            } else {
                // PROCESO EXTERNO: Ir a Visita.kt para GPS
                _eventoNavegacion.value = Navegacion.IrAVisita(idCliente, nombre, codigo)
            }
        }
    }

    fun resetNavegacion() {
        _eventoNavegacion.value = null
    }

}