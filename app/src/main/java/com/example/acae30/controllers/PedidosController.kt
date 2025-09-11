package com.example.acae30.controllers

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.acae30.Funciones
import com.example.acae30.Pedido
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

class PedidosController {

    var funciones = Funciones()
    var inventarioController = InventarioController()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"


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
    fun obtenerPedidosNoTransmitidos(context: Context) :Pedidos?{
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var pedido : Pedidos? = null
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
                    "pedido_dte_error FROM pedidos WHERE Enviado=1 AND pedido_dte=0" +
                    " order by id desc limit 1"
            val cursor = db.query(consulta)
            if(cursor.count > 0){
                cursor.moveToFirst()
                pedido = Pedidos(
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
                    ""
                )
            }

            cursor.close()
            return pedido
        }catch (e:Exception){
            throw Exception("ERROR NO SE ENCONTRARON PEDIDOS -> " + e.message)
        }
    }

    //FUNCION PARA OBTENER INFORMACION DEL PEDIDO
    fun obtenerInformacionPedido(idPedido: Int, context: Context): Pedidos?{
        val base  = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var infoPedido : Pedidos? = null
        try{
            val consulta = "SELECT * FROM pedidos WHERE Id=$idPedido"
            val cursor = base.query(consulta)
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
                    cursor.getString(49)
                )
                cursor.close()
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
            val consulta = "SELECT *  FROM detalle_producto where Id_pedido=$idPedido"
            val cdetalle = base.query(consulta)
            if (cdetalle.count > 0) {
                cdetalle.moveToFirst()
                do {
                    val detalle = DetallePedido(
                        cdetalle.getInt(0),
                        cdetalle.getInt(1),
                        cdetalle.getInt(2),
                        cdetalle.getString(3),
                        cdetalle.getString(4),
                        cdetalle.getFloat(5),
                        cdetalle.getFloat(6),
                        cdetalle.getFloat(7),
                        cdetalle.getFloat(8),
                        cdetalle.getFloat(9),
                        cdetalle.getFloat(10),
                        cdetalle.getFloat(11),
                        cdetalle.getFloat(12),
                        cdetalle.getFloat(13),
                        cdetalle.getFloat(14),
                        cdetalle.getFloat(15),
                        cdetalle.getString(16),
                        cdetalle.getInt(17),
                        cdetalle.getFloat(18),
                        cdetalle.getString(19),
                        cdetalle.getInt(20),
                        cdetalle.getString(21)
                    )
                    lista.add(detalle)
                } while (cdetalle.moveToNext())
                cdetalle.close()
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL DETALLE DEL PEDIDO -> ${e.message}")
        }
        return lista
    }

    //FUNCION PARA ELIMINAR PEDIDOS ANTIGUOS
    fun eliminarPedidosAntiguos(context: Context) {
        val fechanow = funciones.obtenerFecha()
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.beginTransaction()
            val cursor = bd.query("SELECT * FROM pedidos where Enviado=1 AND Fecha_creado != ?", arrayOf(fechanow))
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val id = cursor.getInt(0)
                    bd.delete("detalle_pedidos", "Id_pedido=?", arrayOf(id.toString()))
                    bd.delete("pedidos", "Id=?", arrayOf(id.toString()))

                } while (cursor.moveToNext())
                cursor.close()
                bd.setTransactionSuccessful()
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd.endTransaction()
        }

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
            cursor.close()
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

            var total = 0.toFloat()
            if (cursor.count > 0) {
                cursor.moveToFirst()
                total = cursor.getFloat(0)
                cursor.close()
                //if(total > 0){
                val t = ContentValues()
                t.put("Total", total)
                bd.update("pedidos", SQLiteDatabase.CONFLICT_REPLACE, t, "Id=?",arrayOf(idPedido.toString()))
//                }else{
//                    throw Exception("Error en el total")
//                }
            } else {
                throw Exception("No se encontro el pedido asociado")
            }
        }catch (e:Exception){
            throw Exception("ERROR AL ACTUALIZAR TOTAL EN PEDIDO -> " + e.message)
        }
    }

    //FUNCION PARA OBTENER SI EL DOCUMENTO TRANSMITDO ESTA INVALIDADO
    private suspend fun obtenerDocumentosTransmitidosInvalidados(idPedidoServidor:Int, context:Context, idPedido: Int) {

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val datos = PedidoDTE(
                idPedidoServidor
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = servidor + "pedido/invalidarDTE"
            val url = URL(ruta)
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
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
                                funciones.mensaje(context, "DOCUMENTO INVALIDADO CORRECTAMENTE")
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                actualizarInventarioAlInvalidar(context, idPedido)
                            }
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

    //FUNCION DE MENSAJE DE ADVERTENCIA
    fun mensajeInvalidarDTE(context: Context, mensaje: String, idPedidoServidor : Int, idPedido: Int){
        val dialog = AlertDialog.Builder(context)
            .setTitle("INVALIDAR DTE")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    obtenerDocumentosTransmitidosInvalidados(idPedidoServidor, context, idPedido)
                }
            }
            .setNegativeButton("CANCELAR"){ view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //FUCION PARA RETORNAR INVENTARIO AL INVALIDAR PEDIDO
    private fun actualizarInventarioAlInvalidar(context: Context, idPedido: Int){
        val detallePedido = obtenerDetallePedido(idPedido, context)
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase

        try{
            for(item in detallePedido){
                inventarioController.actualizarExistenciasInventario(context, item.Cantidad!!, item.Id_producto!!)
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
            val cursor = bd.query(
                "SELECT Id FROM Pedidos WHERE Enviado = 0 AND Cerrado = 0"
            )

            while (cursor.moveToNext()) {
                idsPedidos.add(cursor.getInt(0).toString())
            }
            cursor.close()

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
            if(cursor.count > 0){
                cursor.moveToFirst()
                cantidadItems = cursor.getInt(0)
            }
            cursor.close()
        }catch (e: Exception){
            println("ERROR NO SE LOGRO OBTENER LA CANTIDAD DE REGISTROS EN EL PEDIDO -> " + e.message)
        }

        return  cantidadItems
    }

}