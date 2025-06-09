package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_distrito")
data class CatalogoDistritoEntity(
    @PrimaryKey val Id : Int,
    val Codigo : String,
    val Valor : String,
    val Departamento : String,
    val Id_Departamento : Int,
    val Municipio : String,
    val Id_Municipio : Int
)