package com.example.acae30.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.acae30.data.local.entity.PedidoDetalleEntity
import com.example.acae30.data.local.entity.PedidosEntity
import com.example.acae30.data.local.models.PedidosNoTransmitidosModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidosDao {

    //-------------------------------------------------------------
    //Opteniendo listado de pedido no transmitidos del día
    //-------------------------------------------------------------
    @Query("SELECT Id, " +
            "Nombre_cliente " +
            "FROM pedidos " +
            "WHERE Enviado=1 AND pedido_dte=0 AND Tipo_documento != 'RC'")
    fun obtenerListadoPedidosNoTransmitidos() : Flow<List<PedidosNoTransmitidosModel>>

    //-------------------------------------------------------------
    // Obtener todos los pedidos para la lista principal
    //-------------------------------------------------------------
    @Query("""
        SELECT *, strftime('%d/%m/%Y %H:%M', fecha_creado) as Fecha_creado 
        FROM pedidos ORDER BY Id DESC
    """)
    fun obtenerTodosLosPedidosFlow(): Flow<List<PedidosEntity>>

    //--------------------------------------------------------------
    // Confirmando el Envio del Pedido, Actualizadno el IdServidor en ROOM
    //--------------------------------------------------------------
    @Query("""
        UPDATE pedidos
        SET Id_pedido_sistema = :idServidor,
            Enviado = 1,
            Cerrado = 1
        WHERE Id = :idPedido
    """)
    suspend fun actualizarIdServidorConfirmandoPedido(
        idServidor: Int,
        idPedido: Int
    ) : Int

    //--------------------------------------------------------------------
    // Actualizando información del pedido transmitido
    //--------------------------------------------------------------------
    @Query("""
        UPDATE pedidos
        SET pedido_dte = :pedidoDTE, pedido_dte_error = :pedidoDteError,
            dteAmbiente = :dteAmbiente, dteCodigoGeneracion = :dteCodigoGeneracion,
            dteSelloRecibido = :dteSelloRecibido, dteNumeroControl = :dteNumeroControl,
            idDocTransmitido = :idDocTransmitido, Enviado = 1, Cerrado = 1
        WHERE Id = :idPedido

    """)
    suspend fun actualizarInformacionPedido(
        idPedido: Int, pedidoDTE : Int, pedidoDteError:Int,
        dteAmbiente:String, dteCodigoGeneracion:String, dteSelloRecibido:String, dteNumeroControl:String,
        idDocTransmitido: Int
    )

    //-------------------------------------------------------------
    // Obtener lista síncrona para proceso de sincronización
    //-------------------------------------------------------------
    @Query("""
        SELECT * FROM pedidos 
        WHERE (Enviado = 0 OR pedido_dte = 0) 
        AND Tipo_documento != 'RC'
    """)
    suspend fun obtenerListaPedidosNoTransmitidos(): List<PedidosEntity>

    //-------------------------------------------------------------
    // Eliminar detalles de pedidos antiguos
    //-------------------------------------------------------------
    @Query("DELETE FROM detalle_pedidos WHERE Id_pedido IN (SELECT Id FROM pedidos WHERE Fecha != :fechaActual)")
    suspend fun eliminarDetallesAntiguos(fechaActual: String)

    //-------------------------------------------------------------
    // Eliminar detalles de pedidos transmitidos hoy
    //-------------------------------------------------------------
    @Query("""
        DELETE FROM detalle_pedidos 
        WHERE Id_pedido IN (SELECT Id FROM pedidos WHERE Fecha = :fechaActual AND Enviado = 1 AND pedido_dte = 1)
    """)
    suspend fun eliminarDetallesTransmitidosDelDia(fechaActual: String)

    //-------------------------------------------------------------
    // Eliminar pedidos antiguos (no del día actual)
    //-------------------------------------------------------------
    @Query("DELETE FROM pedidos WHERE Fecha != :fechaActual")
    suspend fun eliminarPedidosAntiguos(fechaActual: String)

    //-------------------------------------------------------------
    // Eliminar pedidos del día que ya fueron transmitidos y tienen DTE
    //-------------------------------------------------------------
    @Query("DELETE FROM pedidos WHERE Fecha = :fechaActual AND Enviado = 1 AND pedido_dte = 1")
    suspend fun eliminarPedidosTransmitidosDelDia(fechaActual: String)

    //-------------------------------------------------------------
    // Eliminar detalles de pedidos que ya no existen
    //-------------------------------------------------------------
    @Query("DELETE FROM detalle_pedidos WHERE Id_pedido NOT IN (SELECT Id FROM pedidos)")
    suspend fun limpiarDetallesHuerfanos()

    //-------------------------------------------------------
    // Insertar un nuevo pedido en Room
    //-------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPedido(pedido: PedidosEntity): Long

    //-------------------------------------------------------
    // Gestión de detalle del pedido
    //-------------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDetallePedido(detalle: PedidoDetalleEntity): Long

    @Query("DELETE FROM detalle_pedidos WHERE Id_pedido = :idPedido")
    suspend fun eliminarDetallePorPedido(idPedido: Int)

    @Query("DELETE FROM pedidos WHERE Id = :idPedido")
    suspend fun eliminarPedidoPorId(idPedido: Int)

    @Query("DELETE FROM detalle_pedidos WHERE Id = :idDetalle")
    suspend fun eliminarDetallePedido(idDetalle: Int)

    @Query("SELECT * FROM detalle_pedidos WHERE Id = :idDetalle")
    suspend fun obtenerDetallePedidoPorId(idDetalle: Int): PedidoDetalleEntity?

    @Query("SELECT * FROM detalle_pedidos WHERE Id_pedido = :idPedido AND Id_producto = :idProducto AND Unidad = :unidad")
    suspend fun buscarProductoEnDetalle(idPedido: Int, idProducto: Int, unidad: String): PedidoDetalleEntity?

    @Query("SELECT SUM(Total_iva) FROM detalle_pedidos WHERE Id_pedido = :idPedido")
    suspend fun obtenerSumaTotalPedido(idPedido: Int): Double?

    @Query("SELECT COUNT(*) FROM detalle_pedidos WHERE Id_pedido = :idPedido")
    suspend fun obtenerCantidadItemsPedido(idPedido: Int): Int

    @Query("UPDATE pedidos SET Total = :total WHERE Id = :idPedido")
    suspend fun actualizarTotalCabeceraPedido(idPedido: Int, total: Double)

    //-------------------------------------------------------
    // Actualizar totales fiscales del pedido
    //-------------------------------------------------------
    @Query("""
        UPDATE pedidos 
        SET Sumas = :sumas, Iva = :iva, Iva_percibido = :ivaPerci 
        WHERE Id = :idPedido
    """)
    suspend fun actualizarTotalesFiscales(idPedido: Int, sumas: Double, iva: Double, ivaPerci: Double)

    //-------------------------------------------------------
    // Obtener detalle del pedido de forma reactiva (Flow)
    //-------------------------------------------------------
    @Query("SELECT * FROM detalle_producto WHERE id_pedido = :idPedido ORDER BY OrdenDespacho")
    fun obtenerDetallePedidoFlow(idPedido: Int): kotlinx.coroutines.flow.Flow<List<com.example.acae30.data.local.views.DetalleProductoView>>

    //-------------------------------------------------------
    // Actualizar sucursal del pedido
    //-------------------------------------------------------
    @Query("""
        UPDATE pedidos SET 
            id_sucursal = :idSucursal,
            codigo_sucursal = :codigoSucursal,
            nombre_sucursal = :nombreSucursal,
            Id_ruta = :idRuta,
            Ruta = :ruta,
            DTEDireccion = :dteDireccion,
            DTECodDepto = :dteCodDepto,
            DTECodMunicipio = :dteCodMunicipio,
            DTECodPais = :dteCodPais,
            DTEPais = :dtePais,
            DTECorreo = :dteCorreo,
            DTETelefono = :dteTelefono
        WHERE Id = :idPedido
    """)
    suspend fun actualizarSucursalEnPedido(
        idPedido: Int,
        idSucursal: Int,
        codigoSucursal: String,
        nombreSucursal: String,
        idRuta: Int,
        ruta: String,
        dteDireccion: String,
        dteCodDepto: String,
        dteCodMunicipio: String,
        dteCodPais: String,
        dtePais: String,
        dteCorreo: String,
        dteTelefono: String
    )

    // --- Helper Sync Methods for Sending Order ---

    @Query("SELECT * FROM pedidos WHERE Id = :idPedido")
    suspend fun obtenerPedidoPorIdSync(idPedido: Int): PedidosEntity?

    @Query("SELECT * FROM detalle_producto WHERE id_pedido = :idPedido ORDER BY OrdenDespacho")
    suspend fun obtenerDetallePedidoListSync(idPedido: Int): List<com.example.acae30.data.local.views.DetalleProductoView>

    @Query("""
        SELECT v.Idvisita FROM pedidos p 
        LEFT JOIN visitas v ON p.idvisita = v.id 
        WHERE p.id = :idPedido
    """)
    suspend fun obtenerIdVisitaServidor(idPedido: Int): Int?
}
