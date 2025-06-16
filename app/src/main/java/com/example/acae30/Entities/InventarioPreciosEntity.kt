package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventario_precios")
data class InventarioPreciosEntity(
    @PrimaryKey var id : Int,
    var id_inventario : Int,
    var codigo_producto : String,
    var nombre : String,
    var terminos : String,
    var plazo : Float,
    var unidad : String,
    var cantidad : Float,
    var porcentaje : Float,
    var precio : Float,
    var precio_iva : Float,
    var id_inventario_unidad : Int
)