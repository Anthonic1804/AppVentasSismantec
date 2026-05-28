package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventasTemp")
data class VentasTempEntity(
    @PrimaryKey
    @ColumnInfo(name = "Id", defaultValue = "0")
    val id: Int,

    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_TIMESTAMP")
    val fecha: String,

    @ColumnInfo(name = "Id_cliente", defaultValue = "0")
    val idCliente: Int,

    @ColumnInfo(name = "Id_Sucursal", defaultValue = "0")
    val idSucursal: Int,

    @ColumnInfo(name = "Id_vendedor", defaultValue = "0")
    val idVendedor: Int,

    @ColumnInfo(name = "Vendedor", defaultValue = "")
    val vendedor: String,

    @ColumnInfo(name = "Total", defaultValue = "0")
    val total: Double,

    @ColumnInfo(name = "Numero", defaultValue = "0")
    val numero: Int
)