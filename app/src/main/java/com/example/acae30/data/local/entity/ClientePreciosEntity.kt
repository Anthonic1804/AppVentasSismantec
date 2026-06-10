package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cliente_precios")
data class ClientePreciosEntity(
    @PrimaryKey
    @ColumnInfo(name = "Id")
    val id: Int = 0,

    @ColumnInfo(name = "id_cliente")
    val idCliente : Int = 0,

    @ColumnInfo(name = "id_inventario")
    val idInventario : Int = 0,

    @ColumnInfo(name = "precio_p")
    val precioP : Double = 0.0,

    @ColumnInfo(name = "precio_p_iva")
    val precioPiva : Double = 0.0,

    @ColumnInfo(name = "bonificado")
    val bonificado : Double = 0.0
)