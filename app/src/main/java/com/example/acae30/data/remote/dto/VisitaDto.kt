package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

//--------------------------------------------------------
//DTO para el registro inicial de una visita (Check-in)
//--------------------------------------------------------
data class VisitaRequestDto(
    @SerializedName("Id_app_visita") val idAppVisita: Int,
    @SerializedName("Fecha_hora_checkin") val fechaHoraCheckin: String,
    @SerializedName("Latitud_checkin") val latitudCheckin: String,
    @SerializedName("Longitud_checkin") val longitudCheckin: String,
    @SerializedName("Id_cliente") val idCliente: Int,
    @SerializedName("Cliente") val cliente: String,
    @SerializedName("Id_vendedor") val idVendedor: Int,
    @SerializedName("Fecha_hora_checkout") val fechaHoraCheckout: String,
    @SerializedName("Latitud_checkout") val latitudCheckout: String,
    @SerializedName("Longitud_checkout") val longitudCheckout: String,
    @SerializedName("Comentarios") val comentarios: String
)

//--------------------------------------------------------
//DTO para recibir la respuesta del servidor tras registrar una visita
//--------------------------------------------------------
data class VisitaResponseDto(
    @SerializedName("error") val idServidor: Int, // El WS devuelve el ID en el campo error si es exitoso (> 0)
    @SerializedName("response") val mensaje: String
)

//--------------------------------------------------------
//DTO para finalizar una visita (Check-out)
//--------------------------------------------------------
data class FinalizarVisitaDto(
    @SerializedName("idvisita") val idVisitaServidor: Int,
    @SerializedName("fecha") val fechaFinal: String,
    @SerializedName("latitud") val latitud: String,
    @SerializedName("longitud") val longitud: String,
    @SerializedName("comentarios") val comentarios: String = "",
    @SerializedName("nombreimagen") val nombreImagen: String = "",
    @SerializedName("imagen") val imagen: String = "",
    @SerializedName("Id_vendedor") val idVendedor: Int
)
