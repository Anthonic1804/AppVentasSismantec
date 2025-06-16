package com.example.acae30.modelos

data class Inventario (
    var Id:Int?, // 0
    var Codigo:String?, // 1
    var Tipo:String?, //3
    var descripcion:String?, // 4
    var Unidad_medida:String, // 5
    var Fraccion:Float?, // 6
    var Nombre_fraccion:String?, // 7
    var Existencia:Int?, // 12
    var Costo:Float?, // 8
    var costo_iva:Float?, // 9
    var Precio_iva:Float?, // 17
    var Precio:Float?, // 14
    var Precio_u:Float?, // 15
    var Precio_u_iva:Float?, // 16
    var Fecha_inventario:String?, // 27
    var Bonificado: Float?, // 18
    var Existencia_u:Float, // 13
    var codigo_de_barra: String // 2
)