package com.example.acae30.controllers

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.acae30.Funciones
import com.example.acae30.ui.pedidos.Pedido
import com.example.acae30.R
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.JSONmodels.PedidoDTE
import com.example.acae30.modelos.Pedidos
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import androidx.core.content.edit
import androidx.core.database.getFloatOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import com.example.acae30.Utilidades.CrearSslNoSeguro
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.remote.api.pedidos.PedidosApi
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.data.remote.dto.PedidoTransmitidoDTO
import timber.log.Timber
import java.util.UUID
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class PedidosController {

    var funciones = Funciones()
    var inventarioController = InventarioController()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var utilidades = CrearSslNoSeguro()
    private val clientesController = ClientesController()
    private lateinit var base : AppDatabase
    private lateinit var servidor : String

    private fun inicializarVariables(context: Context){
        base = AppDatabase.getInstance(context)
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)
    }

    //FUNCION PARA ACTUALIZAR EL TIPO DE ENVIO SELECCIONADO
    fun updateTipoPedido(tipoPedido:Int, idpedido:Int, context: Context){
        val data = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            data.execSQL("UPDATE pedidos set tipo_envio=$tipoPedido WHERE id=$idpedido")
        }catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    //FUNCION PARA ACTUALIZAR EL PAGO Y EL CAMBIO DEL CLIENTE
    fun actualizarPagoCambioPedido(context: Context, idpedido: Int, pago:Float, cambio:Float, pagoEfectivo:Float,
                                   pagoCheque:Float,pagoTarjeta:Float,pagoDeposito:Float, numeroOrden:String,
                                   bancoCheque:String,numCuentaCheque:String,numCheque:String, bancoTarjeta:String,
                                   nombreTarjeta:String,numTarjeta:String,bancoDeposito:String,numCuentaDeposito:String,
                                   numDeposito:String, formaPago:String){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        var orden = ""
        orden = if(numeroOrden==""){
            "0"
        }else{
            numeroOrden
        }

        try {
            bd.execSQL("UPDATE pedidos SET pago=$pago, " +
                    "cambio=$cambio," +
                    "pagoEfectivo=$pagoEfectivo," +
                    "pagoCheque=$pagoCheque," +
                    "pagoTarjeta=$pagoTarjeta," +
                    "pagoDeposito=$pagoDeposito," +
                    "bancoCheque='$bancoCheque'," +
                    "numCuentaCheque='$numCuentaCheque'," +
                    "numCheque='$numCheque'," +
                    "bancoTarjeta='$bancoTarjeta'," +
                    "nombreTarjeta='$nombreTarjeta'," +
                    "numTarjeta='$numTarjeta'," +
                    "bancoDeposito='$bancoDeposito'," +
                    "numCuentaDeposito='$numCuentaDeposito'," +
                    "numDeposito='$numDeposito'," +
                    "numero_orden='$orden'," +
                    "formaPago='$formaPago' WHERE id=$idpedido")
        }catch (e:Exception){
            throw Exception(e.message)
        }
    }

    //FUNCION PARA ACTUALIZAR LOS TERMINOS DE ENVIO DEL PEDIDO
    fun actualizarTerminosEnvio(terminos:String, idpedido: Int, context: Context){
        val data = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            data.execSQL("UPDATE pedidos SET Terminos='$terminos' WHERE id=$idpedido")
        }catch (e:Exception){
            throw Exception(e.message)
        }
    }

    //FUNCION PARA ACTUALIZAR LOS TOTAL SEGUN TIPO DE DOCUMENTO
    fun actualizarTotalesFiscales(context: Context, IdPedido: Int, sumas:Float, iva:Float, iva_perci:Float){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE pedidos SET Sumas=$sumas, Iva=$iva, Iva_percibido=$iva_perci" +
                    " WHERE Id=$IdPedido")
        }catch (e:Exception){
            throw Exception(e.message)
        }
    }

    //FUNCION PARA ACTUALIZAR EL TIPO DE DOCUMENTO SELECCIONADO
    fun updateTipoDocumento(tipoDocumento:String, idpedido: Int, context: Context){
        val data = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            data.execSQL("UPDATE pedidos SET tipo_documento='$tipoDocumento' WHERE id=$idpedido")
        }catch (e: Exception){
            throw Exception(e.message)
        }
    }

    //FUNCION PARA ACTUALIZAR EL ESTADO DEL PEDIDO AL GUARDARLO
    fun actualizarEstadoAlGuardar(idpedido: Int, context: Context, view: View){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE pedidos set Cerrado=1 WHERE Id=$idpedido")
        } catch (e: Exception) {
            funciones.mostrarAlerta("ERROR: NO SE ACTUALIZO EL ESTADO DEL PEDIDO AL GUARDARLO", context, view)
        }
    }

    //FUNCION PARA OBTENER PEDIDOS NO TRANSMITIDOS EN SQLITE
    fun obtenerPedidosNoTransmitidos(context: Context) :ArrayList<Pedidos>{
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val pedido = ArrayList<Pedidos>()
        try {
            val consulta = "SELECT Id," +
                    " Id_cliente," +
                    " Nombre_cliente," +
                    " Total," +
                    " Descuento," +
                    " Enviado," +
                    " Fecha_enviado," +
                    " Id_pedido_sistema," +
                    " Gps," +
                    " Cerrado," +
                    " Idvisita," +
                    " strftime('%d/%m/%Y %H:%M'," +
                    " fecha_creado) as fecha_creado," +
                    "Sumas," +
                    "Iva," +
                    "Iva_percibido, " +
                    "pedido_dte, " +
                    "pedido_dte_error," +
                    "Id_pedido_app FROM pedidos WHERE (enviado = 0 OR pedido_dte=0) AND Tipo_documento != 'RC'"
            val cursor = db.query(consulta)
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    do {
                        val item = Pedidos(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getString(2),
                            cursor.getFloat(3),
                            cursor.getFloat(4),
                            cursor.getInt(5),
                            cursor.getString(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getInt(9),
                            cursor.getInt(10),
                            cursor.getString(11),
                            cursor.getFloat(12),
                            cursor.getFloat(13),
                            cursor.getFloat(14),
                            cursor.getInt(15),
                            cursor.getInt(16),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            cursor.getString(17)
                        )
                        pedido.add(item)
                    }while (cursor.moveToNext())
                }
            }
        }catch (e:Exception){
            Timber.e(e, "[PEDIDO_CONTROLLER] ERROR AL OBTENER EL LISTADO DE PEDIDOS NO ENVIADOS")
        }

        return pedido
    }

    //FUNCION PARA OBTENER INFORMACION DEL PEDIDO
    fun obtenerInformacionPedido(idPedido: Int, context: Context): Pedidos?{
        val base  = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var infoPedido : Pedidos? = null
        try{
            val consulta = "SELECT * FROM pedidos WHERE Id=$idPedido"
            val cursor = base.query(consulta)
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    infoPedido = Pedidos(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getFloat(11),
                        cursor.getFloat(5),
                        cursor.getInt(12),
                        cursor.getString(13),
                        cursor.getInt(14),
                        cursor.getString(15),
                        cursor.getInt(16),
                        cursor.getInt(17),
                        cursor.getString(18),
                        cursor.getFloat(6),
                        cursor.getFloat(7),
                        cursor.getFloat(10),
                        cursor.getInt(40),
                        cursor.getInt(41),
                        cursor.getString(42),
                        cursor.getString(43),
                        cursor.getString(44),
                        cursor.getString(45),
                        cursor.getString(22),
                        cursor.getString(24),
                        cursor.getString(21),
                        cursor.getString(49),
                        ""
                    )
                }
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER LA INFORMACION DEL PEDIDO -> ${e.message}")
        }
        return infoPedido
    }

    //FUNCION PARA OBTENER EL DETALLE DEL PEDIDO
    fun obtenerDetallePedido(idPedido: Int, context: Context) : ArrayList<DetallePedido> {
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val lista = ArrayList<DetallePedido>()
        try{
            val consulta = "SELECT *  FROM detalle_producto where Id_pedido=$idPedido ORDER BY Orden_despacho"
            val cdetalle = base.query(consulta)

            cdetalle.use {
                if (cdetalle.count > 0) {
                    cdetalle.moveToFirst()
                    do {
                        val detalle = DetallePedido(
                            cdetalle.getInt(0),
                            cdetalle.getInt(1),
                            cdetalle.getInt(2),
                            cdetalle.getStringOrNull(3) ?: "",
                            cdetalle.getString(4),
                            cdetalle.getFloatOrNull(5) ?: 0f,
                            cdetalle.getFloatOrNull(6) ?: 0f,
                            cdetalle.getFloatOrNull(7) ?: 0f,
                            cdetalle.getFloatOrNull(8) ?: 0f,
                            cdetalle.getFloatOrNull(9) ?: 0f,
                            cdetalle.getFloatOrNull(10) ?: 0f,
                            cdetalle.getFloatOrNull(11) ?: 0f,
                            cdetalle.getFloatOrNull(12) ?: 0f,
                            cdetalle.getFloatOrNull(13) ?: 0f,
                            cdetalle.getFloatOrNull(14) ?: 0f,
                            cdetalle.getFloatOrNull(15) ?: 0f,
                            cdetalle.getString(16),
                            cdetalle.getInt(17),
                            cdetalle.getFloatOrNull(18) ?: 0f,
                            cdetalle.getString(19),
                            cdetalle.getInt(20),
                            cdetalle.getString(21),
                            cdetalle.getFloatOrNull(22) ?: 0f,
                            cdetalle.getFloatOrNull(23) ?: 0f,
                            cdetalle.getString(24),
                            cdetalle.getString(25),
                            cdetalle.getIntOrNull(26),
                            cdetalle.getIntOrNull(27),
                            cdetalle.getIntOrNull(28),
                            cdetalle.getIntOrNull(29),
                            cdetalle.getIntOrNull(30),
                            cdetalle.getIntOrNull(31),
                            cdetalle.getIntOrNull(32),
                            cdetalle.getString(33),
                            cdetalle.getString(34),
                            cdetalle.getIntOrNull(35),
                            cdetalle.getStringOrNull(36),
                            cdetalle.getStringOrNull(37),
                            cdetalle.getIntOrNull(38),
                            cdetalle.getStringOrNull(39),
                            cdetalle.getStringOrNull(40),
                            cdetalle.getInt(41)
                        )
                        lista.add(detalle)
                    } while (cdetalle.moveToNext())
                }
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL DETALLE DEL PEDIDO -> ${e.message}")
        }
        return lista
    }

    //FUNCION PARA ELIMINAR PEDIDOS ANTIGUOS
    fun eliminarPedidosAntiguos(context: Context, local: Boolean) : Boolean{
        var eliminados : Boolean = false
        val fechanow = funciones.obtenerFecha()

        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        val consulta = if(local){
            "SELECT * FROM pedidos where (Enviado=1 AND pedido_dte=1) OR Fecha != '$fechanow'"
        }else{
            "SELECT * FROM pedidos where Fecha != '$fechanow'"
        }

        try {
            bd.beginTransaction()
            val cursor = bd.query(consulta)
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    do {
                        val id = cursor.getInt(0)
                        bd.delete("detalle_pedidos", "Id_pedido=?", arrayOf(id.toString()))
                        bd.delete("pedidos", "Id=?", arrayOf(id.toString()))

                    } while (cursor.moveToNext())
                    eliminados = true
                    bd.setTransactionSuccessful()
                }else{
                    eliminados = false
                }
            }
        } catch (e: Exception) {
            //throw Exception(e.message)
            println("ERROR AL INTENTAR ELIMINAR LOS PEDIDOS -> " + e.message)
            eliminados = false
        } finally {
            bd.endTransaction()
        }

        return eliminados
    }

    //FUNCION PARA ACTUALIZAR EL ESTADO DE TRANSMISION
    fun actualizarEstadoTransmisionPedido(context: Context, idPedidoServidor:Int, pedido_dte : Int, pedido_dte_error:Int,
                                          dteAmbiente:String, dteCodigoGeneracion:String, dteSelloRecibido:String, dteNumeroControl:String,
                                          idDocTransmitido: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE pedidos SET pedido_dte=$pedido_dte, pedido_dte_error=$pedido_dte_error, " +
                    "dteAmbiente='$dteAmbiente', dteCodigoGeneracion='$dteCodigoGeneracion', dteSelloRecibido='$dteSelloRecibido'," +
                    "dteNumeroControl='$dteNumeroControl', idDocTransmitido=$idDocTransmitido " +
                    "WHERE Id_pedido_sistema = $idPedidoServidor")
        }catch (e:Exception){
            throw Exception("ERROR AL ACTUALIZAR EL PEDIDO")
        }
    }


    //ACTUALIZAR TOTALES SEGUN DOCUMENTO
    fun actualizarTotalesPedido(context: Context, idPedido: Int, precioConIVASeleccionado:Boolean){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val precioConIvaShared = preferences.getBoolean("precioConIva", false)
        try {

            val consulta = "SELECT * FROM detalle_pedidos where id_pedido=$idPedido LIMIT 1"
            val cursor = bd.query(consulta)
            cursor.use {
                if (cursor.count > 0) {
                    if(precioConIvaShared != precioConIVASeleccionado){
                        if(!precioConIVASeleccionado){
                            //actualizar quitando iva
                            bd.execSQL("UPDATE detalle_pedidos SET precio=(precio/1.13), precio_iva=(precio_iva/1.13), " +
                                    "total=cantidad*(precio/1.13), total_iva=cantidad*(precio_iva/1.13) WHERE Id_pedido=$idPedido")

                            actualizarTotalPedido(context, idPedido)
                            println("SE QUITO IVA")
                        }else{
                            //actualiar agregando iva
                            bd.execSQL("UPDATE detalle_pedidos SET precio=(precio*1.13), precio_iva=(precio_iva*1.13), " +
                                    "total=cantidad*(precio*1.13), total_iva=cantidad*(precio_iva*1.13) WHERE Id_pedido=$idPedido")

                            actualizarTotalPedido(context, idPedido)
                            println("SE AGREGO IVA")
                        }
                        preferences.edit {
                            remove("precioConIva")
                            putBoolean("precioConIva", precioConIVASeleccionado)
                        }
                    }
                }
            }
        }catch (e:Exception){
            throw Exception("ERROR AL ACTUALIZAR LOS PRECIOS CON IVA O SIN IVA -> " + e.message)
        }
    }

    //ACTUALIZAR TOTAL DEL PEDIDO
    private fun actualizarTotalPedido(context: Context, idPedido: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        try{
            val consulta = "SELECT SUM(Total_iva)  FROM detalle_pedidos where Id_pedido=$idPedido"
            val cursor = bd.query(consulta)
            cursor.use {
                var total = 0.toFloat()
                if (cursor.count > 0) {
                    cursor.moveToFirst()

                    total = cursor.getFloat(0)

                    val t = ContentValues()
                    t.put("Total", total)

                    bd.update("pedidos", SQLiteDatabase.CONFLICT_REPLACE, t, "Id=?",arrayOf(idPedido.toString()))

                } else {
                    throw Exception("No se encontro el pedido asociado")
                }
            }
        }catch (e:Exception){
            throw Exception("ERROR AL ACTUALIZAR TOTAL EN PEDIDO -> " + e.message)
        }
    }

    //FUNCION PARA OBTENER SI EL DOCUMENTO TRANSMITDO ESTA INVALIDADO
    suspend fun obtenerDocumentosTransmitidosInvalidados(idPedidoServidor:Int, context:Context, idPedido: Int) {

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)

        try {
            val datos = PedidoDTE(
                idPedidoServidor
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = servidor + "pedido/invalidarDTE"
            val url = URL(ruta)

            val sslContext = utilidades.crearSslInseguro()

            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {

                if(this is HttpsURLConnection){
                    sslSocketFactory = sslContext.socketFactory
                    hostnameVerifier = HostnameVerifier{_, _ -> true}
                }

                try {
                    connectTimeout = 10000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    )
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //SE ESCRIBE EL OBJ JSON
                    or.flush() //SE ENVIA EL OBJ JSON
                    when (responseCode) {
                        200 -> {
                            withContext(Dispatchers.Main){
                                Toast.makeText(context, "DOCUMENTO INVALIDADO CORRECTAMENTE", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            /*CoroutineScope(Dispatchers.IO).launch {
                                actualizarInventarioAlInvalidar(context, idPedido)
                            }*/

                            actualizarInventarioAlInvalidar(context, idPedido)
                        }
                        else -> {
                            withContext(Dispatchers.Main){
                                funciones.mensaje(context, "EL DOCUMENTO NO HA SIDO INVALIDADO EN EL SISTEMA ACAE")
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main){
                        funciones.mensaje(context, "INESTABILIDAD EN LA CONEXION \n INTENTE MAS TARDE")
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main){
                funciones.mensaje(context, "PROBLEMAS DE CONEXION CON EL SERVIDOR \n INTENTE MAS TARDE")
            }
        }
    }

    //FUCION PARA RETORNAR INVENTARIO AL INVALIDAR PEDIDO
    private fun actualizarInventarioAlInvalidar(context: Context, idPedido: Int){
        val detallePedido = obtenerDetallePedido(idPedido, context)
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase

        try{
            for(item in detallePedido){

                val cantidad = item.Cantidad!! + item.Bonificado!!

                inventarioController.actualizarExistenciasInventario(context, cantidad, item.Id_producto!!)
            }

            bd.execSQL("UPDATE pedidos set pedido_dte_error=2 WHERE id=$idPedido")

        }catch (e:Exception){
            throw Exception("ERROR AL ACTUALIZAR EL INVENTARIO AL INVALIDAR" + e.message)
        }finally {
            if (context is Activity) {
                val intento = Intent(context, Pedido::class.java)
                context.startActivity(intento)
                context.finish()
            } else {
                throw IllegalArgumentException("Contexto debe ser una instancia de Activity")
            }
        }

    }

    //FUNCION PARA ACTUALIZAR EL NOMBRE DEL CLIENTE EN CODIGO 01 -> CLIENTE VARIOS
    fun actualizarNombreClientePedido(context: Context, nombre: String, idPedido: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try{
            bd.execSQL("UPDATE pedidos SET Nombre_cliente='$nombre' WHERE Id=$idPedido")
        }catch (e:Exception){
            println("ERROR: NO SE LOGRO ACTUALIZAR EL NOMBRE DEL CLIENTE EN LE PEDIDO -> " + e.message)
        }
    }

    //FUNCION PARA ELIMINAR EL PEDIDO AL CERRAR LA APP COMPLETAMENTE
    fun eliminarPedidoConError(context: Context){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            // 1. Buscar los Id de los pedidos que cumplen la condición
            val idsPedidos = mutableListOf<String>()
            val consulta = "SELECT Id FROM Pedidos WHERE Enviado = 0 AND Cerrado = 0"
            val cursor = bd.query(consulta)
            cursor.use {
                while (cursor.moveToNext()) {
                    idsPedidos.add(cursor.getInt(0).toString())
                }
            }

            // 2. Eliminar pedidos
            val pedidosEliminados = bd.delete(
                "Pedidos",
                "Enviado = 0 AND Cerrado = 0",
                null
            )

            // 3. Si eliminamos pedidos, borramos sus detalles
            if (pedidosEliminados > 0 && idsPedidos.isNotEmpty()) {
                // Construir cláusula WHERE dinámica con los Ids
                val placeholders = idsPedidos.joinToString(",") { "?" }

                bd.delete(
                    "detalle_pedidos",
                    "Id_pedido IN ($placeholders)",
                    idsPedidos.toTypedArray()
                )
            }
        }catch (e:Exception){
            println("ERROR: NO SE PUDO ELIMINAR EL PEDIDO -> " + e.message)
        }
    }

    //FUNCION PARA CONTAR LA CANTIDAD DE ITEMS EN EL PEDIDO
    fun obtenerCantidadItemsPedido(context: Context, idPedido: Int) : Int{

        var cantidadItems : Int = 0

        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        try {
            val sql = "SELECT COUNT(*) AS Cantidad FROM detalle_pedidos WHERE Id_pedido = $idPedido"
            val cursor = bd.query(sql)
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    cantidadItems = cursor.getInt(0)
                }
            }
        }catch (e: Exception){
            println("ERROR NO SE LOGRO OBTENER LA CANTIDAD DE REGISTROS EN EL PEDIDO -> " + e.message)
        }

        return  cantidadItems
    }

    //FUNCION PARA AGREGAR COMENTARIOS AL PEDIDOS
    fun agregarComentarioAlPedido(context: Context, idPedido: Int, comentario: String){
        val base = funciones.obtenerInstancia(context).openHelper.writableDatabase

        try {
            base.beginTransaction()

            val detalle = ContentValues()
            detalle.put("Id_pedido", idPedido)
            detalle.put("Id_producto", 0)
            detalle.put("Cantidad", 1)
            detalle.put("Unidad", "UNI")
            detalle.put("Descripcion", comentario)
            detalle.put("Idunidad", 0)
            detalle.put("precio", 0f)
            detalle.put("Precio_iva", 0f)
            detalle.put("Precio_oferta", 0f)
            detalle.put("Total", 0f)
            detalle.put("Total_iva", 0f)
            detalle.put("Descuento", 0f)
            detalle.put("Bonificado", 0f)
            detalle.put("Precio_editado", "")
            detalle.put("Id_Inventario_Precios", 0)
            detalle.put("Codigo_de_barra", "")
            detalle.put("EquivaleUni", 0f)
            detalle.put("EquivaleFra", 0f)
            detalle.put("UniEquivale", "")
            detalle.put("Comentario", 1)
            detalle.put("Tipo", "PRD")
            detalle.put("Metodo_gestion", "NINGUNO")
            detalle.put("Tipo_fiscal", "G")
            detalle.put("Departamento", "")

            base.insert("detalle_pedidos", SQLiteDatabase.CONFLICT_REPLACE, detalle)

            base.setTransactionSuccessful()
        }catch (e: Exception){
            println("ERROR AL AGREGAR EL COMENTARIO AL PEDIDO -> " + e.message)
        }finally {
            base.endTransaction()
        }
    }

    //FUNCION PARA OBTENER TOTAL DE ENVIOS AL SERVIDOR
    fun obtenerTotalFacturacion(context: Context) : Float {

        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var totalObtenido = 0f

        val fecha = funciones.obtenerFecha()

        val consulta = "SELECT SUM(Total-iva_percibido) AS Total FROM pedidos WHERE Fecha='$fecha' AND Enviado = 1 AND pedido_dte_error = 0"
        val cursor = bd.query(consulta)
        cursor.use {
            if(cursor.count > 0){
                cursor.moveToFirst()
                totalObtenido = cursor.getFloat(0)
            }else{
                totalObtenido = 0f
            }
        }

        return totalObtenido
    }

    //----------------------------------------
    //Función para verificar si el producto ya existe en el pedido
    //----------------------------------------
    fun obtenerProductoEnDetalle(context: Context, idPedido: Int, idProducto: Int, unidadActual: String) : DetallePedido?{

        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase

        var detalleProducto : DetallePedido? = null

        try{
            val consulta = "SELECT * FROM detalle_pedidos where Id_pedido=$idPedido and Id_producto=$idProducto and Unidad = '$unidadActual'"
            val cdetalle = base.query(consulta)
            cdetalle.use {
                if(cdetalle.count > 0){
                    cdetalle.moveToFirst()
                    detalleProducto = DetallePedido(
                        cdetalle.getInt(0),
                        cdetalle.getInt(1),
                        cdetalle.getInt(2),
                        cdetalle.getStringOrNull(3) ?: "",
                        cdetalle.getString(4),
                        cdetalle.getFloatOrNull(5) ?: 0f,
                        cdetalle.getFloatOrNull(6) ?: 0f,
                        cdetalle.getFloatOrNull(7) ?: 0f,
                        cdetalle.getFloatOrNull(8) ?: 0f,
                        cdetalle.getFloatOrNull(9) ?: 0f,
                        cdetalle.getFloatOrNull(10) ?: 0f,
                        cdetalle.getFloatOrNull(11) ?: 0f,
                        cdetalle.getFloatOrNull(12) ?: 0f,
                        cdetalle.getFloatOrNull(13) ?: 0f,
                        cdetalle.getFloatOrNull(14) ?: 0f,
                        cdetalle.getFloatOrNull(15) ?: 0f,
                        cdetalle.getString(16) ,
                        cdetalle.getInt(17),
                        cdetalle.getFloatOrNull(18) ?: 0f,
                        cdetalle.getString(19),
                        cdetalle.getInt(20),
                        cdetalle.getString(21),
                        cdetalle.getFloatOrNull(22) ?: 0f,
                        cdetalle.getFloatOrNull(23) ?: 0f,
                        cdetalle.getString(24),
                        cdetalle.getString(25),
                        cdetalle.getIntOrNull(26),
                        cdetalle.getIntOrNull(27),
                        cdetalle.getIntOrNull(28),
                        cdetalle.getIntOrNull(29),
                        cdetalle.getIntOrNull(30),
                        cdetalle.getIntOrNull(31),
                        cdetalle.getIntOrNull(32),
                        cdetalle.getString(33),
                        cdetalle.getString(34),
                        cdetalle.getIntOrNull(35),
                        cdetalle.getStringOrNull(36),
                        cdetalle.getStringOrNull(37),
                        cdetalle.getIntOrNull(38),
                        cdetalle.getStringOrNull(39),
                        cdetalle.getStringOrNull(40),
                        cdetalle.getInt(41)
                    )
                }
            }
        }catch (e:Exception){
            println("ERROR AL VERIFICAR EL PRODUCTO EN EL DETALLE -> ${e.message}")
        }
        return detalleProducto
    }

    //=========================================
    //Funcion de Crear Nuevo Pedido
    //Local o Ruta
    //17032026
    //=========================================

    fun crearNuevoPedido(context: Context, idCliente: Int, idVisitaGlobal: Int? = 0) : Int{

        val base = funciones.obtenerInstancia(context).openHelper.writableDatabase
        val fecha = funciones.getFechaHoraProceso()
        val fechaCreado = funciones.obtenerFecha()
        val cliente = clientesController.obtenerInformacionCliente(context, idCliente)

        var idPedido = 0

        var tipoDocumento = "FC"
        val nrc : String = cliente!!.Nrc.toString()
        if(nrc.length > 2 && nrc.isNotBlank()){
            tipoDocumento = "CF"
        }

        val idPedidoApp = UUID.randomUUID().toString()

        try {

            base.beginTransaction()
            val contenido = ContentValues()
            contenido.put("Id_cliente", idCliente)
            contenido.put("Nombre_cliente", cliente.Cliente)
            contenido.put("Total", 0.toFloat())
            contenido.put("Descuento", 0.toFloat())
            contenido.put("Enviado", false)
            contenido.put("Idvisita", idVisitaGlobal)
            contenido.put("Fecha_creado", fecha)
            contenido.put("Terminos", cliente.Terminos_cliente)
            contenido.put("Id_ruta", cliente.Id_ruta)
            contenido.put("Ruta", cliente.Ruta)
            contenido.put("Tipo_documento", tipoDocumento)
            contenido.put("DTEDireccion", cliente.DTEDireccion)
            contenido.put("DTECodDepto", cliente.DTECodDepto)
            contenido.put("DTECodMunicipio", cliente.DTECodMunicipio)
            contenido.put("DTECodPais", cliente.DTECodPais)
            contenido.put("DTEPais", cliente.DTEPais)
            contenido.put("DTECorreo", cliente.DTECorreo)
            contenido.put("DTETelefono", cliente.DTETelefono)
            contenido.put("Fecha", fechaCreado)
            contenido.put("Id_pedido_app", idPedidoApp)

            val id = base.insert("pedidos", SQLiteDatabase.CONFLICT_REPLACE, contenido)

            idPedido = id.toInt()
            base.setTransactionSuccessful()

        } catch (e: Exception) {
            println("ERROR AL REGISTRAR EL PEDIDO -> " + e.message)
        } finally {
            base.endTransaction()
        }
        return idPedido
    }

    //-----------------------------------------------------------
    // Funcion para obtener los pedidos transmitidos
    //-----------------------------------------------------------
    suspend fun obtenerPedidosTransmitidos(
        context: Context, idPedidoApp: String
    ) : PedidoTransmitidoDTO = withContext(Dispatchers.IO){

        inicializarVariables(context)

        val baseUrl : String = servidor
        val api = RetrofitCliente.obtenerApi<PedidosApi>(baseUrl, context)

        try {
            val respuesta = api.obtenerPedidoTransmitido(idPedidoApp)
            if(respuesta.isSuccessful){
                respuesta.body()
            }else{
                Timber.e("[PEDIDO_CONTROLLER] RESPUESTA DEL SERVIDOR VACIA")
            }
        }catch (e: Exception){
            Timber.e(e,"[PEDIDO_CONTROLER] ERROR AL OBTENER LA INFORMACION DEL PEDIDO TRANSMITIDO -> ${e.message}")
        } as PedidoTransmitidoDTO
    }

    //--------------------------------------------------------------
    // Actualizar información del Pedido Transmitido
    //--------------------------------------------------------------
    suspend fun actualizarInformacionPedidoTransmitido(context: Context, idPedido: Int, pedidoDTE : Int, pedidoDteError:Int,
                                                       dteAmbiente:String, dteCodigoGeneracion:String, dteSelloRecibido:String, dteNumeroControl:String,
                                                       idDocTransmitido: Int){
        inicializarVariables(context)

        val dao = base.pedidosDao()

        try {
            dao.actualizarInformacionPedido(idPedido, pedidoDTE, pedidoDteError, dteAmbiente, dteCodigoGeneracion,
                dteSelloRecibido, dteNumeroControl, idDocTransmitido)
        }catch (e: Exception){
            Timber.e(e, "[PEDIDO_CONTROLLER] ERROR AL ACTUALIZAR LA INFORMACIÓN DEL PEDIDO TRANSMITIDO")
        }

    }

    //---------------------------------------------------------
    // Funcion para actualizar el estado del pedido al ser enviado
    //---------------------------------------------------------
    suspend fun actualizarEstadoPedidoEnviado(context: Context, idServidor: Int, idPedido: Int) : Boolean{

        inicializarVariables(context)

        val dao = base.pedidosDao()

        return try{
            val actualizados = dao.actualizarIdServidorConfirmandoPedido(idServidor, idPedido)
            actualizados > 0
        }catch (e: Exception){
            Timber.e(e,"[PEDIDO_CONTROLLER] ERROR AL ACTUALIZAR EL ENVIO DEL PEDIDO")
        } as Boolean


    }
}