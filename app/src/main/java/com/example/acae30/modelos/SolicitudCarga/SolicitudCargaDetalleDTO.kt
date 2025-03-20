package com.example.acae30.modelos.SolicitudCarga

class SolicitudCargaDetalleDTO (
    val id : Int,
    val id_solicitud_carga : Int,
    val id_producto : Int,
    val codigo_producto : String,
    val descripcion : String,
    val cantidad : Float,
    val fraccion : Float,
    val costo : Float,
    val costo_iva : Float,
    val precio_u : Float,
    val precio_u_iva : Float,
    val total : Float
)