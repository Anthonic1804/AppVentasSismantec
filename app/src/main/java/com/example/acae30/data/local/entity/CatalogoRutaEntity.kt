package com.example.acae30.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("cat_ruta")
data class CatalogoRutaEntity (
    @PrimaryKey val id: Int,
    val ruta: String
)