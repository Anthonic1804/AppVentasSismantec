package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventario_lotes")
data class InventarioLotesEntity(
    @PrimaryKey val id: Int,
    val idProducto: Int,
    val codigoProducto: String,
    val lote: String,
    val fechaVencimiento: String,
    val unidades: Float,
    val fracciones: Float
)