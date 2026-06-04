package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visitas")
data class VisitasEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Id_cliente", defaultValue = "0")
    val idCliente: Int,

    @ColumnInfo(name = "Nombre_cliente", defaultValue = "")
    val nombreCliente: String,

    @ColumnInfo(name = "Gps_in")
    val gpsIn: String? = null,

    @ColumnInfo(name = "Fecha_inicial", defaultValue = "CURRENT_TIMESTAMP")
    val fechaInicial: String,

    @ColumnInfo(name = "Gps_out")
    val gpsOut: String? = null,

    @ColumnInfo(name = "Fecha_final", defaultValue = "CURRENT_TIMESTAMP")
    val fechaFinal: String,

    @ColumnInfo(name = "Idvisita", defaultValue = "0")
    val idVisita: Int,

    @ColumnInfo(name = "Comentario", defaultValue = "")
    val comentario: String,

    @ColumnInfo(name = "Imagen_url", defaultValue = "")
    val imagenUrl: String,

    @ColumnInfo(name = "Imagen", defaultValue = "")
    val imagen: String,

    @ColumnInfo(name = "Abierta", defaultValue = "false")
    val abierta: Boolean,

    @ColumnInfo(name = "Enviado", defaultValue = "false")
    val enviado: Boolean,

    @ColumnInfo(name = "Enviado_final", defaultValue = "false")
    val enviadoFinal: Boolean
)