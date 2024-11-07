package com.example.acae30.modelos

data class GastoModel(
    val fecha: String,
    val tipoMovimiento: String,
    val concepto: String,
    val cuentaBanco : String,
    val banco : String,
    val numCheque : String,
    val mas_info: String,
    val valor: Float,
    val forma: String,
    val persona : String,
    val numeroCaja: Int
)