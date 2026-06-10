package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solicitudCargaDetalle")
data class SolicitudCargaDetalleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Id_solicitud_carga", defaultValue = "0")
    val idSolicitudCarga: Int,

    @ColumnInfo(name = "Id_producto", defaultValue = "0")
    val idProducto: Int,

    @ColumnInfo(name = "Codigo_Producto", defaultValue = "")
    val codigoProducto: String,

    @ColumnInfo(name = "Descripcion", defaultValue = "")
    val descripcion: String,

    @ColumnInfo(name = "Cantidad", defaultValue = "0")
    val cantidad: Double,

    @ColumnInfo(name = "Costo", defaultValue = "0.0")
    val costo: Double,

    @ColumnInfo(name = "Costo_iva", defaultValue = "0.0")
    val costoIva: Double,

    @ColumnInfo(name = "Precio_u", defaultValue = "0.0")
    val precioU: Double,

    @ColumnInfo(name = "Precio_u_iva", defaultValue = "0.0")
    val precioUiva: Double,

    @ColumnInfo(name = "Total", defaultValue = "0.0")
    val total: Double,

    @ColumnInfo(name = "Enviado", defaultValue = "0")
    val enviado: Int
)