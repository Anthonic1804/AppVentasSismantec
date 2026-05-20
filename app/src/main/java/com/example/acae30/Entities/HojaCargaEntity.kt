package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hoja_carga")
data class HojaCargaEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id : Int = 0,

    @ColumnInfo(name = "idHojaCarga", defaultValue = "0")
    val idHojaCarga : Int = 0,

    @ColumnInfo(name = "numeroHoja", defaultValue = "0")
    val numeroHoja : Int = 0,

    @ColumnInfo(name = "Fecha_registro", defaultValue = "CURRENT_DATE")
    val fechaRegistro : String = "",

    @ColumnInfo(name = "Id_ruta", defaultValue = "0")
    val idRuta : Int = 0,

    @ColumnInfo(name = "Ruta")
    val ruta : String? = null,

    @ColumnInfo(name = "Devolucion", defaultValue = "0")
    val devolucion : Int = 0,

    @ColumnInfo(name = "Fecha", defaultValue = "")
    val fecha : String = "",

    @ColumnInfo(name = "Recalculada", defaultValue = "false")
    val recalculada : Boolean = false

)