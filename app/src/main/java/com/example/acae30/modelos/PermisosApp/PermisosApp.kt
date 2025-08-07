package com.example.acae30.modelos.PermisosApp

data class PermisosApp(

    var pagareObligarotio: Boolean,
    var modificarPrecio: Boolean,
    var pedidoSinExistencia: String,
    var networkProvider: Boolean,
    var usarHojaCarga: Boolean,
    var mostrarPrecioApp: Int,

    //Acceso a Modulos y Permisos
    var mHistorio: Boolean,
    var mHojaCarga: Boolean,
    var mGastos: Boolean,
    var mReportes: Boolean,
    var mCxC: Boolean,
    var mAbonos: Boolean,
    var pMantto_Clientes: Boolean,
    var pImprimirTKVenta: Boolean,
    var solicitudCargaSinExistencia: Boolean,

    //Documento Permitidos
    var docFactura: Boolean,
    var docCreFiscal: Boolean,
    var docRecibo: Boolean,
    var docRemision: Boolean,
    var docFacExportacion: Boolean,

    //Datos de la Empresa para el TK
    var empresa: String,
    var direccion: String,
    var nrc: String,
    var nit: String,
    var giro: String
)