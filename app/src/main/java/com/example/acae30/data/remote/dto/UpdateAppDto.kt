package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateAppDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("version")
    val version: String,

    @SerializedName("url")
    val url: String
)