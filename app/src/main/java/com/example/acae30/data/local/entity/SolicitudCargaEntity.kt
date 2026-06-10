package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudCarga")
data class SolicitudCargaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Id_Empleado", defaultValue = "0")
    val idEmpleado: Int,

    @ColumnInfo(name = "Empleado", defaultValue = "")
    val empleado: String,

    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_TIMESTAMP")
    val fecha: String,

    @ColumnInfo(name = "enviado", defaultValue = "0")
    val enviado: Int,

    @ColumnInfo(name = "idServidor", defaultValue = "0")
    val idServidor: Int,

    @ColumnInfo(name = "Id_ruta", defaultValue = "0")
    val idRuta: Int,

    @ColumnInfo(name = "Ruta", defaultValue = "")
    val ruta: String,

    @ColumnInfo(name = "Estado", defaultValue = "EMITIDO")
    val estado: String,

    @ColumnInfo(name = "NumHoja", defaultValue = "0")
    val numHoja: Double,

    @ColumnInfo(name = "Guardado", defaultValue = "0")
    val guardado: Int
)