package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "hoja_carga")
data class HojaCargaEntity(
    @PrimaryKey val Id : Int,
    @ColumnInfo(name = "idHojaCarga", defaultValue = "0") val idHojaCarga : Int,
    @ColumnInfo(name = "numeroHoja", defaultValue = "0") val numeroHoja : Int,
    @ColumnInfo(name = "Fecha_registro", defaultValue = "CURRENT_TIMESTAMP") val Fecha_registro : String,
    @ColumnInfo(name = "Id_ruta", defaultValue = "0") val Id_ruta : Int,
    val Ruta : String,
    @ColumnInfo(name = "Devolucion", defaultValue = "0") val Devolucion : Int
)