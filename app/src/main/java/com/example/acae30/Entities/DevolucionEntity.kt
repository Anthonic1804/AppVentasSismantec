package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devolucion")
data class DevolucionEntity(
    @PrimaryKey(autoGenerate = true)
    val Id: Int = 0,

    val Numero: Int,
    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_TIMESTAMP") val Fecha: String, // timestamp con valor por defecto actual
    val Id_hoja_de_carga: Int,
    val Hoja_de_carga: Int,
    val Id_ruta: Int?, // nullable
    val Ruta: String?, // nullable
    val Id_vendedor: Int,
    val Vendedor: String,
    val Estado: String? // nullable
)
