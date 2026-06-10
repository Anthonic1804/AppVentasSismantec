package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hoja_carga_detalle")
data class HojaCargaDetalleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id : Int = 0,

    @ColumnInfo(name = "Id_hojaCarga", defaultValue = "0")
    val idHojaCarga : Int = 0,

    @ColumnInfo(name = "Id_inventario", defaultValue = "0")
    val idInventario : Int = 0,

    @ColumnInfo(name = "Codigo_inventario", defaultValue = "")
    val codigoInventario : String = "",

    @ColumnInfo(name = "Cantidad", defaultValue = "0")
    val cantidad : Double = 0.0
)