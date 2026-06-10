package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reporteTemp")
data class ReporteTempEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Cliente", defaultValue = "")
    val cliente: String,

    @ColumnInfo("Sucursal", defaultValue = "")
    val sucursal: String,

    @ColumnInfo("Total", defaultValue = "0")
    val total: Double
)