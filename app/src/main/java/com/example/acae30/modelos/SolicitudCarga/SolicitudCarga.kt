package com.example.acae30.modelos.SolicitudCarga

data class SolicitudCarga (
    val id : Int,
    val idEmpleado : Int,
    val empleado : String,
    val fecha : String,
    val enviado : Int,
    val idServidor : Int,
    val idRuta : Int,
    val ruta : String,
    val estado : String,
    val numHoja: Float,
    val guardado: Int
)