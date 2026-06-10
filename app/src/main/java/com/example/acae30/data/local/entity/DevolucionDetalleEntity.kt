package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devolucion_detalle")
data class DevolucionDetalleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Id_dev", defaultValue = "0")
    val idDev: Int,

    @ColumnInfo(name = "Numero_dev", defaultValue = "0")
    val numeroDev: Int,

    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_TIMESTAMP")
    val fecha: String,

    @ColumnInfo(name = "Id_producto", defaultValue = "0")
    val idProducto: Int,

    @ColumnInfo(name = "Codigo_producto", defaultValue = "")
    val codigoProducto: String,

    @ColumnInfo(name = "Producto", defaultValue = "")
    val producto: String,

    @ColumnInfo(name = "Fraccion", defaultValue = "")
    val fraccion: String,

    @ColumnInfo(name = "Cantidad", defaultValue = "0")
    val cantidad: Double,

    @ColumnInfo(name = "Bueno", defaultValue = "0.0")
    val bueno: Double,

    @ColumnInfo(name = "Averia", defaultValue = "0.0")
    val averia: Double,

    @ColumnInfo(name = "Tipo_fiscal", defaultValue = "")
    val tipoFiscal: String

)