package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventario_lotes")
data class InventarioLotesEntity(
    @PrimaryKey
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "idProducto")
    val idProducto: Int,

    @ColumnInfo(name = "codigoProducto")
    val codigoProducto: String,

    @ColumnInfo(name = "lote")
    val lote: String,

    @ColumnInfo(name = "fechaVencimiento")
    val fechaVencimiento: String,

    @ColumnInfo(name = "unidades")
    val unidades: Float,

    @ColumnInfo(name = "fracciones")
    val fracciones: Float
)