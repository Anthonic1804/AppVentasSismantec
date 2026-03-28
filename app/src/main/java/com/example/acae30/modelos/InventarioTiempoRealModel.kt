package com.example.acae30.modelos

import com.google.gson.annotations.SerializedName

data class InventarioTiempoRealModel(
    @SerializedName("id") val id : Int,
    @SerializedName("codigo") val codigo : String,
    @SerializedName("descripcion") val descripcion : String,
    @SerializedName("unidad_medida") val unidadMedida : String,
    @SerializedName("nombre_fraccion") val nombreFraccion : String,
    @SerializedName("existencia") val existencia : Float,
    @SerializedName("existencia_u") val exitenciaFraccion : Float,
    @SerializedName("precio_u_iva") val precioUiva: Float
)