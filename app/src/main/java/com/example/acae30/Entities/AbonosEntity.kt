package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "abonos")
data class AbonosEntity(
    @PrimaryKey val id : Int,
    @ColumnInfo(name = "fecha", defaultValue = "CURRENT_TIMESTAMP" ) val fecha : Date,
    val idCliente : Int,
    val codigoCliente : String,
    val cliente : String,
    @ColumnInfo(name = "idSucursal", defaultValue = "0") val idSucursal : Int,
    val sucursal : String,
    @ColumnInfo(name = "abono", defaultValue = "0") val abono : Float,
    val tipoPago : String,
    val numeroCheque : String,
    val cuenta : String,
    val banco : String,
    @ColumnInfo(name = "idVendedor", defaultValue = "0") val idVendedor : Int,
    val vendedor : String,
    @ColumnInfo(name = "fecha_hora_proceso", defaultValue = "CURRENT_TIMESTAMP" ) val fecha_hora_proceso : String,
    @ColumnInfo(name = "idVisitaServer", defaultValue = "0") val idVisitaServer : Int,
    @ColumnInfo(name = "idAbonoServer", defaultValue = "0") val idAbonoServer : Int,
    @ColumnInfo(name = "borradoLogico", defaultValue = "0") val borradoLogico : Int,
    @ColumnInfo(name = "abonoEnviado", defaultValue = "0") val abonoEnviado : Int

    )