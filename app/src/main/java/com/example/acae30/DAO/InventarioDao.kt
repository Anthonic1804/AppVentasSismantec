package com.example.acae30.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.acae30.Entities.InventarioEntity
import com.example.acae30.Entities.InventarioLotesEntity
import com.example.acae30.Entities.InventarioPreciosEntity
import com.example.acae30.Entities.InventarioUnidadesEntity

@Dao
interface InventarioDao {

    //Insertando Inventario
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(items: List<InventarioEntity>)

    //Insertando Escalas de Precios
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEscalas(items : List<InventarioPreciosEntity>)

    //Insertando Lotes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLotes(items: List<InventarioLotesEntity>)

    //Insertando Unidades
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUnidades(items: List<InventarioUnidadesEntity>)

}