package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuentas")
data class CuentaEntity(
    @PrimaryKey val Id: Int,
    val Id_cliente: Int,
    val Codigo_cliente: String? = null,
    val Documento: String,
    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_TIMESTAMP") val Fecha: String, // Formato ISO (ej. "2025-06-05")
    val Valor: Double? = null,
    val Abono_inicial: Double? = null,
    val Saldo_inicial: Double? = null,
    val Plazo: Int? = null,
    val Fecha_vencimiento: String? = null,
    val Saldo_actual: Double? = null,
    val Fecha_ult_pago: String? = null,
    val Valor_pago: Double? = null,
    val Relacionado: String? = null,
    val Status: String? = null,
    val Fecha_cancelado: String? = null,
    val dias_tardios: Int = 0
)
