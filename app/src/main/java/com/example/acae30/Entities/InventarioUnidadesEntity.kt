package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "inventario_unidades")
data class InventarioUnidadesEntity(
    @PrimaryKey
    @ColumnInfo(name = "Id")
    val Id : Int,

    @ColumnInfo(name = "Id_inventario")
    val Id_inventario : Int,

    @ColumnInfo(name = "Nombre_unidad")
    val Nombre_unidad : String,

    @ColumnInfo(name = "Equivale")
    val Equivale : Float,

    @ColumnInfo(name = "Unidades")
    val Unidades : String
)