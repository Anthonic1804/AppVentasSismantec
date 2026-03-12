package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import com.example.acae30.Funciones
import com.example.acae30.Utilidades.AgregarHeaders
import com.example.acae30.Utilidades.ConsumirEndpoint
import com.example.acae30.Utilidades.CrearSslNoSeguro
import com.example.acae30.modelos.Catalogos.DepartamentoModel
import com.example.acae30.modelos.Catalogos.DistritoModel
import com.example.acae30.modelos.Catalogos.MunicipioModel
import com.example.acae30.modelos.Catalogos.PaisModel
import com.example.acae30.modelos.Catalogos.RutaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class CatalogosController {

    //private lateinit var preferences: SharedPreferences
    //private var instancia = "CONFIG_SERVIDOR"
    private val funciones = Funciones()
    //private val agregarHeaders = AgregarHeaders()
    //private val utilidades = CrearSslNoSeguro()

    private val consumirEndpoint = ConsumirEndpoint()

    //FUNCION PARA OBTENER EL CATALOGO DE PAISES
    /*suspend fun obtenerCatalogoPais(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(
                preferences.getString("ip", ""),
                preferences.getInt("puerto", 0).toString(),
                context
        )

        try {
            val direccion = servidor + "catalogos/pais"
            val url = URL(direccion)

            val sslContext = utilidades.crearSslInseguro()
            val token = preferences.getString("token", "")

            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {

                try {

                    requestMethod = "GET"

                    //NUEVA FUNCION CENTRALIZADA
                    agregarHeaders.agregarHeaders(this, token, sslContext)

                    connectTimeout = 10000

                    when(responseCode){
                        200 -> {
                            inputStream.bufferedReader().use {

                                val response = inputStream.bufferedReader().readText()
                                val respuesta = JSONArray(response.toString())

                                if (respuesta.length() > 0) {
                                    insertarCatalogoPais(context, respuesta)
                                }
                            }
                        }
                        401 -> {
                            println("TOKEN INVALIDO O EXPIRADO")
                        }
                        else -> {
                            println("SERVIDOR: NO SE ENCONTRO EL CATALOGO DE PAISES")
                        }
                    }

                } catch (e: Exception) {
                    println("ERROR: NO SE OBTUVO RESPUESTA DEL SERVIDOR " + e.message)
                }
            }
        } catch (e: Exception) {
            println("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR " + e.message)
        }
    }*/
    suspend fun obtenerCatalogoPais(context: Context){
        val response = consumirEndpoint.consumirEndpoint(context, "catalogos/pais")

        if(response != null){
            val respuesta = JSONArray(response)

            if(respuesta.length() > 0){
                insertarCatalogoPais(context, respuesta)
            }
        }
    }

    //FUNCION PARA INSERTAR EL EL CATALOGO DE PAISES EN SQLITE
    private fun insertarCatalogoPais(context: Context, json: JSONArray){
        val supportDb = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            supportDb.beginTransaction()
            supportDb.execSQL("DELETE FROM cat_pais")

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Codigo", funciones.validateJsonIsnullString(dato, "codigo"))
                valor.put("Valor", funciones.validateJsonIsnullString(dato, "valor"))
                supportDb.insert("cat_pais", SQLiteDatabase.CONFLICT_REPLACE, valor)
            }

            supportDb.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE PAISES")
        } finally {
            supportDb.endTransaction()
        }
    }

    //FUNCION PARA OBTENER INFORMAC DEL PAIS SELECCIONADO
    fun obtenerInformacionPais(context: Context, string: String) : PaisModel?{

        val supportDb = funciones.obtenerInstancia(context).openHelper.readableDatabase

        var pais : PaisModel? = null

        try {
            val cursor = supportDb.query("SELECT id, codigo, valor FROM cat_pais WHERE valor = ?", arrayOf(string))
            if(cursor.count > 0){
                cursor.moveToFirst()
                pais = PaisModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER LOS PAISES DE SQLITE -> " + e.message)
        }
        return pais
    }

    //FUNCION PARA OBTENER EL LISTADO DE PAISES.
    fun obtenerListadoPaisesSQLite(context: Context, vista: String, pais: String): ArrayList<String> {

        val supportDb = funciones.obtenerInstancia(context).openHelper.readableDatabase

        val listadoPaises = ArrayList<String>()
        try {
            val cursor = supportDb.query("SELECT valor FROM cat_pais")
            if(cursor.count > 0){
                if(vista == "editar"){
                    listadoPaises.add(pais)
                }
                cursor.moveToFirst()
                do{
                    listadoPaises.add(cursor.getString(0))
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        }
        return listadoPaises
    }



    //FUNCON PARA OBTENER EL CATALOGO DE DEPARTAMENTO
    /*suspend fun obtenerCatalogoDepartamento(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)

        try {
            val direccion = servidor + "catalogos/departamento"
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
                                insertarCatalogoDepartamento(context, respuesta)
                            }
                        }
                    } else {
                        println("SERVIDOR: NO SE ENCONTRO EL CATALOGO DE DEPARTAMENTO")
                    }
                } catch (e: Exception) {
                    println("ERROR: NO SE OBTUVO RESPUESTA DEL SERVIDOR " + e.message)
                }
            }
        } catch (e: Exception) {
            println("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR " + e.message)
        }
    }*/
    suspend fun obtenerCatalogoDepartamento(context: Context){
        val response = consumirEndpoint.consumirEndpoint(context, "catalogos/departamento")
        if(response != null){
            val respuesta = JSONArray(response)

            if (respuesta.length() > 0) {
                insertarCatalogoDepartamento(context, respuesta)
            }
        }
    }

    //FUNCION PARA OBTENER EL CATALOGO DE PAISES DE SQLITE
    fun obtenerInformacionDepartamento(context: Context, departamento: String) : DepartamentoModel?{
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var listaDepartamento : DepartamentoModel? = null

        try {
            val cursor = db.query("SELECT id, codigo, valor FROM cat_departamento WHERE valor = ?", arrayOf(departamento))
            if(cursor.count > 0){
                cursor.moveToFirst()
                listaDepartamento = DepartamentoModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER LOS PAISES DE SQLITE -> " + e.message)
        }
        return listaDepartamento
    }

    //FUNCION PARA OBTENER EL LISTADO DE DEPARTAMENTOS.
    fun obtenerListadoDepartamentosSQLite(context: Context, codigoPais : String, vista: String, depto: String): ArrayList<String> {
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val listadoPaises = ArrayList<String>()

        val consulta : String = if(codigoPais == "SV"){
            "SELECT valor FROM cat_departamento WHERE codigo != '00'"
        }else{
            "SELECT valor FROM cat_departamento WHERE codigo = '00'"
        }
        try {
            val cursor = db.query(consulta)
            if(cursor.count > 0){
                if(vista == "editar"){
                    listadoPaises.add(depto)
                }else{
                    listadoPaises.add("-- SELECCIONE --")
                }

                cursor.moveToFirst()
                do{
                    listadoPaises.add(cursor.getString(0))
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        }
        return listadoPaises
    }

    //FUNCION PARA INSERTAR EL CATALOGO DE DEPARTAMENTOS EN SQLITE
    private fun insertarCatalogoDepartamento(context: Context, json: JSONArray){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.beginTransaction()
            bd.execSQL("DELETE FROM cat_departamento")

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Codigo", funciones.validateJsonIsnullString(dato, "codigo"))
                valor.put("Valor", funciones.validateJsonIsnullString(dato, "valor"))
                bd.insert("cat_departamento", SQLiteDatabase.CONFLICT_REPLACE, valor)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE DEPARTAMENTO")
        } finally {
            bd.endTransaction()
        }
    }




    //FUNCION PARA OBTENER EL CATALOGO DE MUNICIPIOS
    /*suspend fun obtenerCatalogoMunicipio(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)

        try {
            val direccion = servidor + "catalogos/municipio"
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
                                insertarCatalogoMuncipios(context, respuesta)
                            }
                        }
                    } else {
                        println("SERVIDOR: NO SE ENCONTRO EL CATALOGO DE MUNICIPIOS")
                    }
                } catch (e: Exception) {
                    println("ERROR: NO SE OBTUVO RESPUESTA DEL SERVIDOR " + e.message)
                }
            }
        } catch (e: Exception) {
            println("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR " + e.message)
        }
    }*/
    suspend fun obtenerCatalogoMunicipio(context: Context){
        val response = consumirEndpoint.consumirEndpoint(context, "catalogos/municipio")

        if(response != null){
            val respuesta = JSONArray(response)

            if (respuesta.length() > 0) {
                insertarCatalogoMuncipios(context, respuesta)
            }
        }
    }

    //FUNCION PARA INSERTAR EL CATALOGO DE MUNICIPIOS EN SQLITE
    private fun insertarCatalogoMuncipios(context: Context, json: JSONArray){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.beginTransaction()
            bd.execSQL("DELETE FROM cat_municipio")

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Codigo", funciones.validateJsonIsnullString(dato, "codigo"))
                valor.put("Valor", funciones.validateJsonIsnullString(dato, "valor"))
                valor.put("Departamento", funciones.validateJsonIsnullString(dato, "departamento"))
                valor.put("Id_Departamento", funciones.validateJsonIsnullString(dato, "id_Departamento"))
                valor.put("CodPais", funciones.validateJsonIsnullString(dato, "codPais"))
                bd.insert("cat_municipio", SQLiteDatabase.CONFLICT_REPLACE, valor)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE MUNICIPIOS")
        } finally {
            bd.endTransaction()
        }
    }

    //FUNCION PARA OBTENER EL LISTADO DE MUNICIPIOS.
    fun obtenerListadoMunicipiosSQLite(context: Context, codigoPais: String, codigoDepto : String, vista: String, muni: String): ArrayList<String> {
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val listadoPaises = ArrayList<String>()

        val consulta : String = if(codigoPais == "SV" && codigoDepto != "00"){
            "SELECT valor FROM cat_municipio WHERE codigo != '00' AND CodPais = 'SV' AND departamento = '$codigoDepto'"
        }else{
            "SELECT valor FROM cat_municipio WHERE codigo = '00'"
        }
        try {
            val cursor = db.query(consulta)
            if(cursor.count > 0){
                if(vista == "editar"){
                    listadoPaises.add(muni)
                }else{
                    listadoPaises.add("-- SELECCIONE --")
                }

                cursor.moveToFirst()
                do{
                    listadoPaises.add(cursor.getString(0))
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        }
        return listadoPaises
    }

    //FUNCION PARA OBTENER EL CATALOGO DE PAISES DE SQLITE
    fun obtenerInformacionMunicipio(context: Context, nombreMunicipio: String) : MunicipioModel?{
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var municipio : MunicipioModel? = null

        try {
            val cursor = db.query("SELECT id, codigo, valor, Departamento, Id_Departamento, CodPais FROM cat_municipio WHERE valor = ?", arrayOf(nombreMunicipio))
            if(cursor.count > 0){
                cursor.moveToFirst()
                municipio = MunicipioModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getString(5)
                )
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER LOS MUNICIPIOS DE SQLITE -> " + e.message)
        }
        return municipio
    }



    //FUNCION PARA OBTENER EL CATALOGO DE DISTRITOS
    /*suspend fun obtenerCatalogoDistrito(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)

        try {
            val direccion = servidor + "catalogos/distrito"
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
                                insertarCatalogoDistritos(context, respuesta)
                            }
                        }
                    } else {
                        println("SERVIDOR: NO SE ENCONTRO EL CATALOGO DE DISTRITOS")
                    }
                } catch (e: Exception) {
                    println("ERROR: NO SE OBTUVO RESPUESTA DEL SERVIDOR " + e.message)
                }
            }
        } catch (e: Exception) {
            println("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR " + e.message)
        }
    }*/
    suspend fun obtenerCatalogoDistrito(context: Context){
        val response = consumirEndpoint.consumirEndpoint(context, "catalogos/distrito")

        if(response != null){
            val respuesta = JSONArray(response)

            if (respuesta.length() > 0) {
                insertarCatalogoDistritos(context, respuesta)
            }
        }
    }

    //FUNCION PARA INSERTAR EL CATALOGO DE DISTRITOS EN SQLITE
    private fun insertarCatalogoDistritos(context: Context, json: JSONArray){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.beginTransaction()
            bd.execSQL("DELETE FROM cat_distrito")

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Codigo", funciones.validateJsonIsnullString(dato, "codigo"))
                valor.put("Valor", funciones.validateJsonIsnullString(dato, "valor"))
                valor.put("Departamento", funciones.validateJsonIsnullString(dato, "departamento"))
                valor.put("Id_Departamento", funciones.validateJsonIsnullString(dato, "id_Departamento"))
                valor.put("Municipio", funciones.validateJsonIsnullString(dato, "municipio"))
                valor.put("Id_Municipio", funciones.validateJsonIsnullString(dato, "id_Municipio"))
                bd.insert("cat_distrito", SQLiteDatabase.CONFLICT_REPLACE, valor)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE DISTRITOS")
        } finally {
            bd.endTransaction()
        }
    }

    //FUNCION PARA OBTENER EL LISTADO DE MUNICIPIOS.
    fun obtenerListadoDistritosSQLite(context: Context, codigoDepto: String, codigoMuni : String, codigoPais : String, vista: String, distrito: String): ArrayList<String> {
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val listadoPaises = ArrayList<String>()

        val consulta : String = if(codigoPais == "SV" && codigoDepto != "00" && codigoMuni != "00"){
            "SELECT valor FROM cat_distrito WHERE codigo != '00' AND municipio = '$codigoMuni' AND departamento = '$codigoDepto'"
        }else{
            "SELECT valor FROM cat_distrito WHERE codigo = '00'"
        }
        try {
            val cursor = db.query(consulta)
            if(cursor.count > 0){
                if(vista == "editar"){
                    listadoPaises.add(distrito)
                }else{
                    listadoPaises.add("-- SELECCIONE --")
                }

                cursor.moveToFirst()
                do{
                    listadoPaises.add(cursor.getString(0))
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        }
        return listadoPaises
    }

    //FUNCION PARA OBTENER EL CATALOGO DE PAISES DE SQLITE
    fun obtenerInformacionDistrito(context: Context, string: String) : DistritoModel?{
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var distrito : DistritoModel? = null

        try {
            val cursor = db.query("SELECT id, codigo, valor, Departamento, Id_Departamento, Municipio, Id_Municipio FROM cat_distrito WHERE valor = ?", arrayOf(string))
            if(cursor.count > 0){
                cursor.moveToFirst()
                distrito = DistritoModel(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getString(5),
                    cursor.getInt(6)
                )
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER LOS MUNICIPIOS DE SQLITE -> " + e.message)
        }
        return distrito
    }



    //FUNCION PARA OBTENER EL CATALOGO DE GIROS
    /*suspend fun obtenerCatalogoGiro(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)

        try {
            val direccion = servidor + "catalogos/giro"
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
                                insertarCatalogoGiros(context, respuesta)
                            }
                        }
                    } else {
                        println("SERVIDOR: NO SE ENCONTRO EL CATALOGO DE GIROS")
                    }
                } catch (e: Exception) {
                    println("ERROR: NO SE OBTUVO RESPUESTA DEL SERVIDOR " + e.message)
                }
            }
        } catch (e: Exception) {
            println("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR " + e.message)
        }
    }*/
    suspend fun obtenerCatalogoGiro(context: Context){
        val response = consumirEndpoint.consumirEndpoint(context, "catalogos/giro")

        if(response != null){
            val respuesta = JSONArray(response)

            if (respuesta.length() > 0) {
                insertarCatalogoGiros(context, respuesta)
            }
        }
    }

    //FUNCION PARA INSERTAR EL CATALOGO DE GIROS EN SQLITE
    private fun insertarCatalogoGiros(context: Context, json: JSONArray){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.beginTransaction()
            bd.execSQL("DELETE FROM cat_giro")

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Codigo", funciones.validateJsonIsnullString(dato, "codigo"))
                valor.put("Valor", funciones.validateJsonIsnullString(dato, "valor"))
                bd.insert("cat_giro", SQLiteDatabase.CONFLICT_REPLACE, valor)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE GIROS")
        } finally {
            bd.endTransaction()
        }
    }

    //FUNCION PARA OBTENER EL GIRO DE SQLITE
    fun obtenerInformacionGiro(context: Context, codigo: String) : String{
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var item = ""

        try {
            val cursor = db.query("SELECT valor FROM cat_giro WHERE codigo = ?", arrayOf(codigo))
            if(cursor.count > 0){
                cursor.moveToFirst()
                item = cursor.getString(0)
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER LOS MUNICIPIOS DE SQLITE -> " + e.message)
        }
        return item
    }



    //FUNCION PARA OBTENER EL CATALOGO DE RUTAS
    /*suspend fun obtenerCatalogoRuta(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)

        try {
            val direccion = servidor + "catalogos/ruta"
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
                                insertarCatalogoRutas(context, respuesta)
                            }
                        }
                    } else {
                        println("SERVIDOR: NO SE ENCONTRO EL CATALOGO DE RUTAS")
                    }
                } catch (e: Exception) {
                    println("ERROR: NO SE OBTUVO RESPUESTA DEL SERVIDOR " + e.message)
                }
            }
        } catch (e: Exception) {
            println("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR " + e.message)
        }
    }*/
    suspend fun obtenerCatalogoRuta(context: Context){
        val response = consumirEndpoint.consumirEndpoint(context, "catalogos/ruta")

        if(response != null){
            val respuesta = JSONArray(response)

            if (respuesta.length() > 0) {
                insertarCatalogoRutas(context, respuesta)
            }
        }
    }

    //FUNCION PARA INSERTAR EL CATALOGO DE RUTAS EN SQLITE
    private fun insertarCatalogoRutas(context: Context, json: JSONArray){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.beginTransaction()
            bd.execSQL("DELETE FROM cat_ruta")

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Ruta", funciones.validateJsonIsnullString(dato, "ruta"))
                bd.insert("cat_ruta", SQLiteDatabase.CONFLICT_REPLACE, valor)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE RUTAS")
        } finally {
            bd.endTransaction()
        }
    }

    //FUNCION PARA OBTENER EL LISTADO DE RUTAS.
    fun obtenerListadoRutaSQLite(context: Context, vista: String, ruta: String, esSolicitudCarga: Boolean): ArrayList<String> {
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val listadoRutas = ArrayList<String>()

        try {
            val cursor = db.query("SELECT ruta FROM cat_ruta")
            if(cursor.count > 0){
                if(vista == "editar"){
                    listadoRutas.add(ruta)
                }else{
                    if(!esSolicitudCarga){
                        listadoRutas.add("-- SELECCIONE --")
                    }
                }

                cursor.moveToFirst()
                do{
                    listadoRutas.add(cursor.getString(0))
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        }
        return listadoRutas
    }

    //FUNCION PARA OBTENER EL CATALOGO DE RUTAS DE SQLITE
    fun obtenerInformacionRuta(context: Context, string: String) : RutaModel?{
        val db = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var distrito : RutaModel? = null

        try {
            val cursor = db.query("SELECT id, ruta FROM cat_ruta WHERE ruta = ?", arrayOf(string))
            if(cursor.count > 0){
                cursor.moveToFirst()
                distrito = RutaModel(
                    cursor.getInt(0),
                    cursor.getString(1)
                )
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER LAS RUTAS DE SQLITE -> " + e.message)
        }
        return distrito
    }

}