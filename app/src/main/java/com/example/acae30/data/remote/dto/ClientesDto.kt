package com.example.acae30.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClientesDto (

    @SerializedName("id") val id: Int,

    @SerializedName("codigo") val codigo: String,

    @SerializedName("cliente") val cliente: String?,

    @SerializedName("dui") val dui: String?,

    @SerializedName("nit") val nit: String?,

    @SerializedName("nrc") val nrc: String?,

    @SerializedName("giro") val giro: String?,

    @SerializedName("categoria_cliente") val categoriaCliente: String?,

    @SerializedName("terminos_cliente") val terminosCliente: String?,

    @SerializedName("plazo_credito") val plazoCredito: Int?,

    @SerializedName("limite_credito") val limiteCredito: Double?,

    @SerializedName("balance") val balance: Double?,

    @SerializedName("estado_credito") val estadoCredito: String?,

    @SerializedName("direccion") val direccion: String?,

    @SerializedName("municipio") val municipio: String?,

    @SerializedName("departamento") val departamento: String?,

    @SerializedName("telefono1") val telefono1: String?,

    @SerializedName("telefono2") val telefono2: String?,

    @SerializedName("correo") val correo: String?,

    @SerializedName("contacto") val contacto: String?,

    @SerializedName("id_ruta") val idRuta: Int,

    @SerializedName("id_vendedor") val idVendedor: Int,

    @SerializedName("vendedor") val vendedor: String?,

    @SerializedName("status") val status: String?,

    @SerializedName("fecha_ult_venta") val ultimaVenta: String?,

    @SerializedName("aporte_mensual") val aporteMensual: Double?,

    @SerializedName("pagare_Firmado_app") val firmarPagareApp: Boolean,

    @SerializedName("persona_juridica") val personaJuridica: String,

    @SerializedName("dteGiro") val dteGiro: String,

    @SerializedName("ruta") val ruta: String,

    @SerializedName("dteDireccion") val dteDireccion: String,

    @SerializedName("dteCodDepto") val dteCodDepto: String,

    @SerializedName("dteCodMunicipio") val dteCodMunicipio: String,

    @SerializedName("dteCodPais") val dteCodPais: String,

    @SerializedName("dtePais") val dtePais: String,

    @SerializedName("dteCorreo") val dteCorreo: String,

    @SerializedName("dteTelefono") val dteTelefono: String,

    @SerializedName("latitud_app") val latitudApp: String?,

    @SerializedName("longitud_app") val longitudApp: String?,

    @SerializedName("nombre_comercial") val nombreComercial: String,

    @SerializedName("mayorista") val mayorista: String,

    @SerializedName("dteCodGiro") val dteCodGiro: String,

    @SerializedName("dteDistrito") val dteDistrito: String,

    @SerializedName("dteCodDistrito") val dteCodDistrito: String
)