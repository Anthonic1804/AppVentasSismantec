package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hoja_detalle_recargas")
data class HojaDetalleRecargasEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id : Int = 0,

    @ColumnInfo(name = "id_hoja", defaultValue = "0")
    val idHoja : Int = 0,

    @ColumnInfo(name = "id_producto", defaultValue = "0")
    val idProducto : Int = 0,

    @ColumnInfo(name = "codigo_producto", defaultValue = "")
    val codigoProducto : String = "",

    @ColumnInfo(name = "cantidad", defaultValue = "0")
    val cantidad : Double = 0.0,

    @ColumnInfo(name = "recargado", defaultValue = "0")
    val recargado : Int = 0
)