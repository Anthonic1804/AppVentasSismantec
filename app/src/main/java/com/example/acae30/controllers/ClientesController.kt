package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.view.View
import com.example.acae30.Detallepedido
import com.example.acae30.Funciones
import com.example.acae30.Visita
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.JSONmodels.ActualizarPagareFirmadoCliente
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
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

class ClientesController {

    private var funciones = Funciones()
    private var visitaController = VisitaController()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"


    //FUNCION PARA OBTENER LOS PRECIOS PERSONALIZADOS
    suspend fun obtenerPreciosPersonalizados(context: Context){
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto",0).toString())

        /*PRIMER TRY PARA OBTENER LA CANTIDAD DE REGISTROS
        * Y LUEGO CARGARLOS POR BLOQUES
        */
        try {
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
        }
    }

    //FUNCION PARA LAMACENAR LOS PRECIOS PERSONALIZADOS EN SQLITE
    private fun almacenarPrecioPersonalizados(json: JSONArray, context: Context){
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
    }

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
                    "DTECodDistrito FROM clientes " +
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
                        cursor.getString(42)
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
                            ""
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
        val url = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val datos = ActualizarPagareFirmadoCliente(
                idCliente
            )
            val objecto =
                Gson().toJson(datos)
            val ruta: String = url + "clientes/actualizarPagare"
            val url2 = URL(ruta)
            with(url2.openConnection() as HttpURLConnection) {
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
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())
        try {
            val objecto =
                Gson().toJson(clienteJson)
            val ruta: String = servidor + "clientes/registrar"
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

}