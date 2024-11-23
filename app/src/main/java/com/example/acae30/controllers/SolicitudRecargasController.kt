package com.example.acae30.controllers

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.AlertDialogo
import com.example.acae30.Funciones
import com.example.acae30.modelos.Inventario
import com.example.acae30.modelos.SolicitudCarga.SolicitudCarga
import com.example.acae30.modelos.SolicitudCarga.SolicitudCargaDetalle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

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
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd.execSQL("DELETE FROM inventario_solicitud_carga")
        }catch (e:Exception){
            println("ERROR AL LIMPIAR LA TBL INVENTARIO SOLICITUD CARGA -> ${e.message}")
        }finally {
            bd.close()
        }
    }

    //FUNCION PARA ALMACENAR EL INVENTARIO EN SQLITE
    private fun almacenarInventarioEnSQLite(json: JSONArray, context: Context) {
        val bd = funciones.getDataBase(context).writableDatabase

        try {
            bd.beginTransaction()
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)

                val data = ContentValues()
                data.put("Id", dato.getInt("id"))
                data.put("Codigo", funciones.validateJsonIsnullString(dato, "codigo"))
                data.put("codigo_de_barra", funciones.validateJsonIsnullString(dato, "codigo_de_barra"))
                data.put("Tipo", funciones.validateJsonIsnullString(dato, "tipo"))
                data.put("Id_linea", funciones.validateJsonIsNullInt(dato, "id_linea"))
                data.put("Linea", funciones.validateJsonIsnullString(dato, "linea"))
                data.put("Descripcion", funciones.validateJsonIsnullString(dato, "descripcion"))
                data.put(
                    "Unidad_medida",
                    funciones.validateJsonIsnullString(dato, "unidad_medida")
                )
                data.put("Fraccion", funciones.validateJsonIsNullFloat(dato, "fraccion"))
                data.put(
                    "Nombre_fraccion", funciones.validateJsonIsnullString(
                        dato,
                        "nombre_fraccion"
                    )
                )
                data.put("Existencia", funciones.validateJsonIsNullFloat(dato, "existencia"))

                data.put("Costo", funciones.validateJsonIsNullFloat(dato, "costo"))
                data.put("costo_iva", funciones.validateJsonIsNullFloat(dato, "costo_iva"))
                data.put(
                    "Precio_oferta",
                    funciones.validateJsonIsNullFloat(dato, "precio_oferta")
                )
                data.put("Precio_iva", funciones.validateJsonIsNullFloat(dato, "precio_iva"))
                data.put("Precio_u", funciones.validateJsonIsNullFloat(dato, "precio_u"))
                data.put("Precio_u_iva", funciones.validateJsonIsNullFloat(dato, "precio_u_iva"))
                data.put("Precio", funciones.validateJsonIsNullFloat(dato, "precio"))
                data.put("Status", funciones.validateJsonIsnullString(dato, "status"))
                data.put("Id_productor", funciones.validateJsonIsNullInt(dato, "id_productor"))
                data.put("Productor", funciones.validateJsonIsnullString(dato, "productor"))
                data.put("Id_proveedor", funciones.validateJsonIsNullInt(dato, "id_proveedor"))
                data.put("Proveedor", funciones.validateJsonIsnullString(dato, "proveedor"))
                data.put("Cesc", "N")
                data.put("Combustible", "N")
                data.put("Imagen", "")
                data.put("Rubro", funciones.validateJsonIsnullString(dato, "rubro"))
                data.put("Marca", funciones.validateJsonIsnullString(dato, "marca"))
                data.put("Sublinea", funciones.validateJsonIsnullString(dato, "sublinea"))
                data.put("Bonificado", funciones.validateJsonIsNullFloat(dato, "bonificado"))
                data.put(
                    "Desc_automatico", funciones.validateJsonIsNullFloat(
                        dato,
                        "desc_automatico"
                    )
                )
                data.put("Id_sublinea", funciones.validateJsonIsNullInt(dato, "id_sublinea"))
                data.put("Id_rubro", funciones.validateJsonIsNullInt(dato, "id_rubro"))
                data.put("Existencia_u", funciones.validateJsonIsNullFloat(dato, "existencia_u"))
                bd.insert("inventario_solicitud_carga", null, data)
            }
            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR AL ALMACENAR EL INVENTARIO DE SOLICUTD EN LA TABLA" + e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

    //FUNCION PARA OBTENER LA INFORMACION DEL PRODUCTO POR CODIGO O POR NOMBRE
    fun obtenerInformacionProductoPorString(context: Context, busqueda: String): ArrayList<Inventario>{
        val base = funciones.getDataBase(context).readableDatabase
        val lista = ArrayList<Inventario>()
        var query: String = ""

        query = if(busqueda != ""){
            "SELECT * FROM inventario_solicitud_carga WHERE Descripcion LIKE '%$busqueda%' LIMIT 60"
        }else{
            "SELECT * FROM inventario_solicitud_carga limit 60"
        }

        try {
            val cursor = base.rawQuery(query, null)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val arreglo = Inventario(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getFloat(7),
                        cursor.getString(8),
                        cursor.getInt(9),
                        cursor.getFloat(10),
                        cursor.getFloat(11),
                        cursor.getFloat(12),
                        cursor.getFloat(13),
                        cursor.getFloat(14),
                        cursor.getFloat(15),
                        cursor.getFloat(16),
                        cursor.getString(17),
                        cursor.getString(18),
                        cursor.getInt(19),
                        cursor.getString(20),
                        cursor.getInt(21),
                        cursor.getString(22),
                        cursor.getString(23),
                        cursor.getString(24),
                        cursor.getString(25),
                        cursor.getString(26),
                        cursor.getString(27),
                        cursor.getInt(28),
                        cursor.getString(29),
                        cursor.getFloat(30),
                        cursor.getDouble(31),
                        cursor.getInt(32),
                        cursor.getFloat(33),
                        cursor.getString(34)
                    )
                    lista.add(arreglo)
                } while (cursor.moveToNext())
                cursor.close()
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL REALIZAR LA BUSQUEDA EN INVENTARIO -> ${e.message}")
        }finally {
            base.close()
        }
        return lista
    }

    //FUNCION PARA CREAR UNA NUEVA SOLICITUD
    fun guardarNuevaSolicitud(context: Context , solicitud: SolicitudCarga) : Int{
        val bd = funciones.getDataBase(context).writableDatabase
        var idSolicitud : Int = 0
        try {
            bd.beginTransaction()
            val data = ContentValues()
            data.put("Id_Empleado", solicitud.idEmpleado)
            data.put("Empleado", solicitud.empleado)
            data.put("Fecha", solicitud.fecha.toString())
            idSolicitud = bd.insert("solicitudCarga", null, data).toInt()
            bd.setTransactionSuccessful()
        }catch (e:Exception){
            println("ERROR AL INGRESAR LA NUEVA SOLICITUD -> ${e.message}")
        }finally {
            bd!!.endTransaction()
            bd.close()
        }
        return  idSolicitud
    }

    //FUNCION PARA ELIMINAR UNA SOLICITUD Y SU DETALLE
    fun eliminarSolicitud(context: Context, id: Int){
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd.execSQL("DELETE FROM solicitudCarga WHERE id=$id")
            bd.execSQL("DELETE FROM solicitudCargaDetalle WHERE Id_solicitud_carga=$id")
        }catch (e:Exception){
            println("ERROR AL ELIMINAR LA NUEVA SOLICITUD -> ${e.message}")
        }finally {
            bd.close()
        }
    }

    //FUNCION PARA INSERTAR EL DETALLE DE UNA SOLICITUD
    fun insertarDetalleSolicitud(context: Context, detalle : SolicitudCargaDetalle) : Boolean{
        var registrado : Boolean = false
        val bd = funciones.getDataBase(context).writableDatabase
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
            bd.insert("solicitudCargaDetalle", null, data)
            bd.setTransactionSuccessful()
            registrado = true
        }catch (e:Exception){
            println("ERROR AL INSERTAR EL DETALLE DE LA SOLICITUD -> ${e.message}")
            registrado = false
        }finally {
            bd.endTransaction()
            bd.close()
        }
        return registrado
    }

    //FUNCION PARA MOSTRAR EL DETALLE DE LA SOLICITUD
    fun obtenerDetalleSolicitud(context: Context, idSolicitud : Int) : ArrayList<SolicitudCargaDetalle>{
        val bd = funciones.getDataBase(context).readableDatabase
        val detalleSolicitud = ArrayList<SolicitudCargaDetalle>()
        try{
            val cursor = bd.rawQuery("SELECT * FROM solicitudCargaDetalle WHERE Id_solicitud_carga = $idSolicitud", null)
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
                        cursor.getFloat(6),
                        cursor.getFloat(7),
                        cursor.getFloat(8),
                        cursor.getFloat(9),
                        cursor.getFloat(10)
                    )
                    detalleSolicitud.add(detalle)
                }while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL DETALLE DE LA SOLICITUD -> ${e.message}")
        }finally {
            bd.close()
        }
        return detalleSolicitud
    }

    //FUNCION PARA ACTUALIZAR EL ESTADO DE LA SOLICITUD
    fun actualizarEstadoSolicitud(context: Context, idSolicitud : Int) : Boolean{
        val bd = funciones.getDataBase(context).writableDatabase
        var actualizado : Boolean = false

        try {
            bd.execSQL("UPDATE solicitudCarga SET enviado = 1 WHERE id = $idSolicitud")
            actualizado = true
        }catch (e:Exception){
            actualizado = false
            println("ERROR AL ACTUALIZAR EL ESTADO DE LA SOLICITUD -> ${e.message}")
        }finally {
            bd.close()
        }
        return actualizado
    }

    //FUNCION PARA OBTENER EL LISTADO DE SOLICITUDES
    fun obtenerListadosolicitudes(context: Context) : ArrayList<SolicitudCarga>{
        val bd = funciones.getDataBase(context).readableDatabase
        val listaSolicitud = ArrayList<SolicitudCarga>()
        try {
            val cursor = bd.rawQuery("SELECT * FROM solicitudCarga WHERE enviado = 1 ORDER BY id DESC", null)
            if(cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val listado = SolicitudCarga(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5)
                    )
                    listaSolicitud.add(listado)
                }while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL LISTADO DE SOLICITUDES -> ${e.message}")
        }finally {
            bd.close()
        }
        return listaSolicitud
    }

}