package com.example.acae30.modelos

data class SucursalModel (
    val Id : Int,
    val id_cliente : Int,
    val codigo_sucursal : String,
    val nombre_sucursal : String,
    val direccion : String,
    val municipio : String,
    val departamento: String,
    val telefono1 : String,
    val telefono2 : String,
    val correo : String,
    val contacto : String,
    val id_ruta : Int,
    val ruta : String,
    val DTECodDepto : String,
    val DTECodMunicipio : String,
    val DTECodPais : String,
    val DTEDireccion : String,
    val DTEPais : String,
    val DTETelefono : String,
    val DTECorreo : String,
    val DTECodDistrito : String,
    val DTEDistrito : String
)