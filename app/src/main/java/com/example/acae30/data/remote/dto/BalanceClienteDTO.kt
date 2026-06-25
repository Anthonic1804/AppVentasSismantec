package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BalanceClienteDTO (
    @SerializedName("id")
    val id: Int,

    @SerializedName("limiteCredito")
    val limiteCredito: Float,

    @SerializedName("balance")
    val balance: Float
)