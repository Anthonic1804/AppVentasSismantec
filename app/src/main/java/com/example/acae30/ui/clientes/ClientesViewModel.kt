package com.example.acae30.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.domain.usecase.CrearPedidoDirectoUseCase
import com.example.acae30.domain.usecase.clientes.ObtenerListaClientesUseCase
import com.example.acae30.modelos.Cliente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ClientesViewModel(
    private val repository: ClientesRepository,
    private val crearPedidoDirectoUseCase: CrearPedidoDirectoUseCase,
    private val obtenerListaClientesUseCase: ObtenerListaClientesUseCase
) : ViewModel() {

    // Listado de clientes para la UI
    private val _listaClientes = MutableStateFlow<List<Cliente>>(emptyList())
    val listaClientes = _listaClientes.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Eventos de navegación
    sealed class Navegacion {
        data class IrADetallePedido(val idCliente: Int, val nombre: String, val codigo: String, val idPedido: Int) : Navegacion()
        data class IrAVisita(val idCliente: Int, val nombre: String, val codigo: String) : Navegacion()
        data class IrAFirmarPagare(
            val idCliente: Int,
            val nombre: String,
            val direccion: String,
            val dui: String,
            val limiteCredito: Float,
            val plazo: Long
        ) : Navegacion()
        data class IrAHistorico(val idCliente: Int, val nombre: String) : Navegacion()
        data class IrADetalleCliente(val idCliente: Int) : Navegacion()
    }

    private val _eventoNavegacion = MutableStateFlow<Navegacion?>(null)
    val eventoNavegacion = _eventoNavegacion.asStateFlow()

    //--------------------------------------------------------------
    // Carga o filtra el listado de clientes desde Room.
    //--------------------------------------------------------------
    fun cargarClientes(filtro: String = "", idRuta: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val clientes = obtenerListaClientesUseCase.ejecutar(filtro, idRuta)
                _listaClientes.value = clientes
            } catch (e: Exception) {
                Timber.e(e, "[CLIENTES_VM] ERROR AL CARGAR CLIENTES")
            } finally {
                _isLoading.value = false
            }
        }
    }

    //--------------------------------------------------------------
    // Procesa la selección de un cliente basándose en el contexto (Pedido, Histórico, etc.)
    //--------------------------------------------------------------
    fun seleccionarCliente(
        cliente: Cliente,
        busquedaPedido: Boolean,
        tipoVentaLocal: Boolean,
        pagareObligatorio: Boolean,
        historico: Boolean
    ) {
        viewModelScope.launch {
            val idCliente = cliente.Id!!
            val nombre = cliente.Cliente!!
            val codigo = cliente.Codigo!!

            if (busquedaPedido) {
                // NUEVO PEDIDO
                
                // LOGS DE DEPURACIÓN PARA VALIDAR EL BLOQUEO DEL PAGARÉ
                Timber.d("[CLIENTES_VM] idCliente: $idCliente | PagareOblig: $pagareObligatorio | Terminos: ${cliente.Terminos_cliente} | Firmado: ${cliente.Firmar_pagare_app}")

                // Verificación de Pagaré (Solo si es Crédito y no ha firmado)
                val esCredito = cliente.Terminos_cliente?.trim()?.equals("Credito", ignoreCase = true) == true
                val noHaFirmado = (cliente.Firmar_pagare_app ?: 0) == 0

                if (pagareObligatorio && esCredito && noHaFirmado) {
                    Timber.d("[CLIENTES_VM] REDIRECCIONANDO A LECTURA DE PAGARÉ")
                    _eventoNavegacion.value = Navegacion.IrAFirmarPagare(
                        idCliente = idCliente,
                        nombre = nombre,
                        direccion = cliente.Direccion ?: "",
                        dui = cliente.Dui ?: "",
                        limiteCredito = cliente.Limite_credito ?: 0f,
                        plazo = cliente.Plazo_credito?.toLong() ?: 0L
                    )
                    return@launch
                }

                // Decisión entre Local o Externa (Si ya firmó o es Contado)
                if (tipoVentaLocal) {
                    try {
                        val nuevoIdPedido = crearPedidoDirectoUseCase.ejecutar(idCliente)
                        _eventoNavegacion.value = Navegacion.IrADetallePedido(idCliente, nombre, codigo, nuevoIdPedido)
                    } catch (e: Exception) {
                        Timber.e(e, "[CLIENTES_VM] ERROR CREANDO PEDIDO DIRECTO")
                    }
                } else {
                    _eventoNavegacion.value = Navegacion.IrAVisita(idCliente, nombre, codigo)
                }
            } else if (historico) {
                _eventoNavegacion.value = Navegacion.IrAHistorico(idCliente, nombre)
            } else {
                _eventoNavegacion.value = Navegacion.IrADetalleCliente(idCliente)
            }
        }
    }

    fun resetNavegacion() {
        _eventoNavegacion.value = null
    }
}
