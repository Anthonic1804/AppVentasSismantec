package com.example.acae30.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lineas")
data class LineasEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val mayoreoDetalle: String,
    val ordenDespacho: Int
)