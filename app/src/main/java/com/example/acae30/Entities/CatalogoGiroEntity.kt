package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("cat_giro")
data class CatalogoGiroEntity(
    @PrimaryKey val id: Int,
    val codigo: String,
    val valor: String
)