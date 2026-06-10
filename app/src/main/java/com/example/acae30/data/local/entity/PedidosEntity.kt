package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("pedidos")
data class PedidosEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Id_cliente", defaultValue = "0")
    val idCliente: Int,

    @ColumnInfo(name = "Nombre_cliente", defaultValue = "")
    val nombreCliente: String,

    @ColumnInfo(name = "Pago", defaultValue = "0.0")
    val pago: Double,

    @ColumnInfo(name = "Cambio", defaultValue = "0.0")
    val cambio: Double,

    @ColumnInfo(name = "Descuento", defaultValue = "0.0")
    val descuento: Double,

    @ColumnInfo(name = "Sumas", defaultValue = "0.0")
    val sumas: Double,

    @ColumnInfo(name = "Iva", defaultValue = "0.0")
    val iva: Double,

    @ColumnInfo(name = "SubTotal", defaultValue = "0.0")
    val subTotal: Double,

    @ColumnInfo(name = "Iva_retenido", defaultValue = "0.0")
    val ivaRetenido: Double,

    @ColumnInfo(name = "Iva_percibido", defaultValue = "0.0")
    val ivaPercibido: Double,

    @ColumnInfo(name = "Total", defaultValue = "0.0")
    val total: Double,

    @ColumnInfo(name = "Enviado", defaultValue = "false")
    val enviado: Boolean,

    @ColumnInfo(name = "Fecha_enviado", defaultValue = "CURRENT_TIMESTAMP")
    val fechaEnviado: String,

    @ColumnInfo(name = "Id_pedido_sistema", defaultValue = "0")
    val idPedidoSistema: Int,

    @ColumnInfo(name = "Gps")
    val gps: String? = null,

    @ColumnInfo(name = "Cerrado", defaultValue = "0")
    val cerrado: Int,

    @ColumnInfo(name = "Idvisita", defaultValue = "0")
    val idVisita: Int,

    @ColumnInfo(name = "Fecha_creado", defaultValue = "CURRENT_TIMESTAMP")
    val fechaCreado: String,

    @ColumnInfo(name = "Id_sucursal", defaultValue = "0")
    val idSucursal: Int,

    @ColumnInfo(name = "Codigo_sucursal", defaultValue = "")
    val codigoSucursal: String,

    @ColumnInfo(name = "Nombre_sucursal", defaultValue = "")
    val nombreSucursal: String,

    @ColumnInfo(name = "Tipo_documento", defaultValue = "FC")
    val tipoDocumento: String,

    @ColumnInfo(name = "Tipo_envio", defaultValue = "0")
    val tipoEnvio: Int = 0,

    @ColumnInfo(name = "Terminos")
    val terminos: String,

    @ColumnInfo(name = "pagoEfectivo", defaultValue = "0.0")
    val pagoEfectivo: Double,

    @ColumnInfo(name = "pagoCheque", defaultValue = "0.0")
    val pagoCheque: Double,

    @ColumnInfo(name = "pagoTarjeta", defaultValue = "0.0")
    val pagoTarjeta: Double,

    @ColumnInfo(name = "pagoDeposito", defaultValue = "0.0")
    val pagoDeposito: Double,

    @ColumnInfo(name = "bancoCheque", defaultValue = "")
    val bancoCheque: String,

    @ColumnInfo(name = "numCuentaCheque", defaultValue = "")
    val numCuentaCheque: String,

    @ColumnInfo(name = "numCheque", defaultValue = "")
    val numCheque: String,

    @ColumnInfo(name = "bancoTarjeta", defaultValue = "")
    val bancoTarjeta: String,

    @ColumnInfo(name = "nombreTarjeta", defaultValue = "")
    val nombreTarjeta: String,

    @ColumnInfo(name = "numTarjeta", defaultValue = "")
    val numTarjeta: String,

    @ColumnInfo(name = "bancoDeposito", defaultValue = "")
    val bancoDeposito: String,

    @ColumnInfo(name = "numCuentaDeposito", defaultValue = "")
    val numCuentaDeposito: String,

    @ColumnInfo(name = "numDeposito", defaultValue = "")
    val numDeposito: String,

    @ColumnInfo(name = "formaPago", defaultValue = "")
    val formaPago: String,

    @ColumnInfo(name = "numero_orden", defaultValue = "0")
    val numeroOrden: String,

    @ColumnInfo(name = "pedido_dte", defaultValue = "0")
    val pedidoDte: Int,

    @ColumnInfo(name = "pedido_dte_error", defaultValue = "0")
    val pedidoDteError: Int,

    @ColumnInfo(name = "dteAmbiente", defaultValue = "")
    val dteAmbiente: String,

    @ColumnInfo(name = "dteCodigoGeneracion", defaultValue = "")
    val dteCodigoGeneracion: String,

    @ColumnInfo(name = "dteSelloRecibido", defaultValue = "")
    val dteSelloRecibido: String,

    @ColumnInfo(name = "dteNumeroControl", defaultValue = "")
    val dteNumeroControl: String,

    @ColumnInfo(name = "idDocTransmitido", defaultValue = "0")
    val idDocTransmitido: Int,

    @ColumnInfo(name = "Id_ruta", defaultValue = "0")
    val idRuta: Int,

    @ColumnInfo(name = "Ruta", defaultValue = "")
    val ruta: String,

    @ColumnInfo(name = "DTEDireccion", defaultValue = "")
    val dteDireccion: String,

    @ColumnInfo(name = "DTECodDepto", defaultValue = "")
    val dteCodDepto: String,

    @ColumnInfo(name = "DTECodMunicipio", defaultValue = "")
    val dteCodMunicipio: String,

    @ColumnInfo(name = "DTECodPais", defaultValue = "")
    val dteCodPais: String,

    @ColumnInfo(name = "DTEPais", defaultValue = "")
    val dtePais: String,

    @ColumnInfo(name = "DTECorreo", defaultValue = "")
    val dteCorreo: String,

    @ColumnInfo(name = "DTETelefono", defaultValue = "")
    val dteTelefono: String,

    @ColumnInfo(name = "Fecha", defaultValue = "")
    val fecha: String,

    @ColumnInfo(name = "Id_pedido_app")
    val idPedidoApp: String? = null
    )