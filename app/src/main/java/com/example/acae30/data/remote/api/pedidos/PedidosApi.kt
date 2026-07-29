package com.example.acae30.data.remote.api.pedidos

import com.example.acae30.data.remote.dto.PedidoTransmitidoDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PedidosApi {

    //------------------------------------------------------
    // Obtener pedidos transmitidos
    //------------------------------------------------------
    @GET("pedido/transmitido/{idPedidoApp}")
    suspend fun obtenerPedidoTransmitido(
        @Path("idPedidoApp") idPedidoApp: String
    ) : Response<PedidoTransmitidoDTO>

}