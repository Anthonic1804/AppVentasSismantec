package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventario_solicitud_carga")
data class InventarioSolicitudCargaEntity(
    @PrimaryKey
    val Id: Int,

    val Codigo: String? = null,
    val Tipo: String? = null,
    val Id_linea: Int? = null,
    val Linea: String? = null,
    val Descripcion: String? = null, // Corrigido "VaRCHAR" a String?
    val Unidad_medida: String? = null,
    val Fraccion: Double? = null,
    val Nombre_fraccion: String? = null,
    val Existencia: Double? = null,

    val Costo: Double,
    val costo_iva: Double,

    val Precio_oferta: Double? = null,
    val Precio_iva: Double? = null,
    val Precio: Double? = null,
    val Precio_u: Double? = null,
    val Precio_u_iva: Double? = null,

    val Status: String? = null,

    @ColumnInfo(name = "Fecha_final", defaultValue = "CURRENT_TIMESTAMP") val Fecha_inventario: String, // Usa TypeConverter para Date <-> Long

    val Id_productor: Int? = null,
    val Productor: String? = null,
    val Id_proveedor: Int? = 0,
    val Proveedor: String? = null,

    val Cesc: String,
    val Combustible: String,

    val Imagen: String? = null,
    val Rubro: String? = null,
    val Marca: String? = null,
    val Id_sublinea: Int? = null,
    val Sublinea: String? = null,
    val Desc_automatico: Double? = null,
    val Bonificado: Double? = null,
    val Id_rubro: Int? = null,
    val Existencia_u: Double? = null,

    val codigo_de_barra: String = ""
)
