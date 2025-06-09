package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventario_precios")
data class InventarioPreciosEntity(
    @PrimaryKey var Id:Int?,
    var Id_inventario:Int,
    var Codigo_producto:String,
    var Nombre:String,
    var Terminos:String?,
    var Plazo:Float,
    var Unidad:String,
    var Cantidad:Float,
    var Porcentaje:Float,
    var Precio:Float,
    var Precio_iva:Float,
    @ColumnInfo(name = "Id_inventario_unidad", defaultValue = "0") var Id_inventario_unidad:Int
)