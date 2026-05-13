package com.example.acae30.Interface

import com.example.acae30.Entities.InventarioEntity
import com.example.acae30.Entities.InventarioLotesEntity
import com.example.acae30.Entities.InventarioPreciosEntity
import com.example.acae30.Entities.InventarioUnidadesEntity
import com.example.acae30.listas.InventarioRetrofit
import com.example.acae30.modelos.InventarioTiempoRealModel
import com.example.acae30.modelos.JSONmodels.HojaCargaJSON
import com.example.acae30.modelos.Login.LoginModel
import com.example.acae30.modelos.Login.RespuestaLogin
import com.example.acae30.modelos.RespuestaConexion
import com.example.acae30.modelos.cierreParcial.CierreParcialDTO
import com.example.acae30.modelos.reporteUnidadesVendidas.UnidadesVendidasPorProducto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.time.LocalDate
import java.util.Date

interface AppVentasApi {

    //Conexion con el Servidor
    @GET("conexion")
    suspend fun conectarServidor() : List<RespuestaConexion>

    //--------------------------------------
    //EndPoints Login
    //11-03-2026
    //--------------------------------------

    @POST("login")
    suspend fun login(
        @Body credenciales : LoginModel
    ) : Response<RespuestaLogin>


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
    ) : List<InventarioUnidadesEntity>

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

    //---------------------------------------------------------
    // Endpoint para el recalculo de hoja de carga
    //---------------------------------------------------------
    @GET("inventario/recalcularhoja/{fecha}/{idVendedor}")
    suspend fun obtenerRecalculoHojaCarga(
        @Path("fecha") fecha : LocalDate,
        @Path("idVendedor") idVendedor : Int
    ) : List<UnidadesVendidasPorProducto>

    //----------------------------------------------------------
    //Reporte de Unidades Vendidas por Producto
    //----------------------------------------------------------
    @GET("ventasPorProducto/{numeroCaja}/{fecha}")
    suspend fun obtenerUnidadesVendidasPorProducto(
        @Path("numeroCaja") numeroCaja : Int,
        @Path("fecha") fecha : LocalDate
    ) : List<UnidadesVendidasPorProducto>

    //----------------------------------------------------------
    //Obtener Datos del Cierre Parcial
    //----------------------------------------------------------
    @GET("generarCierreParcial/{numeroCaja}/{fecha}")
    suspend fun obtenerDatosCierreParcialCaja(
        @Path("numeroCaja") numeroCaja : Int,
        @Path("fecha") fecha : LocalDate
    ) : List<CierreParcialDTO>


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
    ) : List<InventarioTiempoRealModel>

    //Obtener Inventario Precios por IdProducto
    @GET("inventario/busqueda/precios/{idproducto}")
    suspend fun obtenerProductoPreciosPorId(
        @Path("idproducto") idproducto: Int
    ) : List<InventarioPreciosEntity>

    //Obtener Inventario Unidades por IdProducto
    @GET("inventario/busqueda/unidades/{idproducto}")
    suspend fun obtenerProductoUnidadesPorId(
        @Path("idproducto") idproducto: Int
    ) : List<InventarioUnidadesEntity>

    //Obtener Inventario Lotes por IdProducto
    @GET("inventario/busqueda/lotes/{idproducto}")
    suspend fun obtenerProductoLotesPorId(
        @Path("idproducto") idproducto: Int
    ) : List<InventarioLotesEntity>


}