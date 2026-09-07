package com.example.acae30.data.remote.api.pedidos

import com.example.acae30.data.remote.dto.PedidoTransmitidoDTO
import com.example.acae30.data.remote.dto.ReportePedidoDTO
import com.example.acae30.modelos.JSONmodels.BusquedaReporteJSON
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PedidosApi {

    //------------------------------------------------------
    // Obtener pedidos transmitidos
    //------------------------------------------------------
    @GET("pedido/transmitido/{idPedidoApp}")
    suspend fun obtenerPedidoTransmitido(
        @Path("idPedidoApp") idPedidoApp: String
    ) : Response<PedidoTransmitidoDTO>

    //------------------------------------------------------
    // Obtener listado de pedidos para reporte diario
    //------------------------------------------------------
    @POST("pedido/reporte")
    suspend fun obtenerReporteDiario(
        @Body busqueda: BusquedaReporteJSON
    ) : Response<List<ReportePedidoDTO>>

    //------------------------------------------------------
    // Enviar pedido completo al servidor
    //------------------------------------------------------
    @POST("pedido")
    suspend fun enviarPedido(
        @Body request: com.example.acae30.data.remote.dto.EnviarPedidoRequestDto
    ): Response<com.example.acae30.data.remote.dto.VisitaResponseDto>
}
