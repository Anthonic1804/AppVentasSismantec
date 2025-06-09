package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventasDetalleTemp")
data class VentasDetalleTempEntity(
    @PrimaryKey val Id : Int,
    val Id_venta: Int,
    val Id_producto: Int,
    val Producto: String,
    val Precio_u_iva: Double,
    val Cantidad: Int,
    val Total_iva: Double
)
