package com.example.acae30.modelos

data class InventarioLotesModel (
    val id: Int,
    val idProducto: Int,
    val codigoProducto: String,
    val lote: String,
    val fechaVencimiento: String,
    val unidades: Float,
    val fracciones: Float
)