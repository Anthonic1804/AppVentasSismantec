package com.example.acae30.modelos.SolicitudCarga

data class SolicitudCargaDetalle (

    val id : Int,
    val idSolicitudCarga : Int,
    val idProducto : Int,
    val codigoProducto : String,
    val descripcion : String,
    val cantidad : Float,
    val costo : Float,
    val costoIva : Float,
    val precio : Float,
    val precio_iva : Float,
    val total : Float

)