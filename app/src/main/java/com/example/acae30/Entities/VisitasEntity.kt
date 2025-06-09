package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visitas")
data class VisitaEntity(
    @PrimaryKey(autoGenerate = true)
    val Id: Int = 0,

    val Id_cliente: Int = 0,
    val Nombre_cliente: String,

    val Gps_in: String? = null,
    @ColumnInfo(name = "Fecha_inicial", defaultValue = "CURRENT_TIMESTAMP") val Fecha_inicial: String, // ISO 8601 string. Usa TypeConverter si necesitas Date.

    val Gps_out: String? = null,
    @ColumnInfo(name = "Fecha_final", defaultValue = "CURRENT_TIMESTAMP") val Fecha_final: String, // ISO 8601 string.

    val Idvisita: Int = 0,
    val Comentario: String = "",
    val Imagen_url: String = "",
    val Imagen: String = "",

    val Abierta: Boolean = false,
    val Enviado: Boolean = false,
    val Enviado_final: Boolean = false
)
