package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventasDetalleTemp")
data class VentasDetalleTempEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Id_venta", defaultValue = "0")
    val idVenta: Int,

    @ColumnInfo(name = "Id_producto", defaultValue = "0")
    val idProducto: Int,

    @ColumnInfo(name = "Producto", defaultValue = "")
    val producto: String,

    @ColumnInfo(name = "Precio_u_iva", defaultValue = "0")
    val precioIva: Double,

    @ColumnInfo(name = "Cantidad", defaultValue = "0")
    val cantidad: Int,

    @ColumnInfo(name = "Total_iva", defaultValue = "0")
    val totalIva: Double
)