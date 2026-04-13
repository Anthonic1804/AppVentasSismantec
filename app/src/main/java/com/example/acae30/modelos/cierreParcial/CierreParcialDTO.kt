package com.example.acae30.modelos.cierreParcial

import com.google.gson.annotations.SerializedName

data class CierreParcialDTO(

    @SerializedName("tipo") val tipo: String,
    @SerializedName("correlativoInicial") val correlativoInicial: String,
    @SerializedName("correlativoFinal") val correlativoFinal: String,
    @SerializedName("totalVenta") val tota: Double,
    @SerializedName("terminosDocumentos")val terminos: List<Terminos>,
    @SerializedName("formaPagoDocumentos") val formasPago: List<FormaPago>

)