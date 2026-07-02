package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateAppDto(

    @SerializedName("versionApp")
    val version: String?,

    @SerializedName("enlaceDescargaActualizacionApp")
    val enlaceDescarga: String?,

    @SerializedName("obligarEliminarDbInternaApp")
    val eliminarBd: Boolean?
)