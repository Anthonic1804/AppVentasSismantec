package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import com.example.acae30.Funciones
import com.example.acae30.Utilidades.CrearSslNoSeguro
import com.example.acae30.modelos.InventarioHojaValidar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class HojaCargaController {

    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var utilidades = CrearSslNoSeguro()

    //-----------------------------------------
    //NUEVAS FUNCIONES DE RECARGAS PARA HOJA DE CARGA
    //-----------------------------------------

    //Funcion para comparar la nueva hoja con la actual
    fun compararActualizarInventarioYHojaDeCarga(context: Context, json: JSONArray) : Int{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val idHojaCarga = preferences.getInt("idHojaCarga", 0)
        var registrosActualizados : Int = 0

        try{

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)

                val idProducto = dato.getInt("id")
                val cantidadNuevaProducto = funciones.validateJsonIsNullFloat(dato, "existencia")

                //val consulta = "SELECT Id_hojaCarga, Id_inventario, Cantidad FROM hoja_carga_detalle WHERE Id_hojaCarga = $idHojaCarga AND Id_inventario = $idProducto"
                val consulta = "SELECT Id_hojaCarga, Id_inventario, Cantidad FROM hoja_carga_detalle WHERE Id_inventario = $idProducto"
                val cursor = bd.query(consulta)

                cursor.use {

                    if(cursor.count > 0){
                        cursor.moveToFirst()

                        //ACTUALIZANDO REGISTRO SOLO SI LA CANTIDAD ES MAYOR A LA HOJA ORIGINAL
                        val existenciaOriginal = cursor.getInt(2)
                        val diferenciaCantidad = cantidadNuevaProducto - existenciaOriginal
                        if(diferenciaCantidad > 0){

                            CoroutineScope(Dispatchers.IO).launch {
                                //ACTUALIZANDO MI INVENTARIO

                                bd.execSQL("UPDATE inventario SET existencia = (existencia + $diferenciaCantidad) WHERE id = $idProducto")

                                //ACTTUALIZANDO MI HOJA_DETALLE

                                bd.execSQL("UPDATE hoja_carga_detalle SET Cantidad = (Cantidad + $diferenciaCantidad) WHERE Id_inventario = $idProducto")
                            }

                            registrosActualizados += 1

                        }else{
                            registrosActualizados += 0
                        }

                    }else{
                        //AGREGANDO EL PRODUCTO NUEVO
                        try{
                            CoroutineScope(Dispatchers.IO).launch {
                                guardarProductoEnInventarioSQLite(context, dato)
                            }

                            registrosActualizados += 1
                        }catch (e:Exception){
                            throw Exception("ERROR AL INTENTAR ALMACENAR EL PRODUCTO EN SQLITE -> " + e.message)
                        }
                    }

                }

            }
        }catch (e:Exception){
            println("ERROR AL COMPARAR LA HOJA DE CARGA -> " + e.message)
        }

        return registrosActualizados
    }

    //ALMACENAR EN SQLITE EL PRODUCTO QUE NO EXISTIA EN LA HOJA DE CARGA INICIAL
    private fun guardarProductoEnInventarioSQLite(context: Context, dato: JSONObject){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase

        try {
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
            data.put("fecha_inventario", LocalDate.now().toString())
            data.put("validadoHoja", 1)
            data.put("condicion_mercado", "NORMAL")
            data.put("id_marca", funciones.validateJsonIsNullInt(dato, "id_marca"))
            data.put("marca", funciones.validateJsonIsnullString(dato, "marca"))
            data.put("id_sku", funciones.validateJsonIsNullInt(dato, "id_sku"))
            data.put("sku", funciones.validateJsonIsnullString(dato, "sku"))
            data.put("id_rubro", funciones.validateJsonIsNullInt(dato, "id_rubro"))
            data.put("rubro", funciones.validateJsonIsnullString(dato, "rubro"))
            data.put("id_linea", funciones.validateJsonIsNullInt(dato, "id_linea"))
            data.put("linea", funciones.validateJsonIsnullString(dato, "linea"))
            data.put("id_sublinea", funciones.validateJsonIsNullInt(dato, "id_sublinea"))
            data.put("sublinea", funciones.validateJsonIsnullString(dato, "sublinea"))
            data.put("id_productor", funciones.validateJsonIsNullInt(dato, "id_productor"))
            data.put("productor", funciones.validateJsonIsnullString(dato, "productor"))
            data.put("id_proveedor", funciones.validateJsonIsNullInt(dato, "id_proveedor"))
            data.put("proveedor", funciones.validateJsonIsnullString(dato, "proveedor"))
            data.put("metodo_gestion", funciones.validateJsonIsnullString(dato, "metodo_gestion"))
            data.put("tipo_fiscal", funciones.validateJsonIsnullString(dato, "tipo_fiscal"))

            bd.insert("inventario", SQLiteDatabase.CONFLICT_REPLACE, data)
        }catch (e:Exception){
            println("ERROR AL INSERTAR EL PRODUCTO EN INVENTARIO -> " + e.message)
        }finally {
            //INSERTANDO EL PRODUCTO NUEVO EN LA TBL HOJA_CARGA_DETALLE
            almacenarProductoEnHojaDetalle(context, dato)
        }
    }

    //FUNCION PARA ALMACENAR EL NUEVO PRODUCTO EN HOJA_CARGA_DETALL
    private fun almacenarProductoEnHojaDetalle(context: Context, dato: JSONObject){
        val db = funciones.obtenerInstancia(context).openHelper.writableDatabase
        val idHojaCarga = preferences.getInt("idHojaCarga", 0)
        val idProducto = dato.getInt("id")
        val codigoInventario = funciones.validateJsonIsnullString(dato, "codigo")
        val cantidad = funciones.validateJsonIsNullFloat(dato, "existencia")

        try {
            db.execSQL("INSERT INTO hoja_carga_detalle(Id_hojaCarga, Id_inventario, Codigo_inventario, Cantidad) VALUES($idHojaCarga, $idProducto, '$codigoInventario', $cantidad)")
        }catch (e:Exception){
            println("ERROR AL INSERTAR EL PRODUCTO EN HOJA DETALLE -> " + e.message)
        }
    }

    //FUNCION PARA OBTENER LA INFORMACION DEL PRODUCTO POR CODIGO O POR NOMBRE
    fun validarProductoPorString(context: Context, busqueda: String): ArrayList<InventarioHojaValidar>{
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val lista = ArrayList<InventarioHojaValidar>()

        val query: String = if (busqueda.isNotEmpty()) {
            """
            SELECT * FROM inventario 
            WHERE Descripcion LIKE '%' || ? || '%' OR Codigo LIKE '%' || ? || '%'
            """
        } else {
            "SELECT * FROM inventario LIMIT 60"
        }


        try {
            val cursor = if (busqueda.isNotEmpty()) {
                base.query(query, arrayOf(busqueda, busqueda))
            } else {
                base.query(query)
            }
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    do {
                        val arreglo = InventarioHojaValidar(
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
                            cursor.getString(2),
                            cursor.getInt(28)
                        )
                        lista.add(arreglo)
                    } while (cursor.moveToNext())
                    //cursor.close()
                }
            }
        }catch (e:Exception){
            println("ERROR AL REALIZAR LA BUSQUEDA EN INVENTARIO -> ${e.message}")
        }
        return lista
    }

    //FUNCION PARA ACTUALIZAR ESTADO DE PRODUCTO EN VALIDACION DE HOJA DE CARGA
    fun actualizarProductoValidacionHojaCarga(context: Context, codigoInventario: String, validado: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE inventario SET validadoHoja = $validado WHERE codigo = '$codigoInventario'")
        }catch (e:Exception){
            println("ERROR AL ACTUALIZAR EL ESTADO DEL PRODUCO AL VALIDAR HOJA CARGA -> " + e.message)
        }
    }

    //FUNCION PARA VERIFICAR SI TODOS LOS PRODUCTO YA FUERON VALIDADOS
    fun obtenerProductosSinValidar(context: Context) : Int{
        var productoSinValidar: Int = 0
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        try {
            val consulta = "SELECT * FROM inventario WHERE validadoHoja = 0"
            val cursor = bd.query(consulta)
            if(cursor.count > 0){
                productoSinValidar = 1
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER LOS PRODUCTO SIN VALIDAR -> " + e.message)
            productoSinValidar = 0
        }
        return productoSinValidar
    }

    //FUNCION PARA VALIDAR HOJA DE CARGAR EN EL SERVIDOR
    suspend fun validarHojaCargaServidor(context: Context, numeroHoja: Int, idVendedor: Int) : Boolean{

        var aceptada: Boolean = false
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString().toString(), context)
        val ruta = servidor + "Inventario/validarHojaCarga/${numeroHoja.toString()}/${idVendedor.toString()}"
        val url = URL(ruta)

        val sslContext = utilidades.crearSslInseguro()

        with(withContext(Dispatchers.IO){
            url.openConnection()
        } as HttpURLConnection){

            if(this is HttpsURLConnection){
                sslSocketFactory = sslContext.socketFactory
                hostnameVerifier = HostnameVerifier{_, _ -> true}
            }

            try {
                connectTimeout = 10000
                requestMethod = "GET"

                when(responseCode){
                    200 -> {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            try {
                                val respuesta = StringBuffer()
                                var inputline = it.readLine()
                                while(inputline != null){
                                    respuesta.append(inputline)
                                    inputline = it.readLine()
                                }
                                it.close()
                                val res: JSONObject = JSONObject(respuesta.toString())
                                if(res.getString("respuesta").contains("HOJA_ACTUALIZADA")){
                                    aceptada = true
                                }
                            }catch (e:Exception){
                                println("ERROR AL OBTENER LA RESPUESTA DEL SERVIDOR -> " + e.message)
                                aceptada = false
                            }
                        }
                    }
                    404-> {
                        println("HOJA NO ENCONTRADA")
                        aceptada = false
                    }
                    else -> {
                        println("ERROR DE SERVIDOR")
                        aceptada = false
                    }
                }

            }catch (e:Exception){
                println("ERROR AL PROCESAR LA CONEXION CON EL SERVIDOR -> " + e.message)
                aceptada = false
            }

        }
        return  aceptada
    }

    //FUNCION PARA VERIFICAR SI LA HOJA DE CARGA YA FUE INGRESADA
    fun verificarHojaCargaIngresada(context: Context, numeroHojaCarga: Int) : Boolean{

        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var registrada : Boolean = false

        val consulta = "SELECT numeroHoja FROM hoja_carga WHERE numeroHoja = $numeroHojaCarga"
        val cursor = bd.query(consulta)
        cursor.use {
            if(cursor.count > 0){
                registrada = true
            }
        }
        return registrada
    }

}