package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.view.View
import android.widget.Toast
import androidx.core.content.contentValuesOf
import com.example.acae30.Funciones
import com.example.acae30.listas.InventarioRetrofit
import com.example.acae30.modelos.Inventario
import com.example.acae30.modelos.InventarioPrecios
import com.example.acae30.modelos.JSONmodels.HojaCargaJSON
import com.example.acae30.modelos.JSONmodels.HojaRecargasJSON
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import androidx.core.content.edit
import com.google.gson.JsonArray

class InventarioController {

    private var funciones = Funciones()
    private var hojaController = HojaCargaController()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    //FUNCION PARA OBTENER INFORMACION DEL PRODUCTO POR ID
    fun obtenerInformacionProductoPorId(context: Context ,idInventario: Int, facExpo: Boolean): Inventario?{
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var datos: Inventario? = null
        try {
            val consulta = "SELECT * FROM inventario WHERE Id=$idInventario"
            val cursor = base.query(consulta)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                if(facExpo){
                    //FACTURA DE EXPORTACION ACTIVA
                    datos = Inventario(
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
                        cursor.getFloat(14)/1.13f,
                        cursor.getFloat(15)/1.13f,
                        cursor.getFloat(16),
                        cursor.getString(27),
                        cursor.getFloat(18),
                        cursor.getFloat(13),
                        cursor.getString(2)
                    )
                }else{
                    datos = Inventario(
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
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR: DETALLE DEL PRODUCTO -> ${e.message}")
        }
        return datos
    }

    //FUNCION PARA OBTENER LAS ESCALAS DE PRECIO POR PRODUCTO
    fun obtenerEscalaPrecios(context: Context, idInventario: Int, facExpo: Boolean, unidadMedida: String): ArrayList<InventarioPrecios>{
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val listaEscalas = ArrayList<InventarioPrecios>()

        try {
            val consulta = "SELECT * FROM Inventario_precios WHERE id_inventario = '$idInventario' AND unidad='${unidadMedida.trim()}'"
            val cursor = base.query(consulta)
            if (cursor.count > 0) {
                cursor.moveToFirst()

                if(facExpo){
                    //ACTIVANDO FACTURA DE EXPORTACION
                    do {
                        val escalas = InventarioPrecios(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getFloat(5),
                            cursor.getString(6),
                            cursor.getFloat(7),
                            cursor.getFloat(8),
                            cursor.getFloat(9)/1.13f,
                            cursor.getFloat(9),
                            cursor.getInt(11)
                        )
                        listaEscalas.add(escalas)
                    } while (cursor.moveToNext())
                }else{
                    //SIN FACTURA DE EXPORTACION
                    do {
                        val escalas = InventarioPrecios(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getFloat(5),
                            cursor.getString(6),
                            cursor.getFloat(7),
                            cursor.getFloat(8),
                            cursor.getFloat(9),
                            cursor.getFloat(10),
                            cursor.getInt(11)
                        )
                        listaEscalas.add(escalas)
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR: OBTENER ESCALAS DE PRECIOS -> ${e.message}")
        }
        return listaEscalas
    }

    //FUNCION PARA OBTENER LA INFORMACION DEL PRODUCTO POR CODIGO O POR NOMBRE
    fun obtenerInformacionProductoPorString(context: Context, busqueda: String, vista:String): ArrayList<Inventario>{
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val lista = ArrayList<Inventario>()

        val query: String = when (vista) {
            "devolucion" -> {
                if (busqueda.isNotEmpty()) {
                    """
            SELECT * FROM inventario 
            WHERE Existencia > 0 
              AND (Descripcion LIKE '%' || ? || '%' OR Codigo LIKE '%' || ? || '%')
            """
                } else {
                    "SELECT * FROM inventario WHERE Existencia > 0 LIMIT 60"
                }
            }
            else -> {
                if (busqueda.isNotEmpty()) {
                    """
            SELECT * FROM inventario 
            WHERE Descripcion LIKE '%' || ? || '%' OR Codigo LIKE '%' || ? || '%'
            """
                } else {
                    "SELECT * FROM inventario LIMIT 60"
                }
            }
        }


        try {
            val cursor = if (busqueda.isNotEmpty()) {
                base.query(query, arrayOf(busqueda, busqueda))
            } else {
                base.query(query)
            }
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

    //FUNCION PARA OBTENER LA FECHA DEL INVENTARIO
    fun obtenerFechaInventario(context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        try {
            var fechaInventario: String = "NULL"
            val consulta = base.query("SELECT Fecha_inventario FROM inventario LIMIT 1")

            if(consulta.count > 0){
                consulta.moveToFirst()
                fechaInventario = consulta.getString(0).toString()
            }

            preferences.edit {
                putString("fechaInventario", fechaInventario)
            }

            consulta.close()
        }catch (e:Exception){
            print("ERROR: ${e.message}")
        }
    }

    //FUNCION PARA ALMACENAR LOS PRECIOS EN LA BASE DE DATOS
    fun saveInventarioPreciosDatabase(json: JSONArray, context: Context) {
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.beginTransaction()
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val data = ContentValues()
                data.put("Id", dato.getInt("id"))
                data.put(
                    "id_inventario",
                    funciones.validateJsonIsnullString(dato, "id_inventario")
                )
                data.put(
                    "Codigo_producto",
                    funciones.validateJsonIsnullString(dato, "codigo_producto")
                )
                data.put(
                    "Id_inventario_unidad",
                    funciones.validateJsonIsNullInt(dato, "id_inventario_unidad")
                )
                data.put("Unidad", funciones.validateJsonIsnullString(dato, "unidad"))
                data.put("Nombre", funciones.validateJsonIsnullString(dato, "nombre"))
                data.put("Terminos", funciones.validateJsonIsnullString(dato, "terminos"))
                data.put("Plazo", funciones.validateJsonIsNullFloat(dato, "Plazo"))
                data.put("cantidad", funciones.validateJsonIsNullFloat(dato, "cantidad"))
                data.put("porcentaje", funciones.validateJsonIsNullFloat(dato, "porcentaje"))
                data.put("precio", funciones.validateJsonIsNullFloat(dato, "precio"))
                data.put("precio_iva", funciones.validateJsonIsNullFloat(dato, "precio_iva"))

                bd.insert("inventario_precios", SQLiteDatabase.CONFLICT_REPLACE, data)
            }
            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd.endTransaction()
        }
    }

    //FUNCIONES PARA HOJA DE CARGA
    //FUNCION PARA OBTENER EL INVENTARIO DESDE LA HOJA DE CARGA DE ESCARRSA
    suspend fun obtenerInventarioHojaCarga(id: Int,  numero: Int, id_vendedor: Int, context: Context) {

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        //OBTENIENDO HOJA DE CARGA ACTIVA
        val hojaCargaActiva = preferences.getInt("hojaCarga", 0)

        //OBTENIENDO FECHA
        val fecha = funciones.obtenerFecha()

        try {
            val datos = HojaCargaJSON(
                id,
                numero,
                id_vendedor,
                fecha!!
            )
            val objecto =
                Gson().toJson(datos)
            println(objecto)
            val ruta: String = servidor + "inventario/hojacarga"
            val url = URL(ruta)
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 20000
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
                            BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                                try {
                                    val respuesta = StringBuffer()
                                    var inpuline = it.readLine()
                                    while (inpuline != null) {
                                        respuesta.append(inpuline)
                                        inpuline = it.readLine()
                                    }
                                    it.close()
                                    val res = JSONArray(respuesta.toString())
                                    if (res.length() > 0) {

                                        //VERIFICANDO SI LA HOJA CORRESPONDE AL MISMO DIA
                                        if(!verificarFechaInventario(context) || numero != hojaCargaActiva){

                                            //LIMPIANDO INVENTARIO YA QUE NO CORRESPONDE AL MISMO DIA U HOJA DE CARGA
                                            limpiarInventarioHojaCarga(context) //LIMPIAR LAS TABLAS

                                            //INSERTANDO INFORMACION EN TABLA DE INVENTARIO Y PRIMERA HOJA DE CARGA
                                            //ALMACENANDO INVENTARIO NUEVO
                                            saveInventarioDatabase(res, context, numero,0)

                                        }else{

                                            //AQUI SE CARGAR SOLO LAS EXISTENCIA DE ACUERDO A LA HOJA DE CARGA
                                            //INSERTANDO INSERTANDO INFORMACION SOLO EN HOJA DE CARGA Y DETALLE
                                            //ALMACENANDO INVENTARIO NUEVO
                                            insertarHojaDeCargar(res, context, numero)
                                        }

                                    } else {
                                        //println("ERROR: ERROR NO SE ENCONTRARON DATOS PARA ALMACENAR 222222")
                                        withContext(Dispatchers.Main){
                                            funciones.mensaje(context, "ERROR: NO SE ENCONTRO LA HOJA DE CARGA")
                                        }
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }
                        400 -> {
                            println("ERROR: ERROR AL CARGAR EL INVENTARIO POR HOJA DE CARGA")
                        }

                        404 -> {
                            withContext(Dispatchers.Main){
                                funciones.mensaje(context, "ERROR: NO SE ENCONTRO LA HOJA DE CARGA")
                            }
                        }

                        else -> {
                            println("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main){
                        funciones.mensaje(context, "ERROR -> " + e.message)
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main){
                funciones.mensaje(context, "ERROR EN LA CONEXION CON EL SERVIDOR -> " + e.message)
            }
        }
    }

    //FUNCION PARA ALMACENAR EL INVENTARIO EN SQLITE
    private fun saveInventarioDatabase(json: JSONArray, context: Context, numeroHojaCarga:Int, recarga: Int) {
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        var idHojaCarga : Int = 0
        var idRutaHojaCarga : Int = 0
        var rutaHojaCarga : String = ""

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
                data.put(
                    "unidad_medida",
                    funciones.validateJsonIsnullString(dato, "unidad_medida")
                )
                data.put("fraccion", funciones.validateJsonIsNullFloat(dato, "fraccion"))
                data.put(
                    "nombre_fraccion", funciones.validateJsonIsnullString(
                        dato,
                        "nombre_fraccion"
                    )
                )
                data.put("costo", funciones.validateJsonIsNullFloat(dato, "costo"))
                data.put("costo_iva", funciones.validateJsonIsNullFloat(dato, "costo_iva"))
                data.put("ult_costo", funciones.validateJsonIsNullFloat(dato, "ult_costo"))
                data.put("ult_costo_iva", funciones.validateJsonIsNullFloat(dato, "ult_costo_iva"))
                data.put("existencia", 0)
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
                data.put("fecha_inventario", LocalDate.now().toString())

                idHojaCarga = funciones.validateJsonIsNullInt(dato, "idHojaCarga")
                idRutaHojaCarga = funciones.validateJsonIsNullInt(dato, "idRuta")
                rutaHojaCarga = funciones.validateJsonIsnullString(dato, "ruta")

                bd.insert("inventario", SQLiteDatabase.CONFLICT_REPLACE, data)
            }

            //ALAMACENANDO EN SHARED PREFERENCES EL ID DE LA HOJA DE CARGA ACTIVA
            preferences.edit {
                putInt("idHojaCarga", idHojaCarga)
                putInt("idRutaHojaCarga", idRutaHojaCarga)
                putString("rutaHojaCarga", rutaHojaCarga)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd.endTransaction()
            if(recarga == 0){
                CoroutineScope(Dispatchers.IO).launch {
                    insertarHojaDeCargar(json, context, numeroHojaCarga)
                }
            }
        }
    }

    //DESCARGA DE INVENTARIO DE HOJA DE CARGA
    fun descargarProductosInventario(idPedido: Int, context: Context){
        val base = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            val consulta = "SELECT ID_PRODUCTO, (CANTIDAD + BONIFICADO) AS CANTIDAD, Unidad FROM DETALLE_PEDIDOS WHERE ID_PEDIDO=$idPedido"
            val cursor = base.query(consulta)
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    try {

                        base.execSQL("UPDATE Inventario SET Existencia = (Existencia - ${cursor.getInt(1)}) WHERE Id=${cursor.getInt(0)}")

                        //DESCARGA DE INVENTARIO PARA HOJAS DE CARGA CON FRACCIONES
                        /*when(cursor.getString(2)){
                            "UNI" -> descargarUnidades(context, cursor.getInt(0), cursor.getInt(1))
                            "FRA" -> descargarFracciones(context, cursor.getInt(0), cursor.getInt(1))
                            else -> descargarUnidadesMedida(context,cursor.getInt(0), cursor.getInt(1), cursor.getString(2))
                        }*/
                    }catch (e:Exception){
                        println("ERROR: NO SE ACTUALIZARON LAS EXITENCIAS EN INVENTARIO -> ${e.message}")
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR: NO SE ENCONTRARON REGISTROS EN EL PEDIDO -> ${e.message}")
        }
    }

    //FUNCION PARA DESCARGAR UNIDADES
    /*private fun descargarUnidades(context: Context, idProducto: Int, cantidad: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE Inventario SET Existencia = (Existencia - $cantidad) WHERE Id=$idProducto")
        }catch (e: Exception){
            println("ERROR NO SE PUEDE ACTUALIZAR LA EXISTENCIA DEL PRODUCTO -> " + e.message)
        }
    }*/

    //FUNCION PARA DESCARGAR FRACCIONES
    /*private fun descargarFracciones(context: Context, idProducto: Int, cantidad: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try{
            val sql = "SELECT Existencia, Existencia_u, Fraccion FROM inventario WHERE id = $idProducto"
            val cursor = bd.query(sql)

            var existenciaActual : Int = 0
            var existenciaUActual : Int = 0
            var fraccionamiento : Int = 0
            var totalFraccionesActuales : Int = 0


            var existenciaFinal : Int = 0
            var existenciaUFinal : Int = 0
            var totalFraccionesFinal : Int = 0

            if(cursor.count > 0){
                cursor.moveToFirst()
                existenciaActual = cursor.getInt(0)
                existenciaUActual = cursor.getInt(1)
                fraccionamiento = cursor.getInt(2)
            }
            cursor.close()

            //CALCULANDO EL TOTAL DE FACCIONES ACTUAL EN INVENTARIO
            totalFraccionesActuales = (existenciaActual * fraccionamiento) + existenciaUActual

            totalFraccionesFinal = totalFraccionesActuales - cantidad

            existenciaFinal = totalFraccionesFinal / fraccionamiento   // cuántas unidades completas quedan
            existenciaUFinal = totalFraccionesFinal % fraccionamiento  // fracciones restantes

            bd.execSQL("UPDATE inventario SET Existencia = $existenciaFinal, Existencia_u = $existenciaUFinal WHERE Id = $idProducto")

        }catch (e: Exception){
            println("ERROR NO SE PUEDE ACTUALIZAR LA EXISTENCIA EN FRACCION DEL PRODUCTO -> " + e.message)
        }
    }*/

    //FUNCION PARA VALIDAR DESCARGA DE UNIDADES DE MEDIDA
    /*private fun descargarUnidadesMedida(context: Context, idProducto: Int, cantidad: Int, unidadMedida: String){

        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        try {
            //SELECCIONANDO LA UNIDAD DE MEDIDA
            val sql = "SELECT Equivale, Unidades FROM inventario_unidades WHERE id_inventario = $idProducto AND Nombre_unidad = '$unidadMedida'"
            val cursor = bd.query(sql)

            var cantidadDescargar: Int = 0

            if(cursor.count > 0){
                cursor.moveToFirst()
                cantidadDescargar = cantidad * cursor.getInt(0)

                when(cursor.getString(1)){
                    "UNI" -> descargarUnidades(context, idProducto, cantidadDescargar)
                    "FRA" -> descargarFracciones(context, idProducto, cantidadDescargar)
                }
            }
            cursor.close()

        }catch (e: Exception){
            println("ERROR NO SE PUEDE ACTUALIZAR LA EXISTENCIA DEL PRODUCTO POR UNIDAD DE MEDIDA -> " + e.message)
        }
    }*/

    //FUNCION PARA VERIFICAR FECHA DE INVENTARIO CON HOJA DE CARGA
    fun verificarFechaInventario(context: Context) : Boolean{
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val fechaActual = funciones.obtenerFecha()
        val fechaInventario = preferences.getString("fechaInventario", "NULL")

        val respuesta : Boolean = if(fechaInventario == "NULL"){
            false
        }else if(fechaActual != fechaInventario){
            false
        }else{
            true
        }

        return respuesta
    }

    //FUNCION PARA LIMPIAR TABLAS DE INVENTARIO Y HOJA DE CARGA
    private fun limpiarInventarioHojaCarga(context: Context){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("DELETE FROM Inventario")
            bd.execSQL("DELETE FROM hoja_carga")
            bd.execSQL("DELETE FROM hoja_carga_detalle")
            bd.execSQL("DELETE FROM hoja_detalle_recargas")
        }catch (e: Exception){
            throw Exception("ERROR LA ELIMINAR EL INVENTARIO -> " + e.message)
        }
    }

    //FUNCION PARA INSERTAR MAESTRO HOJA CARGA
    private fun insertarHojaDeCargar(json: JSONArray, context: Context, numeroHojaCarga:Int){

        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        val idHojaCarga = preferences.getInt("idHojaCarga", 0)
        val idRutaHojaCarga = preferences.getInt("idRutaHojaCarga", 0)
        val rutaHojaCarga = preferences.getString("rutaHojaCarga", "")
        val fechaHojaCarga = funciones.obtenerFecha()

        try {
            bd.beginTransaction()
            val data = ContentValues()
            data.put("idHojaCarga", idHojaCarga)
            data.put("numeroHoja", numeroHojaCarga)
            data.put("Fecha_registro", fechaHojaCarga)
            data.put("Id_ruta", idRutaHojaCarga)
            data.put("Ruta", rutaHojaCarga)
            bd.insert("hoja_carga", SQLiteDatabase.CONFLICT_REPLACE, data)

            bd.setTransactionSuccessful()
        }catch (e:Exception){
            throw Exception("ERROR AL INSERTAR HOJA DE CARGA MAESTRO -> " + e.message)
        }finally {
            bd.endTransaction()
            CoroutineScope(Dispatchers.IO).launch {
                insertarDetalleHojaCarga(json, context, numeroHojaCarga)
            }
        }
    }

    //FUNCION PARA INSERTAR DETALLE DE HOJA DE CARGA
    private suspend fun insertarDetalleHojaCarga(json: JSONArray, context: Context, numeroHojaCarga: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        val idHojaCarga = preferences.getInt("idHojaCarga", 0)

        try {
            bd.beginTransaction()
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)

                val data = ContentValues()
                data.put("Id_hojaCarga", idHojaCarga)
                data.put("Id_inventario", dato.getInt("id"))
                data.put("Codigo_inventario", funciones.validateJsonIsnullString(dato, "codigo"))
                data.put("Cantidad", funciones.validateJsonIsNullFloat(dato, "existencia"))
                bd.insert("hoja_carga_detalle", SQLiteDatabase.CONFLICT_REPLACE, data)

                //ACTUALIZANDO EXISTENCIAS
                CoroutineScope(Dispatchers.IO).launch {
                    actualizarExistenciasInventario(context, funciones.validateJsonIsNullFloat(dato, "existencia"), dato.getInt("id"))
                }

            }
            bd.setTransactionSuccessful()
        }catch (e:Exception){
            throw Exception("ERROR AL INSERTAR HOJA DE CARGA DETALLE -> " + e.message)
        }finally {
            bd.endTransaction()

            //OBTENIENDO FECHA DE INVENTARIO
            CoroutineScope(Dispatchers.IO).launch {
                obtenerFechaInventario(context)
            }

            preferences.edit {
                putInt("hojaCarga", numeroHojaCarga)
            }

            //funciones.mostrarMensaje("INVENTARIO CARGADO CORRECTAMENTE", context, view)
            withContext(Dispatchers.Main){
                funciones.mensaje(context, "INVENTARIO CARGADO CORRECTAMENTE")
            }
        }

    }

    //FUNCION ACTUALIZAR INVENTARIO POR HOJA DE CARGA
    fun actualizarExistenciasInventario(context: Context, cantidad:Float, id:Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE inventario SET Existencia=(Existencia + $cantidad) WHERE id=$id")
        }catch (e:Exception){
            throw Exception("ERROR AL ACTULIZAR LA EXISTENCIA DEL INVENTARIO  -> " + e.message)
        }
    }

    //ACTUALIZAR INFORMACION DE INVENTARIO
    suspend fun actualizarInventarioHojaCarga(id: Int,  numero: Int, id_vendedor: Int, context: Context, view:View) {

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())
        val fecha = funciones.obtenerFecha()

        try {

            val datos = HojaCargaJSON(id, numero, id_vendedor, fecha!!)

            val objecto = Gson().toJson(datos)

            //println(objecto)

            val ruta: String = servidor + "inventario/hojacarga"
            val url = URL(ruta)
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 20000
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
                            BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                                try {
                                    val respuesta = StringBuffer()
                                    var inpuline = it.readLine()
                                    while (inpuline != null) {
                                        respuesta.append(inpuline)
                                        inpuline = it.readLine()
                                    }

                                    it.close()

                                    val res = JSONArray(respuesta.toString())
                                    if (res.length() > 0) {

                                        try{
                                            actualizarInventarioDatabase(res, context, view)
                                        }catch (e:Exception){
                                            println("ERROR AL ACTUALIZAR EL INVENTARIO -> ${e.message}")
                                        }

                                        delay(1000)

                                        try{
                                            obtenerEscalasPrecios(context)
                                        }catch (e:Exception){
                                            println("ERROR AL ACTUALIZAR LAS ESCALAS DE PRECIO -> ${e.message}")
                                        }

                                        withContext(Dispatchers.Main){
                                            Toast.makeText(context, "INFORMACION DE INVENTARIO ACTUALIZADOS", Toast.LENGTH_SHORT).show()

                                        }

                                    } else {
                                        withContext(Dispatchers.Main){
                                            Toast.makeText(context, "ERROR: NO SE ENCONTRO LA HOJA DE CARGA", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }
                        400 -> {
                            println("ERROR: ERROR AL CARGAR EL INVENTARIO POR HOJA DE CARGA")
                        }

                        404 -> {
                            withContext(Dispatchers.Main){
                                Toast.makeText(context, "ERROR: NO SE ENCONTRO LA HOJA DE CARGA", Toast.LENGTH_SHORT).show()
                            }
                        }

                        else -> {
                            println("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main){
                        funciones.mensaje(context, "ERROR -> " + e.message)
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main){
                funciones.mensaje(context, "ERROR EN LA CONEXION CON EL SERVIDOR -> " + e.message)
            }
        }
    }

    private fun actualizarInventarioDatabase(json: JSONArray, context: Context, view:View) {
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase

        try {
            bd.beginTransaction()
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)

                val idProducto = dato.getInt("id")
                val precio = funciones.validateJsonIsNullFloat(dato, "precio")
                val precio_iva = funciones.validateJsonIsNullFloat(dato, "precio_iva")

                bd.execSQL("UPDATE inventario SET precio=$precio, precio_iva=$precio_iva WHERE id=$idProducto")
            }
            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd.endTransaction()
        }
    }

    //FUNCION PARA OBTENER DATOS EN LA TABLA DE REACARGAS DE LA HOJA DE CARGA
    suspend fun obtenerHojaRecargas(context: Context, idHojaCarga:Int, view:View){

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val datos = HojaRecargasJSON(
                idHojaCarga
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = servidor + "inventario/hojarecarga"
            val url = URL(ruta)
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 20000
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
                            BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                                try {
                                    val respuesta = StringBuffer()
                                    var inpuline = it.readLine()
                                    while (inpuline != null) {
                                        respuesta.append(inpuline)
                                        inpuline = it.readLine()
                                    }
                                    it.close()
                                    val res = JSONArray(respuesta.toString())
                                    if (res.length() > 0) {
                                        println(res)
                                        //INSERTANDO LAS RECARGAS ENCONTRADAS
                                        insertarHojaRecargas(res, context, view)
                                    } else {
                                        withContext(Dispatchers.Main){
                                            funciones.mensaje(context, "NO SE ENCONTRARON RECARGAS PARA SU HOJA")
                                        }
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }
                        400 -> {
                            println("ERROR: ERROR AL CARGAR EL INVENTARIO POR HOJA DE CARGA")
                        }

                        404 -> {
                            withContext(Dispatchers.Main){
                                funciones.mensaje(context, "NO SE ENCONTRARON RECARGAS PARA SU HOJA")
                            }
                        }

                        else -> {
                            println("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR")
                        }
                    }
                } catch (e: Exception) {
                    throw Exception("ERROR: " + e.message)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main){
                funciones.mensaje(context, "ERROR EN LA CONEXION CON EL SERVIDOR -> " + e.message)
            }
        }

    }

    //INSERTANDO EN TBL RECARGAS
    private suspend fun insertarHojaRecargas(json: JSONArray, context: Context, view:View){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        var hojaRecargada = 0

        try {

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val id = dato.getInt("id")
                val id_hoja = dato.getInt("id_hoja")
                val id_producto = dato.getInt("id_producto")
                val codigo = funciones.validateJsonIsnullString(dato, "codigo_producto")
                val cantidad = funciones.validateJsonIsNullFloat(dato, "salida")

                //refactorizando codigo
                try{

                    //Buscar el id en el inventario si no Existe se buscar en el servidor para agregarlo
                    val producto = obtenerInformacionProductoPorId(context, id_producto, false)
                    if(producto != null){
                        //existe en el inventario
                        //buscando en tbl recargas detalle
                        val idRecarga = hojaController.obtenerRecargasRealizadas(context, id)
                        if(idRecarga == 0){
                            //INSERTANDO DATOS DE RECARGA
                            insertandoInformacionRecarga(context, id, id_hoja, id_producto, codigo, cantidad)

                            hojaRecargada = 1
                        }
                    }else{
                        //NO EXISTE EN EL INVENTARIO
                        try{
                            //BUSCANDO EN EL SERVIDOR
                            val buscandoProducto = obtenerProductoPorIdServidor(id_producto, context)
                            when(buscandoProducto){
                                1 -> {
                                    //INSERTANDO DATOS DE RECARGA
                                    insertandoInformacionRecarga(context, id, id_hoja, id_producto, codigo, cantidad)
                                }
                                else -> {
                                    throw Exception("NO SE ENCONTRO EL PRODUCTO EN EL SERVIDOR")
                                }
                            }
                        }catch (e:Exception){
                            throw Exception("Error al obtener el producto por id -> " + e.message)
                        }

                        hojaRecargada = 1

                    }
                }catch (e:Exception){
                    throw Exception("Error al realizar la busqueda en las recargas")
                }
            }

            when(hojaRecargada){
                1 -> {
                    withContext(Dispatchers.Main){
                        Toast.makeText(context, "SU HOJA HA SIDO RECARGADA CORRECTAMENTE", Toast.LENGTH_SHORT).show()
                    }

                    val intento = Intent(context, com.example.acae30.Inventario::class.java)
                    context.startActivity(intento)
                }
                else -> {
                    withContext(Dispatchers.Main){
                        funciones.mensaje(context,"NO HAY RECARGAS PARA SU HOJA")
                    }
                }
            }
        }catch (e:Exception){
            throw Exception("ERROR AL INSERTAR HOJA DE CARGA DETALLE -> " + e.message)
        }
    }

    //OBTENER PRODUCTO POR ID DESDE EL SERVIDOR
    private suspend fun obtenerProductoPorIdServidor(id: Int, context: Context) : Int {
        var encontrado = 0
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {

            val ruta: String = servidor + "inventario/ObtenerProductoPorId/" + id.toString()
            val url = URL(ruta)
            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    when (responseCode) {
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
                                    val res = JSONArray(respuesta.toString())
                                    if (res.length() > 0) {
                                        //Almacenar registro en tbl inventario
                                        saveInventarioDatabase(res, context, 0,1)
                                        encontrado = 1
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }
                        404 -> {
                            withContext(Dispatchers.Main){
                                funciones.mensaje(context, "ERROR: NO SE ENCONTRO EL PRODUCTO")
                            }
                        }
                        else -> {
                            withContext(Dispatchers.Main){
                                funciones.mensaje(context, "ERROR EN LA CONEXION CON EL SERVIDOR -> ")
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main){
                        funciones.mensaje(context, "ERROR OBTENIENDO EL PRODUCTO POR ID -> " + e.message)
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main){
                funciones.mensaje(context, "ERROR EN LA CONEXION CON EL SERVIDOR -> " + e.message)
            }
        }

        return encontrado
    }

    //INSERTANDO INFORMACION DE LA RECARGA
    private fun insertandoInformacionRecarga(context: Context, id: Int, id_hoja: Int, id_producto: Int, codigo: String, cantidad: Float){
        //INSERTANDO RECARGA
        CoroutineScope(Dispatchers.IO).launch {
            hojaController.insertarRecargaProducto(context, id, id_hoja, id_producto, codigo, cantidad)
        }

        //ACTUALIZANDO EXISTENCIAS
        CoroutineScope(Dispatchers.IO).launch {
            actualizarExistenciasInventario(context, cantidad, id_producto)
        }
    }

    //FUNCION PARA OBTENER LAS ESCALAS DE PRECIOS
    suspend fun obtenerEscalasPrecios(context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        // TABLA INVENTARIO PRECIOS
        try {
            val direccionprecioscantidad = servidor + "inventario/precios/cantidad"
            val urlprecioscantidad = URL(direccionprecioscantidad)
            var cantidadPreciosRegistros = 0.toInt()
            with(withContext(Dispatchers.IO) {
                urlprecioscantidad.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { data ->
                            val readline = data.readLine()

                            cantidadPreciosRegistros = readline.toInt()
                        }

                    } else {
                        throw Exception("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                    throw Exception("Error #5 Linea 325:$responseCode")
                }
            }

            val BLOQUE_PRECIOS = 1000.toInt()

            //Log.d("Cantidad: ", cantidadRegistros!!.toString())
            var inicioPrecios = 0.toInt()
            var longitudPrecios = 0.toInt()
            var registrosPreciosCargados = 0.toInt()

            if (cantidadPreciosRegistros < BLOQUE_PRECIOS) {
                longitudPrecios = cantidadPreciosRegistros
                registrosPreciosCargados = cantidadPreciosRegistros
            } else {
                longitudPrecios = BLOQUE_PRECIOS
                registrosPreciosCargados = BLOQUE_PRECIOS
            }

            val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
            try {
                bd.beginTransaction() //inicio la transaccion
                bd.delete("inventario_precios", null, null)

                val sql2 = "DELETE FROM SQLITE_SEQUENCE WHERE NAME =  'inventario_precios'"
                bd.execSQL(sql2)

                bd.setTransactionSuccessful()
            } catch (e: Exception) {
                throw Exception("Error #6 Linea 354")
            } finally {
                bd.endTransaction()
            }

            do {
                var porcentaje = (100 * registrosPreciosCargados) / cantidadPreciosRegistros

                if (porcentaje > 100.toInt()) {
                    porcentaje = 100.toInt()
                }

                val direccion =
                    servidor + "inventario/precios/" + inicioPrecios.toString() + "/" + longitudPrecios.toString()
                val url = URL(direccion)
                with(withContext(Dispatchers.IO) {
                    url.openConnection()
                } as HttpURLConnection) {
                    try {
                        connectTimeout = 30000
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
                                    saveInventarioPreciosDatabase(respuesta, context)
                                } else {
                                    throw Exception("Servidor no Devolvio datos")
                                } //caso que la respuesta venga vacia
                            }
                        } else {
                            throw Exception("Error de Comunicacion con el servidor:$responseCode")
                        }
                    } catch (e: Exception) {
                        throw Exception("Error #7 Linea 401:$responseCode")
                    }
                }//termina de obtener los datos

                inicioPrecios += BLOQUE_PRECIOS
                registrosPreciosCargados += longitudPrecios

                if (registrosPreciosCargados > cantidadPreciosRegistros && inicioPrecios < cantidadPreciosRegistros) {
                    longitudPrecios = cantidadPreciosRegistros
                }

            } while (inicioPrecios < cantidadPreciosRegistros)

        } catch (e: Exception) {
            println("ERROR AL CARGAR LAS ESCALAS DE PRECIOS -> " + e.message)
        }
    }

    //FUNCION PARA OBTENER LA CANTIDAD DE LA ESCALA SELECCIONADA
    fun obtenerEscalaSeleccionada(context: Context, idProducto: Int, precio: Float, unidad : String): Int {
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var cantidadEscala = 0
        try {
            val query = """
            SELECT Cantidad 
            FROM inventario_precios 
            WHERE id_inventario = ? 
            AND ROUND(Precio_iva, 2) = ROUND(?, 2)
            AND unidad = ?
        """.trimIndent()

            val cursor = bd.query(query, arrayOf(idProducto.toString(), precio.toString(), unidad))

            if (cursor.moveToFirst()) {
                cantidadEscala = cursor.getInt(0)
            }
            cursor.close()
        } catch (e: Exception) {
            println("ERROR AL OBTENER LA CANTIDAD DE LA ESCALA SELECCIONADA: ${e.message}")
        }
        return cantidadEscala
    }

    //FUNCION PARA OBTENER DEL SERVIDOR LAS UNIDADES DE MEDIDA
    suspend fun obtenerUnidadesMedidaServidor(context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val url = funciones.getServidor(preferences.getString("ip", ""),
                preferences.getInt("puerto", 0).toString())

        try{
            val servidor = url + "Inventario/unidades"
            val conexionServidor = URL(servidor)

            with(withContext(Dispatchers.IO){
                conexionServidor.openConnection()
            } as HttpURLConnection){
                try {

                    connectTimeout = 10000
                    requestMethod = "GET"
                    if(responseCode == 200){
                        inputStream.bufferedReader().use { data->
                            val response = StringBuffer()
                            var inputLine = data.readLine()

                            while(inputLine != null){
                                response.append(inputLine)
                                inputLine = data.readLine()
                            }
                            data.close()

                            val respuesta = JSONArray(response.toString())
                            if(respuesta.length() > 0){
                                registrarUnidadesMedidaSQLite(respuesta, context)
                            }
                        }
                    }

                }catch (e: Exception){
                    println("ERROR NO SE OBTUVO LA RESPUESTA DEL SEVIDOR -> " + e.message)
                }
            }

        }catch (e:Exception){
            println("ERROR: LOGRO CONECTAR CON EL SERVIDOR -> " + e.message)
        }
    }

    //FUNCION PARA REGISTRAR LAS UNIDADES DE MEDIDA EN SQLITE
    private fun registrarUnidadesMedidaSQLite(json: JSONArray, context: Context) {

        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {

            bd.beginTransaction()

            bd.execSQL("DELETE FROM inventario_unidades")

            for(i in 0 until json.length()){

                val dato = json.getJSONObject(i)
                val valor = ContentValues()

                valor.put("Id", dato.getInt("id"))
                valor.put("Id_inventario", dato.getInt("id_inventario"))
                valor.put("Nombre_unidad", dato.getString("nombre_unidad")).toString().trim()
                valor.put("Equivale", dato.getInt("equivale"))
                valor.put("Unidades", dato.getString("unidades")).toString().trim()

                bd.insert("inventario_unidades", SQLiteDatabase.CONFLICT_REPLACE, valor)
            }
            bd.setTransactionSuccessful()
        }catch (e:Exception){
            println("ERROR AL ALMECENAR LAS UNIDAD DE MEDIDA EN SQLITE -> " + e.message)
        }finally {
            bd.endTransaction()
        }

    }

    //FUNCION PARA OBTENER EL LISTADO DE UNIDADES DE MEDIDA
    fun listadoUnidadesMedidaProductoById(context: Context, idProducto: Int, hojaCargaActiva: Boolean) : ArrayList<String>{

        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val listado = ArrayList<String>()
        var inventarioFraccion = 0f

        listado.add("UNIDAD")

        if(!hojaCargaActiva){
            try {
                val consulta = "SELECT fraccion FROM Inventario WHERE Id=$idProducto"
                val cursor = bd.query(consulta)
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    inventarioFraccion = cursor.getFloat(0)
                }
                cursor.close()

                if(inventarioFraccion > 1){
                    listado.add("FRACCION")
                }

                val consulta2 = "SELECT Nombre_unidad FROM inventario_unidades WHERE Id_inventario=$idProducto"
                val cursor2 = bd.query(consulta2)
                if(cursor2.count > 0){
                    cursor2.moveToFirst()
                    do {
                        listado.add(cursor2.getString(0))
                    }while (cursor2.moveToNext())
                }
                cursor2.close()


            }catch (e:Exception){
                println("ERROR NO SE ENCONTRARON UNIDADES EN INVENTARIO -> " + e.message)
            }
        }

        return listado

    }

    //OBTENER EL ID DE LA UNIDAD DE MEDIDA PARA VENTAS
    fun obtenerIdUnidadMedida(context: Context, idProducto: Int, unidadMedida: String) : Int{

        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var idUnidadMedida = 0
        try {
            val consulta = "SELECT Id FROM inventario_unidades WHERE Id_inventario = $idProducto AND Nombre_unidad = '$unidadMedida' "
            val cursor = bd.query(consulta)
            if(cursor.count > 0){
                cursor.moveToFirst()
                idUnidadMedida = cursor.getInt(0)
            }
            cursor.close()
        }catch (e: Exception){
            println("Error: no se obtuvo la unidad de medida -> " + e.message)
        }
        return idUnidadMedida
    }

}