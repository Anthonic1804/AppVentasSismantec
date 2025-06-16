package com.example.acae30.Interface

import com.example.acae30.Entities.InventarioEntity
import com.example.acae30.Entities.InventarioPreciosEntity
import com.example.acae30.listas.InventarioRetrofit
import retrofit2.http.GET
import retrofit2.http.Path

interface AppVentasApi {

    //ENDPOINTS DE INVENTARIO

    //Obtener Inventario
    @GET("inventario/{offset}/{limit}")
    suspend fun obtenerInventario(
        @Path("offset") offset: Int,
        @Path("limit") limit: Int
    ) : List<InventarioEntity>

    //Obtener Cantidad de Registros de Inventario
    @GET("inventario/cantidad")
    suspend fun obtenerTotalRegistrosInventario() : Int

    //Obtner Cantidad de Registros de Escalas de PRecios
    @GET("inventario/precios/cantidad")
    suspend fun obtenerTotalRegistrosPrecios() : Int

    //Obtener Escalas de Precios
    @GET("inventario/precios/{offset}/{limit}")
    suspend fun obtenerEscalasPrecios(
        @Path("offset") offset: Int,
        @Path("limit") limit: Int
    ) : List<InventarioPreciosEntity>

}