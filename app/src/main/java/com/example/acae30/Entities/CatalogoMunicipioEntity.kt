package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("cat_municipio")
data class CatalogoMunicipioEntity (
    @PrimaryKey val id: Int,
    val codigo: String,
    val valor: String,
    val departamento: String,
    val id_departamento: Int,
    val codPais: Int
)