package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "virtualinventario")
@Fts4(contentEntity = InventarioEntity::class)
data class VirtualInventarioEntity(
    @ColumnInfo(name = "Codigo" )
    val Codigo: String,
    @ColumnInfo(name = "Descripcion" )
    val Descripcion: String
)