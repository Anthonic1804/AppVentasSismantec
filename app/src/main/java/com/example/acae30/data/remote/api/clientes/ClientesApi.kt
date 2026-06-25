package com.example.acae30.data.remote.api.clientes

import com.example.acae30.data.remote.dto.BalanceClienteDTO
import com.example.acae30.data.remote.dto.ClienteSucursalDto
import com.example.acae30.data.remote.dto.ClientesDto
import com.example.acae30.data.remote.dto.ClientesPreciosDto
import com.example.acae30.data.remote.dto.CxCDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ClientesApi {
    //---------------------------------------------------------
    //Funciones para Clientes
    //---------------------------------------------------------

    //Obatener clientes precios cantidad
    @GET("clientes/precios/cantidad")
    suspend fun obtenerTotalRegistrosClientesPrecios() : Int

    @GET("clientes/precios/{lastId}/{take}")
    suspend fun obtenerClientesPrecios(
        @Path("lastId") lastId: Int,
        @Path("take") take: Int
    ) : List<ClientesPreciosDto>

    //Obtener clientes cantidad
    @GET("clientes/cantidad")
    suspend fun obtenerTotalRegistrosClientes() : Int

    //Obtener Listado de Clientes
    @GET("clientes/todos/{lastId}/{take}")
    suspend fun obtenerListadoClientes(
        @Path("lastId") lastId: Int,
        @Path("take") take: Int
    ) : List<ClientesDto>

    //Obtener Listado de Clientes
    @GET("clientes/vendedor/{idVendedor}/{lastId}/{take}")
    suspend fun obtenerListadoClientesVendedor(
        @Path("idVendedor") idVendedor: Int,
        @Path("lastId") lastId: Int,
        @Path("take") take: Int
    ) : List<ClientesDto>

    //Obtener CAntidad Registros Sucursal
    @GET("sucursales/cantidad")
    suspend fun obtenerTotalRegitroSucursal() : Int

    //Obtener Listado Clientes Sucursal
    @GET("sucursales/{lastId}/{take}")
    suspend fun obtenerListadoSucursales(
        @Path("lastId") lastId: Int,
        @Path("take") take: Int
    ) : List<ClienteSucursalDto>

    //Obtener Cantidad de Registros de CxC Pendientes
    @GET("cuentas/cantidad")
    suspend fun obtenerTotalRegistroCxCPendientes() : Int

    //Obtener Listado de CxC Pendientes
    @GET("cuentas/{lastId}/{take}")
    suspend fun obtenerListadoCuentasPendientes(
        @Path("lastId") lastId: Int,
        @Path("take") take: Int
    ) : List<CxCDto>

    //Obtener el Balance y Limite de Credito en Tiempo Real
    @GET("clientes/balance/{idCliente}")
    suspend fun obtenerBalancePorIdCliente(
        @Path("idCliente") idCliente: Int
    ) : Response<BalanceClienteDTO>
}