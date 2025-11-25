package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import com.example.acae30.AlertDialogo
import com.example.acae30.Funciones
import com.example.acae30.modelos.Inventario
import com.example.acae30.modelos.SolicitudCarga.SolicitudCarga
import com.example.acae30.modelos.SolicitudCarga.SolicitudCargaDTO
import com.example.acae30.modelos.SolicitudCarga.SolicitudCargaDetalle
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate

class SolicitudRecargasController {

    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    suspend fun obtenerInventarioServidor(context: Context, alert : AlertDialogo) {

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        limpiarInventariosolicitud(context)

        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val direccioncantidad = servidor + "inventario/cantidad"
            val urlcantidad = URL(direccioncantidad)
            var cantidadRegistros = 0

            with(withContext(Dispatchers.IO) {
                urlcantidad.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 10000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { data ->
                            val readline = data.readLine()
                            cantidadRegistros = readline.toInt()
                            println("CANTIDAD DE REGISTROS -> $cantidadRegistros")
                        }

                    } else {
                        println("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main){
                        funciones.mensaje(context, "CONEXION CON EL SERVIDOR INESTABLE \n INTENTE MAS TARDE -> ${e.message}")
                    }
                }
            }

            val BLOQUE = 1000

            var inicio = 0
            var longitud = 0
            var registrosCargados = 0

            if (cantidadRegistros < BLOQUE) {
                longitud = cantidadRegistros
                registrosCargados = cantidadRegistros
            } else {
                longitud = BLOQUE
                registrosCargados = BLOQUE
            }


            do {

                withContext(Dispatchers.Main){
                    alert.changeText("ACTUALIZANDO INVENTARIO ...")
                }

                val direccion =
                    servidor + "inventario/" + inicio.toString() + "/" + longitud.toString()
                val url = URL(direccion)
                with(withContext(Dispatchers.IO) {
                    url.openConnection()
                } as HttpURLConnection) {
                    try {
                        connectTimeout = 10000
                        requestMethod = "GET"
                        if (responseCode == 200) {

                            inputStream.bufferedReader().use { data ->
                                val response = StringBuffer()
                                var inputLine = data.readLine()
                                while (inputLine != null) {
                                    response.append(inputLine)
                                    inputLine = data.readLine()
                                }
                                data.close()
                                val respuesta = JSONArray(response.toString())
                                if (respuesta.length() > 0) {
                                    almacenarInventarioEnSQLite(respuesta, context)
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main){
                                funciones.mensaje(context, "EL SERVIDOR NO RETORNO DATOS -> $responseCode")
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main){
                            funciones.mensaje(context, "CONEXION CON EL SERVIDOR INESTABLE \n INTENTE MAS TARDE -> ${e.message}")
                        }
                    }
                }//termina de obtener los datos

                inicio += BLOQUE
                registrosCargados += longitud

                if (cantidadRegistros in (inicio + 1)..<registrosCargados) {
                    longitud = cantidadRegistros
                }

            } while (inicio < cantidadRegistros)

            withContext(Dispatchers.Main){
                alert.changeText("INVENTARIO ACTUALIZADO CORRECTAMENTE")
            }

            delay(1500)

            withContext(Dispatchers.Main){
                alert.dismisss()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main){
                funciones.mensaje(context, "ERROR DE CONEXION CON EL SERVIDOR \n INTENTE MAS TARDE -> ${e.message}")
            }
        }
    }

    private fun limpiarInventariosolicitud(context: Context) {
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("DELETE FROM inventario_solicitud_carga")
        }catch (e:Exception){
            println("ERROR AL LIMPIAR LA TBL INVENTARIO SOLICITUD CARGA -> ${e.message}")
        }
    }

    //FUNCION PARA ALMACENAR EL INVENTARIO EN SQLITE
    private fun almacenarInventarioEnSQLite(json: JSONArray, context: Context) {
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase

        try {
            bd.beginTransaction()
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)

                val data = ContentValues()
                data.put("id", dato.getInt("id"))
                data.put("codigo", funciones.validateJsonIsnullString(dato, "codigo"))
                data.put("codigo_de_barra", funciones.validateJsonIsnullString(dato, "codigo_de_barra"))
                data.put("tipo", funciones.validateJsonIsnullString(dato, "tipo"))
                data.put("descripcion", funciones.validateJsonIsnullString(dato, "descripcion"))
                data.put("unidad_medida", funciones.validateJsonIsnullString(dato, "unidad_medida"))
                data.put("fraccion", funciones.validateJsonIsNullFloat(dato, "fraccion"))
                data.put("nombre_fraccion", funciones.validateJsonIsnullString(dato, "nombre_fraccion"))
                data.put("costo", funciones.validateJsonIsNullFloat(dato, "costo"))
                data.put("costo_iva", funciones.validateJsonIsNullFloat(dato, "costo_iva"))
                data.put("ult_costo", funciones.validateJsonIsNullFloat(dato, "ult_costo"))
                data.put("ult_costo_iva", funciones.validateJsonIsNullFloat(dato, "ult_costo_iva"))
                data.put("existencia", funciones.validateJsonIsNullFloat(dato, "existencia"))
                data.put("existencia_u", funciones.validateJsonIsNullFloat(dato, "existencia_u"))
                data.put("precio", funciones.validateJsonIsNullFloat(dato, "precio"))
                data.put("precio_u", funciones.validateJsonIsNullFloat(dato, "precio_u"))
                data.put("precio_u_iva", funciones.validateJsonIsNullFloat(dato, "precio_u_iva"))
                data.put("precio_iva", funciones.validateJsonIsNullFloat(dato, "precio_iva"))
                data.put("bonificado", funciones.validateJsonIsNullFloat(dato, "bonificado"))
                data.put("lote", funciones.validateJsonIsnullString(dato, "lote"))
                data.put("fecha_vencimiento", funciones.validateJsonDate(dato, "fecha_vencimiento"))
                data.put("precio2", funciones.validateJsonIsNullFloat(dato, "precio2"))
                data.put("precio2_iva", funciones.validateJsonIsNullFloat(dato, "precio2_iva"))
                data.put("precio_u2", funciones.validateJsonIsNullFloat(dato, "precio_u2"))
                data.put("precio_u2_iva", funciones.validateJsonIsNullFloat(dato, "precio_u2_iva"))
                data.put("precio_viñeta", funciones.validateJsonIsNullFloat(dato, "precio_viñeta"))
                data.put("precio_viñeta_iva", funciones.validateJsonIsNullFloat(dato, "precio_viñeta_iva"))
                data.put("fechaInventario", LocalDate.now().toString())


                bd.insert("inventario_solicitud_carga", SQLiteDatabase.CONFLICT_REPLACE, data)
            }
            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR AL ALMACENAR EL INVENTARIO DE SOLICUTD EN LA TABLA" + e.message)
        } finally {
            bd.endTransaction()
        }
    }

    //FUNCION PARA OBTENER LA INFORMACION DEL PRODUCTO POR CODIGO O POR NOMBRE
    fun obtenerInformacionProductoPorString(context: Context, busqueda: String): ArrayList<Inventario>{
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val lista = ArrayList<Inventario>()

        val query: String = if(busqueda != ""){
            "SELECT * FROM inventario_solicitud_carga WHERE Descripcion LIKE '%$busqueda%' LIMIT 60"
        }else{
            "SELECT * FROM inventario_solicitud_carga limit 60"
        }

        try {
            val cursor = base.query(query)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val arreglo = Inventario(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getFloat(6),
                        cursor.getString(7),
                        cursor.getInt(12),
                        cursor.getFloat(8),
                        cursor.getFloat(9),
                        cursor.getFloat(17),
                        cursor.getFloat(14),
                        cursor.getFloat(15),
                        cursor.getFloat(16),
                        cursor.getString(27),
                        cursor.getFloat(18),
                        cursor.getFloat(13),
                        cursor.getString(2)
                    )
                    lista.add(arreglo)
                } while (cursor.moveToNext())
                //cursor.close()
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL REALIZAR LA BUSQUEDA EN INVENTARIO -> ${e.message}")
        }
        return lista
    }

    //FUNCION PARA OBTENER LA INFORMACION DEL PRODUCTO POR CODIGO
    fun obtenerInformacionProductoPorCodigo(context: Context, codigoProducto: String) : Inventario?{

        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var producto : Inventario? = null

        try {
            val consutla = "SELECT * FROM inventario_solicitud_carga WHERE codigo='$codigoProducto'"
            val cursor = bd.query(consutla)

            if(cursor.count > 0){
                cursor.moveToFirst()

                producto = Inventario(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getFloat(6),
                    cursor.getString(7),
                    cursor.getInt(12),
                    cursor.getFloat(8),
                    cursor.getFloat(9),
                    cursor.getFloat(17),
                    cursor.getFloat(14),
                    cursor.getFloat(15),
                    cursor.getFloat(16),
                    cursor.getString(27),
                    cursor.getFloat(18),
                    cursor.getFloat(13),
                    cursor.getString(2)
                )
            }
        }catch (e:Exception){
            println("ERROR AL BUSCAR EL PRODUCTO POR ID -> " + e.message)
        }

        return producto
    }

    //FUNCION PARA ELIMINAR EL PRODUCTO DEL DETALLE DE LA SOLICITUD
    fun eliminarProductoDelDetalle(context: Context, idSolicitud: Int, codigoProducto: String){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("DELETE FROM solicitudCargaDetalle WHERE Id_solicitud_carga = $idSolicitud AND Codigo_Producto = '$codigoProducto'")
        }catch (e:Exception){
            println("ERROR AL ELIMINAR EL PRODUCTO DE LA SOLICITUD -> " + e.message)
        }
    }

    //FUNCION PARA ACTUALIZAR EL PRODUCTO DEL DETALLE DE LA SOLICITUD
    fun actualizarProductoDelDetalle(context: Context, idSolicitud: Int, codigoProducto: String, cantidad: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE solicitudCargaDetalle SET Cantidad = $cantidad, Total = (Precio_u_iva * $cantidad) " +
                    "WHERE Id_solicitud_carga = $idSolicitud AND Codigo_Producto = '$codigoProducto'")
        }catch (e:Exception){
            println("ERROR AL ACTUALIZAR EL PRODUCTO DE LA SOLICITUD -> " + e.message)
        }
    }

    //FUNCION PARA CREAR UNA NUEVA SOLICITUD
    fun guardarNuevaSolicitud(context: Context , solicitud: SolicitudCarga) : Int{
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        var idSolicitud : Int = 0
        try {
            bd.beginTransaction()
            val data = ContentValues()
            data.put("Id_Empleado", solicitud.idEmpleado)
            data.put("Empleado", solicitud.empleado)
            data.put("Fecha", solicitud.fecha.toString())
            data.put("Ruta", solicitud.ruta)
            idSolicitud = bd.insert("solicitudCarga", SQLiteDatabase.CONFLICT_REPLACE, data).toInt()
            bd.setTransactionSuccessful()
        }catch (e:Exception){
            println("ERROR AL INGRESAR LA NUEVA SOLICITUD -> ${e.message}")
        }finally {
            bd.endTransaction()
        }
        return  idSolicitud
    }

    //FUNCION PARA VALIDAR PRODUCTO EN SOLICITUD DETALLE
    fun validarProductoDetalle(context: Context, detalle: SolicitudCargaDetalle) : Boolean{
        var encontrado : Boolean = false

        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        try {
            val consulta = "SELECT Id_Producto FROM solicitudCargaDetalle WHERE Id_producto = ${detalle.idProducto} " +
                    "AND Id_solicitud_carga = ${detalle.idSolicitudCarga}"

            val cursor = bd.query(consulta)
            if(cursor.count > 0){
                encontrado = true
            }
            cursor.close()
        }catch (e : Exception){
            println("ERROR AL BUSCAR EL PRODUCTO EN DETALLE DE CARGA -> ${e.message}")
            encontrado = false
        }

        return encontrado
    }

    //FUNCION PARA INSERTAR EL DETALLE DE UNA SOLICITUD
    fun insertarDetalleSolicitud(context: Context, detalle : SolicitudCargaDetalle) : Boolean{
        var registrado : Boolean = false
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.beginTransaction()
            val data = ContentValues()
            data.put("Id_solicitud_carga", detalle.idSolicitudCarga)
            data.put("Id_producto", detalle.idProducto)
            data.put("Codigo_Producto", detalle.codigoProducto)
            data.put("Descripcion", detalle.descripcion)
            data.put("Cantidad", detalle.cantidad)
            data.put("Costo", detalle.costo)
            data.put("Costo_iva", detalle.costoIva)
            data.put("Precio_u", detalle.precio)
            data.put("Precio_u_iva", detalle.precio_iva)
            data.put("Total", detalle.total)
            bd.insert("solicitudCargaDetalle", SQLiteDatabase.CONFLICT_REPLACE, data)
            bd.setTransactionSuccessful()
            registrado = true
        }catch (e:Exception){
            println("ERROR AL INSERTAR EL DETALLE DE LA SOLICITUD -> ${e.message}")
            registrado = false
        }finally {
            bd.endTransaction()
        }
        return registrado
    }

    //FUNCION PARA ACTUALIZAR LA CANTIDAD DEL PRODUCTO EN DETALLE
    fun actualizarCantidadProductoDetalle(context: Context, detalle: SolicitudCargaDetalle) : Boolean{
        var actualizado : Boolean = false
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase

        try{
            bd.execSQL("UPDATE solicitudCargaDetalle SET Cantidad = (Cantidad + ${detalle.cantidad}) " +
                    "WHERE Id_solicitud_carga = ${detalle.idSolicitudCarga} AND Id_producto=${detalle.idProducto}")

            actualizado = true
        }catch (e : Exception){
            println("ERROR AL ACTUALIZAR LA CANTIDAD DE PRODUCTO EN DETALLE -> ${e.message}")

            actualizado = false
        }

        return actualizado
    }

    //Funcion para enviar la solicitud de carga al servidor
    suspend fun enviarSolicitudCargaAlServidor(context : Context, idSolicitud : Int) : Boolean{
        var envio = false
        preferences  = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        val solicitudJson = convertirSolicitudJSON(context, idSolicitud)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString().toString())

        try {
            val objecto =
                Gson().toJson(solicitudJson)

            println(objecto)

            val ruta: String = servidor + "Solicitudes/registrar_solicitud"
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
                                if (res.getInt("error") > 0) {
                                    val idServidor = res.getInt("error")
                                    actualizarEstadoSolicitud(context, idSolicitud, idServidor)
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

    //Funcion para convertir la solicitud en JSON
    private fun convertirSolicitudJSON(context: Context, idSolicitud: Int) : JsonObject{
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val puntoVenta = preferences.getString("puntoVenta","").toString()

        val solicitud = obtenerEncabezadoSolicitud(context, idSolicitud)

        val json = JsonObject()
        json.addProperty("id_empleado", solicitud!!.id_empleado)
        json.addProperty("empleado", solicitud.empleado)
        json.addProperty("fecha", solicitud.fecha)
        json.addProperty("punto_venta", puntoVenta)
        json.addProperty("id_ruta", solicitud.id_ruta)
        json.addProperty("ruta", solicitud.ruta)
        json.addProperty("idServidor", solicitud.idServidor)

        val detalle = JsonArray()
        for(i in 0..<solicitud.detalle!!.size){
            val data = solicitud.detalle!![i]
            val d = JsonObject()

            d.addProperty("id", data.id)
            d.addProperty("id_solicitud_carga", data.idSolicitudCarga)
            d.addProperty("id_producto", data.idProducto)
            d.addProperty("codigo_producto", data.codigoProducto)
            d.addProperty("descripcion", data.descripcion)
            d.addProperty("cantidad", data.cantidad)
            d.addProperty("fraccion", data.fraccion)
            d.addProperty("costo", data.costo)
            d.addProperty("costo_iva", data.costoIva)
            d.addProperty("precio_u", data.precio)
            d.addProperty("precio_u_iva", data.precio_iva)
            d.addProperty("total", data.total)

            detalle.add(d)
        }

        json.add("detalle", detalle)

        return  json
    }

    //Funcion para validar una Solicitud Procesada
    suspend fun validarSolicitudProcesadaEnServidor(context: Context, idSolicitudServidor: Int) : Boolean{
        var procesada : Boolean = false
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString().toString())

        try {
            val ruta : String = servidor + "Solicitudes/verificar_solicitud/" + idSolicitudServidor.toString()
            val url = URL(ruta)

            with(withContext(Dispatchers.IO){
                url.openConnection()
            } as HttpURLConnection){
                try {
                    connectTimeout = 10000
                    requestMethod = "GET"
                    when(responseCode){
                        200 -> {
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
                                    val solicitud = res.getString("response")
                                    val numHoja = res.getInt("numHojaCarga")
                                    when(solicitud){
                                        "SOLICITUD_PROCESADA" -> {
                                            actualizarEstadoSolicitudCarga(context, idSolicitudServidor, numHoja, "PROCESADO")
                                            procesada = true
                                        }
                                        "SOLICITUD_ANULADA" -> {
                                            actualizarEstadoSolicitudCarga(context, idSolicitudServidor, numHoja, "ANULADO")
                                            procesada = true
                                        }
                                        else -> {
                                            //ESPACIO PARA LA SOLICITUD AUN EN EMITIDO
                                            procesada = false
                                        }
                                    }
                                } catch (e: Exception) {
                                    println("ERROR AL LEER LA RESPUESTA ->  $responseCode -> $responseMessage")
                                    procesada = false
                                }
                            }
                        }
                        else -> {
                            println("ERROR AL OBTENER LA RESPUESTA ->  $responseCode -> $responseMessage")
                            procesada = false
                        }
                    }
                }catch (e:Exception){
                    println("ERROR AL PROCESAR LA CONEXION CON EL SERVIDOR -> " + e.message)
                    procesada = false
                }
            }

        }catch (e:Exception){
            println("ERROR AL CONECTAR CON EL SERVIDOR -> " + e.message)
            procesada = false
        }

        return procesada
    }

    //Funcion para actualizar el numero y estado de la Solicitud de Carga
    private fun actualizarEstadoSolicitudCarga(context: Context, idSolicitudServidor: Int, numeroHoja: Int, estado: String){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE solicitudCarga SET NumHoja = $numeroHoja, Estado = '$estado' WHERE idServidor = $idSolicitudServidor")
        }catch (e:Exception){
            println("ERROR AL ACTUALIZAR EL ESTADO Y EL NUMERO DE LA SOLICITUD DE CARGA -> " + e.message)
        }
    }

    //Funcion para eliminar un producto de la solicitud en el servidor
    suspend fun eliminarProductoEnSolicitudServidor(context: Context, idSolicitudServidor: Int, codigoProducto: String) : Boolean{
        var eliminado : Boolean = false
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString().toString())

        try {
            val ruta: String = servidor + "Solicitudes/eliminar_producto_solicitud/" + idSolicitudServidor.toString()  +"/" +  codigoProducto.toString()
            val url = URL(ruta)

            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 10000
                    requestMethod = "DELETE"
                    when(responseCode){
                        200 -> {
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
                                    if (res.getInt("error") == 0) {
                                        eliminado = true
                                    }
                                } catch (e: Exception) {
                                    println("ERROR DE LECTURA EN LA RESPUESTA 201 " + e.message)
                                    eliminado = false
                                }
                            }
                        }
                        else -> {
                            println("ERRR AL ELIMINAR EL PRODUCTO EN LA SOLICITUD -> $responseCode")
                            eliminado = false
                        }
                    }
                } catch (e: Exception) {
                    println("INESTABILIDAD DE CONEXION")
                    eliminado = false
                }
            }
        }catch (e:Exception){
            println("ERROR AL CONECTAR AL SERVIDOR PARA ELIMINAR EL PRODUCTO -> " + e.message)
            eliminado = false
        }

        return  eliminado
    }

    //Funcion para actualizar un producto de la solicitud en el servidor
    suspend fun actualizarProductoEnSolicitudServidor(context: Context, item: SolicitudCargaDetalle) : Boolean{
        var actualizado : Boolean = false
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString().toString())
        val detalleJson = convertirDetalleSolicitudJSON(context, item)

        try {
            val objecto =
                Gson().toJson(detalleJson)

            val ruta: String = servidor + "Solicitudes/actualizar_producto_solicitud"
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
                    requestMethod = "PUT"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto)
                    or.flush()
                    if (responseCode == 200) {
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
                                if (res.getInt("error") == 0) {
                                    actualizado = true
                                }
                            } catch (e: Exception) {
                                println("ERROR DE LECTURA EN LA RESPUESTA 201 " + e.message)
                                actualizado = false
                            }
                        }
                    }else {
                        println("ERROR NO SE LOGRO ACTUALIZAR EL PRODUCTO EN EL SERVIDOR -> $responseCode -> $responseMessage")
                        actualizado = false
                    }

                } catch (e: Exception) {
                    println("INESTABILIDAD DE CONEXION")
                    actualizado = false
                }
            }
        } catch (e: Exception) {
            println("PROBLEMAS DE CONEXION CON EL SERVIDOR")
            actualizado = false
        }

        return actualizado
    }

    //Funcion convertir detalle solicitud del producto en JSON para enviar al servidor
    private fun convertirDetalleSolicitudJSON(context: Context, item: SolicitudCargaDetalle) : JsonObject{
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val json = JsonObject()

        json.addProperty("id", item.id)
        json.addProperty("id_solicitud_carga", item.idSolicitudCarga)
        json.addProperty("id_producto", item.idProducto)
        json.addProperty("codigo_producto", item.codigoProducto)
        json.addProperty("descripcion", item.descripcion)
        json.addProperty("cantidad", item.cantidad)
        json.addProperty("fraccion", item.fraccion)
        json.addProperty("costo", item.costo)
        json.addProperty("costo_iva", item.costoIva)
        json.addProperty("precio_u", item.precio)
        json.addProperty("precio_u_iva", item.precio_iva)
        json.addProperty("total", item.total)

        return json
    }

    //ACTUALIZAR ESTADO DEL DETALLE A ENVIADO
    fun actualizarEstadoAlDetalle(context: Context, idSolicitud: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE solicitudCargaDetalle SET Enviado = 1 WHERE Id_solicitud_carga = $idSolicitud")
        }catch (e:Exception){
            println("ERROR AL ACTUALIZAR EL ESTADO DEL DETALLE -> " + e.message)
        }
    }

    //FUNCION PARA ACTUALIZAR EL ESTADO GUARDADO DE LA SOLICITUD DE CARGA CUANDO NO ES ENVIADA O DA ERROR
    fun actualizarEstadoGuardado(context: Context, idSolicitud: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE solicitudCarga SET Guardado = 1 WHERE Id = $idSolicitud")
        }catch (e:Exception){
            println("ERROR AL ACTUALIZAR EL ESTADO GUARDADO DE LA SOLICITUD -> " + e.message)
        }
    }

    //Funcion actualizar ruta de solicitud
    fun actualizarRuta(context: Context, idRuta: Int, ruta: String, idSolicitud: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE solicitudCarga SET Id_ruta = $idRuta, ruta = '$ruta' WHERE id = $idSolicitud")
        }catch (e:Exception){
            println("ERROR AL ACTUALIZAR LA RUTA DE LA SOLICITUD -> ${e.message}")
        }
    }

    //FUNCION PARA ELIMINAR UNA SOLICITUD Y SU DETALLE
    fun eliminarSolicitud(context: Context, id: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("DELETE FROM solicitudCarga WHERE id=$id")
            bd.execSQL("DELETE FROM solicitudCargaDetalle WHERE Id_solicitud_carga=$id")
        }catch (e:Exception){
            println("ERROR AL ELIMINAR LA NUEVA SOLICITUD -> ${e.message}")
        }
    }

    //FUNCION PARA OBTENER EL ENCABEZADO DE LA SOLICITUD
    private fun obtenerEncabezadoSolicitud(context: Context, idSolicitud: Int): SolicitudCargaDTO? {

        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var solicitud : SolicitudCargaDTO? = null
        try {
            val consulta = "SELECT * FROM solicitudCarga WHERE Id = $idSolicitud"
            val cursor = bd.query(consulta)
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    solicitud = SolicitudCargaDTO(
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        "",
                        cursor.getInt(6),
                        cursor.getString(7),
                        cursor.getInt(5),
                        null
                    )
                    val cdetalle = obtenerDetalleSolicitudNoEnviado(context, idSolicitud)
                    solicitud!!.detalle = cdetalle
                }
            }
        }catch (e: Exception){
            println("ERROR AL OBTENER EL ENCABEZADO DE LA SOLICITUDA -> ${e.message}")
        }
        return solicitud
    }

    //FUNCION PARA MOSTRAR EL DETALLE DE LA SOLICITUD NO ENVIADO AL SERVIDOR
    fun obtenerDetalleSolicitudNoEnviado(context: Context, idSolicitud : Int) : ArrayList<SolicitudCargaDetalle> {
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val detalleSolicitud = ArrayList<SolicitudCargaDetalle>()
        try{
            val consulta = "SELECT * FROM solicitudCargaDetalle WHERE Id_solicitud_carga = $idSolicitud AND Enviado = 0"
            val cursor = bd.query(consulta)
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    do {
                        val detalle = SolicitudCargaDetalle(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getInt(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getFloat(5),
                            0f,
                            cursor.getFloat(6),
                            cursor.getFloat(7),
                            cursor.getFloat(8),
                            cursor.getFloat(9),
                            cursor.getFloat(10),
                            cursor.getInt(11)
                        )
                        detalleSolicitud.add(detalle)
                    }while (cursor.moveToNext())
                }
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL DETALLE DE LA SOLICITUD -> ${e.message}")
        }
        return detalleSolicitud
    }

    //FUNCION PARA MOSTRAR EL DETALLE DE LA SOLICITUD
    fun obtenerDetalleSolicitud(context: Context, idSolicitud : Int) : ArrayList<SolicitudCargaDetalle> {
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val detalleSolicitud = ArrayList<SolicitudCargaDetalle>()
        try{
            val consulta = "SELECT * FROM solicitudCargaDetalle WHERE Id_solicitud_carga = $idSolicitud"
            val cursor = bd.query(consulta)
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    do {
                        val detalle = SolicitudCargaDetalle(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getInt(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getFloat(5),
                            0f,
                            cursor.getFloat(6),
                            cursor.getFloat(7),
                            cursor.getFloat(8),
                            cursor.getFloat(9),
                            cursor.getFloat(10),
                            cursor.getInt(11)
                        )
                        detalleSolicitud.add(detalle)
                    }while (cursor.moveToNext())
                }
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL DETALLE DE LA SOLICITUD -> ${e.message}")
        }
        return detalleSolicitud
    }

    //FUNCION PARA ACTUALIZAR EL ESTADO DE LA SOLICITUD
    private fun actualizarEstadoSolicitud(context: Context, idSolicitud : Int, idServidor : Int) : Boolean{
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        var actualizado : Boolean = false

        try {
            bd.execSQL("UPDATE solicitudCarga SET enviado = 1, idServidor = $idServidor WHERE id = $idSolicitud")
            actualizado = true
        }catch (e:Exception){
            actualizado = false
            println("ERROR AL ACTUALIZAR EL ESTADO DE LA SOLICITUD -> ${e.message}")
        }
        return actualizado
    }

    //FUNCION PARA OBTENER EL LISTADO DE SOLICITUDES
    fun obtenerListadosolicitudes(context: Context) : ArrayList<SolicitudCarga>{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val listaSolicitud = ArrayList<SolicitudCarga>()
        try {
            val cursor = bd.query("SELECT * FROM solicitudCarga ORDER BY id DESC LIMIT 20")
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    do {
                        val listado = SolicitudCarga(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getInt(4),
                            cursor.getInt(5),
                            cursor.getInt(6),
                            cursor.getString(7),
                            cursor.getString(8),
                            cursor.getFloat(9),
                            cursor.getInt(10)
                        )
                        listaSolicitud.add(listado)
                    }while (cursor.moveToNext())
                }
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL LISTADO DE SOLICITUDES -> ${e.message}")
        }
        return listaSolicitud
    }

    //FUNCION PARA OBTENER LA SOLICITUD POR ID
    fun obtenerSolicitudCargaPorId(context: Context, idSolicitud: Int) : SolicitudCarga?{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var item : SolicitudCarga? = null

        try {
            val consulta = "SELECT * FROM solicitudCarga WHERE Id = $idSolicitud"
            val cursor = bd.query(consulta)
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    item = SolicitudCarga(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getFloat(9),
                        cursor.getInt(10)
                    )
                }
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER LA SOLICITUD DE CARGA POR ID -> " + e.message)
        }
        return item
    }
}