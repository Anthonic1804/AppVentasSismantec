package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventario")
data class InventarioEntity(
    @PrimaryKey
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "codigo")
    val codigo: String,

    @ColumnInfo(name = "codigo_de_barra")
    val codigo_de_barra: String,

    @ColumnInfo(name = "tipo")
    val tipo: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String,

    @ColumnInfo(name = "unidad_medida")
    val unidad_medida: String,

    @ColumnInfo(name = "fraccion")
    val fraccion: Float,

    @ColumnInfo(name = "nombre_fraccion")
    val nombre_fraccion: String,

    @ColumnInfo(name = "costo")
    val costo: Float,

    @ColumnInfo(name = "costo_iva")
    val costo_iva: Float,

    @ColumnInfo(name = "ult_costo")
    val ult_costo: Float,

    @ColumnInfo(name = "ult_costo_iva")
    val ult_costo_iva: Float,

    @ColumnInfo(name = "existencia")
    val existencia: Float,

    @ColumnInfo(name = "existencia_u")
    val existencia_u: Float,

    @ColumnInfo(name = "precio")
    val precio: Float,

    @ColumnInfo(name = "precio_u")
    val precio_u: Float,

    @ColumnInfo(name = "precio_u_iva")
    val precio_u_iva: Float,

    @ColumnInfo(name = "precio_iva")
    val precio_iva: Float,

    @ColumnInfo(name = "bonificado")
    val bonificado: Float,

    @ColumnInfo(name = "lote")
    val lote: String,

    @ColumnInfo(name = "fecha_vencimiento")
    val fecha_vencimiento: String,

    @ColumnInfo(name = "precio2")
    val precio2: Float,

    @ColumnInfo(name = "precio2_iva")
    val precio2_iva: Float,

    @ColumnInfo(name = "precio_u2")
    val precio_u2: Float,

    @ColumnInfo(name = "precio_u2_iva")
    val precio_u2_iva: Float,

    @ColumnInfo(name = "precio_viñeta")
    val precio_viñeta: Float,

    @ColumnInfo(name = "precio_viñeta_iva")
    val precio_viñeta_iva: Float,

    @ColumnInfo(name = "fecha_inventario")
    val fecha_inventario: String,

    @ColumnInfo(name = "validadoHoja")
    val validadoHoja: Int,

    @ColumnInfo(name = "condicion_mercado")
    val condicion_mercado: String,

    @ColumnInfo(name = "id_marca")
    val id_marca: Int?,

    @ColumnInfo(name = "marca")
    val marca: String?,

    @ColumnInfo(name = "id_sku")
    val id_sku: Int?,

    @ColumnInfo(name = "Sku")
    val Sku: String?,

    @ColumnInfo(name = "id_rubro")
    val id_rubro: Int?,

    @ColumnInfo(name = "rubro")
    val rubro: String?,

    @ColumnInfo(name = "id_linea")
    val id_linea: Int?,

    @ColumnInfo(name = "linea")
    val linea: String?,

    @ColumnInfo(name = "id_sublinea")
    val id_sublinea: Int?,

    @ColumnInfo(name = "sublinea")
    val sublinea: String?,

    @ColumnInfo(name = "id_productor")
    val id_productor: Int?,

    @ColumnInfo(name = "productor")
    val productor: String?,

    @ColumnInfo(name = "id_proveedor")
    val id_proveedor: Int?,

    @ColumnInfo(name = "proveedor")
    val proveedor: String?,

    @ColumnInfo(name = "metodo_gestion")
    val metodo_gestion: String?,

    @ColumnInfo(name = "tipo_fiscal")
    val tipo_fiscal: String?
)