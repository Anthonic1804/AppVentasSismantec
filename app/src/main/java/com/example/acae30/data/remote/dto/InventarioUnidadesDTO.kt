package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

data class InventarioUnidadesDTO (
    @SerializedName("id")
    val id: Int,

    @SerializedName("id_inventario")
    val idInventario: Int,

    @SerializedName("nombre_unidad")
    val nombreUnidad: String,

    @SerializedName("equivale")
    val equivale: Float,

    @SerializedName("unidades")
    val unidades: String
)