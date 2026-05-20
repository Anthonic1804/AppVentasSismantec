package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("pedidos")
data class PedidosEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int = 0,

    @ColumnInfo(name = "Id_cliente")
    val idCliente: Int = 0,

    @ColumnInfo(name = "Nombre_cliente")
    val nombreCliente: String,

    @ColumnInfo(name = "Pago")
    val pago: Double = 0.0,

    @ColumnInfo(name = "Cambio")
    val cambio: Double = 0.0,

    @ColumnInfo(name = "Descuento")
    val descuento: Double = 0.0,

    @ColumnInfo(name = "Sumas")
    val sumas: Double = 0.0,

    @ColumnInfo(name = "Iva")
    val iva: Double = 0.0,

    @ColumnInfo(name = "SubTotal")
    val subTotal: Double = 0.0,

    @ColumnInfo(name = "Iva_retenido")
    val ivaRetenido: Double = 0.0,

    @ColumnInfo(name = "Iva_percibido")
    val ivaPercibido: Double = 0.0,

    @ColumnInfo(name = "Total")
    val total: Double,

    @ColumnInfo(name = "Enviado")
    val enviado: Boolean = false,

    @ColumnInfo(name = "Fecha_enviado")
    val fechaEnviado: String = "",

    @ColumnInfo(name = "Id_pedido_sistema")
    val idPedidoSistema: Int = 0,

    @ColumnInfo(name = "Gps")
    val gps: String? = null,

    @ColumnInfo(name = "Cerrado")
    val cerrado: Int = 0,

    @ColumnInfo(name = "Idvisita")
    val idVisita: Int = 0,

    @ColumnInfo(name = "Fecha_creado")
    val fechaCreado: String = "",

    @ColumnInfo(name = "Id_sucursal")
    val idSucursal: Int = 0,

    @ColumnInfo(name = "Codigo_sucursal")
    val codigoSucursal: String = "",

    @ColumnInfo(name = "Nombre_sucursal")
    val nombreSucursal: String = "",

    @ColumnInfo(name = "Tipo_documento")
    val tipoDocumento: String = "FC",

    @ColumnInfo(name = "Tipo_envio")
    val tipoEnvio: Int = 0,

    @ColumnInfo(name = "Terminos")
    val terminos: String,

    @ColumnInfo(name = "pagoEfectivo")
    val pagoEfectivo: Double = 0.0,

    @ColumnInfo(name = "pagoCheque")
    val pagoCheque: Double = 0.0,

    @ColumnInfo(name = "pagoTarjeta")
    val pagoTarjeta: Double = 0.0,

    @ColumnInfo(name = "pagoDeposito")
    val pagoDeposito: Double = 0.0,

    @ColumnInfo(name = "bancoCheque")
    val bancoCheque: String = "",

    @ColumnInfo(name = "numCuentaCheque")
    val numCuentaCheque: String = "",

    @ColumnInfo(name = "numCheque")
    val numCheque: String = "",

    @ColumnInfo(name = "bancoTarjeta")
    val bancoTarjeta: String = "",

    @ColumnInfo(name = "nombreTarjeta")
    val nombreTarjeta: String = "",

    @ColumnInfo(name = "numTarjeta")
    val numTarjeta: String = "",

    @ColumnInfo(name = "bancoDeposito")
    val bancoDeposito: String = "",

    @ColumnInfo(name = "numCuentaDeposito")
    val numCuentaDeposito: String = "",

    @ColumnInfo(name = "numDeposito")
    val numDeposito: String = "",

    @ColumnInfo(name = "formaPago")
    val formaPago: String = "",

    @ColumnInfo(name = "numero_orden")
    val numeroOrden: String = "0",

    @ColumnInfo(name = "pedido_dte")
    val pedidoDte: Int = 0,

    @ColumnInfo(name = "pedido_dte_error")
    val pedidoDteError: Int = 0,

    @ColumnInfo(name = "dteAmbiente")
    val dteAmbiente: String = "",

    @ColumnInfo(name = "dteCodigoGeneracion")
    val dteCodigoGeneracion: String = "",

    @ColumnInfo(name = "dteSelloRecibido")
    val dteSelloRecibido: String = "",

    @ColumnInfo(name = "dteNumeroControl")
    val dteNumeroControl: String = "",

    @ColumnInfo(name = "idDocTransmitido")
    val idDocTransmitido: Int = 0,

    @ColumnInfo(name = "Id_ruta")
    val idRuta: Int = 0,

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

    @ColumnInfo(name = "Fecha")
    val fecha: String = "",

    @ColumnInfo(name = "Id_pedido_app")
    val idPedidoApp: String? = null
    )