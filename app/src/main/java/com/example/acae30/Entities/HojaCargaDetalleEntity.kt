package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hoja_carga_detalle")
data class HojaCargaDetalleEntity(
    @PrimaryKey val Id : Int,
    @ColumnInfo(name = "Id_hojaCarga", defaultValue = "0") val Id_hojaCarga : Int,
    @ColumnInfo(name = "Id_inventario", defaultValue = "0") val Id_inventario : Int,
    val Codigo_inventario : String,
    @ColumnInfo(name = "Cantidad", defaultValue = "0") val Cantidad : Float
)