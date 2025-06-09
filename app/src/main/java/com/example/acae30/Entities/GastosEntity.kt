package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos")
data class GastosEntity(
    @PrimaryKey val Id : Int,
    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_TIMESTAMP") val Fecha : String,
    val Tipo_movimiento : String,
    val Concepto : String,
    val Cuenta_bco : String,
    val Banco : String,
    val numero_cheque : String,
    val Mas_infor : String,
    @ColumnInfo(name = "Valor", defaultValue = "0") val Valor : Float,
    val Forma : String,
    val Persona : String,
    @ColumnInfo(name = "NumeroCaja", defaultValue = "0") val NumeroCaja : Int,
    @ColumnInfo(name = "gastoEnviardo", defaultValue = "0") val gastoEnviado : Int,
    @ColumnInfo(name = "idServidor", defaultValue = "0") val idServidor : Int
)