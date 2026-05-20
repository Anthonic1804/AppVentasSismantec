package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cliente_sucursal")
data class ClienteSucursalEntity(
    @PrimaryKey
    @ColumnInfo(name = "Id")
    val id : Int = 0,

    @ColumnInfo(name = "id_cliente")
    val idCliente : Int = 0,

    @ColumnInfo(name = "codigo_sucursal")
    val codigoSucursal : String? = null,

    @ColumnInfo(name = "nombre_sucursal")
    val nombreSucursal : String? = null,

    @ColumnInfo(name = "direccion_sucursal")
    val direccionSucursal : String? = null,

    @ColumnInfo(name = "municipio_sucursal")
    val municipioSucursal : String? = null,

    @ColumnInfo(name = "depto_sucursal")
    val deptoSucursal : String? = null,

    @ColumnInfo(name = "telefono_1")
    val telefono1 : String? = null,

    @ColumnInfo(name = "telefono_2")
    val telefono2 : String? = null,

    @ColumnInfo(name = "correo_sucursal")
    val correoSucursal : String? = null,

    @ColumnInfo(name = "contacto_sucursal")
    val contatoSucursal : String? = null,

    @ColumnInfo(name = "Id_ruta")
    val idRuta : Int = 0,

    @ColumnInfo(name = "Ruta", defaultValue = "")
    val ruta : String = "",

    @ColumnInfo(name = "DTECodDepto", defaultValue = "")
    val dteCodDepto: String = "",

    @ColumnInfo(name = "DTECodMunicipio", defaultValue = "")
    val dteCodMunicipio: String = "",

    @ColumnInfo(name = "DTECodPais", defaultValue = "")
    val dteCodPais: String = "",

    @ColumnInfo(name = "DTEPais", defaultValue = "")
    val dtePais: String = "",

    @ColumnInfo(name = "DTECorreo", defaultValue = "")
    val dteCorreo: String = "",

    @ColumnInfo(name = "Latitud_app")
    val latitudApp : String? = null,

    @ColumnInfo(name = "Longitud_app")
    val longitudApp : String? = null
)