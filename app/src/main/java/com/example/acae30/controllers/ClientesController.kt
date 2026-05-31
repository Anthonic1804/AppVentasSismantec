package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.view.View
import com.example.acae30.AlertDialogo
import com.example.acae30.DAO.ClientesDao
import com.example.acae30.Detallepedido
import com.example.acae30.Entities.ClientePreciosEntity
import com.example.acae30.Entities.ClienteSucursalEntity
import com.example.acae30.Entities.ClientesEntity
import com.example.acae30.Entities.CuentasEntity
import com.example.acae30.Funciones
import com.example.acae30.Retrofit.RetrofitCliente
import com.example.acae30.Utilidades.AgregarHeaders
import com.example.acae30.Utilidades.ConsumirEndpoint
import com.example.acae30.Utilidades.CrearSslNoSeguro
import com.example.acae30.Visita
import com.example.acae30.database.AppDatabase
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.JSONmodels.ActualizarPagareFirmadoCliente
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Timer
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class ClientesController {

    private var funciones = Funciones()
    private var visitaController = VisitaController()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    private var utilidades = CrearSslNoSeguro()
    //private var consumirEndpoint = ConsumirEndpoint()
    //private val agregarHeaders = AgregarHeaders()


    private lateinit var base : AppDatabase
    private lateinit var servidor : String
    private lateinit var clientesDao : ClientesDao

    private val BLOQUE : Int = 300
    var cargarClientes: String = ""
    var idVendedor: Int = 0

    private fun inicializarVariables(context: Context){
        base = AppDatabase.getInstance(context)
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)
        cargarClientes = preferences.getString("cargarClientesPorRuta", "").toString()
        idVendedor = preferences.getInt("Idvendedor", 0)
    }

    //OBTENER CLIENTES DEL SERVIDOR
    /*suspend fun obtenerClientesServidor(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto",0).toString(), context)

        try {
            //val direccion = url!! + "clientes"
            val id_vendedor = preferences.getInt("Idvendedor", 0)
            val direccion = servidor + "clientes/vendedor/"+id_vendedor
            val url = URL(direccion)

            val sslContext = utilidades.crearSslInseguro()

            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {

                val token = preferences.getString("token", "")
                agregarHeaders.agregarHeaders(this, token, sslContext)

                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { data ->
                            var talla = 0
                            val response = StringBuffer()
                            var inputLine = data.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = data.readLine()
                            }
                            data.close()

                            val respuesta = JSONArray(response.toString())
                            if (respuesta.length() > 0) {
                                saveClienteDataBase(respuesta, context)
                            } else {
                                throw Exception("Servidor no Devolvio datos")
                            } //caso que la respuesta venga vacia
                        }
                    } else {
                        throw Exception("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                    println("ERROR AL OBTENER LA RESPUESTA DEL SERVIDOR 1: ${e.message}")

                }
            } //ABRIMOS LA CONEXION
        } catch (e: Exception) {
            println("ERROR AL CONECTAR CON EL SERVIDOR 2: ${e.message}")

        }
    }*/

    //GUARDAR DATOS DE CLIENTES EN SQLITE
    /*private fun saveClienteDataBase(json: JSONArray, context: Context) {

        val total = json.length()
        val talla = (50.toFloat() / total.toFloat()).toFloat()
        var contador: Float = 0.toFloat()
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.beginTransaction() //inicio la transaccion

            bd.execSQL("DELETE FROM clientes") //limpiamos los registros viejos par obtener los nuevos
            bd.execSQL("DELETE FROM cliente_precios")
            bd.execSQL("DELETE FROM cliente_sucursal") //LIMPIANDO TABLA SUCURSALES

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i) //obtenemos el objecto json
                val data = ContentValues()
                data.put("Id", dato.getInt("id"))
                data.put("Codigo", funciones.validate(dato.getString("codigo")))
                data.put("Cliente", funciones.validate(dato.getString("cliente")))
                data.put("Dui", funciones.validate(dato.getString("dui")))
                data.put("Nit", funciones.validate(dato.getString("nit")))
                data.put("Nrc", funciones.validate(dato.getString("nrc")))
                data.put("Giro", funciones.validate(dato.getString("giro")))
                data.put(
                    "Categoria_cliente",
                    funciones.validate(dato.getString("categoria_cliente"))
                )
                data.put(
                    "Terminos_cliente",
                    funciones.validate(dato.getString("terminos_cliente"))
                )
                data.put("Plazo_credito", funciones.validate(dato.getInt("plazo_credito")))
                data.put("Limite_credito",
                    funciones.validate(dato.getString("limite_credito").toFloat())
                )
                data.put("Balance", funciones.validate(dato.getString("balance").toFloat()))
                data.put("Estado_credito", funciones.validate(dato.getString("estado_credito")))
                data.put("Direccion", funciones.validate(dato.getString("direccion")))
                data.put("Municipio", funciones.validate(dato.getString("municipio")))
                data.put("Departamento", funciones.validate(dato.getString("departamento")))
                data.put("Telefono_1", funciones.validate(dato.getString("telefono1")))
                data.put("Telefono_2", funciones.validate(dato.getString("telefono2")))
                data.put("Correo", funciones.validate(dato.getString("correo")))
                data.put("Contacto", funciones.validate((dato.getString("contacto"))))
                data.put("Id_ruta", funciones.validateJsonIsNullInt(dato, "id_ruta"))
                data.put("Id_vendedor", funciones.validateJsonIsNullInt(dato, "id_vendedor"))
                data.put("Vendedor", funciones.validate(dato.getString("vendedor")))
                data.put("Status", funciones.validate(dato.getString("status")))
                data.put("Ultima_venta", funciones.validate(dato.getString("fecha_ult_venta")))
                data.put(
                    "Aporte_mensual",
                    funciones.validate(dato.getString("aporte_mensual").toFloat())
                )

                //AGREGADO EL CAMPO PARA VERIFICACION DEL PAGARE
                val pagareFirmado = if(dato.getBoolean("pagare_Firmado_app")) 1 else 0
                data.put("Firmar_pagare_app", pagareFirmado)

                //AGREGANDO EL CAMPO PARA VERIFICACION DE PERSONA JURIDICA
                data.put("Persona_juridica", funciones.validate(dato.getString("persona_juridica")))

                //VALIDANDO EL DTEGIRO ALMACENADO EN EL SERVIDOR
                data.put("dteGiro", funciones.validate(dato.getString("dteGiro")))
                data.put("Ruta", funciones.validate(dato.getString("ruta")))

                data.put("Ruta", funciones.validate(dato.getString("ruta")))
                data.put("DTECodDepto", funciones.validate(dato.getString("dteCodDepto")))
                data.put("DTECodMunicipio", funciones.validate(dato.getString("dteCodMunicipio")))
                data.put("DTECodPais", funciones.validate(dato.getString("dteCodPais")))
                data.put("DTEDireccion", funciones.validate(dato.getString("dteDireccion")))
                data.put("DTEPais", funciones.validate(dato.getString("dtePais")))
                data.put("DTETelefono", funciones.validate(dato.getString("dteTelefono")))
                data.put("DTECorreo", funciones.validate(dato.getString("dteCorreo")))
                data.put("Latitud_app", funciones.validate(dato.getString("latitud_app")))
                data.put("Longitud_app", funciones.validate(dato.getString("longitud_app")))
                data.put("Nombre_comercial", funciones.validate(dato.getString("nombre_comercial")))
                data.put("Mayorista", funciones.validate(dato.getString("mayorista")))
                data.put("DTECodGiro", funciones.validate(dato.getString("dteCodGiro")))
                data.put("DTEDistrito", funciones.validate(dato.getString("dteDistrito")))
                data.put("DTECodDistrito", funciones.validate(dato.getString("dteCodDistrito")))

                bd.insert("clientes", SQLiteDatabase.CONFLICT_REPLACE, data)
            } //recorre el json array
            bd.setTransactionSuccessful()

        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd.endTransaction()
        }
    }//guarda los datos en la bd*/

    //FUNCION PARA OBTENER SUCURSALES DE LOS CLIENTES DESDE EL SERVIDOR
    /*suspend fun obtenerClienteSucursalesServidor(context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto",0).toString(), context)

        try {
            val direccion = servidor + "sucursales"
            val url = URL(direccion)

            val sslContext = utilidades.crearSslInseguro()

            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {

                if(this is HttpsURLConnection){
                    sslSocketFactory = sslContext.socketFactory
                    hostnameVerifier = HostnameVerifier{_, _ -> true}
                }

                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { data ->
                            var talla = 0
                            val response = StringBuffer()
                            var inputLine = data.readLine()
                            while (inputLine != null) {
                                response.append(inputLine)
                                inputLine = data.readLine()
                            }
                            data.close()
                            val respuesta = JSONArray(response.toString())
                            if (respuesta.length() > 0) {
                                saveSucursalesDatabase(respuesta, context) //guarda los datos en la bd
                            }
                        }
                    } else {
                        throw Exception("SERVIDOR: NO SE ENCONTRARON SUCURSALES REGISTRADAS")
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }//termina de obtener los datos
        } catch (e: Exception) {
            // alert!!.dismisss()
            //funciones.mostrarAlerta("ERROR -> ${e.message}", this@carga_datos, binding.vistaalerta)
            println("NO SE ENCONTRARON DATOS REGISTRADOS DE SUCURSALES")
        }
    }*/
    /*suspend fun obtenerClienteSucursalesServidor(context: Context){
        val response = consumirEndpoint.consumirEndpoint(context, "sucursales")

        if(response != null){
            val respuesta = JSONArray(response)

            if(respuesta.length() > 0){
                saveSucursalesDatabase(respuesta, context)
            }
        }
    }*/

    //ALMACENAR SUCURSALES EN SQLITE
    /*private fun saveSucursalesDatabase(json: JSONArray, context: Context) {
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        val total = json.length()
        val talla = (50.toFloat() / total.toFloat()).toFloat()
        var contador: Float = 0.toFloat()
        try {
            bd.beginTransaction() //INICIANDO TRANSACCION DE REGISTRO
            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Id_cliente", dato.getInt("id_cliente"))
                valor.put("codigo_sucursal", funciones.validateJsonIsnullString(dato, "codigo_sucursal"))
                valor.put("nombre_sucursal", funciones.validateJsonIsnullString(dato, "nombre_sucursal"))
                valor.put("direccion_sucursal", funciones.validateJsonIsnullString(dato, "dteDireccion"))//DATO DTE
                valor.put("municipio_sucursal", funciones.validateJsonIsnullString(dato, "municipio"))//DATO DTE
                valor.put("depto_sucursal", funciones.validateJsonIsnullString(dato, "departamento"))//DATO DTE
                valor.put("telefono_1", funciones.validateJsonIsnullString(dato, "dteTelefono"))//DATO DTE
                valor.put("telefono_2", funciones.validateJsonIsnullString(dato, "telefono2"))
                valor.put("correo_sucursal", funciones.validateJsonIsnullString(dato, "correo"))
                valor.put("contacto_sucursal", funciones.validateJsonIsnullString(dato, "contacto"))

                //ARGEGANDO DATOS PENDIENTE Y DTE DE LA SUCURSAL
                valor.put("Id_ruta", dato.getInt("id_ruta"))
                valor.put("Ruta", funciones.validateJsonIsnullString(dato, "ruta"))
                valor.put("DTECodDepto", funciones.validateJsonIsnullString(dato, "dteCodDepto"))
                valor.put("DTECodMunicipio", funciones.validateJsonIsnullString(dato, "dteCodMunicipio"))
                valor.put("DTECodPais", funciones.validateJsonIsnullString(dato, "dteCodPais"))
                valor.put("DTEPais", funciones.validateJsonIsnullString(dato, "dtePais"))
                valor.put("Latitud_app", funciones.validateJsonIsnullString(dato, "latitud_app"))
                valor.put("Longitud_app", funciones.validateJsonIsnullString(dato, "longitud_app"))

                bd.insert("cliente_sucursal", SQLiteDatabase.CONFLICT_REPLACE, valor)
            }
            bd.setTransactionSuccessful() //TRANSACCION COMPLETA
        } catch (e: Exception) {
            throw  Exception(e.message)
        } finally {
            bd.endTransaction()
        }
    } //INSERTANDO DATOS EN LA TABLA SUCURSALES EN SQLITE*/

    //FUNCION PARA OBTENER LAS CXC DESDE EL SERVIDOR
    /*suspend fun obtenerCxcServidor(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto",0).toString(), context)

        try {
            val direccion = servidor + "cuentas"
            val url = URL(direccion)

            val sslContext = utilidades.crearSslInseguro()

            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {

                if(this is HttpsURLConnection){
                    sslSocketFactory = sslContext.socketFactory
                    hostnameVerifier = HostnameVerifier{_, _ -> true}
                }

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
                                saveCuentaDatabase(respuesta, context) //guarda los datos en la bd
                            }
                        }
                    } else {
                        throw Exception("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }//termina de obtener los datos
        } catch (e: Exception) {
            //alert!!.dismisss()
        }
    }*/
    /*suspend fun obtenerCxcServidor(context: Context){
        val response = consumirEndpoint.consumirEndpoint(context, "cuentas")
        if(response != null){
            val respuesta = JSONArray(response)
            if(respuesta.length() > 0){
                saveCuentaDatabase(respuesta, context)
            }
        }
    }*/

    //FUNCION PARA ALMACENAR LAS CXC EN SQLITE
    /*private fun saveCuentaDatabase(json: JSONArray, context: Context) {
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        val total = json.length()
        val talla = (50.toFloat() / total.toFloat()).toFloat()
        var contador: Float = 0.toFloat()
        try {
            bd.beginTransaction() //inicia la transaccion
            bd.execSQL("DELETE FROM cuentas") //eliminamos la cuentas

            val sql2 = "DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'cuentas'"
            bd.execSQL(sql2)

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Id_cliente", dato.getInt("id_cliente"))
                valor.put(
                    "Codigo_cliente",
                    funciones.validateJsonIsnullString(dato, "codigo_cliente")
                )
                valor.put("Documento", funciones.validateJsonIsnullString(dato, "documento"))
                valor.put("Fecha", funciones.validateJsonIsnullString(dato, "fecha"))
                valor.put("Valor", funciones.validateJsonIsNullFloat(dato, "valor"))
                valor.put(
                    "Abono_inicial",
                    funciones.validateJsonIsNullFloat(dato, "abono_inicial")
                )
                valor.put(
                    "Saldo_inicial",
                    funciones.validateJsonIsNullFloat(dato, "saldo_inicial")
                )
                valor.put("Plazo", funciones.validateJsonIsNullFloat(dato, "plazo"))
                valor.put(
                    "Fecha_vencimiento",
                    funciones.validateJsonIsnullString(dato, "fecha_vencimiento")
                )
                valor.put("Saldo_actual", funciones.validateJsonIsNullFloat(dato, "saldo_actual"))
                valor.put(
                    "Fecha_ult_pago",
                    funciones.validateJsonIsnullString(dato, "fecha_ult_pago")
                )
                valor.put("Valor_pago", funciones.validateJsonIsNullFloat(dato, "valor_pago"))
                valor.put("Relacionado", funciones.validateJsonIsnullString(dato, "relacionado"))
                valor.put("Status", funciones.validateJsonIsnullString(dato, "status"))
                valor.put(
                    "Fecha_cancelado",
                    funciones.validateJsonIsnullString(dato, "fecha_cancelado")
                )
                valor.put("dias_tardios", funciones.validateJsonIsNullInt(dato, "dias_tardios"))

                bd.insert("cuentas", SQLiteDatabase.CONFLICT_REPLACE, valor)
            } //termina el for
            bd.setTransactionSuccessful() //transaccion exitosa
        } catch (e: Exception) {
            throw  Exception(e.message)
        } finally {
            bd.endTransaction()
        }
    } //inserta las cxc en la tabla*/

    //FUNCION PARA OBTENER LOS PRECIOS PERSONALIZADOS
    /*suspend fun obtenerPreciosPersonalizados(context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto",0).toString(), context)

        /*PRIMER TRY PARA OBTENER LA CANTIDAD DE REGISTROS
        * Y LUEGO CARGARLOS POR BLOQUES
        */
        /*try {
            val urlCantidadRegistros = url + "clientes/precios/cantidad"
            val urlCantidad = URL(urlCantidadRegistros)
            var cantRegistros = 0

            with(withContext(Dispatchers.IO) {
                urlCantidad.openConnection()
            } as HttpURLConnection) {
                try {
                    connectTimeout = 30000
                    requestMethod = "GET"
                    if (responseCode == 200) {
                        inputStream.bufferedReader().use { data ->
                            val readline = data.readLine()

                            cantRegistros = readline.toInt()
                        }

                    } else {
                        throw Exception("Error de Comunicacion con el servidor:$responseCode")
                    }
                } catch (e: Exception) {
                    throw Exception("Error #1 Linea 207:$responseCode")
                }
            }

            //CALCULANDO BLOQUE DE REGISTROS
            val bloque = 500
            var inicio = 0
            var longitud: Int
            var registrosCargados: Int

            if(cantRegistros < bloque){
                longitud = cantRegistros
                registrosCargados = cantRegistros
            }else{
                longitud = bloque
                registrosCargados = bloque
            }


            var porcentaje = 2
            do{

                if (porcentaje <= 99) {
                    funciones.messageAsync("Cargando $porcentaje%")
                }else{
                    funciones.messageAsync("Cargando 100%")
                }

                porcentaje += 13

                val urlPreciosPersonalizados = url + "clientes/precios/" + inicio.toString() + "/" + longitud.toString()
                val urlPrecios = URL(urlPreciosPersonalizados)

                with(withContext(Dispatchers.IO) {
                    urlPrecios.openConnection()
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
                                    almacenarPrecioPersonalizados(respuesta, context)
                                    println("PRECIOS PERSONALIZADOS ALMACENADOS CORRECTAMEMENTE")
                                } else {
                                    throw Exception("EL SERVIDOR NO DEVOLVIO DATOS")
                                }
                            }
                        } else {
                            throw Exception("ERROR DE COMUNICACIOIN CON EL SERVIDOR:$responseCode")
                        }
                    } catch (e: Exception) {
                        throw Exception("ERROR DE CONEXION: $responseCode")
                    }
                }

                inicio += bloque
                registrosCargados += longitud

                if (cantRegistros in (inicio + 1) until registrosCargados) {
                    longitud = cantRegistros
                }

            }while(inicio < cantRegistros)


        }catch (e:Exception){
            println("ERROR AL OBTENER LOS PRECIOS PERSONALIZADOS -> ${e.message}")
        }*/

        try {

            //OBTENIENDO LA CANTIDAD DE REGISTROS
            val cantidadURL = URL(servidor + "clientes/precios/cantidad")

            val sslContext = utilidades.crearSslInseguro()

            val cantidadRegistros = withContext(Dispatchers.IO){
                (cantidadURL.openConnection() as HttpURLConnection).run {

                    val token = preferences.getString("token", "")
                    agregarHeaders.agregarHeaders(this, token, sslContext)

                    requestMethod = "GET"
                    inputStream.bufferedReader().readLine().toInt()
                }
            }

            println("CANTIDAD DE REGISTROS -> $cantidadRegistros")

            val bloque = 1000
            var inicio = 0

            while(inicio < cantidadRegistros){

                val longitud = minOf(bloque, cantidadRegistros - inicio)

                val ok = descargarBloquePrecios(servidor, inicio, longitud, context)

                if(ok){
                    inicio += bloque

                }else{
                    funciones.messageAsync("Error de conexion... Reintentado...")
                    delay(2000)
                }

            }

            funciones.messageAsync("Carga completada 100%")

        }catch (e:Exception){
            println("ERROR GENERAL -> ${e.message}")
        }
    }*/

    //FUNCION PARA HACER LOS REINTENTOS DE OBTENER Y ALMACENAR LOS PRECIOS PERSONALIZADOS
    /*private suspend fun descargarBloquePrecios(url: String, inicio: Int, longitud: Int, context: Context) : Boolean{

        var ok : Boolean = false

        try {
            val urlFinal = URL(url + "clientes/precios/$inicio/$longitud")

            val sslContext = utilidades.crearSslInseguro()

            withContext(Dispatchers.IO){
                val conn = urlFinal.openConnection() as HttpURLConnection

                val token = preferences.getString("token", "")
                agregarHeaders.agregarHeaders(conn, token, sslContext)

                conn.connectTimeout = 30000
                conn.readTimeout = 30000
                conn.requestMethod = "GET"

                if(conn.responseCode == 200){
                    val response = conn.inputStream.bufferedReader().readText()
                    val json = JSONArray(response)

                    if (json.length() > 0) {
                        almacenarPrecioPersonalizados(json, context)
                        ok = true
                    }else{
                        //
                    }
                }else{
                    throw Exception("HTTP ${conn.responseCode}")
                }

            }
        }catch (e:Exception){
            println("Error bloque $inicio ------  ${e.message}")
            ok = false
        }

        return ok
    }*/

    //FUNCION PARA LAMACENAR LOS PRECIOS PERSONALIZADOS EN SQLITE
    /*private fun almacenarPrecioPersonalizados(json: JSONArray, context: Context){
        val base = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            base.beginTransaction()
            for (i in 0 until json.length()){
                val datos = json.getJSONObject(i)
                val valor = ContentValues()

                valor.put("id_cliente", datos.getInt("id_cliente"))
                valor.put("id_inventario", datos.getInt("id_inventario"))
                valor.put("precio_p", funciones.validateJsonIsNullFloat(datos, "precio_p"))
                valor.put("precio_p_iva", funciones.validateJsonIsNullFloat(datos, "precio_p_iva"))
                valor.put("bonificado", funciones.validateJsonIsNullFloat(datos, "bonificado"))

                base.insert("cliente_precios", SQLiteDatabase.CONFLICT_REPLACE, valor)
            }
            base.setTransactionSuccessful()
        }catch (e:Exception){
            println("ERROR AL INSERTAR LOS PRECIOS PERSONALIZADOS -> ${e.message}")
        }finally {
            base.endTransaction()
        }
    }*/

    //FUNCION PARA OBTENER LOS DATOS DEL CLIENTE POR ID
    fun obtenerInformacionCliente(context: Context, idCliente: Int): Cliente?{

        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var datosCliente: Cliente? = null

        try {
            val cursor = base.query("SELECT Id," +
                    "Codigo," +
                    "Cliente," +
                    "Dui," +
                    "Nit," +
                    "Nrc," +
                    "Giro," +
                    "Categoria_cliente," +
                    "Terminos_cliente," +
                    "Plazo_credito, " +
                    "Limite_credito, " +
                    "Balance, " +
                    "Estado_credito, " +
                    "Direccion," +
                    "Municipio, " +
                    "Departamento, " +
                    "Telefono_1," +
                    "Telefono_2, " +
                    "Correo, " +
                    "Contacto, " +
                    "Id_ruta," +
                    "Id_vendedor," +
                    "Vendedor, " +
                    "Status, " +
                    "Ultima_venta, " +
                    "Aporte_mensual," +
                    "Firmar_pagare_app, " +
                    "Persona_juridica, " +
                    "dteGiro," +
                    "Ruta, " +
                    "DTEDireccion, " +
                    "DTECodDepto," +
                    "DTECodMunicipio, " +
                    "DTECodPais, " +
                    "DTEPais ," +
                    "DTECorreo, " +
                    "DTETelefono , " +
                    "Latitud_app," +
                    "Longitud_app, " +
                    "Nombre_comercial , " +
                    "DTECodGiro, " +
                    "DTEDistrito, " +
                    "DTECodDistrito, " +
                    "Mayorista FROM clientes " +
                    "WHERE id=?", arrayOf(idCliente))
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    datosCliente = Cliente(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getInt(9),
                        cursor.getFloat(10),
                        cursor.getFloat(11),
                        cursor.getString(12),
                        cursor.getString(13),
                        cursor.getString(14),
                        cursor.getString(15),
                        cursor.getString(16),
                        cursor.getString(17),
                        cursor.getString(18),
                        cursor.getString(19),
                        cursor.getInt(20),
                        cursor.getInt(21),
                        cursor.getString(22),
                        cursor.getString(23),
                        cursor.getString(24),
                        cursor.getFloat(25),
                        cursor.getInt(26),
                        cursor.getString(27),
                        cursor.getString(28),
                        cursor.getString(29),
                        cursor.getString(30),
                        cursor.getString(31),
                        cursor.getString(32),
                        cursor.getString(33),
                        cursor.getString(34),
                        cursor.getString(35),
                        cursor.getString(36),
                        cursor.getString(37),
                        cursor.getString(38),
                        cursor.getString(39),
                        cursor.getString(40),
                        cursor.getString(41),
                        cursor.getString(42),
                        cursor.getString(43)
                    )
                    // cursor.close()
                }
            }
        }catch (e: Exception){
            println("ERROR: NO SE ENCONTRO EL CLIENTE -> ${e.message}")
        }

        return datosCliente
    }

    //FUNCION PARA OBTENER TODOS LOS CLIENTES
    fun obtenerListaClientes(context: Context, busqueda: String, rutaClientes: String): ArrayList<Cliente>{
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val listaClientes = ArrayList<Cliente>()
        val consultaSql: String
        val argumentos: Array<String>

        /*
        * VALIDACION PARA LA CARGA DE CLIENTES
        * R-> POR RUTA CARGADA
        * TODOS LOS CLIENTES
        * */
        when (rutaClientes) {
            "R" -> {
                val idRuta = preferences.getInt("idRutaSeleccionada", 0)

                if (busqueda.isNotEmpty()) {
                    consultaSql = """
                SELECT * FROM Clientes 
                WHERE Id_ruta = ? 
                AND (Cliente LIKE ? OR Codigo LIKE ? OR Nombre_comercial LIKE ?)
            """.trimIndent()
                    argumentos = arrayOf(idRuta.toString(), "%$busqueda%", "%$busqueda%", "%$busqueda%")
                } else {
                    consultaSql = "SELECT * FROM Clientes WHERE Id_ruta = ? LIMIT 50"
                    argumentos = arrayOf(idRuta.toString())
                }
            }

            else -> {
                if (busqueda.isNotEmpty()) {
                    consultaSql = """
                SELECT * FROM Clientes 
                WHERE Cliente LIKE ? OR Codigo LIKE ? OR Nombre_comercial LIKE ?
            """.trimIndent()
                    argumentos = arrayOf("%$busqueda%", "%$busqueda%", "%$busqueda%")
                } else {
                    consultaSql = "SELECT * FROM Clientes LIMIT 50"
                    argumentos = emptyArray()
                }
            }
        }

        try {
            val consulta = base.query(consultaSql, argumentos)
            consulta.use {
                if (consulta.count > 0) {
                    consulta.moveToFirst()
                    do {
                        val listado = Cliente(
                            consulta.getInt(0),
                            consulta.getString(1),
                            consulta.getString(2),
                            consulta.getString(3),
                            consulta.getString(4),
                            consulta.getString(5),
                            consulta.getString(6),
                            consulta.getString(7),
                            consulta.getString(8),
                            consulta.getInt(9),
                            consulta.getFloat(10),
                            consulta.getFloat(11),
                            consulta.getString(12),
                            consulta.getString(13),
                            consulta.getString(14),
                            consulta.getString(15),
                            consulta.getString(16),
                            consulta.getString(17),
                            consulta.getString(18),
                            consulta.getString(19),
                            consulta.getInt(20),
                            consulta.getInt(21),
                            consulta.getString(22),
                            consulta.getString(23),
                            consulta.getString(24),
                            consulta.getFloat(25),
                            consulta.getInt(27),
                            consulta.getString(28),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "N"
                        )
                        listaClientes.add(listado)

                    } while (consulta.moveToNext())
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }

        return listaClientes
    }

    //FUNCION PARA ACTUALIZAR EL PAGARE EN SERVIDOR SQL SERVER
    fun actualizarPagareFirmadoSqlServer(context: Context, idCliente: Int, vista: View){

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)

        try {
            val datos = ActualizarPagareFirmadoCliente(
                idCliente
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = url + "clientes/actualizarPagare"
            val url2 = URL(ruta)

            val sslContext = utilidades.crearSslInseguro()

            with(url2.openConnection() as HttpURLConnection) {

                if(this is HttpsURLConnection){
                    sslSocketFactory = sslContext.socketFactory
                    hostnameVerifier = HostnameVerifier{_, _ -> true}
                }

                try {
                    connectTimeout = 20000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    )
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //escribo el json
                    or.flush() //se envia el json
                    if (responseCode == 200) {
                        BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                            try {
                                val respuesta = StringBuffer()
                                var inpuline = it.readLine()
                                while (inpuline != null) {
                                    respuesta.append(inpuline)
                                    inpuline = it.readLine()
                                }

                                when (respuesta.toString()) {
                                    "CLIENTE_ACTUALIZADO" -> {
                                        actualizarPagareFirmadoSqLite(context, idCliente)
                                    }
                                    "ERROR_CLIENTE_NO_ENCONTRADO" -> {
                                        funciones.mostrarAlerta("ERROR AL ACTUALIZAR LA TABLA CLIENTES", context, vista)
                                    }
                                }

                            } catch (e: Exception) {
                                println("ERROR 1: ${e.message}")
                            }
                        }
                    }else {
                        println("ERROR AL ACTUALIZAR LA TABLA CLIENTES")
                    }

                } catch (e: Exception) {
                    println("ERROR 2: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("ERROR 3: ${e.message}")
        }
    }

    //FUNCION PARA ACTUALIZAR EL PAGARE DE FORMA LOCAL
    private fun actualizarPagareFirmadoSqLite(context: Context, idCliente: Int){
        val db = funciones.obtenerInstancia(context).openHelper.writableDatabase

        try {
            db.beginTransaction()

            db.execSQL("UPDATE Clientes SET Firmar_pagare_app= 1 WHERE Id = ?", arrayOf(idCliente))

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            println("Error al actualizar pagaré: ${e.message}")
        } finally {
            db.endTransaction()
        }
    }

    //FUNCION DE REDIRECCION CUDNO SE VERIFICAR SI EL PAGARE ES OBLIGATORIO O NO
    fun verificarPagareObligatorio(context: Context, idCliente: Int, nomCliente:String, codCliente:String, visita:Boolean){
        if (visita) {
            val datos_visitas = visitaController.obtenerVisita(idCliente, context)
            if (datos_visitas != null) {
                if (datos_visitas.Abierta) {
                    val intento = Intent(context, Visita::class.java)
                    intento.putExtra("idcliente", idCliente)
                    intento.putExtra("nombrecliente", nomCliente)
                    intento.putExtra("codigo", codCliente)
                    intento.putExtra("visitaid", datos_visitas.Id)
                    intento.putExtra("idapi", datos_visitas.Idvisita)
                    context.startActivity(intento)
                } else {
                    val intento = Intent(context, Visita::class.java)
                    intento.putExtra("idcliente", idCliente)
                    intento.putExtra("nombrecliente", nomCliente)
                    intento.putExtra("codigo", codCliente)
                    context.startActivity(intento)

                } //valida si la visita esta abierta

            } else {
                val intento = Intent(context, Visita::class.java)
                intento.putExtra("idcliente", idCliente)
                intento.putExtra("nombrecliente", nomCliente)
                intento.putExtra("codigo", codCliente)
                context.startActivity(intento)
            } //valida si existe visita

        } else {
            val intento = Intent(context, Detallepedido::class.java)
            intento.putExtra("id", idCliente)
            intento.putExtra("nombrecliente", nomCliente)
            context.startActivity(intento)
        }
    }

    //FUNCION PARA OBTENER EL PRECIO PERSONALIZADO POR ID CLIENTE E ID PRODUCTO
    fun obtenerPrecioPersoCliente(idCliente: Int, idProducto: Int, context: Context, facExpo: Boolean) : Float{
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var precioIva = 0f

        //MODIFICANDO PARA FACTURA DE EXPORTACION
        val consulta = if(facExpo){
            "SELECT precio_p from cliente_precios WHERE id_cliente=$idCliente AND id_inventario=$idProducto";
        }else{
            "SELECT precio_p_iva from cliente_precios WHERE id_cliente=$idCliente AND id_inventario=$idProducto";
        }

        try {
            val cursor = base.query(consulta)
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    precioIva = cursor.getFloat(0)
                }
            }
        }catch (e:Exception){
            println("ERROR AL BUSCAR EL PRECIO PERSONALIZADO -> ${e.message}")
        }
        return precioIva
    }

    //FUNCION PARA OBTENER LAS BONIFICACIONES PERSONALIDAS POR ID CLIENTE E ID PRODUCTO
    fun obtenerBonificacionCliente(idCliente: Int, idProducto: Int, context: Context) : Float{
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var bonificacion = 0f

        try {
            val consulta = "SELECT bonificado FROM cliente_precios " +
                    "WHERE id_cliente = $idCliente AND id_inventario = $idProducto"
            val cursor = base.query(consulta)
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    bonificacion = cursor.getFloat(0)
                }
            }
        }catch (e:Exception){
            println("ERROR AL BUSCAR LA BONIFICACIONI PERSONALIZADA ->  ${e.message}")
        }
        return bonificacion
    }

    //FUNCION PARA REGISTRAR EL CLIENTE EN EL SERVIDOR
    suspend fun enviarRegistroClienteAlServidor(context: Context, cliente: Cliente) : Boolean {
        var envio = false
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val clienteJson = convertirClienteToJson(context, cliente)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)
        try {
            val objecto =
                Gson().toJson(clienteJson)
            val ruta: String = servidor + "clientes/registrar"
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
                                if (res.getInt("idCliente") > 0 && !res.isNull("respuesta")) {
                                    val idCliente: Int = res.getInt("idCliente")
                                    val respues : String = res.getString("respuesta")
                                    val codCliente : String = res.getString("codigo")
                                    if(idCliente == 0){
                                        println("ERROR")
                                    }else{
                                        if(respues == "CLIENTE_REGISTRADO"){
                                            registrarClienteDataBase(cliente, idCliente, codCliente, context)
                                        }else{
                                            actualizarClienteDataBase(cliente, idCliente, context)
                                        }

                                        envio = true
                                    }
                                }
                            } catch (e: Exception) {
                                println("ERROR DE LECTURA EN LA RESPUESTA 201 " + e.message)
                            }
                        }
                    }else {
                        println("ERROR NO SE LOGRO REGISTRAR EL CLIENTE EN EL SERVIDOR")
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main){
                        funciones.mensaje(context, "INESTABILIDAD EN LA CONEXION \n INTENTE MAS TARDE \n ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main){
                funciones.mensaje(context, "PROBLEMAS DE CONEXION CON EL SERVIDOR \n INTENTE MAS TARDE \n ${e.message}")
            }
        }
        return envio
    }

    //FUNCION PARA CONVERTIR LOS DATOS DE REGISTRO DEL CLIENTE EN JSON
    private fun convertirClienteToJson(context: Context, cliente: Cliente): JsonObject {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        val json = JsonObject()
        json.addProperty("Id", cliente.Id)
        json.addProperty("Codigo", cliente.Codigo)
        json.addProperty("Cliente", cliente.Cliente)
        json.addProperty("Nrc", cliente.Nrc)
        json.addProperty("Dui", cliente.Dui)
        json.addProperty("Giro", cliente.Giro)
        json.addProperty("Nit", cliente.Nit)
        json.addProperty("Categoria_cliente", cliente.Categoria_cliente)
        json.addProperty("Terminos_cliente", cliente.Terminos_cliente)
        json.addProperty("Plazo_credito", cliente.Plazo_credito)
        json.addProperty("Limite_credito", cliente.Limite_credito)
        json.addProperty("Balance", cliente.Balance)
        json.addProperty("Estado_credito", cliente.Estado_credito)
        json.addProperty("Direccion", cliente.Direccion)
        json.addProperty("Municipio", cliente.Municipio)
        json.addProperty("Departamento", cliente.Departamento)
        json.addProperty("Telefono1", cliente.Telefono_1)
        json.addProperty("Telefono2", cliente.Telefono_2)
        json.addProperty("Correo", cliente.Correo)
        json.addProperty("Contacto", cliente.Contacto)
        json.addProperty("Id_ruta", cliente.Id_ruta)
        json.addProperty("Id_vendedor", cliente.Id_vendedor)
        json.addProperty("vendedor", cliente.Vendedor)
        json.addProperty("Status", cliente.Status)
        json.addProperty("Aporte_mensual", cliente.Aporte_mensual)
        json.addProperty("Firmar_pagare_app", cliente.Firmar_pagare_app)
        json.addProperty("Persona_juridica", cliente.Persona_juridica)
        json.addProperty("dteGiro", cliente.dteGiro)
        json.addProperty("Ruta", cliente.Ruta)
        json.addProperty("DTECodDepto", cliente.DTECodDepto)
        json.addProperty("DTECodMunicipio", cliente.DTECodMunicipio)
        json.addProperty("DTECodPais", cliente.DTECodPais)
        json.addProperty("DTEDireccion", cliente.DTEDireccion)
        json.addProperty("DTEPais", cliente.DTEPais)
        json.addProperty("DTECorreo", cliente.DTECorreo)
        json.addProperty("DTETelefono", cliente.DTETelefono)
        json.addProperty("DTECodGiro", cliente.DTECodGiro)
        json.addProperty("Latitud_app", cliente.Latitud)
        json.addProperty("Longitud_app", cliente.Longitud)
        json.addProperty("Nombre_comercial", cliente.NombreComercial)
        json.addProperty("DTEDistrito", cliente.DTEDistrito)
        json.addProperty("DTECodDistrito", cliente.DTECodDistrito)


        return json
    }

    //FUNINON PARA REGISTRAR EL NUEVO CLIENTE EN SLITE
    private fun registrarClienteDataBase(cliente: Cliente, idCliente: Int, codigo: String, context: Context) {
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.beginTransaction()

            val data = ContentValues()
            data.put("Id", idCliente)
            data.put("Codigo", codigo)
            data.put("Cliente", funciones.validate(cliente.Cliente))
            data.put("Dui", funciones.validate(cliente.Dui))
            data.put("Nit", funciones.validate(cliente.Nit))
            data.put("Nrc", funciones.validate(cliente.Nrc))
            data.put("Giro", funciones.validate(cliente.Giro))
            data.put(
                "Categoria_cliente",
                funciones.validate(cliente.Categoria_cliente)
            )
            data.put(
                "Terminos_cliente",
                funciones.validate(cliente.Terminos_cliente)
            )
            data.put("Plazo_credito", funciones.validate(cliente.Plazo_credito))
            data.put("Limite_credito",
                funciones.validate(cliente.Limite_credito)
            )
            data.put("Balance", funciones.validate(cliente.Balance))
            data.put("Estado_credito", funciones.validate(cliente.Estado_credito))
            data.put("Direccion", funciones.validate(cliente.Direccion))
            data.put("Municipio", funciones.validate(cliente.Municipio))
            data.put("Departamento", funciones.validate(cliente.Departamento))
            data.put("Telefono_1", funciones.validate(cliente.Telefono_1))
            data.put("Telefono_2", funciones.validate(cliente.Telefono_2))
            data.put("Correo", funciones.validate(cliente.Correo))
            data.put("Contacto", funciones.validate((cliente.Contacto)))
            data.put("Id_ruta", funciones.validate(cliente.Id_ruta))
            data.put("Id_vendedor", funciones.validate(cliente.Id_vendedor))
            data.put("Vendedor", funciones.validate(cliente.Vendedor))
            data.put("Status", funciones.validate(cliente.Status))
            data.put(
                "Aporte_mensual",
                funciones.validate(cliente.Aporte_mensual)
            )
            data.put("Firmar_pagare_app", funciones.validate(cliente.Firmar_pagare_app))
            data.put("Persona_juridica", funciones.validate(cliente.Persona_juridica))
            data.put("dteGiro", funciones.validate(cliente.dteGiro))
            data.put("Ruta", funciones.validate(cliente.Ruta))
            data.put("DTECodDepto", funciones.validate(cliente.DTECodDepto))
            data.put("DTECodMunicipio", funciones.validate(cliente.DTECodDepto))
            data.put("DTECodPais", funciones.validate(cliente.DTECodPais))
            data.put("DTEDireccion", funciones.validate(cliente.DTEDireccion))
            data.put("DTEPais", funciones.validate(cliente.DTEPais))
            data.put("DTETelefono", funciones.validate(cliente.DTETelefono))
            data.put("DTECorreo", funciones.validate(cliente.DTECorreo))
            data.put("Latitud_app", funciones.validate(cliente.Latitud))
            data.put("Longitud_app", funciones.validate(cliente.Longitud))
            data.put("Nombre_comercial", funciones.validate(cliente.NombreComercial))
            data.put("DTECodGiro", funciones.validate(cliente.DTECodGiro))
            data.put("DTEDistrito", funciones.validate(cliente.DTEDistrito))
            data.put("DTECodDistrito", funciones.validate(cliente.DTECodDistrito))
            data.put("Mayorista", funciones.validate(cliente.Mayorista))

            bd.insert("clientes", SQLiteDatabase.CONFLICT_REPLACE, data)
            bd.setTransactionSuccessful()

        } catch (e: Exception) {
            println("ERROR AL REGISTRAR EL CLIENTE EN SQLITE -> " + e.message)
        } finally {
            bd.endTransaction()
        }
    }

    //FUNINON PARA ACTUALIZAR EL NUEVO CLIENTE EN SLITE
    private fun actualizarClienteDataBase(cliente: Cliente, idCliente: Int, context: Context) {
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.beginTransaction()

            val data = ContentValues()
            data.put("Id", idCliente)
            data.put("Cliente", funciones.validate(cliente.Cliente))
            data.put("Dui", funciones.validate(cliente.Dui))
            data.put("Nit", funciones.validate(cliente.Nit))
            data.put("Nrc", funciones.validate(cliente.Nrc))
            data.put("Giro", funciones.validate(cliente.Giro))
            data.put(
                "Categoria_cliente",
                funciones.validate(cliente.Categoria_cliente)
            )
            data.put(
                "Terminos_cliente",
                funciones.validate(cliente.Terminos_cliente)
            )
            data.put("Plazo_credito", funciones.validate(cliente.Plazo_credito))
            data.put("Limite_credito",
                funciones.validate(cliente.Limite_credito)
            )
            data.put("Balance", funciones.validate(cliente.Balance))
            data.put("Estado_credito", funciones.validate(cliente.Estado_credito))
            data.put("Direccion", funciones.validate(cliente.Direccion))
            data.put("Municipio", funciones.validate(cliente.Municipio))
            data.put("Departamento", funciones.validate(cliente.Departamento))
            data.put("Telefono_1", funciones.validate(cliente.Telefono_1))
            data.put("Telefono_2", funciones.validate(cliente.Telefono_2))
            data.put("Correo", funciones.validate(cliente.Correo))
            data.put("Contacto", funciones.validate((cliente.Contacto)))
            data.put("Id_ruta", funciones.validate(cliente.Id_ruta))
            data.put("Status", funciones.validate(cliente.Status))
            data.put(
                "Aporte_mensual",
                funciones.validate(cliente.Aporte_mensual)
            )
            data.put("Firmar_pagare_app", funciones.validate(cliente.Firmar_pagare_app))
            data.put("Persona_juridica", funciones.validate(cliente.Persona_juridica))
            data.put("dteGiro", funciones.validate(cliente.dteGiro))
            data.put("Ruta", funciones.validate(cliente.Ruta))
            data.put("DTECodDepto", funciones.validate(cliente.DTECodDepto))
            data.put("DTECodMunicipio", funciones.validate(cliente.DTECodDepto))
            data.put("DTECodPais", funciones.validate(cliente.DTECodPais))
            data.put("DTEDireccion", funciones.validate(cliente.DTEDireccion))
            data.put("DTEPais", funciones.validate(cliente.DTEPais))
            data.put("DTETelefono", funciones.validate(cliente.DTETelefono))
            data.put("DTECorreo", funciones.validate(cliente.DTECorreo))
            data.put("Latitud_app", funciones.validate(cliente.Latitud))
            data.put("Longitud_app", funciones.validate(cliente.Longitud))
            data.put("Nombre_comercial", funciones.validate(cliente.NombreComercial))
            data.put("DTECodGiro", funciones.validate(cliente.DTECodGiro))
            data.put("DTEDistrito", funciones.validate(cliente.DTEDistrito))
            data.put("DTECodDistrito", funciones.validate(cliente.DTECodDistrito))

            bd.update("clientes", SQLiteDatabase.CONFLICT_REPLACE, data,"Id = ?", arrayOf(idCliente.toString()))

            bd.setTransactionSuccessful()

        } catch (e: Exception) {
            println("ERROR AL REGISTRAR EL CLIENTE EN SQLITE -> " + e.message)
        } finally {
            bd.endTransaction()
        }
    }

    //-------------------------------------------------------
    //Funcion para obtener los precios personalizados de clientes del servidor 30-05-2026
    //-------------------------------------------------------
    suspend fun obtenerClientesPrecios(context: Context, dialogo: AlertDialogo){
        withContext(Dispatchers.IO){

            inicializarVariables(context)

            val baseUrl = servidor

            clientesDao = base.clienteDao()

            val api = RetrofitCliente.obtenerApi(baseUrl, context)

            val limite = BLOQUE
            var lastId = 0
            var hayMas = true
            var totalInsertados = 0


            try {

                val totalClientesPrecios = try {
                    api.obtenerTotalRegistrosClientesPrecios()
                }catch (e: Exception){
                    Timber.e(e, "[CLIENTES_CONTROLLER] ERROR AL OBTENER EL TOTAL DE REGISTROS DE CLIENTES PRECIOS  -> ${e.message}")
                    null
                }


                while (hayMas){
                    val respuesta = api.obtenerClientesPrecios(lastId, limite)

                    if(respuesta.isNotEmpty() && respuesta.last().id != 0){

                        val registros = respuesta.map {
                            ClientePreciosEntity(
                                id = it.id,
                                idCliente = it.idCliente ?: 0,
                                idInventario = it.idInventario ?: 0,
                                precioP = it.precioP ?: 0.0,
                                precioPiva = it.precioPiva ?: 0.0,
                                bonificado = it.bonificado ?: 0.0
                            )
                        }

                        clientesDao.insertarPreciosPersonalizados(registros)
                        totalInsertados += registros.size


                        //Calculando el porcentaje
                        if(totalClientesPrecios != null && totalClientesPrecios > 0){
                            val progreso = (totalInsertados * 100) / totalClientesPrecios

                            withContext(Dispatchers.Main){
                                dialogo.changeText("Cargando Precios Personalizados: $progreso %")
                            }
                        }

                        lastId = respuesta.last().id

                    }else{
                        hayMas = false
                    }

                }

            }catch (e: Exception){
                Timber.e(e,"[CLIENTES_CONTROLLER] ERROR AL OBTENER LOS PRECIOS PERSONALIZADOS DE LOS CLIENTES -> ${e.message}")
            }
        }
    }

    //-------------------------------------------------------
    //Funcion para obtener los clientes del Servidor 30-05-2026
    //-------------------------------------------------------
    suspend fun obtenerListadoClientes(context: Context, dialogo: AlertDialogo){
        withContext(Dispatchers.IO){
            inicializarVariables(context)

            val baseUrl = servidor

            clientesDao = base.clienteDao()

            val api = RetrofitCliente.obtenerApi(baseUrl, context)

            val limite = BLOQUE
            var lastId = 0
            var hayMas = true
            var totalInsertados = 0

            try {

                //--------------------------------------------
                //Obteneniendo Cantidad de Registros
                //--------------------------------------------
                val totalRegistrosClientes = try {
                    api.obtenerTotalRegistrosClientes()
                }catch (e: Exception){
                    Timber.e(e, "[CLIENTE_CONTROLLER] ERROR AL OBTENER EL TOTAL DE REGISTROS DE CLIENTES -> ${e.message}")
                    null
                }

                while (hayMas){

                    val respuesta = if(cargarClientes == "V"){
                        api.obtenerListadoClientesVendedor(idVendedor, lastId, limite)
                    }else{
                        api.obtenerListadoClientes(lastId, limite)
                    }

                    if(respuesta.isNotEmpty() && respuesta.last().id != 0){

                        //println("RESPUES -> $respuesta")

                        val item = respuesta.map {
                            ClientesEntity(
                                id = it.id,
                                codigo = it.codigo,
                                cliente = it.cliente,
                                dui = it.dui,
                                nit = it.nit,
                                nrc = it.nrc,
                                giro = it.giro,
                                categoriaCliente = it.categoriaCliente,
                                terminosCliente = it.terminosCliente,
                                plazoCredito = it.plazoCredito,
                                limiteCredito = it.limiteCredito,
                                balance = it.balance,
                                estadoCredito = it.estadoCredito,
                                direccion = it.direccion,
                                municipio = it.municipio,
                                departamento = it.departamento,
                                telefono1 = it.telefono1,
                                telefono2 = it.telefono2,
                                correo = it.correo,
                                contacto = it.contacto,
                                idRuta = it.idRuta,
                                idVendedor = it.idVendedor,
                                vendedor = it.vendedor,
                                status = it.status,
                                ultimaVenta = it.ultimaVenta,
                                aporteMensual = it.aporteMensual,
                                fechaInventario = "",
                                firmarPagareApp = it.firmarPagareApp,
                                personaJuridica = it.personaJuridica,
                                dteGiro = it.dteGiro,
                                ruta = it.ruta,
                                dteDireccion = it.dteDireccion,
                                dteCodDepto = it.dteCodDepto,
                                dteCodMunicipio = it.dteCodMunicipio,
                                dteCodPais = it.dteCodPais,
                                dtePais = it.dtePais,
                                dteCorreo = it.dteCorreo,
                                dteTelefono = it.dteTelefono,
                                latitudApp = it.latitudApp,
                                longitudApp = it.longitudApp,
                                nombreComercial = it.nombreComercial,
                                mayorista = it.mayorista,
                                dteCodGiro = it.dteCodGiro,
                                dteDistrito = it.dteDistrito,
                                dteCodDistrito = it.dteCodDistrito
                            )
                        }

                        clientesDao.insertarClientes(item)
                        totalInsertados += item.size


                        if (totalRegistrosClientes != null && totalRegistrosClientes > 0){
                            val progreso = (totalInsertados * 100) / totalRegistrosClientes

                            withContext(Dispatchers.Main){
                                dialogo.changeText("Cargando Clientes: $progreso %")
                            }

                        }

                        lastId = respuesta.last().id

                    }else{
                        hayMas = false
                    }

                }

            }catch (e: Exception){
                Timber.e(e, "[CLIENTE_CONTROLLER] ERROR AL OBTENER EL LISTADO DE CLIENTES -> ${e.message}")
            }

        }
    }

    //-------------------------------------------------------
    //Funcion para obtener Sucursales de los Clientes del Servidor 30-05-2026
    //-------------------------------------------------------
    suspend fun obtenerListadoSucursales(context: Context, dialogo: AlertDialogo){
        withContext(Dispatchers.IO){

            inicializarVariables(context)

            val baseUrl = servidor

            val api = RetrofitCliente.obtenerApi(baseUrl, context)

            val limite = BLOQUE
            var lastId = 0
            var hayMas = true
            var totalInsertados = 0

            try {

                val totalRegistroSucursales = try {
                    api.obtenerTotalRegitroSucursal()
                }catch (e: Exception){
                    Timber.e(e, "[CLIENTE_CONTROLLER] ERROR AL OBTENER EL TOTAL DE REGISTROS DE CLIENTES -> ${e.message}")
                    null
                }

                while (hayMas){

                    val respuesta = api.obtenerListadoSucursales(lastId, limite)

                    if(respuesta.isNotEmpty() && respuesta.last().id != 0){

                        val item = respuesta.map {
                            ClienteSucursalEntity(
                                id = it.id,
                                idCliente = it.idCliente,
                                codigoSucursal = it.codigoSucursal?.trim(),
                                nombreSucursal = it.nombreSucursal?.trim(),
                                direccionSucursal = it.direccionSucursal?.trim(),
                                municipioSucursal = it.municipioSucursal?.trim(),
                                deptoSucursal = it.deptoSucursal?.trim(),
                                telefono1 = it.telefono1?.trim(),
                                telefono2 = it.telefono2?.trim(),
                                correoSucursal = it.dteCorreo?.trim(),
                                contatoSucursal = it.contatoSucursal?.trim(),
                                idRuta = it.idRuta,
                                ruta = it.ruta?.trim(),
                                dteCodDepto = it.dteCodDepto?.trim(),
                                dteCodMunicipio = it.dteCodMunicipio?.trim(),
                                dteCodPais = it.dteCodPais?.trim(),
                                dteCorreo = it.dteCorreo?.trim(),
                                latitudApp = it.latitudApp?.trim(),
                                longitudApp = it.longitudApp?.trim()
                            )
                        }

                        clientesDao.insertarSucursales(item)
                        totalInsertados += item.size

                        //Calculado el porcentahe
                        if(totalRegistroSucursales != null && totalRegistroSucursales > 0){
                            val progreso = (totalInsertados * 100) / totalRegistroSucursales

                            withContext(Dispatchers.Main){
                                dialogo.changeText("Cargando Sucursales Clientes: $progreso %")
                            }
                        }

                        lastId = respuesta.last().id

                    }else{
                        hayMas = false
                    }

                }

            }catch (e: Exception){
                Timber.e(e, "[CLIENTE_CONTROLLER] ERROR AL OBTENER EL LISTADO DE SUCURSALES -> ${e.message}")
            }


        }
    }

    //--------------------------------------------------------
    //Funcion para obtener el Listado de CxC Pendientes del Servidor 30-05-2026
    //--------------------------------------------------------
    suspend fun obtenerListadoCuentasPendientes(context: Context, dialogo: AlertDialogo){
        withContext(Dispatchers.IO){

            inicializarVariables(context)

            val baseUrl: String = servidor
            clientesDao = base.clienteDao()

            val api = RetrofitCliente.obtenerApi(baseUrl, context)

            val limite = BLOQUE
            var lastId = 0
            var hayMas = true
            var totalInsertados = 0


            try {

                //Obteniendo la cantidad de registros CxC pendientes
                val totalRegistros = try {
                    api.obtenerTotalRegistroCxCPendientes()
                }catch (e: Exception){
                    Timber.e(e, "[CLIENTE_CONTROLLER] ERROR AL OBTENER LA CANTIDAD DE REGISTROS DE CXC -> ${e.message}")
                    null
                }

                while (hayMas){

                    val respuesta = api.obtenerListadoCuentasPendientes(lastId, limite)

                    if(respuesta.isNotEmpty() && respuesta.last().id != 0){
                        val item = respuesta.map {
                            CuentasEntity(
                                id = it.id,
                                idCliente = it.idCliente,
                                codigoCliente = it.codigoCliente?.trim(),
                                documento = it.documento.trim(),
                                fecha = it.fecha.trim(),
                                valor = it.valor,
                                abonoInicial = it.abonoInicial,
                                saldoInicial = it.saldoInicial,
                                plazo = it.plazo,
                                fechaVencimiento = it.fechaVencimiento?.trim(),
                                saldoActual = it.saldoActual,
                                fechaUltPago = it.fechaUltPago,
                                valorPago = it.valorPago,
                                relacionado = it.relacionado,
                                status = it.status.trim(),
                                fechaCancelado = it.fechaCancelado?.trim(),
                                diasTardios = it.diasTardios
                            )
                        }

                        clientesDao.insertarCuentasPendientes(item)
                        totalInsertados += item.size

                        //Calculando porcentaje
                        if(totalRegistros != null && totalRegistros > 0){
                            val progreso = (totalInsertados * 100) / totalRegistros

                            withContext(Dispatchers.Main){
                                dialogo.changeText("Cargando CxC Pendientes: $progreso %")
                            }
                        }

                        lastId = respuesta.last().id

                    }else{
                        hayMas = false
                    }

                }

            }catch (e: Exception){
                Timber.e(e, "[CLIENTE_CONTROLLER] ERROR AL OBTENER LISTADO DE CXC DEL SERVIDOR -> ${e.message}")
            }

        }
    }

}