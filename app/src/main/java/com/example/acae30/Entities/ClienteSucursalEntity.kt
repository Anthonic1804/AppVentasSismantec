package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cliente_sucursal")
data class ClienteSucursalEntity (
    @PrimaryKey val Id : Int,
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
    @ColumnInfo(name = "id_ruta", defaultValue = "0") val id_ruta : Int,
    val ruta : String,
    val DTECodDepto : String,
    val DTECodMunicipio : String,
    val DTECodPais : String,
    val DTEDireccion : String,
    val DTEPais : String,
    val DTETelefono : String,
    val DTECorreo : String,
    val DTECodDistrito : String,
    val DTEDistrito : String,
    val Latitud_app : String,
    val Longitud_app: String
)