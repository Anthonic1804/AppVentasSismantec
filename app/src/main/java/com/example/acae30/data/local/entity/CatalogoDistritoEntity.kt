package com.example.acae30.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("cat_distrito")
data class CatalogoDistritoEntity (
    @PrimaryKey val id: Int,
    val codigo: String,
    val valor: String,
    val departamento: String,
    val id_departamento: Int,
    val municipio: String,
    val id_municipio: Int
)