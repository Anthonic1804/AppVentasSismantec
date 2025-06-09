package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preciosAutorizados")
data class PreciosPersonalizadosEntity(
    @PrimaryKey val Id : Int,
    val Id_vendedor : Int,
    val Id_admin : Int,
    val cod_producto : String,
    val precio_asig : Float,
    @ColumnInfo(name = "fecha_registrado", defaultValue = "CURRENT_TIMESTAMP") val fecha_registrado : String,
    val id_server : Int
)