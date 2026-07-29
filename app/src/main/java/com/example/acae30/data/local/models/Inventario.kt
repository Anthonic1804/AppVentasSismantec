package com.example.acae30.data.local.models

import androidx.room.ColumnInfo

data class Inventario (
    @ColumnInfo(name = "Id")
    var Id:Int?, // 0

    @ColumnInfo(name = "codigo")
    var Codigo:String?, // 1

    @ColumnInfo(name = "tipo")
    var Tipo:String?, //3

    @ColumnInfo(name = "descripcion")
    var descripcion:String?, // 4

    @ColumnInfo(name = "unidad_medida")
    var Unidad_medida:String, // 5

    @ColumnInfo(name = "fraccion")
    var Fraccion:Float?, // 6

    @ColumnInfo(name = "nombre_fraccion")
    var Nombre_fraccion:String?, // 7

    @ColumnInfo(name = "existencia")
    var Existencia:Float?, // 12

    @ColumnInfo(name = "costo")
    var Costo:Float?, // 8

    @ColumnInfo(name = "costo_iva")
    var costo_iva:Float?, // 9

    @ColumnInfo(name = "precio_iva")
    var Precio_iva:Float?, // 17

    @ColumnInfo(name = "precio")
    var Precio:Float?, // 14

    @ColumnInfo(name = "precio_u")
    var Precio_u:Float?, // 15

    @ColumnInfo(name = "precio_u_iva")
    var Precio_u_iva:Float?, // 16

    @ColumnInfo(name = "fecha_inventario")
    var Fecha_inventario:String?, // 27

    @ColumnInfo(name = "bonificado")
    var Bonificado: Float?, // 18

    @ColumnInfo(name = "existencia_u")
    var Existencia_u:Float, // 13

    @ColumnInfo(name = "codigo_de_barra")
    var codigo_de_barra: String, // 2

    @ColumnInfo(name = "condicion_mercado")
    var condicionMercado : String, //29

    @ColumnInfo(name = "id_marca")
    var IdMarca: Int?, //30

    @ColumnInfo(name = "id_sku")
    var IdSku: Int?,//32

    @ColumnInfo(name = "id_rubro")
    var IdRubro: Int?,//34

    @ColumnInfo(name = "id_linea")
    var IdLinea: Int?,//36

    @ColumnInfo(name = "id_sublinea")
    var IdSubLinea: Int?,//38

    @ColumnInfo(name = "id_productor")
    var IdProductor: Int?,//40

    @ColumnInfo(name = "id_proveedor")
    var IdProveedor: Int?,//42

    @ColumnInfo(name = "metodo_gestion")
    var MetodoGestion: String?,//44

    @ColumnInfo(name = "tipo_fiscal")
    var TipoFiscal: String?//45
)