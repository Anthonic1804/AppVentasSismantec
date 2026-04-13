package com.example.acae30.modelos.cierreParcial

import com.google.gson.annotations.SerializedName

data class Terminos(
    @SerializedName("terminos") val termino: String,
    @SerializedName("totalTerminos") val total: Double
)