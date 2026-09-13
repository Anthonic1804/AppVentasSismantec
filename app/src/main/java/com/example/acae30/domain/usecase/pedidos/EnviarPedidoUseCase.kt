package com.example.acae30.domain.usecase.pedidos

import android.content.Context
import com.example.acae30.Funciones
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.data.remote.api.pedidos.PedidosApi
import com.example.acae30.data.remote.dto.EnviarPedidoDetalleDto
import com.example.acae30.data.remote.dto.EnviarPedidoRequestDto
import com.example.acae30.data.repository.PedidosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Caso de uso para el envío integral de un pedido al servidor.
 */
class EnviarPedidoUseCase(
    private val repository: PedidosRepository,
    private val context: Context
) {
    private val funciones = Funciones()
    private val db = AppDatabase.getInstance(context)

    suspend operator fun invoke(idPedido: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            // Obtener información del pedido (Room)
            val pedidoEntity = db.pedidosDao().obtenerPedidoPorIdSync(idPedido) ?: return@withContext false
            val detalleView = db.pedidosDao().obtenerDetallePedidoListSync(idPedido)
            
            // Obtener ID de visita del servidor
            val idVisitaServidor = db.pedidosDao().obtenerIdVisitaServidor(idPedido) ?: 0

            // Obtener preferencias
            val prefs = context.getSharedPreferences("CONFIG_SERVIDOR", Context.MODE_PRIVATE)
            val puntoVenta = prefs.getString("puntoVenta", "") ?: ""
            val idVendedor = prefs.getInt("Idvendedor", 0)
            val vendedor = prefs.getString("Vendedor", "") ?: ""
            val numeroCaja = prefs.getInt("numeroCaja", 0)
            
            val multiplesHoja = prefs.getBoolean("multiplesHojaDeCarga", false)
            val idHoja = if (!multiplesHoja) prefs.getInt("idHojaCarga", 0) else 0
            val numHoja = if (!multiplesHoja) prefs.getInt("hojaCarga", 0) else 0

            val idBodega = prefs.getInt("idBodega", -1).let { if (it == -1) null else it }
            val codBodega = prefs.getString("codBodega", "-1").let { if (it == "-1") null else it }
            val bodega = prefs.getString("bodega", "-1").let { if (it == "-1") null else it }

            // Construir DTO de Cabecera
            val request = EnviarPedidoRequestDto(
                idCliente = pedidoEntity.idCliente,
                cliente = pedidoEntity.nombreCliente,
                subtotal = pedidoEntity.subTotal,
                descuento = pedidoEntity.descuento,
                total = pedidoEntity.total,
                idSucursal = pedidoEntity.idSucursal,
                codigoSucursal = pedidoEntity.codigoSucursal,
                nombreSucursal = pedidoEntity.nombreSucursal,
                tipoEnvio = pedidoEntity.tipoEnvio,
                tipoDocumentoApp = pedidoEntity.tipoDocumento,
                idVendedor = idVendedor,
                vendedor = vendedor,
                terminos = pedidoEntity.terminos,
                fechaCreado = pedidoEntity.fechaCreado,
                horaProceso = funciones.getFechaHoraProceso(),
                idApp = idVisitaServidor,
                idHojaCarga = idHoja,
                numHojaCarga = numHoja,
                puntoVenta = puntoVenta,
                formaPago = pedidoEntity.formaPago,
                numeroOrden = try { pedidoEntity.numeroOrden.toBigDecimal() } catch (e: Exception) { java.math.BigDecimal.ZERO },
                efectivoPago = pedidoEntity.pagoEfectivo,
                tarjetaPago = pedidoEntity.pagoTarjeta,
                tarjetaBanco = pedidoEntity.bancoTarjeta,
                tarjetaNombre = pedidoEntity.nombreTarjeta,
                tarjetaNumero = pedidoEntity.numTarjeta,
                chequePago = pedidoEntity.pagoCheque,
                chequeBanco = pedidoEntity.bancoCheque,
                chequeCuenta = pedidoEntity.numCuentaCheque,
                chequeNumero = pedidoEntity.numCheque,
                depositoPago = pedidoEntity.pagoDeposito,
                depositoBanco = pedidoEntity.bancoDeposito,
                depositoCuenta = pedidoEntity.numCuentaDeposito,
                depositoNumero = pedidoEntity.numDeposito,
                idRutaHeader = pedidoEntity.idRuta,
                rutaHeader = pedidoEntity.ruta,
                dteDireccion = pedidoEntity.dteDireccion,
                dteTelefono = pedidoEntity.dteTelefono,
                dteCorreo = pedidoEntity.dteCorreo,
                dteCodDepto = pedidoEntity.dteCodDepto,
                dteCodMunicipio = pedidoEntity.dteCodMunicipio,
                dteCodPais = pedidoEntity.dteCodPais,
                dtePais = pedidoEntity.dtePais,
                idPedidoApp = pedidoEntity.idPedidoApp,
                numeroCaja = numeroCaja,
                idBodega = idBodega,
                codBodega = codBodega,
                bodega = bodega,
                detalle = detalleView.map { d ->
                    EnviarPedidoDetalleDto(
                        id = d.Id,
                        idPedido = d.Id_pedido,
                        idProducto = d.Id_producto,
                        codigo = d.Codigo,
                        codigoBarra = d.Codigo_de_barra,
                        descripcion = d.Descripcion,
                        precio = d.Precio ?: 0.0,
                        precioIva = d.Precio_iva ?: 0.0,
                        precioU = d.Precio_u ?: 0.0,
                        precioUIva = d.Precio_u_iva ?: 0.0,
                        precioVenta = d.Precio_venta ?: 0.0,
                        precioOfertado = d.Precio_ofertado ?: 0.0, // Campo incluido
                        cantidad = d.Cantidad ?: 0.0,
                        subtotal = d.Total ?: 0.0, // Room.Total -> JSON.subtotal (sin iva)
                        total = d.Total_iva ?: 0.0, // Room.Total_iva -> JSON.total (con iva)
                        bonificado = (d.Bonificado ?: 0).toDouble(),
                        descuento = d.Descuento ?: 0.0,
                        precioEditado = d.Precio_editado,
                        idUnidad = d.Idunidad ?: 0,
                        equivaleUni = d.EquivaleUni ?: 0.0,
                        equivaleFra = d.EquivaleFra ?: 0.0,
                        uniEquivale = d.UniEquivale,
                        fechaCreado = pedidoEntity.fechaCreado,
                        tipo = d.Tipo,
                        idMarca = d.IdMarca,
                        idSku = d.IdSku,
                        idLinea = d.IdLinea,
                        idSublinea = d.IdSubLinea,
                        idRubro = d.IdRubro,
                        idProductor = d.IdProductor,
                        idProveedor = d.IdProveedor,
                        idRuta = pedidoEntity.idRuta,
                        idVendedor = idVendedor,
                        metodoGestion = d.MetodoGestion,
                        tipoFiscal = d.TipoFiscal,
                        departamento = pedidoEntity.dteCodDepto,
                        idLote = d.IdLote,
                        lote = d.Lote,
                        fechaVencimiento = d.FechaVencimiento,
                        idBodega = d.IdBodega,
                        codBodega = d.CodBodega,
                        bodega = d.Bodega,
                        ordenDespacho = d.OrdenDespacho ?: 0,
                        unidad = d.Unidad
                    )
                }
            )

            // Enviar vía Retrofit
            val ip = prefs.getString("ip", "") ?: ""
            val puerto = prefs.getInt("puerto", 0).toString()
            val baseUrl = funciones.getServidor(ip, puerto, context)
            val api = RetrofitCliente.obtenerApi<PedidosApi>(baseUrl, context)
            
            val response = api.enviarPedido(request)
            
            if (response.isSuccessful && response.body() != null) {
                val idServidor = response.body()!!.idServidor
                if (idServidor > 0) {
                    // Actualizar estado local
                    repository.actualizarEstadoPedidoEnviado(idServidor, idPedido)
                    return@withContext true
                }
            }
            return@withContext false
        } catch (e: Exception) {
            Timber.e(e, "Error al enviar pedido")
            return@withContext false
        }
    }
}
