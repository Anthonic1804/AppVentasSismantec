package com.example.acae30.data.local.models

import androidx.room.ColumnInfo

data class UnidadMedidaModelo(
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Id_inventario")
    val idInventario: Int,

    @ColumnInfo(name = "Nombre_unidad")
    val nombreUnidad: String?,

    @ColumnInfo(name = "Equivale")
    val equivale: Float,

    @ColumnInfo(name = "Unidades")
    val unidades: String?
)