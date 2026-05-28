package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preciosAutorizados")
data class PreciosAutorizadosEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Id_vendedor", defaultValue = "0")
    val idVendedor: Int,

    @ColumnInfo(name = "Id_admin", defaultValue = "0")
    val idAdmin: Int,

    @ColumnInfo(name = "cod_producto", defaultValue = "")
    val codProducto: String,

    @ColumnInfo(name = "precio_asig", defaultValue = "0")
    val precioAsignado: Double,

    @ColumnInfo(name = "fecha_registrado", defaultValue = "CURRENT_TIMESTAMP")
    val fechaRegistrado: String,

    @ColumnInfo(name = "id_server", defaultValue = "0")
    val idServer: Int
)