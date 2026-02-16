package com.example.acae30.modelos.Servidores

data class ServidoresModel (
    var id: Int,
    var nombre: String,
    var ip: String,
    var puerto: String,
    var ssl: Int
)