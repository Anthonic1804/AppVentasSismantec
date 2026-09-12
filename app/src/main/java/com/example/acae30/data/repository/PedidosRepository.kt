package com.example.acae30.data.repository

import android.content.Context
import com.example.acae30.Funciones
import com.example.acae30.data.local.dao.PedidosDao
import com.example.acae30.data.local.dao.ReporteDao
import com.example.acae30.data.local.entity.PedidoDetalleEntity
import com.example.acae30.data.local.entity.PedidosEntity
import com.example.acae30.data.local.entity.ReporteTempEntity
import com.example.acae30.data.remote.api.pedidos.PedidosApi
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.data.remote.dto.PedidoTransmitidoDTO
import com.example.acae30.data.remote.dto.ReportePedidoDTO
import com.example.acae30.modelos.JSONmodels.BusquedaReporteJSON
import timber.log.Timber
import java.util.UUID

class PedidosRepository(
    private val dao: PedidosDao,
    private val reporteDao: ReporteDao
) {
    private val funciones = Funciones()

    //---------------------------------------------------------
    // CREACIÓN DE PEDIDO
    //---------------------------------------------------------

    suspend fun crearNuevoPedidoLocal(
        idCliente: Int,
        nombreCliente: String,
        terminos: String,
        idRuta: Int,
        ruta: String,
        tipoDocumento: String,
        dteDireccion: String,
        dteCodDepto: String,
        dteCodMunicipio: String,
        dteCodPais: String,
        dtePais: String,
        dteCorreo: String,
        dteTelefono: String,
        idVisitaGlobal: Int = 0,
        gps: String = "0,0"
    ): Int {
        val fechaHora = funciones.getFechaHoraProceso() ?: ""
        val fecha = funciones.obtenerFecha() ?: ""
        val idPedidoApp = UUID.randomUUID().toString()

        val pedido = PedidosEntity(
            id = 0, // Auto-generado por Room
            idCliente = idCliente,
            nombreCliente = nombreCliente,
            pago = 0.0,
            cambio = 0.0,
            descuento = 0.0,
            sumas = 0.0,
            iva = 0.0,
            subTotal = 0.0,
            ivaRetenido = 0.0,
            ivaPercibido = 0.0,
            total = 0.0,
            enviado = false,
            fechaEnviado = "",
            idPedidoSistema = 0,
            gps = gps,
            cerrado = 0,
            idVisita = idVisitaGlobal,
            fechaCreado = fechaHora,
            idSucursal = 0,
            codigoSucursal = "",
            nombreSucursal = "",
            tipoDocumento = tipoDocumento,
            terminos = terminos,
            pagoEfectivo = 0.0,
            pagoCheque = 0.0,
            pagoTarjeta = 0.0,
            pagoDeposito = 0.0,
            bancoCheque = "",
            numCuentaCheque = "",
            numCheque = "",
            bancoTarjeta = "",
            nombreTarjeta = "",
            numTarjeta = "",
            bancoDeposito = "",
            numCuentaDeposito = "",
            numDeposito = "",
            formaPago = "",
            numeroOrden = "0",
            pedidoDte = 0,
            pedidoDteError = 0,
            dteAmbiente = "",
            dteCodigoGeneracion = "",
            dteSelloRecibido = "",
            dteNumeroControl = "",
            idDocTransmitido = 0,
            idRuta = idRuta,
            ruta = ruta,
            dteDireccion = dteDireccion,
            dteCodDepto = dteCodDepto,
            dteCodMunicipio = dteCodMunicipio,
            dteCodPais = dteCodPais,
            dtePais = dtePais,
            dteCorreo = dteCorreo,
            dteTelefono = dteTelefono,
            fecha = fecha,
            idPedidoApp = idPedidoApp
        )

        return dao.insertarPedido(pedido).toInt()
    }

    //---------------------------------------------------------
    // Flujo para la lista de la UI (Todos los pedidos)
    //---------------------------------------------------------
    fun obtenerTodosLosPedidosFlow() = dao.obtenerTodosLosPedidosFlow()

    //---------------------------------------------------------
    // REFACTORIZACIÓN MULTIPLES PEDIDOS: Borradores globales
    //---------------------------------------------------------
    fun obtenerTodosLosPedidosBorradores() = dao.obtenerTodosLosPedidosBorradores()

    //---------------------------------------------------------
    // Lista síncrona para el proceso de sincronización
    //---------------------------------------------------------
    suspend fun obtenerPedidosNoTransmitidosLocal() = dao.obtenerListaPedidosNoTransmitidos()

    //---------------------------------------------------------
    // Consulta remota al servidor vía Retrofit
    //---------------------------------------------------------
    suspend fun obtenerPedidoTransmitidoRemote(idPedidoApp: String, context: Context): PedidoTransmitidoDTO? {
        val preferencias = context.getSharedPreferences("CONFIG_SERVIDOR", Context.MODE_PRIVATE)
        val ip = preferencias.getString("ip", "") ?: ""
        val puerto = preferencias.getInt("puerto", 0).toString()
        val servidor = funciones.getServidor(ip, puerto, context)
        
        val api = RetrofitCliente.obtenerApi<PedidosApi>(servidor, context)
        
        return try {
            val respuesta = api.obtenerPedidoTransmitido(idPedidoApp)
            if (respuesta.isSuccessful) {
                respuesta.body()
            } else {
                Timber.e("Error al obtener pedido remoto: ${respuesta.code()}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error de conexión al obtener pedido remoto")
            null
        }
    }

    //---------------------------------------------------------
    // Actualización en BD local (Room)
    //---------------------------------------------------------
    suspend fun actualizarInformacionPedido(
        idPedido: Int, pedidoDTE: Int, pedidoDteError: Int,
        dteAmbiente: String, dteCodigoGeneracion: String,
        dteSelloRecibido: String, dteNumeroControl: String,
        idDocTransmitido: Int
    ) = dao.actualizarInformacionPedido(
        idPedido, pedidoDTE, pedidoDteError, dteAmbiente,
        dteCodigoGeneracion, dteSelloRecibido, dteNumeroControl, idDocTransmitido
    )

    //---------------------------------------------------------
    // Marcar pedido como enviado y cerrar
    //---------------------------------------------------------
    suspend fun actualizarEstadoPedidoEnviado(idServidor: Int, idPedido: Int) =
        dao.actualizarIdServidorConfirmandoPedido(idServidor, idPedido)

    //---------------------------------------------------------
    // REFACTORIZACIÓN MVVM: ELIMINAR PEDIDO LOCAL
    //---------------------------------------------------------
    suspend fun eliminarPedidoLocal(idPedido: Int) {
        dao.eliminarDetallePorPedido(idPedido)
        dao.eliminarPedidoPorId(idPedido)
    }

    //---------------------------------------------------------
    // Limpieza de pedidos antiguos o ya procesados
    //---------------------------------------------------------
    suspend fun eliminarPedidos(fechaActual: String, eliminarCompletos: Boolean) {
        
        // Eliminamos los detalles
        dao.eliminarDetallesAntiguos(fechaActual)
        
        if (eliminarCompletos) {
            dao.eliminarDetallesTransmitidosDelDia(fechaActual)
        }

        // Eliminanos Pedidos
        dao.eliminarPedidosAntiguos(fechaActual)

        if (eliminarCompletos) {
            dao.eliminarPedidosTransmitidosDelDia(fechaActual)
        }

        // Eliminamos pedidos con error
        dao.limpiarDetallesHuerfanos()
    }

    //---------------------------------------------------------
    // MÉTODOS PARA REPORTE PDF
    //---------------------------------------------------------

    //OBTENER PEDIDOS ENVIADOS
    suspend fun obtenerReporteDiarioRemote(idVendedor: Int, fecha: String, context: Context): List<ReportePedidoDTO>? {
        val preferencias = context.getSharedPreferences("CONFIG_SERVIDOR", Context.MODE_PRIVATE)
        val ip = preferencias.getString("ip", "") ?: ""
        val puerto = preferencias.getInt("puerto", 0).toString()
        val servidor = funciones.getServidor(ip, puerto, context)

        val api = RetrofitCliente.obtenerApi<PedidosApi>(servidor, context)

        return try {
            val busqueda = BusquedaReporteJSON(idVendedor, fecha)
            val respuesta = api.obtenerReporteDiario(busqueda)
            if (respuesta.isSuccessful) {
                respuesta.body()
            } else {
                Timber.e("Error API reporte: ${respuesta.code()}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error de conexión en reporte")
            null
        }
    }

    //---------------------------------------------------------
    //INSERTAR PEDOS EN LA TBL REPORTETMP
    //---------------------------------------------------------
    suspend fun actualizarTablaReporteLocal(datos: List<ReportePedidoDTO>) {
        reporteDao.limpiarTabla()
        val entidades = datos.map { dto ->
            ReporteTempEntity(
                id = 0,
                cliente = dto.cliente ?: "",
                sucursal = dto.sucursal ?: "",
                total = dto.total ?: 0.0
            )
        }
        reporteDao.insertarLista(entidades)
    }

    //---------------------------------------------------------
    //OBTENIENDO LOS PEDIDOS DEL REPORTE
    //---------------------------------------------------------
    suspend fun obtenerDatosReporteLocal() = reporteDao.obtenerTodos()

    //---------------------------------------------------------
    // MÉTODOS DE DETALLE DE PEDIDO
    //---------------------------------------------------------

    suspend fun insertarDetallePedido(detalle: PedidoDetalleEntity) = dao.insertarDetallePedido(detalle)

    suspend fun eliminarDetallePedido(idDetalle: Int) = dao.eliminarDetallePedido(idDetalle)

    suspend fun obtenerDetallePedidoPorId(idDetalle: Int) = dao.obtenerDetallePedidoPorId(idDetalle)

    suspend fun buscarProductoEnDetalle(idPedido: Int, idProducto: Int, unidad: String) = 
        dao.buscarProductoEnDetalle(idPedido, idProducto, unidad)

    suspend fun recalcularTotalPedido(idPedido: Int) {
        val suma = dao.obtenerSumaTotalPedido(idPedido) ?: 0.0
        dao.actualizarTotalCabeceraPedido(idPedido, suma)
    }

    suspend fun actualizarNombreClienteLocal(idPedido: Int, nombre: String) = dao.actualizarNombreCliente(idPedido, nombre)

    suspend fun obtenerCantidadItemsPedidoLocal(idPedido: Int) = dao.obtenerCantidadItemsPedido(idPedido)

    // --- Helper Sync Methods for Printing and Domain Logic ---

    suspend fun obtenerPedidoPorIdSync(idPedido: Int) = dao.obtenerPedidoPorIdSync(idPedido)

    suspend fun obtenerDetallePedidoListSync(idPedido: Int) = dao.obtenerDetallePedidoListSync(idPedido)

    // --- Totales Fiscales ---

    fun obtenerDetallePedidoFlow(idPedido: Int) = dao.obtenerDetallePedidoFlow(idPedido)

    suspend fun actualizarTotalesFiscalesLocal(idPedido: Int, sumas: Double, iva: Double, ivaPerci: Double, totalFinal: Double) =
        dao.actualizarTotalesFiscales(idPedido, sumas, iva, ivaPerci, totalFinal)

    //---------------------------------------------------------
    // REFACTORIZACIÓN MVVM: ACTUALIZAR SUCURSAL
    //---------------------------------------------------------
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
    ) = dao.actualizarSucursalEnPedido(
        idPedido, idSucursal, codigoSucursal, nombreSucursal, idRuta, ruta,
        dteDireccion, dteCodDepto, dteCodMunicipio, dteCodPais, dtePais,
        dteCorreo, dteTelefono
    )
}
