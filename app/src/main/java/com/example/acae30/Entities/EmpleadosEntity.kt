package com.example.acae30.Entities

import androidx.room.Entity

@Entity(tableName = "empleado")
data class EmpleadosEntity(
    val id_empleado : Int,
    val nombre_empleado : String
)