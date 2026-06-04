package com.example.acae30.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.acae30.Entities.InventarioSolicitudCargaEntity

@Dao
interface InventarioSolicitudDao {

    //Insertando Inventario Solicitud Carga
    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert
    suspend fun insertarInventarioSolicitud(item: List<InventarioSolicitudCargaEntity>)

    //Eliminando Registros de Inventario Solicitud
    @Query("DELETE FROM inventario_solicitud_carga")
    suspend fun eliminarInventarioSolicutd()
}