package com.example.acae30.data.repository

import android.content.Context
import com.example.acae30.Funciones
import com.example.acae30.data.local.dao.PedidosDao
import com.example.acae30.data.remote.api.pedidos.PedidosApi
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.data.remote.dto.PedidoTransmitidoDTO
import timber.log.Timber

class PedidosRepository(
    private val dao: PedidosDao
) {
    private val funciones = Funciones()

    // REFACTORIZACIÓN MVVM: Flujo para la lista de la UI (Todos los pedidos)
    fun obtenerTodosLosPedidosFlow() = dao.obtenerTodosLosPedidosFlow()

    // REFACTORIZACIÓN MVVM: Lista síncrona para el proceso de sincronización
    suspend fun obtenerPedidosNoTransmitidosLocal() = dao.obtenerListaPedidosNoTransmitidos()

    // REFACTORIZACIÓN MVVM: Consulta remota al servidor vía Retrofit
    suspend fun obtenerPedidoTransmitidoRemote(idPedidoApp: String, context: Context): PedidoTransmitidoDTO? {
        val preferencias = context.getSharedPreferences("CONFIG_SERVIDOR", Context.MODE_PRIVATE)
        val ip = preferencias.getString("ip", "") ?: ""
        val puerto = preferencias.getInt("puerto", 0).toString()
        val servidor = funciones.getServidor(ip, puerto, context)
        
        val api = RetrofitCliente.obtenerApi<PedidosApi>(servidor, context)
        
        return try {
            val respuesta = api.obtenerPedidoTransmitido(idPedidoApp)
            if (respuesta.isSuccessful) {
                respuesta.body()
            } else {
                Timber.e("Error al obtener pedido remoto: ${respuesta.code()}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error de conexión al obtener pedido remoto")
            null
        }
    }

    // REFACTORIZACIÓN MVVM: Actualización en BD local (Room)
    suspend fun actualizarInformacionPedido(
        idPedido: Int, pedidoDTE: Int, pedidoDteError: Int,
        dteAmbiente: String, dteCodigoGeneracion: String,
        dteSelloRecibido: String, dteNumeroControl: String,
        idDocTransmitido: Int
    ) = dao.actualizarInformacionPedido(
        idPedido, pedidoDTE, pedidoDteError, dteAmbiente,
        dteCodigoGeneracion, dteSelloRecibido, dteNumeroControl, idDocTransmitido
    )

    // REFACTORIZACIÓN MVVM: Marcar pedido como enviado y cerrar
    suspend fun actualizarEstadoPedidoEnviado(idServidor: Int, idPedido: Int) =
        dao.actualizarIdServidorConfirmandoPedido(idServidor, idPedido)

    // REFACTORIZACIÓN MVVM: Limpieza de pedidos antiguos o ya procesados
    suspend fun eliminarPedidos(fechaActual: String, eliminarCompletos: Boolean) {
        
        // PASO 1: Primero eliminamos los detalles (hijos) para evitar el error de Foreign Key
        dao.eliminarDetallesAntiguos(fechaActual)
        
        if (eliminarCompletos) {
            dao.eliminarDetallesTransmitidosDelDia(fechaActual)
        }

        // PASO 2: Ahora que los hijos han sido borrados, ya podemos borrar los padres (pedidos)
        dao.eliminarPedidosAntiguos(fechaActual)

        if (eliminarCompletos) {
            dao.eliminarPedidosTransmitidosDelDia(fechaActual)
        }

        // PASO 3: Limpieza final de seguridad
        dao.limpiarDetallesHuerfanos()
    }
}
