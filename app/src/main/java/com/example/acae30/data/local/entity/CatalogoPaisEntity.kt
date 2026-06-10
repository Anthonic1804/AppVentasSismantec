package com.example.acae30.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("cat_pais")
data class CatalogoPaisEntity(
    @PrimaryKey val id: Int,
    val codigo: String,
    val valor: String
)