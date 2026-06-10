package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("servidores")
data class ServidoresEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Nombre")
    val nombre: String,

    @ColumnInfo(name = "Ip")
    val ip: String,

    @ColumnInfo(name = "Puerto")
    val puerto: String,

    @ColumnInfo(name = "Ssl", defaultValue = "0")
    val ssl: Int
)