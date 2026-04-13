package com.example.acae30.modelos.cierreParcial

import com.google.gson.annotations.SerializedName

data class FormaPago(

    @SerializedName("formaPago") val formaPago: String,
    @SerializedName("totalFormaPago") val total: Double

)