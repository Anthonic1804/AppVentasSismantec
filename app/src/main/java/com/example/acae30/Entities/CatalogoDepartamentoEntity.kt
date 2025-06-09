package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_departamento")
data class CatalogoDepartamentoEntity(
    @PrimaryKey val Id : Int,
    val Codigo : String,
    val Valor : String
)