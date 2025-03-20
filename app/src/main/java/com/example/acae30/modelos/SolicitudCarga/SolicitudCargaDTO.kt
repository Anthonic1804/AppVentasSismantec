package com.example.acae30.modelos.SolicitudCarga

class SolicitudCargaDTO (
    val id_empleado : Int,
    val empleado : String,
    val fecha : String,
    val punto_venta : String,
    val id_ruta : Int,
    val ruta : String,
    val detalle : List<SolicitudCargaDetalleDTO>
)