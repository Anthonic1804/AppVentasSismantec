package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TokenResponseDTO(
    @SerializedName("precio_asig")
    val precioAsignado: Float? = 0f,
    
    @SerializedName("respuesta")
    val respuesta: String? = ""
)
