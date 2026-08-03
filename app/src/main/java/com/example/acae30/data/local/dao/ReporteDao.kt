package com.example.acae30.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.acae30.data.local.entity.ReporteTempEntity

@Dao
interface ReporteDao {

    //-----------------------------------------------------------
    // Funcion para obtener todos los registros de Reporte a Generar
    //-----------------------------------------------------------
    @Query("SELECT * FROM reporteTemp")
    suspend fun obtenerTodos(): List<ReporteTempEntity>

    //------------------------------------------------------------
    // Funcion para Insertar los registros obtenidos desde la API
    //------------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLista(reporte: List<ReporteTempEntity>)

    //-------------------------------------------------------------
    // Funcion para Limpiar la tbl reportTemp
    //-------------------------------------------------------------
    @Query("DELETE FROM reporteTemp")
    suspend fun limpiarTabla()
}
