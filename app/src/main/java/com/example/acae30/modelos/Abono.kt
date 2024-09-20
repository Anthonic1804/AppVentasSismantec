package com.example.acae30.modelos

data class Abono (
    var Fecha : String?,
    var IdCliente : Int?,
    var codigoCliente : String?,
    var Cliente : String?,
    var IdSucursal : Int?,
    var Sucursal : String?,
    var Abono : Float?,
    var Tipo_pago : String?,
    var Numero_cheque: String?,
    var Cuenta : String?,
    var Banco : String?,
    var IdVendedor : Int?,
    var Vendedor : String?,
    var Fecha_hora_proceso : String?,
    var Id_app_visita : Int?,
    var PedidoEnviado : Int?
)