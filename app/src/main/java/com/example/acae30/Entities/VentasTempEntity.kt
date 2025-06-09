package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "ventasTemp")
data class VentasTempEntity(
    @PrimaryKey
    val Id: Int,

    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_TIMESTAMP") val Fecha: String, // Se asume CURRENT_TIMESTAMP, se puede mapear con TypeConverter

    val Id_cliente: Int,
    val Id_Sucursal: Int,
    val Id_vendedor: Int,

    val Vendedor: String,

    val Total: Double,

    val Numero: Int
)
