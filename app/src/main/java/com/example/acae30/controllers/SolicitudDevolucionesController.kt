package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import com.example.acae30.Funciones
import com.example.acae30.modelos.SolcitudDevolucion.SolicitudDevolucion
import com.example.acae30.modelos.SolcitudDevolucion.SolicitudDevolucionDetalle
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SolicitudDevolucionesController {

    private var funciones = Funciones()

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    //FUNCION PARA CREAR UNA NUEVA DEVOLUCION
    fun crearDevolucion(context: Context) : Int{
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val idVendedor = preferences.getInt("Idvendedor", 0)
        val nombreVendedor =  preferences.getString("Vendedor", "")
        val hojaCargaActiva = preferences.getInt("hojaCarga", 0)
        val idHojaCarga = preferences.getInt("idHojaCarga", 0)
        val idRuta = preferences.getInt("idRutaSeleccionada", 0)
        val ruta = preferences.getString("rutaSeleccionada","")



        val base = funciones.obtenerInstancia(context).openHelper.writableDatabase
        val fecha = funciones.obtenerFecha()
        var idDevolucion : Int = 0

        try{
            base.beginTransaction()
            val contenido = ContentValues()
            contenido.put("Numero" , 0)
            contenido.put("Fecha", fecha)
            contenido.put("Id_hoja_de_carga", idHojaCarga)
            contenido.put("Hoja_de_carga", hojaCargaActiva)
            contenido.put("Id_ruta", idRuta)
            contenido.put("Ruta", ruta)
            contenido.put("Id_vendedor", idVendedor)
            contenido.put("Vendedor", nombreVendedor)
            contenido.put("Estado", "PROCESADO")
            val id = base.insert("devolucion", SQLiteDatabase.CONFLICT_REPLACE, contenido)
            idDevolucion = id.toInt()

            base.setTransactionSuccessful()

        }catch (e: Exception){
            idDevolucion = 0
            println("ERROR AL CREAR LA NUEVA DEVOLUCION " + e.message)
        }finally {
            base.endTransaction()
        }

        return  idDevolucion
    }

    //FUNCION PARA AGREGAR PRODUCTOS A LA DEVOLUCION
    fun agregarProductoDetalleDevolucion(context: Context,  obj: SolicitudDevolucionDetalle) : Boolean{
        var registrado = false
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase

        val fecha = funciones.obtenerFecha()
        try {
            bd.beginTransaction()
            val contenido = ContentValues()
            contenido.put("Id_dev", obj.Id_dev)
            contenido.put("Numero_dev", obj.Numero_dev)
            contenido.put("Fecha", fecha)
            contenido.put("Id_producto", obj.id_producto)
            contenido.put("Codigo_producto", obj.Codigo_producto)
            contenido.put("Producto", obj.Producto)
            contenido.put("Fraccion", "UNI")
            contenido.put("Cantidad", obj.Cantidad)
            contenido.put("Bueno", obj.Bueno)
            contenido.put("Averia", obj.Averia)
            contenido.put("Tipo_fiscal", "G")
            bd.insert("devolucion_detalle", SQLiteDatabase.CONFLICT_REPLACE, contenido)
            registrado = true

            bd.setTransactionSuccessful()
        }catch (e:Exception){
            registrado = false
            println("ERROR AL REGISTRAR EL DETALLE DE LA DEVOLUCION -> ${e.message}")
        }finally {
            bd.endTransaction()
        }


        return registrado
    }

    //FUNCION PARA CANCELAR EL PROCESO DE DEVOLUCION
    fun eliminarDevolucion(context: Context, idDevolucion : Int): Boolean{
        var eliminado : Boolean = false
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("DELETE FROM devolucion WHERE id=$idDevolucion")
            bd.execSQL("DELETE FROM devolucion_detalle WHERE Id_dev=$idDevolucion")

            eliminado = true
        }catch (e:Exception){
            println("ERROR AL ELIMINAR LA DEVOLUCION DESDE LA BD DE SQLITE")
            eliminado = false
        }
        return eliminado
    }

    //FUNCION PARA OBTENER EL DETALLE DE LA DEVOLUCION
    fun obtenerDetalleDevolucion(context: Context, idDevolucion: Int) : ArrayList<SolicitudDevolucionDetalle>{
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val detalleDevolucion = ArrayList<SolicitudDevolucionDetalle>()
        try {
            val consulta = "SELECT * FROM devolucion_detalle WHERE Id_dev=$idDevolucion"
            val cursor = db.query(consulta)
            if(cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val item = SolicitudDevolucionDetalle(
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getFloat(8),
                        cursor.getFloat(9),
                        cursor.getFloat(10),
                        cursor.getString(11)
                    )
                    detalleDevolucion.add(item)
                }while(cursor.moveToNext())
                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL DETALLE DE LA DEVOLUCION -> ${e.message}")
        }
        return  detalleDevolucion
    }

    //FUNCION PARA BUSCAR EL PRODUCTO YA AGREGADO AL DETALLE DE LA DEVOLUCION
    fun obtenerProductoEnDevolucion(context: Context, idProducto: Int, idDevolucion: Int) : Boolean{
        var encontrado = false
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        try {
            val sql = "SELECT * FROM devolucion_detalle WHERE Id_producto=$idProducto AND Id_dev=$idDevolucion"
            val cursor = db.query(sql)
            if(cursor.count > 0){
                encontrado = true
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL ENCONTRAR EL PRODUCTO EN EL DETALLE DE LA DEVOLUCION -> ${e.message}")
            encontrado = false
        }
        return encontrado
    }

    //FUNCION PARA OBTENER TODA LA DEVOLCION ENCABEZADO Y DETALLE
    private fun obtenerDevolucion(context: Context, idDevolucion: Int) : SolicitudDevolucion?{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var devolucion : SolicitudDevolucion? = null
        try {
            val consulta = "SELECT * FROM devolucion WHERE id=$idDevolucion"
            val cursor = bd.query(consulta)
            if(cursor.count > 0){
                cursor.moveToFirst()
                devolucion = SolicitudDevolucion(
                    cursor.getInt(0),
                    0,
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getString(8),
                    cursor.getString(9),
                    null
                )
                cursor.close()
                val cdetalle = obtenerDetalleDevolucion(context, idDevolucion)
                devolucion.detalle = cdetalle
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER LA DEVOLUCION -> ${e.message}")
        }
        return devolucion
    }

    //Funcion para enviar la solicitud de carga al servidor
    suspend fun enviarDevolucionAlServidor(context : Context, idDevolucion : Int) : Boolean{
        var envio = false
        preferences  = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        val solicitudJson = convertirDevolucionJSON(context, idDevolucion)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString().toString())

        try {
            val objecto =
                Gson().toJson(solicitudJson)
            val ruta: String = servidor + "Solicitudes/registrar_devolucion"
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
                    or.write(objecto) //escribo el json
                    or.flush() //se envia el json
                    if (responseCode == 201) {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            try {
                                val respuesta = StringBuffer()
                                var inpuline = it.readLine()
                                while (inpuline != null) {
                                    respuesta.append(inpuline)
                                    inpuline = it.readLine()
                                }
                                it.close()

                                val res: JSONObject = JSONObject(respuesta.toString())
                                if (res.getInt("error") > 0 && res.getString("response") == "DEVOLUCION_REGISTRADA") {
                                    val numero = res.getInt("error")
                                    actualizarNumeroDcolucion(context, idDevolucion, numero)
                                    envio = true
                                }
                            } catch (e: Exception) {
                                println("ERROR DE LECTURA EN LA RESPUESTA 201 " + e.message)
                                envio = false
                            }
                        }
                    }else {
                        println("ERROR NO SE LOGRO REGISTRAR EL ABONO EN EL SERVIDOR")
                        envio = false
                    }

                } catch (e: Exception) {
                    println("INESTABILIDAD DE CONEXION")
                    envio = false
                }
            }
        } catch (e: Exception) {
            println("PROBLEMAS DE CONEXION CON EL SERVIDOR")
            envio = false
        }
        return envio
    }

    private fun actualizarNumeroDcolucion(context: Context, idDevolucion: Int, numero: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE devolucion SET numero=$numero WHERE Id=$idDevolucion")
            bd.execSQL("UPDATE devolucion_detalle SET Numero_dev = $numero WHERE Id_dev=$idDevolucion ")
        }catch (e:Exception){
            println("ERROR AL ACTUALIZAR EL NUMERO DE LA DEVOLUCION -> ${e.message}")
        }
    }

    //Funcion para convertir la solicitud en JSON
    private fun convertirDevolucionJSON(context: Context, idDevolucion: Int) : JsonObject {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        val obj = obtenerDevolucion(context, idDevolucion)

        val json = JsonObject()
        json.addProperty("fecha", obj!!.Fecha)
        json.addProperty("id_hoja_de_carga", obj.Id_hoja_de_carga)
        json.addProperty("hoja_de_carga", obj.Hoja_de_carga)
        json.addProperty("id_ruta", obj.Id_ruta)
        json.addProperty("ruta", obj.Ruta)
        json.addProperty("id_vendedor", obj.Id_vendedor)
        json.addProperty("vendedor", obj.Vendedor)

        val detalle = JsonArray()
        for(i in 0..<obj.detalle!!.size){
            val data = obj.detalle!![i]
            val d = JsonObject()

            d.addProperty("id", 0)
            d.addProperty("id_dev", 0)
            d.addProperty("numero_dev", 0)
            d.addProperty("fecha", data.Fecha)
            d.addProperty("id_producto", data.id_producto)
            d.addProperty("codigo_producto", data.Codigo_producto)
            d.addProperty("producto", data.Producto)
            d.addProperty("fraccion", data.Fraccion)
            d.addProperty("cantidad", data.Cantidad)
            d.addProperty("bueno", data.Bueno)
            d.addProperty("averia", data.Averia)
            d.addProperty("tipo_fiscal", data.Tipo_fiscal)

            detalle.add(d)
        }

        json.add("detalle", detalle)

        return  json
    }

    //DESCARGA DE INVENTARIO LA DEVOLUCION
    fun descargarProductosInventario(idDevolucion: Int, context: Context){
        val base = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            val consulta = "SELECT Id_producto, Cantidad FROM devolucion_detalle WHERE Id_dev=$idDevolucion"
            val cursor = base.query(consulta)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    try {
                        base.execSQL("UPDATE Inventario SET Existencia = (Existencia - ${cursor.getInt(1)}) WHERE Id=${cursor.getInt(0)}")
                    }catch (e:Exception){
                        println("ERROR: NO SE ACTUALIZARON LAS EXITENCIAS EN INVENTARIO -> ${e.message}")
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR: NO SE ENCONTRARON REGISTROS EN LA DEVOLUCION -> ${e.message}")
        }
    }

    //FUNCION PARA OBTENER TODA LA DEVOLCION ENCABEZADO Y DETALLE
    fun obtenerListadoDevoluciones(context: Context) : ArrayList<SolicitudDevolucion>{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val devolucion = ArrayList<SolicitudDevolucion>()
        try {
            val cursor = bd.query("SELECT * FROM devolucion ORDER BY ID DESC LIMIT 20")
            if(cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val listado = SolicitudDevolucion(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getString(8),
                        cursor.getString(9),
                        null
                    )
                    devolucion.add(listado)
                }while (cursor.moveToNext())

                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER LA DEVOLUCION -> ${e.message}")
        }
        return devolucion
    }
}