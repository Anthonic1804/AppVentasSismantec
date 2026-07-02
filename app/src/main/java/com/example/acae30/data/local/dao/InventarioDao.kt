package com.example.acae30.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.acae30.data.local.entity.InventarioEntity
import com.example.acae30.data.local.entity.InventarioFTSEntity
import com.example.acae30.data.local.entity.InventarioLotesEntity
import com.example.acae30.data.local.entity.InventarioPreciosEntity
import com.example.acae30.data.local.entity.InventarioUnidadesEntity

@Dao
interface InventarioDao {

    //Insertando Inventario
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarTodos(items: List<InventarioEntity>)

    //Insertando Inventario FTS
    /*@Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarInventarioFTS(items: List<InventarioFTSEntity>)*/

    //Insertando Escalas de Precios
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarEscalas(items : List<InventarioPreciosEntity>)

    //Insertando Lotes
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarLotes(items: List<InventarioLotesEntity>)

    //Insertando Unidades
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarUnidades(items: List<InventarioUnidadesEntity>)

    //Mostrando el listado de inventario
    @Query("SELECT * FROM Inventario LIMIT 50")
    suspend fun mostrarListadoInventario(): List<InventarioEntity>

    //Busqueda en Inventario Normal
    @Query("""
        SELECT 
            * 
        FROM Inventario 
        WHERE descripcion LIKE :busqueda OR codigo LIKE :busqueda
        LIMIT 50
    """)
    suspend fun busquedaInventarioNormal(busqueda: String) : List<InventarioEntity>

    //Busqueda en Inventario FTS
    /*@Query("""
        SELECT i.*
        FROM inventario i
        INNER JOIN InventarioFTS f
            ON i.id = f.id
        WHERE InventarioFTS MATCH :busqueda
        LIMIT 50
    """)
    suspend fun busquedaInventarioFTS(busqueda: String) : List<InventarioEntity>*/

}