package com.example.acae30.data.remote.api.inventario

import com.example.acae30.data.local.entity.InventarioEntity
import com.example.acae30.data.local.entity.InventarioLotesEntity
import com.example.acae30.data.local.entity.InventarioPreciosEntity
import com.example.acae30.data.local.entity.InventarioUnidadesEntity
import com.example.acae30.data.remote.dto.InventarioTiempoRealDto
import com.example.acae30.data.remote.dto.InventarioUnidadesDTO
import com.example.acae30.modelos.reporteUnidadesVendidas.UnidadesVendidasPorProducto
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.LocalDate

interface InventarioApi {
    //------------------------------------------------------
    //EndPoints Inventario
    //------------------------------------------------------

    @GET("inventario/cantidad")
    suspend fun obtenerTotalRegistrosInventario() : Int

    @GET("inventario/{offset}/{limit}")
    suspend fun obtenerInventario(
        @Path("offset") offset: Int,
        @Path("limit") limit: Int
    ) : List<InventarioEntity>

    //-------------------------------------------------------
    //Endpoints Inventario Precios
    //-------------------------------------------------------

    @GET("inventario/precios/cantidad")
    suspend fun obtenerTotalRegistrosPrecios() : Int

    @GET("inventario/precios/{offset}/{limit}")
    suspend fun obtenerEscalasPrecios(
        @Path("offset") offset: Int,
        @Path("limit") limit: Int
    ) : List<InventarioPreciosEntity>


    //---------------------------------------------------------
    //Endpoints Inventario Unidades
    //---------------------------------------------------------

    @GET("inventario/unidades/cantidad")
    suspend fun obtenerTotalRegistroUnidades() : Int

    @GET("inventario/unidades/{offset}/{limit}")
    suspend fun obtenerUnidadesInventario(
        @Path("offset") offset: Int,
        @Path("limit") limit: Int
    ) : List<InventarioUnidadesDTO>

    //----------------------------------------------------------
    //EndPoints Inventario Lotes
    //----------------------------------------------------------
    @GET("inventario/lotes/cantidad")
    suspend fun obtenerTotalRegistroLotes() : Int

    @GET("inventario/lotes/{offset}/{limit}")
    suspend fun obtenerLotesInventario(
        @Path("offset") offset: Int,
        @Path("limit") limit: Int
    ) : List<InventarioLotesEntity>

    //----------------------------------------------------------
    //Busqueda de productos en tiempo real
    //----------------------------------------------------------

    //Obtener un Producto por Id
    @GET("inventario/ObtenerProductoPorId/{id}")
    suspend fun obtenerProductoPorId(
        @Path("id") id : Int
    ) : List<InventarioEntity>

    //Búsqueda Inventario
    @GET("inventario/busqueda/inventario/{busqueda}")
    suspend fun obtenerProductoPorString(
        @Path("busqueda")  busqueda : String
    ) : List<InventarioTiempoRealDto>

    //Obtener Inventario Precios por IdProducto
    @GET("inventario/busqueda/precios/{idproducto}")
    suspend fun obtenerProductoPreciosPorId(
        @Path("idproducto") idproducto: Int
    ) : List<InventarioPreciosEntity>

    //Obtener Inventario Unidades por IdProducto
    @GET("inventario/busqueda/unidades/{idproducto}")
    suspend fun obtenerProductoUnidadesPorId(
        @Path("idproducto") idproducto: Int
    ) : List<InventarioUnidadesDTO>

    //Obtener Inventario Lotes por IdProducto
    @GET("inventario/busqueda/lotes/{idproducto}")
    suspend fun obtenerProductoLotesPorId(
        @Path("idproducto") idproducto: Int
    ) : List<InventarioLotesEntity>

    //---------------------------------------------------------
    // Endpoint para el recalculo de hoja de carga
    //---------------------------------------------------------
    @GET("inventario/recalcularhoja/{fecha}/{idVendedor}")
    suspend fun obtenerRecalculoHojaCarga(
        @Path("fecha") fecha : LocalDate,
        @Path("idVendedor") idVendedor : Int
    ) : List<UnidadesVendidasPorProducto>
}