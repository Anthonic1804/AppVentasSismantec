package com.example.acae30.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.acae30.Entities.ClientePreciosEntity
import com.example.acae30.Entities.ClienteSucursalEntity
import com.example.acae30.Entities.ClientesEntity

@Dao
interface ClientesDao {

    //--------------------------------------------------
    //Insertando Clientes
    //--------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarClientes(item: List<ClientesEntity>)

    //--------------------------------------------------
    //Insertando Sucursales
    //--------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSucursales(item: List<ClienteSucursalEntity>)

    //--------------------------------------------------
    //Insertando Precios Personalizado
    //--------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPreciosPersonalizados(item: List<ClientePreciosEntity>)
}