package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedidos")
class PedidosEntity(
    @PrimaryKey(autoGenerate = true)
    val Id: Int = 0,

    val Id_cliente: Int = 0,
    val Nombre_cliente: String = "",
    val Pago: Double = 0.0,
    val Cambio: Double = 0.0,
    val Descuento: Double = 0.0,
    val Sumas: Double = 0.0,
    val Iva: Double = 0.0,
    val SubTotal: Double = 0.0,
    val Iva_retenido: Double = 0.0,
    val Iva_percibido: Double = 0.0,
    val Total: Double,

    val Enviado: Boolean = false,
    val Fecha_enviado: String = "",  // Guardar como texto ISO 8601 si usas Date converter

    val Id_pedido_sistema: Int = 0,
    val Gps: String? = null,
    val Cerrado: Int = 0,
    val Idvisita: Int = 0,
    val Fecha_creado: String = "",

    val Id_sucursal: Int = 0,
    val Codigo_sucursal: String = "",
    val Nombre_sucursal: String = "",
    val Tipo_documento: String = "FC",
    val Tipo_envio: Int = 0,
    val Terminos: String = "",

    val pagoEfectivo: Double = 0.0,
    val pagoCheque: Double = 0.0,
    val pagoTarjeta: Double = 0.0,
    val pagoDeposito: Double = 0.0,

    val bancoCheque: String = "",
    val numCuentaCheque: String = "",
    val numCheque: String = "",

    val bancoTarjeta: String = "",
    val nombreTarjeta: String = "",
    val numTarjeta: String = "",

    val bancoDeposito: String = "",
    val numCuentaDeposito: String = "",
    val numDeposito: String = "",

    val formaPago: String = "",
    val numero_orden: String = "0",

    val pedido_dte: Int = 0,
    val pedido_dte_error: Int = 0,
    val dteAmbiente: String = "",
    val dteCodigoGeneracion: String = "",
    val dteSelloRecibido: String = "",
    val dteNumeroControl: String = "",

    val idDocTransmitido: Int = 0,
    val Id_ruta: Int = 0,
    val Ruta: String = "",

    val DTEDireccion: String = "",
    val DTECodDepto: String = "",
    val DTECodMunicipio: String = "",
    val DTECodPais: String = "",
    val DTEPais: String = "",
    val DTECorreo: String = "",
    val DTETelefono: String = ""
)