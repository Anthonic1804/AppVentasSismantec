package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos")
data class GastosEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_TIMESTAMP")
    val fecha: String,

    @ColumnInfo(name = "Tipo_movimiento", defaultValue = "")
    val tipoMovimiento: String,

    @ColumnInfo(name = "Concepto", defaultValue = "")
    val concepto: String,

    @ColumnInfo(name = "Cuenta_bco")
    val cuentaBco: String?,

    @ColumnInfo(name = "Banco")
    val banco: String?,

    @ColumnInfo(name = "numero_cheque")
    val numeroCheque: String?,

    @ColumnInfo(name = "Mas_infor")
    val masInfor: String?,

    @ColumnInfo(name = "Valor", defaultValue = "0")
    val valor: Double,

    @ColumnInfo(name = "Forma", defaultValue = "")
    val forma: String,

    @ColumnInfo(name = "Persona", defaultValue = "")
    val persona: String,

    @ColumnInfo(name = "NumeroCaja", defaultValue = "0")
    val numeroCaja: Int,

    @ColumnInfo(name = "gastoEnviado", defaultValue = "0")
    val gastoEnviado: Int,

    @ColumnInfo(name = "idServidor", defaultValue = "0")
    val idServidor: Int

)