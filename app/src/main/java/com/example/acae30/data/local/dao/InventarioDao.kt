package com.example.acae30.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.acae30.data.local.entity.InventarioEntity
import com.example.acae30.data.local.entity.InventarioLotesEntity
import com.example.acae30.data.local.entity.InventarioPreciosEntity
import com.example.acae30.data.local.entity.InventarioUnidadesEntity

@Dao
interface InventarioDao {

    //Insertando Inventario
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarTodos(items: List<InventarioEntity>)

    //Insertando Escalas de Precios
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarEscalas(items : List<InventarioPreciosEntity>)

    //Insertando Lotes
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarLotes(items: List<InventarioLotesEntity>)

    //Insertando Unidades
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarUnidades(items: List<InventarioUnidadesEntity>)

}