package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "devolucion_detalle")
data class DevolucionDetalleEntity(
    @PrimaryKey(autoGenerate = true)
    val Id: Int = 0,

    val Id_dev: Int,
    val Numero_dev: Int,
    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_TIMESTAMP") val Fecha: String, // timestamp con valor por defecto actual
    val Id_producto: Int,
    val Codigo_producto: String,
    val Producto: String,
    val Fraccion: String,
    val Cantidad: Int,
    val Bueno: Int,
    val Averia: Int,
    val Tipo_fiscal: String
)
