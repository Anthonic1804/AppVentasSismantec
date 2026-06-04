package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "inventario_precios")
data class InventarioPreciosEntity(
    @PrimaryKey
    @ColumnInfo(name = "Id")
    var id : Int,

    @ColumnInfo(name = "id_inventario")
    var id_inventario : Int,

    @ColumnInfo(name = "codigo_producto")
    var codigo_producto : String,

    @ColumnInfo(name = "nombre")
    var nombre : String,

    @ColumnInfo(name = "terminos")
    var terminos : String,

    @ColumnInfo(name = "plazo")
    var plazo : Float,

    @ColumnInfo(name = "unidad")
    var unidad : String,

    @ColumnInfo(name = "cantidad")
    var cantidad : Float,

    @ColumnInfo(name = "porcentaje")
    var porcentaje : Float,

    @ColumnInfo(name = "precio")
    var precio : Float,

    @ColumnInfo(name = "precio_iva")
    var precio_iva : Float,

    @ColumnInfo(name = "id_inventario_unidad")
    var id_inventario_unidad : Int
)