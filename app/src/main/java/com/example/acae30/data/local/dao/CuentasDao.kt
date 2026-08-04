package com.example.acae30.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.acae30.data.local.entity.ClientesEntity
import com.example.acae30.data.local.entity.CuentasEntity

@Dao
interface CuentasDao {

    //-------------------------------------------------------------
    // Obtiene los clientes que tienen facturas con estado 'PENDIENTE'.
    //-------------------------------------------------------------
    @Query("""
        SELECT DISTINCT c.* FROM clientes c 
        INNER JOIN cuentas p ON c.id = p.id_cliente 
        WHERE p.status = 'PENDIENTE' 
        GROUP BY c.id 
        LIMIT 50
    """)
    suspend fun obtenerClientesConCuentasPendientes(): List<ClientesEntity>

    //-------------------------------------------------------------
    //Busca clientes por nombre que tengan facturas con estado 'PENDIENTE'.
    //-------------------------------------------------------------
    @Query("""
        SELECT DISTINCT c.* FROM clientes c 
        INNER JOIN cuentas p ON c.id = p.id_cliente 
        WHERE p.status = 'PENDIENTE' AND c.cliente LIKE '%' || :nombre || '%'
        GROUP BY c.id
    """)
    suspend fun buscarClientesConCuentasPendientes(nombre: String): List<ClientesEntity>

    //-------------------------------------------------------------
    //Cuenta la cantidad de facturas pendientes de un cliente.
    //-------------------------------------------------------------
    @Query("SELECT COUNT(*) FROM cuentas WHERE id_cliente = :idCliente AND status = 'PENDIENTE'")
    suspend fun contarCuentasPendientesPorCliente(idCliente: Int): Int

    //-------------------------------------------------------------
    // Obtiene todas las facturas pendientes de un cliente.
    //-------------------------------------------------------------
    @Query("SELECT * FROM cuentas WHERE id_cliente = :idCliente AND status = 'PENDIENTE' ORDER BY fecha DESC")
    suspend fun obtenerCuentasTodas(idCliente: Int): List<CuentasEntity>

    //-------------------------------------------------------------
    //Obtiene las facturas pendientes y vencidas
    //-------------------------------------------------------------
    @Query("SELECT * FROM cuentas WHERE id_cliente = :idCliente AND status = 'PENDIENTE' AND dias_tardios > 0 ORDER BY fecha DESC")
    suspend fun obtenerCuentasVencidas(idCliente: Int): List<CuentasEntity>

    //-------------------------------------------------------------
    //Obtiene las facturas pendientes y vigentes
    //-------------------------------------------------------------
    @Query("SELECT * FROM cuentas WHERE id_cliente = :idCliente AND status = 'PENDIENTE' AND dias_tardios = 0 ORDER BY fecha DESC")
    suspend fun obtenerCuentasVigentes(idCliente: Int): List<CuentasEntity>
}
