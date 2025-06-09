package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.acae30.modelos.dataPedidos

@Entity(tableName = "cat_municipio")
data class CatalogoMunicipioEntity(
    @PrimaryKey val Id : Int,
    val Codigo : String,
    val Valor : String,
    val Departamento : String,
    val Id_Departamento : Int,
    val codPais : String
)