package com.example.acae30.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.acae30.data.local.models.PedidosNoTransmitidosModel
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

}