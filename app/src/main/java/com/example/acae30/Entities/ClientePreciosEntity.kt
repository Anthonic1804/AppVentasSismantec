package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "cliente_precios")
data class ClientePreciosEntity(
    val id_cliente : Int,
    val id_inventario : Int,
    val precio_p : Float,
    val precio_p_iva : Float,
    @ColumnInfo(name = "bonificado" , defaultValue = "0") val bonificado : Float
)