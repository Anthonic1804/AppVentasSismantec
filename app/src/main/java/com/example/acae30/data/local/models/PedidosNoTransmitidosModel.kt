package com.example.acae30.data.local.models

import androidx.room.ColumnInfo

data class PedidosNoTransmitidosModel (
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Nombre_cliente")
    val nombreCliente: String
)