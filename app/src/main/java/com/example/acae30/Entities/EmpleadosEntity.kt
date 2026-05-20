package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empleado")
data class EmpleadosEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_empleado")
    val idEmpleado : Int = 0,

    @ColumnInfo(name = "nombre_empleado")
    val nombreEmpleado : String = ""
)