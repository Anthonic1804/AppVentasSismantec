package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devolucion")
data class DevolucionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Numero", defaultValue = "0")
    val numero: Int,

    @ColumnInfo(name = "Fecha", defaultValue = "CURRENT_TIMESTAMP")
    val fecha: String,

    @ColumnInfo(name = "Id_hoja_de_carga", defaultValue = "0")
    val idHojadeCarga: Int,

    @ColumnInfo(name = "Hoja_de_carga", defaultValue = "0")
    val hojadeCarga: Int,

    @ColumnInfo(name = "Id_ruta")
    val idRuta: Int? = null,

    @ColumnInfo(name = "Ruta")
    val ruta: String? = null,

    @ColumnInfo(name = "Id_vendedor", defaultValue = "0")
    val idVendedor: Int,

    @ColumnInfo(name = "Vendedor", defaultValue = "")
    val vendedor: String,

    @ColumnInfo(name = "Estado")
    val estado: String? = null
)