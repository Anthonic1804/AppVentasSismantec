package com.example.acae30.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.acae30.data.local.models.PedidosNoTransmitidosModel
import com.example.acae30.data.remote.dto.PedidoTransmitidoDTO
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidosDao {

    //-------------------------------------------------------------
    //Opteniendo listado de pedido no transmitidos del día
    //-------------------------------------------------------------
    @Query("SELECT Id, " +
            "Nombre_cliente " +
            "FROM pedidos " +
            "WHERE Enviado=1 AND pedido_dte=0 AND Tipo_documento != 'RC'")
    fun obtenerListadoPedidosNoTransmitidos() : Flow<List<PedidosNoTransmitidosModel>>

    //--------------------------------------------------------------
    // Confirmando el Envio del Pedido, Actualizadno el IdServidor en ROOM
    //--------------------------------------------------------------
    @Query("""
        UPDATE pedidos
        SET Id_pedido_sistema = :idServidor,
            Enviado = 1,
            Cerrado = 1
        WHERE Id = :idPedido
    """)
    suspend fun actualizarIdServidorConfirmandoPedido(
        idServidor: Int,
        idPedido: Int
    ) : Int

    //--------------------------------------------------------------------
    // Actualizando información del pedido transmitido
    //--------------------------------------------------------------------
    @Query("""
        UPDATE pedidos
        SET pedido_dte = :pedidoDTE, pedido_dte_error = :pedidoDteError,
            dteAmbiente = :dteAmbiente, dteCodigoGeneracion = :dteCodigoGeneracion,
            dteSelloRecibido = :dteSelloRecibido, dteNumeroControl = :dteNumeroControl,
            idDocTransmitido = :idDocTransmitido, Enviado = 1, Cerrado = 1
        WHERE Id = :idPedido

    """)
    suspend fun actualizarInformacionPedido(
        idPedido: Int, pedidoDTE : Int, pedidoDteError:Int,
        dteAmbiente:String, dteCodigoGeneracion:String, dteSelloRecibido:String, dteNumeroControl:String,
        idDocTransmitido: Int
    )



}