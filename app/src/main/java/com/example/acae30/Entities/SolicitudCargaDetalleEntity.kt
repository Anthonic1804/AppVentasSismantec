package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudCargaDetalle")
data class SolicitudCargaDetalleEntity(
    @PrimaryKey(autoGenerate = true)
    val Id: Int = 0,

    val Id_solicitud_carga: Int,
    val Id_producto: Int,
    val Codigo_Producto: String,
    val Descripcion: String,
    val Cantidad: Long,
    val Costo: Double,
    val Costo_iva: Double,
    val Precio_u: Double,
    val Precio_u_iva: Double,
    val Total: Double
)
