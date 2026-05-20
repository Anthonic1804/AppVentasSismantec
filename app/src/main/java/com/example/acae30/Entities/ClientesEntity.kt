package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClientesEntity (
    @PrimaryKey
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Codigo")
    val codigo: String,

    @ColumnInfo(name = "Cliente")
    val cliente: String? = null,

    @ColumnInfo(name = "Dui")
    val dui: String? = null,

    @ColumnInfo(name = "Nit")
    val nit: String? = null,

    @ColumnInfo(name = "Nrc")
    val nrc: String? = null,

    @ColumnInfo(name = "Giro")
    val giro: String? = null,

    @ColumnInfo(name = "Categoria_cliente")
    val categoriaCliente: String? = null,

    @ColumnInfo(name = "Terminos_cliente")
    val terminosCliente: String? = null,

    @ColumnInfo(name = "Plazo_credito")
    val plazoCredito: Int? = null,

    @ColumnInfo(name = "Limite_credito")
    val limiteCredito: Double? = null,

    @ColumnInfo(name = "Balance")
    val balance: Double? = null,

    @ColumnInfo(name = "Estado_credito")
    val estadoCredito: String? = null,

    @ColumnInfo(name = "Direccion")
    val direccion: String? = null,

    @ColumnInfo(name = "Municipio")
    val municipio: String? = null,

    @ColumnInfo(name = "Departamento")
    val departamento: String? = null,

    @ColumnInfo(name = "Telefono_1")
    val telefono1: String? = null,

    @ColumnInfo(name = "Telefono_2")
    val telefono2: String? = null,

    @ColumnInfo(name = "Correo")
    val correo: String? = null,

    @ColumnInfo(name = "Contacto")
    val contacto: String? = null,

    @ColumnInfo(name = "Id_ruta")
    val idRuta: Int = 0,

    @ColumnInfo(name = "Id_vendedor")
    val idVendedor: Int = 0,

    @ColumnInfo(name = "Vendedor")
    val vendedor: String? = null,

    @ColumnInfo(name = "Status")
    val status: String? = null,

    @ColumnInfo(name = "Ultima_venta")
    val ultimaVenta: String? = null,

    @ColumnInfo(name = "Aporte_mensual")
    val aporteMensual: Double? = null,

    @ColumnInfo(name = "Fecha_inventario")
    val fechaInventario: String? = null,

    @ColumnInfo(name = "Firmar_pagare_app")
    val firmarPagareApp: Int = 0,

    @ColumnInfo(name = "Persona_juridica")
    val personaJuridica: String = "N",

    @ColumnInfo(name = "dteGiro")
    val dteGiro: String = "",

    @ColumnInfo(name = "Ruta")
    val ruta: String = "",

    @ColumnInfo(name = "DTEDireccion")
    val dteDireccion: String = "",

    @ColumnInfo(name = "DTECodDepto")
    val dteCodDepto: String = "",

    @ColumnInfo(name = "DTECodMunicipio")
    val dteCodMunicipio: String = "",

    @ColumnInfo(name = "DTECodPais")
    val dteCodPais: String = "",

    @ColumnInfo(name = "DTEPais")
    val dtePais: String = "",

    @ColumnInfo(name = "DTECorreo")
    val dteCorreo: String = "",

    @ColumnInfo(name = "DTETelefono")
    val dteTelefono: String = "",

    @ColumnInfo(name = "Latitud_app")
    val latitudApp: String? = null,

    @ColumnInfo(name = "Longitud_app")
    val longitudApp: String? = null,

    @ColumnInfo(name = "Nombre_comercial")
    val nombreComercial: String = "",

    @ColumnInfo(name = "Mayorista")
    val mayorista: String = "N",

    @ColumnInfo(name = "DTECodGiro")
    val dteCodGiro: String = "",

    @ColumnInfo(name = "DTEDistrito")
    val dteDistrito: String = "",

    @ColumnInfo(name = "DTECodDistrito")
    val dteCodDistrito: String = ""
)