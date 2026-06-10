package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClientesPreciosDto(
    @SerializedName("id") val id: Int,
    @SerializedName("id_cliente") val idCliente: Int,
    @SerializedName("id_inventario") val idInventario: Int,
    @SerializedName("precio_p") val precioP: Double,
    @SerializedName("precio_p_iva") val precioPiva: Double,
    @SerializedName("bonificado") val bonificado: Double
)