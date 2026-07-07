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
    var validarHojaCarga: Boolean,

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
    var giro: String,

    //datos Qr
    var dteUrlQRHacienda: String,
    var dteUrlQRempresa: String,

    //Item por documento
    var numItemFactura : Int,
    var numItemCreFiscal : Int,
    var numItemRecibo : Int,
    var numItemRemision: Int,

    //Modo de configuracion Local o Ruta
    var tipoVentaLocal : Boolean,

    //Modo Desarrollo
    var modoDesarrollo : Boolean,

    //Carga automatica de Catalogos Clientes
    var cargaAutomaticaCatalogos: Boolean,

    //Decimales para sumas y precios
    var decPrecios : Int,
    var decTotales : Int,

    //Usar multiples hojas de carga
    var multiplesHojaDeCarga: Boolean,

    //Uso de bodega de ventas
    var idBodega: Int?,
    var codBodega: String?,
    var bodega: String?,

    //Inventario en tiempo real
    var inventarioTiempoReal : Boolean,
    var eliminarPedidosAutomaticos: Boolean,

    //Tipo de Bonificacion
    /**
     * T -> TODOS (FICHA CLIENTE Y PRODUCTO)
     * BC -> BONICIFACION FICHA CLIENTE
     * BP -> BONIFICACION FICHA PRODUCTO
     * SB -> SIN BONIFICACION
     */
    val tipoBonificacion: String,

    //Habilitando Full Text Search
    val habilitarFTS4: Boolean?
)