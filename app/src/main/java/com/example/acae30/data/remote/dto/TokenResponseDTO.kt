package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * REFACTORIZACIÓN MVVM: DTO para la respuesta de búsqueda de precio autorizado.
 */
data class TokenResponseDTO(
    @SerializedName("precio_asig")
    val precioAsignado: Float? = 0f,
    
    @SerializedName("respuesta")
    val respuesta: String? = ""
)
