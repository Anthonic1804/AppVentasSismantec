package com.example.acae30.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.acae30.Entities.InventarioEntity

@Dao
interface InventarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(items: List<InventarioEntity>)
}