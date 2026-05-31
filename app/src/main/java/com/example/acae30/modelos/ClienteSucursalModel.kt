package com.example.acae30.modelos

import com.google.gson.annotations.SerializedName

data class ClienteSucursalModel(

    @SerializedName("id") val id : Int,

    @SerializedName("id_cliente") val idCliente : Int,

    @SerializedName("codigo_sucursal") val codigoSucursal : String?,

    @SerializedName("nombre_sucursal") val nombreSucursal : String?,

    @SerializedName("dteDireccion") val direccionSucursal : String?,

    @SerializedName("municipio") val municipioSucursal : String?,

    @SerializedName("departamento") val deptoSucursal : String?,

    @SerializedName("dteTelefono") val telefono1 : String?,

    @SerializedName("telefono2") val telefono2 : String?,

    @SerializedName("contacto") val contatoSucursal : String?,

    @SerializedName("id_ruta") val idRuta : Int,

    @SerializedName("ruta") val ruta : String?,

    @SerializedName("dteCodDepto") val dteCodDepto: String?,

    @SerializedName("dteCodMunicipio") val dteCodMunicipio: String?,

    @SerializedName("dteCodPais") val dteCodPais: String?,

    @SerializedName("dtePais") val dtePais: String?,

    @SerializedName("correo") val dteCorreo: String?,

    @SerializedName("latitud_app") val latitudApp : String?,

    @SerializedName("longitud_app") val longitudApp : String?
)