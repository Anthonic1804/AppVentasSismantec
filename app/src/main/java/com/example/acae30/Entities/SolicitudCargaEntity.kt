package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "solicitudCarga")
data class SolicitudCargaEntity(
    @PrimaryKey(autoGenerate = true)
    val Id: Int = 0,

    val Id_Empleado: Int,
    val Empleado: String,
    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_TIMESTAMP") val Fecha: String, // Necesitarás un TypeConverter para Date <-> Long

    val enviado: Int = 0,
    val idServidor: Int = 0,
    val Id_ruta: Int = 0,
    val Ruta: String = "",
    val Estado: String = "EMITIDO",
    val NumHoja: Long = 0
)
