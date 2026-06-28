package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PedidoTransmitidoDTO(

    @SerializedName("pedido_dte")
    val pedidoDte: Boolean,

    @SerializedName("pedido_dte_error")
    val pedidoDteError: Boolean,

    @SerializedName("dteAmbiente")
    val dteAmbiente: String,

    @SerializedName("dteCodigoGeneracion")
    val dteCodigoGeneracion: String,

    @SerializedName("dteSelloRecibido")
    val dteSelloRecibido: String,

    @SerializedName("dteNumeroControl")
    val dteNumeroControl: String,

    @SerializedName("idDocTransmitido")
    val idDocTransmitido: Int


)