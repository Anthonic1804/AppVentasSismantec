package com.example.acae30.modelos.reporteUnidadesVendidas

import com.google.gson.annotations.SerializedName

data class UnidadesVendidasPorProducto(
    @SerializedName("idProducto") val idProducto: Int,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("cantidadVendida") val cantidad : Float
)