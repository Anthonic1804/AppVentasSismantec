package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class ClientesEntity (
    @PrimaryKey var  id:Int?,
    var codigo:String,
    var cliente:String,
    var dui:String,
    var nit:String,
    var nrc:String,
    var giro:String,
    var categoria_cliente:String,
    var terminos_cliente:String,
    var plazo_credito:Int,
    var limite_credito:Float,
    var balance:Float,
    var estado_credito:String,
    var direccion:String,
    var municipio:String,
    var departamento:String,
    var telefono1:String,
    var telefono2:String,
    var correo:String,
    var contacto:String,
    @ColumnInfo(name = "id_ruta", defaultValue = "0") var id_ruta:Int,
    @ColumnInfo(name = "id_vendedor", defaultValue = "0") var id_vendedor:Int,
    var vendedor:String,
    var status:String,
    var fecha_ult_venta:String,
    var aporte_mensual:Float,
    @ColumnInfo(name = "pagare_Firmado_app", defaultValue = "0") var pagare_Firmado_app:Int,
    @ColumnInfo(name = "persona_juridica", defaultValue = "N") var persona_juridica:String,
    var dteGiro: String,
    var ruta: String,
    var dteDireccion: String,
    var dteCodDepto: String,
    var dteCodMunicipio: String,
    var dteCodPais: String,
    var dtePais: String,
    var dteCorreo: String,
    var dteTelefono: String,
    var latitud_app : String,
    var longitud_app : String,
    var nombre_comercial : String,
    var mayorista : String,
    var dteCodGiro: String,
    var dteCodDistrito : String,
    var dteDistrito : String
)