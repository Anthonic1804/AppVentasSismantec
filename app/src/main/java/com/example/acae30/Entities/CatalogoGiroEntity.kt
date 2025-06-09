package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_giro")
data class CatalogoGiroEntity(
    @PrimaryKey val Id : Int,
    val Codigo : String,
    val Valor : String
)