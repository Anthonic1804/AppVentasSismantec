package com.example.acae30.domain.usecase

import android.content.Context
import com.example.acae30.Funciones
import com.example.acae30.data.repository.PedidosRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class SincronizarPedidosUseCase(
    private val repository: PedidosRepository
) {
    private val funciones = Funciones()

    // Clase para representar el estado del progreso
    sealed class SyncProgress {
        object Iniciando : SyncProgress()
        data class Procesando(val mensaje: String) : SyncProgress()
        object Exito : SyncProgress()
        data class Error(val error: String) : SyncProgress()
        object Finalizado : SyncProgress()
    }

     //Ejecuta la sincronización emitiendo estados de progreso.
    fun ejecutar(
        context: Context,
        eliminarAutomaticos: Boolean,
        tipoVentaLocal: Boolean
    ): Flow<SyncProgress> = flow {
        
        Timber.d("Iniciando sincronización - tipoVentaLocal: $tipoVentaLocal, eliminarAutomaticos: $eliminarAutomaticos")
        
        emit(SyncProgress.Iniciando)

        // Verificación de internet
        if (!funciones.isInternetAvailable(context)) {
            emit(SyncProgress.Error("NO TIENE CONEXION A INTERNET"))
            return@flow
        }

        try {
            // Obtener pedidos pendientes
            val pedidos = repository.obtenerPedidosNoTransmitidosLocal()
            delay(500)

            if (pedidos.isNotEmpty()) {
                for (item in pedidos) {
                    emit(SyncProgress.Procesando("SINCRONIZANDO PEDIDO:\n${item.nombreCliente}\nID: ${item.idPedidoApp}"))
                    delay(800)

                    // Consultar al servidor
                    val idApp = item.idPedidoApp ?: ""
                    if (idApp.isNotEmpty()) {
                        val pedidoRemoto = repository.obtenerPedidoTransmitidoRemote(idApp, context)

                        if (pedidoRemoto != null && pedidoRemoto.encontrado) {
                            val pedidoDTE = if (pedidoRemoto.pedidoDte == true) 1 else 0
                            val pedidoDteError = if (pedidoRemoto.pedidoDteError == true) 1 else 0

                            if (pedidoRemoto.pedidoDte == true) {
                                // Actualizar información DTE
                                repository.actualizarInformacionPedido(
                                    item.id,
                                    pedidoDTE,
                                    pedidoDteError,
                                    pedidoRemoto.dteAmbiente ?: "",
                                    pedidoRemoto.dteCodigoGeneracion ?: "",
                                    pedidoRemoto.dteSelloRecibido ?: "",
                                    pedidoRemoto.dteNumeroControl ?: "",
                                    pedidoRemoto.idDocTransmitido ?: 0
                                )
                            } else {
                                // Marcar como enviado
                                repository.actualizarEstadoPedidoEnviado(
                                    pedidoRemoto.idPedido ?: 0,
                                    item.id
                                )
                            }
                        }
                    }
                }
                emit(SyncProgress.Exito)
            } else {
                emit(SyncProgress.Procesando("NO HAY PEDIDOS PENDIENTES"))
            }

            // Eliminar (tipoVentaLocal y eliminarAutomaticos activos)
            if (tipoVentaLocal && eliminarAutomaticos) {
                val fechaActual = funciones.obtenerFecha() ?: ""
                repository.eliminarPedidos(fechaActual, eliminarCompletos = true)
            }

        } catch (e: Exception) {
            Timber.e(e, "Error durante la ejecución del UseCase de sincronización")
            emit(SyncProgress.Error("ERROR INESPERADO: ${e.message}"))
        } finally {
            emit(SyncProgress.Finalizado)
        }
    }
}
