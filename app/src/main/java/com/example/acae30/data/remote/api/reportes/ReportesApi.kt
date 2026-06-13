package com.example.acae30.data.remote.api.reportes

import com.example.acae30.modelos.cierreParcial.CierreParcialDTO
import com.example.acae30.modelos.reporteUnidadesVendidas.UnidadesVendidasPorProducto
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.LocalDate

interface ReportesApi {
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
}