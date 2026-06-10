package com.example.acae30.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.acae30.data.local.entity.ClientePreciosEntity
import com.example.acae30.data.local.entity.ClienteSucursalEntity
import com.example.acae30.data.local.entity.ClientesEntity
import com.example.acae30.data.local.entity.CuentasEntity

@Dao
interface ClientesDao {

    //--------------------------------------------------
    //Insertando Clientes
    //--------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)

    suspend fun insertarClientes(item: List<ClientesEntity>)

    //--------------------------------------------------
    //Eliminando Clientes
    //--------------------------------------------------
    @Query("DELETE FROM clientes")
    suspend fun eliminarClientes()

    //--------------------------------------------------
    //Insertando Sucursales
    //--------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertarSucursales(item: List<ClienteSucursalEntity>)

    //--------------------------------------------------
    //Eliminando Sucursales
    //--------------------------------------------------
    @Query("DELETE FROM cliente_sucursal")
    suspend fun eliminarSucursales()

    //--------------------------------------------------
    //Insertando Precios Personalizado
    //--------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertarPreciosPersonalizados(item: List<ClientePreciosEntity>)

    //--------------------------------------------------
    //Eliminando Precios Personalizados
    //--------------------------------------------------
    @Query("DELETE FROM cliente_precios")
    suspend fun eliminarPreciosPersonalizados()

    //--------------------------------------------------
    //Insertando CxC Pendientes
    //--------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertarCuentasPendientes(item: List<CuentasEntity>)

    //----------------------------------------------------
    //Eliminando CxC
    //----------------------------------------------------
    @Query("DELETE FROM cuentas")
    suspend fun eliminarCuentasClientes()
}