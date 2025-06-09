package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventario")
data class InventarioEntity (
    val bonificado: Float,
    val codigo: String,
    val costo: Float,
    val costo_iva: Float,
    val descripcion: String,
    val existencia: Float,
    val existencia_u: Float,
    val fraccion: Float,
    @PrimaryKey val id: Int,
    val precio: Float,
    val precio2: Float,
    val precio2_iva: Float,
    val precio_iva: Float,
    val precio_u: Float,
    val precio_u2: Float,
    val precio_u2_iva: Float,
    val precio_u_iva: Float,
    val precio_viñeta: Float,
    val precio_viñeta_iva: Float
)