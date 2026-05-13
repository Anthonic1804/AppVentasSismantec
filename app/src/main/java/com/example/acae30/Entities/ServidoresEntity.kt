package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("servidores")
data class ServidoresEntity (
    @PrimaryKey val id: Int,
    val nombre: String,
    val ip: String,
    val puerto: String,
    @ColumnInfo(defaultValue = "0") val ssl: Int
)