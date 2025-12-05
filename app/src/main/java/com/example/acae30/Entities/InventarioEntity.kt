package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

@Entity(tableName = "inventario")
data class InventarioEntity(
    @PrimaryKey val id: Int,

    val codigo: String,
    val codigo_de_barra: String,
    val tipo: String,
    val descripcion: String,
    val unidad_medida: String,
    val fraccion: Float,
    val nombre_fraccion: String,
    val costo: Float,
    val costo_iva: Float,
    val ult_costo: Float,
    val ult_costo_iva: Float,
    val existencia: Float,
    val existencia_u: Float,
    val precio: Float,
    val precio_u: Float,
    val precio_u_iva: Float,
    val precio_iva: Float,
    val bonificado: Float,
    val lote: String,
    val fecha_vencimiento: String,
    val precio2: Float,
    val precio2_iva: Float,
    val precio_u2: Float,
    val precio_u2_iva: Float,
    val precio_viñeta: Float,
    val precio_viñeta_iva: Float,
    val fecha_inventario: String,
    val validadoHoja: Int,
    val condicion_mercado: String
)
