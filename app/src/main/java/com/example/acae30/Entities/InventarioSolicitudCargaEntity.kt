package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "inventario_solicitud_carga")
data class InventarioSolicitudCargaEntity(
    @PrimaryKey
    @ColumnInfo(name = "id", defaultValue = "0")
    val id: Int,

    @ColumnInfo(name = "codigo", defaultValue = "")
    val codigo: String,

    @ColumnInfo(name = "codigo_de_barra", defaultValue = "")
    val codigoBarra: String,

    @ColumnInfo(name = "tipo", defaultValue = "")
    val tipo: String,

    @ColumnInfo(name = "descripcion", defaultValue = "")
    val descripcion: String,

    @ColumnInfo(name = "unidad_medida", defaultValue = "")
    val unidadMedida: String,

    @ColumnInfo(name = "fraccion", defaultValue = "0.0")
    val fraccion: Float,

    @ColumnInfo(name = "nombre_fraccion", defaultValue = "")
    val nombreFraccion: String,

    @ColumnInfo(name = "costo", defaultValue = "0.0")
    val costo: Float,

    @ColumnInfo(name = "costo_iva", defaultValue = "0.0")
    val costoIva: Float,

    @ColumnInfo(name = "ult_costo", defaultValue = "0.0")
    val ultCosto: Float,

    @ColumnInfo(name = "ult_costo_iva", defaultValue = "0.0")
    val ultCostoIva: Float,

    @ColumnInfo(name = "existencia", defaultValue = "0.0")
    val existencia: Float,

    @ColumnInfo(name = "existencia_u", defaultValue = "0.0")
    val existenciaU: Float,

    @ColumnInfo(name = "precio", defaultValue = "0.0")
    val precio: Float,

    @ColumnInfo(name = "precio_u", defaultValue = "0.0")
    val precioU: Float,

    @ColumnInfo(name = "precio_u_iva", defaultValue = "0.0")
    val precioUiva: Float,

    @ColumnInfo(name = "precio_iva", defaultValue = "0.0")
    val precioIva: Float,

    @ColumnInfo(name = "bonidicado", defaultValue = "0.0")
    val bonificado: Float,

    @ColumnInfo(name = "lote")
    val lote: String? = null,

    @ColumnInfo(name = "fecha_vencimiento")
    val fechaVencimiento: String? = null,

    @ColumnInfo(name = "precio2", defaultValue = "0.0")
    val precio2: Float,

    @ColumnInfo(name = "precio2_iva", defaultValue = "0.0")
    val precio2Iva: Float,

    @ColumnInfo(name = "precio_u2", defaultValue = "0.0")
    val precioU2: Float,

    @ColumnInfo(name = "precio_u2_iva", defaultValue = "0.0")
    val precioU2Iva: Float,

    @ColumnInfo(name = "precio_viñeta", defaultValue = "0.0")
    val precioVineta: Float,

    @ColumnInfo(name = "precio_viñeta_iva", defaultValue = "0.0")
    val precioVinetaIva: Float,

    @ColumnInfo(name = "fechaInventario")
    val fechaInventario: String? = null


)