package com.example.acae30.modelos

data class Inventario (
    var Id:Int?,
    var Codigo:String?,
    var Tipo:String?,
    var descripcion:String?,
    var Unidad_medida:String,
    var Fraccion:Float?,
    var Nombre_fraccion:String?,
    var Existencia:Int?,
    var Costo:Float?,
    var costo_iva:Float?,
    var Precio_iva:Float?,
    var Precio:Float?,
    var Precio_u:Float?,
    var Precio_u_iva:Float?,
    var Fecha_inventario:String?,
    var Bonificado: Float?,
    var Existencia_u:Float,
    var codigo_de_barra: String
)