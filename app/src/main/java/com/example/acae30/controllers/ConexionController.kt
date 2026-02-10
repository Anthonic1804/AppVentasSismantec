package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import com.example.acae30.Funciones
import com.example.acae30.Retrofit.RetrofitCliente

class ConexionController {

    private var funciones = Funciones()

    private lateinit var  preferencias: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    //----------------------------------------
    //Función para validar los datos de conexion con el servidor
    //----------------------------------------
    fun validarDatosConexion(ip: String, puerto: String) : Boolean{
        var validos = true
        if(ip.isEmpty() or puerto.isEmpty()){
            validos = false
        }
        return validos
    }

    //--------------------------------------------
    //Funcón para conectar con el servidor
    //--------------------------------------------
    suspend fun verificarConexionServidor(ip: String, puerto: String) : String{
        val servidor = funciones.getServidor(ip, puerto)
        val api = RetrofitCliente.obtenerApi(servidor)

        var respuestaServidor: String = ""

        try {

            val respuesta = api.conectarServidor()
            respuestaServidor = respuesta.firstOrNull()?.respuestaConexion ?: "SIN_RESPUESTA"

        }catch (e:Exception){

            respuestaServidor = "ERROR_CONEXION -> " + e.message

        }

        return respuestaServidor

    }

    //--------------------------------------------
    //Función para Almacenar el Servidor en SQlite
    //--------------------------------------------
    fun almacenarServidorSQLite(context: Context, ip: String, puerto: String) : Boolean{
        var servidorRegistrado = false

        val db = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            db.beginTransaction()

            val data = ContentValues()
            data.put("ip", ip.trim().toString())
            data.put("puerto", puerto.trim().toString())

            db.insert("servidores", SQLiteDatabase.CONFLICT_REPLACE, data)

            servidorRegistrado = true

            db.setTransactionSuccessful()

        }catch (e:Exception){
            println("ERROR AL REGISTRAR EL SERVIDOR -> " + e.message)
            servidorRegistrado = false
        }finally {
            db.endTransaction()
        }

        return servidorRegistrado

    }


}