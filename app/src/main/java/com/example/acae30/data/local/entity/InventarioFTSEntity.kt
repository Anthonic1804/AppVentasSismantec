package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

//@Fts4(contentEntity = InventarioEntity::class)
@Fts4
@Entity(tableName = "InventarioFTS")
data class InventarioFTSEntity(

    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "codigo")
    val codigo: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String

)