package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hoja_detalle_recargas")
data class HojaDetalleRecargasEntity(
    @PrimaryKey val id : Int,
    @ColumnInfo(name = "id_hoja", defaultValue = "0") val id_hoja : Int,
    @ColumnInfo(name = "id_producto", defaultValue = "0") val id_producto : Int,
    val codigo_producto : String,
    @ColumnInfo(name = "cantidad", defaultValue = "0") val cantidad : Float,
    @ColumnInfo(name = "recargado", defaultValue = "0") val recargado : Int
)