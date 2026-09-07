package com.example.acae30.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.acae30.data.local.entity.VisitasEntity

@Dao
interface VisitasDao {

    // Registrar Check-In (Nueva Visita)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarCheckIn(visita: VisitasEntity): Long

    // Obtener una visita específica por su ID local
    @Query("SELECT * FROM visitas WHERE Id = :id")
    suspend fun obtenerVisitaPorId(id: Int): VisitasEntity?

    // Actualizar datos de la visita (Check-Out, Coordenadas, Estados de envío)
    @Update
    suspend fun actualizarVisita(visita: VisitasEntity): Int

    // Actualizar ID del servidor y marcar como enviado inicial
    @Query("UPDATE visitas SET Idvisita = :idServidor, Enviado = 1 WHERE Id = :idLocal")
    suspend fun confirmarEnvioCheckIn(idLocal: Int, idServidor: Int)

    // Marcar como enviado final (Check-Out)
    @Query("UPDATE visitas SET Enviado_final = 1 WHERE Id = :idLocal")
    suspend fun confirmarEnvioCheckOut(idLocal: Int)

    // Obtener visita activa por cliente
    @Query("SELECT * FROM visitas WHERE Id_cliente = :idCliente AND Abierta = 1 ORDER BY Id DESC LIMIT 1")
    suspend fun obtenerVisitaPorIdCliente(idCliente: Int): VisitasEntity?
}
