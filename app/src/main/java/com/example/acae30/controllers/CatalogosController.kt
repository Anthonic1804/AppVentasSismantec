package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones
import com.example.acae30.modelos.Catalogos.DepartamentoModel
import com.example.acae30.modelos.Catalogos.DistritoModel
import com.example.acae30.modelos.Catalogos.GiroModel
import com.example.acae30.modelos.Catalogos.MunicipioModel
import com.example.acae30.modelos.Catalogos.PaisModel
import com.example.acae30.modelos.Catalogos.RutaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class CatalogosController {

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private val funciones = Funciones()

    //FUNCION PARA OBTENER EL CATALOGO DE PAISES
    suspend fun obtenerCatalogoPais(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val direccion = servidor + "catalogos/pais"
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
                                insertarCatalogoPais(context, respuesta)
                            }
                        }
                    } else {
                        println("SERVIDOR: NO SE ENCONTRO EL CATALOGO DE PAISES")
                    }
                } catch (e: Exception) {
                    println("ERROR: NO SE OBTUVO RESPUESTA DEL SERVIDOR " + e.message)
                }
            }
        } catch (e: Exception) {
            println("ERROR: NO SE LOGRO CONECTAR CON EL SERVIDOR " + e.message)
        }
    }

    //FUNCION PARA INSERTAR EL EL CATALOGO DE PAISES EN SQLITE
    private fun insertarCatalogoPais(context: Context, json: JSONArray){
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd!!.beginTransaction()
            bd.execSQL("DELETE FROM cat_pais")

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Codigo", funciones.validateJsonIsnullString(dato, "codigo"))
                valor.put("Valor", funciones.validateJsonIsnullString(dato, "valor"))
                bd.insert("cat_pais", null, valor)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE PAISES")
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

    //FUNCION PARA OBTENER INFORMAC DEL PAIS SELECCIONADO
    fun obtenerInformacionPais(context: Context, string: String) : PaisModel?{
        val db = funciones.getDataBase(context).readableDatabase
        var pais : PaisModel? = null

        try {
            val cursor = db.rawQuery("SELECT id, codigo, valor FROM cat_pais WHERE valor = '$string'", null)
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
        }finally {
            db.close()
        }
        return pais
    }

    //FUNCION PARA OBTENER EL LISTADO DE PAISES.
    fun obtenerListadoPaisesSQLite(context: Context): ArrayList<String> {
        val db = funciones.getDataBase(context).readableDatabase
        val listadoPaises = ArrayList<String>()
        try {
            val cursor = db.rawQuery("SELECT valor FROM cat_pais", null)
            if(cursor.count > 0){
                cursor.moveToFirst()
                do{
                    listadoPaises.add(cursor.getString(0))
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listadoPaises
    }



    //FUNCON PARA OBTENER EL CATALOGO DE DEPARTAMENTO
    suspend fun obtenerCatalogoDepartamento(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val direccion = servidor + "catalogos/departamento"
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
    }

    //FUNCION PARA OBTENER EL CATALOGO DE PAISES DE SQLITE
    fun obtenerInformacionDepartamento(context: Context, departamento: String) : DepartamentoModel?{
        val db = funciones.getDataBase(context).readableDatabase
        var listaDepartamento : DepartamentoModel? = null

        try {
            val cursor = db.rawQuery("SELECT id, codigo, valor FROM cat_departamento WHERE valor = '$departamento'", null)
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
        }finally {
            db.close()
        }
        return listaDepartamento
    }

    //FUNCION PARA OBTENER EL LISTADO DE DEPARTAMENTOS.
    fun obtenerListadoDepartamentosSQLite(context: Context, codigoPais : String): ArrayList<String> {
        val db = funciones.getDataBase(context).readableDatabase
        val listadoPaises = ArrayList<String>()

        val consulta : String = if(codigoPais == "SV"){
            "SELECT valor FROM cat_departamento WHERE codigo != '00'"
        }else{
            "SELECT valor FROM cat_departamento WHERE codigo = '00'"
        }
        try {
            val cursor = db.rawQuery(consulta, null)
            if(cursor.count > 0){
                listadoPaises.add("-- SELECCIONE --")
                cursor.moveToFirst()
                do{
                    listadoPaises.add(cursor.getString(0))
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listadoPaises
    }

    //FUNCION PARA INSERTAR EL CATALOGO DE DEPARTAMENTOS EN SQLITE
    private fun insertarCatalogoDepartamento(context: Context, json: JSONArray){
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd!!.beginTransaction()
            bd.execSQL("DELETE FROM cat_departamento")

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Codigo", funciones.validateJsonIsnullString(dato, "codigo"))
                valor.put("Valor", funciones.validateJsonIsnullString(dato, "valor"))
                bd.insert("cat_departamento", null, valor)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE DEPARTAMENTO")
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }




    //FUNCION PARA OBTENER EL CATALOGO DE MUNICIPIOS
    suspend fun obtenerCatalogoMunicipio(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val direccion = servidor + "catalogos/municipio"
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
    }

    //FUNCION PARA INSERTAR EL CATALOGO DE MUNICIPIOS EN SQLITE
    private fun insertarCatalogoMuncipios(context: Context, json: JSONArray){
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd!!.beginTransaction()
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
                bd.insert("cat_municipio", null, valor)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE MUNICIPIOS")
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

    //FUNCION PARA OBTENER EL LISTADO DE MUNICIPIOS.
    fun obtenerListadoMunicipiosSQLite(context: Context, codigoPais: String, codigoDepto : String): ArrayList<String> {
        val db = funciones.getDataBase(context).readableDatabase
        val listadoPaises = ArrayList<String>()

        val consulta : String = if(codigoPais == "SV" && codigoDepto != "00"){
            "SELECT valor FROM cat_municipio WHERE codigo != '00' AND CodPais = 'SV' AND departamento = '$codigoDepto'"
        }else{
            "SELECT valor FROM cat_municipio WHERE codigo = '00'"
        }
        try {
            val cursor = db.rawQuery(consulta, null)
            if(cursor.count > 0){
                listadoPaises.add("-- SELECCIONE --")
                cursor.moveToFirst()
                do{
                    listadoPaises.add(cursor.getString(0))
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listadoPaises
    }

    //FUNCION PARA OBTENER EL CATALOGO DE PAISES DE SQLITE
    fun obtenerInformacionMunicipio(context: Context, nombreMunicipio: String) : MunicipioModel?{
        val db = funciones.getDataBase(context).readableDatabase
        var municipio : MunicipioModel? = null

        try {
            val cursor = db.rawQuery("SELECT id, codigo, valor, Departamento, Id_Departamento, CodPais FROM cat_municipio WHERE valor = '$nombreMunicipio'", null)
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
        }finally {
            db.close()
        }
        return municipio
    }



    //FUNCION PARA OBTENER EL CATALOGO DE DISTRITOS
    suspend fun obtenerCatalogoDistrito(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val direccion = servidor + "catalogos/distrito"
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
    }

    //FUNCION PARA INSERTAR EL CATALOGO DE DISTRITOS EN SQLITE
    private fun insertarCatalogoDistritos(context: Context, json: JSONArray){
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd!!.beginTransaction()
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
                bd.insert("cat_distrito", null, valor)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE DISTRITOS")
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

    //FUNCION PARA OBTENER EL LISTADO DE MUNICIPIOS.
    fun obtenerListadoDistritosSQLite(context: Context, codigoDepto: String, codigoMuni : String, codigoPais : String): ArrayList<String> {
        val db = funciones.getDataBase(context).readableDatabase
        val listadoPaises = ArrayList<String>()

        val consulta : String = if(codigoPais == "SV" && codigoDepto != "00" && codigoMuni != "00"){
            "SELECT valor FROM cat_distrito WHERE codigo != '00' AND municipio = '$codigoMuni' AND departamento = '$codigoDepto'"
        }else{
            "SELECT valor FROM cat_distrito WHERE codigo = '00'"
        }
        try {
            val cursor = db.rawQuery(consulta, null)
            if(cursor.count > 0){
                listadoPaises.add("-- SELECCIONE --")
                cursor.moveToFirst()
                do{
                    listadoPaises.add(cursor.getString(0))
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listadoPaises
    }

    //FUNCION PARA OBTENER EL CATALOGO DE PAISES DE SQLITE
    fun obtenerInformacionDistrito(context: Context, string: String) : DistritoModel?{
        val db = funciones.getDataBase(context).readableDatabase
        var distrito : DistritoModel? = null

        try {
            val cursor = db.rawQuery("SELECT id, codigo, valor, Departamento, Id_Departamento, Municipio, Id_Municipio FROM cat_distrito WHERE valor = '$string'", null)
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
        }finally {
            db.close()
        }
        return distrito
    }



    //FUNCION PARA OBTENER EL CATALOGO DE GIROS
    suspend fun obtenerCatalogoGiro(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val direccion = servidor + "catalogos/giro"
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
    }

    //FUNCION PARA INSERTAR EL CATALOGO DE GIROS EN SQLITE
    private fun insertarCatalogoGiros(context: Context, json: JSONArray){
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd!!.beginTransaction()
            bd.execSQL("DELETE FROM cat_giro")

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Codigo", funciones.validateJsonIsnullString(dato, "codigo"))
                valor.put("Valor", funciones.validateJsonIsnullString(dato, "valor"))
                bd.insert("cat_giro", null, valor)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE GIROS")
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

    //FUNCION PARA OBTENER EL GIRO DE SQLITE
    fun obtenerInformacionGiro(context: Context, codigo: String) : String{
        val db = funciones.getDataBase(context).readableDatabase
        var item = ""

        try {
            val cursor = db.rawQuery("SELECT valor FROM cat_giro WHERE codigo = '$codigo'", null)
            if(cursor.count > 0){
                cursor.moveToFirst()
                item = cursor.getString(0)
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER LOS MUNICIPIOS DE SQLITE -> " + e.message)
        }finally {
            db.close()
        }
        return item
    }



    //FUNCION PARA OBTENER EL CATALOGO DE RUTAS
    suspend fun obtenerCatalogoRuta(context: Context) {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())

        try {
            val direccion = servidor + "catalogos/ruta"
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
    }

    //FUNCION PARA INSERTAR EL CATALOGO DE RUTAS EN SQLITE
    private fun insertarCatalogoRutas(context: Context, json: JSONArray){
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd!!.beginTransaction()
            bd.execSQL("DELETE FROM cat_ruta")

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Id", dato.getInt("id"))
                valor.put("Ruta", funciones.validateJsonIsnullString(dato, "ruta"))
                bd.insert("cat_ruta", null, valor)
            }

            bd.setTransactionSuccessful()
        } catch (e: Exception) {
            println("ERROR NO SE LOGRO INSERTAR EL CATALOGO DE RUTAS")
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

    //FUNCION PARA OBTENER EL LISTADO DE RUTAS.
    fun obtenerListadoRutaSQLite(context: Context): ArrayList<String> {
        val db = funciones.getDataBase(context).readableDatabase
        val listadoRutas = ArrayList<String>()

        try {
            val cursor = db.rawQuery("SELECT ruta FROM cat_ruta", null)
            if(cursor.count > 0){
                listadoRutas.add("-- SELECCIONE --")
                cursor.moveToFirst()
                do{
                    listadoRutas.add(cursor.getString(0))
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listadoRutas
    }

    //FUNCION PARA OBTENER EL CATALOGO DE RUTAS DE SQLITE
    fun obtenerInformacionRuta(context: Context, string: String) : RutaModel?{
        val db = funciones.getDataBase(context).readableDatabase
        var distrito : RutaModel? = null

        try {
            val cursor = db.rawQuery("SELECT id, ruta FROM cat_ruta WHERE ruta = '$string'", null)
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
        }finally {
            db.close()
        }
        return distrito
    }

}