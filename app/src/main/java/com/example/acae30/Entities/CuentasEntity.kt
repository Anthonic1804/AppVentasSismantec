package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuentas")
data class CuentasEntity(
    @PrimaryKey
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Id_cliente")
    val idCliente: Int,

    @ColumnInfo(name = "Codigo_cliente")
    val codigoCliente: String?,

    @ColumnInfo("Documento")
    val documento: String,

    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_DATE")
    val fecha: String,

    @ColumnInfo(name = "Valor", defaultValue = "0")
    val valor: Double,

    @ColumnInfo(name = "Abono_inicial", defaultValue = "0")
    val abonoInicial: Double,

    @ColumnInfo(name = "Saldo_inicial", defaultValue = "0")
    val saldoInicial: Double,

    @ColumnInfo(name = "Plazo", defaultValue = "0")
    val plazo: Double,

    @ColumnInfo(name = "Fecha_vencimiento", defaultValue = "CURRENT_DATE")
    val fechaVencimiento: String? = null,

    @ColumnInfo(name = "Saldo_actual", defaultValue = "0")
    val saldoActual: Double,

    @ColumnInfo(name = "Fecha_ult_pago")
    val fechaUltPago: String? = null,

    @ColumnInfo(name = "Valor_pago", defaultValue = "0")
    val valorPago: Double? = null,

    @ColumnInfo(name = "Relacionado")
    val relacionado: String,

    @ColumnInfo(name = "Status")
    val status: String,

    @ColumnInfo(name = "Fecha_cancelado", defaultValue = "CURRENT_DATE")
    val fechaCancelado: String? = null,

    @ColumnInfo(name = "dias_tardios", defaultValue = "0")
    val diasTardios: Double

)