package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reporteTemp")
data class ReporteTempEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,  // Room requiere una clave primaria, y SQLite también funciona mejor con una
    val Cliente: String,
    val Sucursal: String,
    val Total: Double
)
