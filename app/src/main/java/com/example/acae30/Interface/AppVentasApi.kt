package com.example.acae30.Interface

import com.example.acae30.listas.InventarioRetrofit
import retrofit2.http.GET
import retrofit2.http.Path

interface AppVentasApi {

    //ENDPOINTS DE INVENTARIO

    @GET("inventario/{offset}/{limit}")
    suspend fun obtenerInventario(
        @Path("offset") offset: Int,
        @Path("limit") limit: Int
    ) : List<InventarioRetrofit>

}