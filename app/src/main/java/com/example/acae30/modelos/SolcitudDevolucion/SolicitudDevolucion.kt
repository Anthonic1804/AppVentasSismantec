package com.example.acae30.modelos.SolcitudDevolucion

class SolicitudDevolucion (
    val Id : Int,
    val Numero: Int,
    val Fecha: String,
    val Id_hoja_de_carga: Int,
    val Hoja_de_carga: Int,
    val Id_ruta: Int,
    val Ruta: String,
    val Id_vendedor: Int,
    val Vendedor: String,
    val Estado: String,
    var detalle: ArrayList<SolicitudDevolucionDetalle>?
)