package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "abonos")
data class AbonosEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "fecha" , defaultValue = "CURRENT_TIMESTAMP")
    val fecha: String,

    @ColumnInfo(name = "idCliente")
    val idCliente: Int,

    @ColumnInfo(name = "codigoCliente", defaultValue = "")
    val codigoCliente: String,

    @ColumnInfo(name = "cliente", defaultValue = "")
    val cliente: String,

    @ColumnInfo(name = "idSucursal", defaultValue = "0")
    val idSucursal: Int,

    @ColumnInfo(name = "sucursal")
    val sucursal: String?,

    @ColumnInfo(name = "abono", defaultValue = "0")
    val abono: Double,

    @ColumnInfo(name = "tipoPago", defaultValue = "")
    val tipoPago: String,

    @ColumnInfo(name = "numeroCheque")
    val numeroCheque: String?,

    @ColumnInfo(name = "cuenta")
    val cuenta: String?,

    @ColumnInfo(name = "banco")
    val banco: String?,

    @ColumnInfo(name = "idVendedor", defaultValue = "0")
    val idVendedor: Int,

    @ColumnInfo(name = "vendedor", defaultValue = "")
    val vendedor: String,

    @ColumnInfo(name = "fecha_hora_proceso", defaultValue = "CURRENT_TIMESTAMP")
    val fechaHoraProceso: String,

    @ColumnInfo(name = "idVisitaServer", defaultValue = "0")
    val idVisitaServer: Int,

    @ColumnInfo(name = "idAbonoServer", defaultValue = "0")
    val idAbonoServer: Int,

    @ColumnInfo(name = "borradoLogico", defaultValue = "0")
    val borradoLogico: Int,

    @ColumnInfo(name = "abonoEnviado", defaultValue = "0")
    val abonoEnviado: Int
)