package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("cat_ruta")
data class CatalogoRutaEntity (
    @PrimaryKey val id: Int,
    val ruta: String
)