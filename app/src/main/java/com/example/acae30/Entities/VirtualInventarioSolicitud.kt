package com.example.acae30.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "virtualinventariosolicitud")
@Fts4(contentEntity = InventarioSolicitudCargaEntity::class)
data class VirtualInventarioSolicitud(
    @ColumnInfo(name = "Codigo" )
    val Codigo: String,
    @ColumnInfo(name = "Descripcion" )
    val Descripcion: String
)