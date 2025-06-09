package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventario_unidades")
data class InventarioUnidadesEntity(
    @PrimaryKey val Id : Int,
    val Id_inventario : Int,
    val Nombre_unidad : String,
    val Equivale : Float,
    val Unidades : String
)