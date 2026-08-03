package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReportePedidoDTO(
    @SerializedName("cliente")
    val cliente: String?,
    
    @SerializedName("sucursal")
    val sucursal: String?,
    
    @SerializedName("total")
    val total: Double?
)
