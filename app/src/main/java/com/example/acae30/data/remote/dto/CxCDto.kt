package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CxCDto(
    @SerializedName("id") val id: Int,
    @SerializedName("id_cliente") val idCliente: Int,
    @SerializedName("codigo_cliente") val codigoCliente: String?,
    @SerializedName("documento") val documento: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("valor") val valor: Double,
    @SerializedName("abono_inicial") val abonoInicial: Double,
    @SerializedName("saldo_inicial") val saldoInicial: Double,
    @SerializedName("plazo") val plazo: Double,
    @SerializedName("fecha_vencimiento") val fechaVencimiento: String?,
    @SerializedName("saldo_actual") val saldoActual: Double,
    @SerializedName("fecha_ult_pago") val fechaUltPago: String?,
    @SerializedName("valor_pago") val valorPago: Double?,
    @SerializedName("relacionado") val relacionado: String,
    @SerializedName("status") val status: String,
    @SerializedName("fecha_cancelado") val fechaCancelado: String?,
    @SerializedName("dias_tardios") val diasTardios: Double
)