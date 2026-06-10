package com.example.acae30.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.acae30.data.local.entity.ServidoresEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServidoresDao {

    //-----------------------------------------------
    //Seleccionar todos los servidores registrados
    //-----------------------------------------------
    @Query("SELECT Id, Nombre, Ip, Puerto, Ssl FROM servidores")
    fun obtenerListadoServidores(): Flow<List<ServidoresEntity>>

    //-----------------------------------------------
    //Registrando un nuevo Servidor
    //-----------------------------------------------
    @Insert
    suspend fun registrarNuevoServidor(item: ServidoresEntity)

    //-----------------------------------------------
    //Actualizar un Servidor
    //-----------------------------------------------
    @Update
    suspend fun actualizarServidor(item: ServidoresEntity): Int

    //-----------------------------------------------
    //Eliminar un servidor
    //-----------------------------------------------
    @Query("DELETE FROM servidores WHERE Id = :id")
    suspend fun eliminarServidor(id: Int): Int

}