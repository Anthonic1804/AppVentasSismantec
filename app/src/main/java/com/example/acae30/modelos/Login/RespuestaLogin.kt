package com.example.acae30.modelos.Login

data class RespuestaLogin (
    val error: Int,
    val nombreEmpleado: String,
    val response: String,
    val identidad: String,
    val estado: String,
    val generaToken: Int,
    val todos_clientes_App: String,
    val token: String
)