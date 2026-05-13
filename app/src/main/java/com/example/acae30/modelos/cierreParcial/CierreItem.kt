package com.example.acae30.modelos.cierreParcial

sealed class CierreItem {

    data class Header(val titulo: String) : CierreItem()

    data class TipoDocumento(val nombre: String) : CierreItem()

    data class Correlativo(
        val inicial: String,
        val final: String
    ) : CierreItem()

    data class FormaPago(
        val nombre: String,
        val monto: Double
    ) : CierreItem()

    data class Termino(
        val nombre: String,
        val monto: Double
    ) : CierreItem()

    data class Resumen(
        val titulo: String,
        val monto: Double
    ) : CierreItem()

}