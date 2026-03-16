package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.acae30.Funciones
import com.example.acae30.Retrofit.RetrofitCliente
import com.example.acae30.modelos.Servidores.ServidoresModel

class ConexionController {

    private var funciones = Funciones()

    //----------------------------------------
    //Función para validar los datos de conexion con el servidor
    //----------------------------------------
    fun validarDatosConexion(ip: String, nombre: String) : Boolean{
        var validos = true
        if(ip.isEmpty() or nombre.isEmpty()){
            validos = false
        }
        return validos
    }

    //--------------------------------------------
    //Funcón para conectar con el servidor
    //--------------------------------------------
    suspend fun verificarConexionServidor(ip: String, puerto: String, sslActivo: Int, context: Context) : String{
        val servidor = funciones.verificarServidor(ip, puerto, sslActivo)

        val api = RetrofitCliente.obtenerApi(servidor, context)

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
    fun almacenarServidorSQLite(context: Context, obj: ServidoresModel) : Boolean{
        var servidorRegistrado = false

        val puerto = obj.puerto

        val puertoServidor = if(puerto.isBlank()){
            0
        }else{
            puerto.trim()
        }

        val db = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            db.beginTransaction()

            val data = ContentValues()

            data.put("nombre", obj.nombre)
            data.put("ip", obj.ip)
            data.put("puerto", puertoServidor.toString())
            data.put("ssl", obj.ssl)

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

    //------------------------------------
    //Funcion para Obtener el Listado de Servidores
    //------------------------------------
    fun obtenerListadoServidores(context: Context) : ArrayList<ServidoresModel>{

        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val listaServidores = ArrayList<ServidoresModel>()

        try {
            val consulta = "SELECT Id, Nombre, Ip, Puerto, Ssl FROM servidores"
            val cursor = base.query(consulta)

            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    do {
                        val i = ServidoresModel(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getInt(4)
                        )
                        listaServidores.add(i)
                    }while (cursor.moveToNext())
                }
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL LISTADO DE SERVIDORES -> " + e.message)
        }
        return listaServidores
    }

    //------------------------------------------
    //Funcion para actualizar el servidor
    //------------------------------------------
    fun actualizarServidor(context: Context, obj: ServidoresModel) : Boolean{

        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var actualizado : Boolean = false

        try {
            val consulta = "UPDATE servidores SET Nombre = '${obj.nombre}', Ip = '${obj.ip}', Puerto = '${obj.puerto}', Ssl = ${obj.ssl} WHERE Id = ${obj.id}"
            base.execSQL(consulta)

            actualizado  = true
        }catch (e:Exception){
            println("ERROR AL ACTUALIZAR EL SERVIDOR -> " + e.message)
            actualizado = false
        }
        return actualizado
    }

    //------------------------------------------
    //Funcion para obtener el listado de nombre de los Servidores registrados
    //------------------------------------------
    fun obtenerListadoNombreServidores(context: Context) : ArrayList<String>{
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val listado = ArrayList<String>()

        val consulta = "SELECT nombre FROM servidores"

        try {
            val cursor = base.query(consulta)
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    listado.add("-- SELECCIONE --")
                    do {
                        listado.add(cursor.getString(0))
                    }while (cursor.moveToNext())
                }
            }
        }catch (e:Exception){
            println("ERROR AL GENERAR EL LISTADO DE NOMBRE DE SERVIDOR " + e.message)
        }
        return listado
    }

    //-----------------------------------------
    //Funcion para obtener datos del servidor seleccionado
    //-----------------------------------------
    fun obtenerInformacionServidorSeleccionado(context: Context, nombre: String) : ServidoresModel?{

        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var servidor : ServidoresModel? = null

        try {
            val consulta = "SELECT Id, Nombre, Ip, Puerto, Ssl FROM servidores WHERE nombre = '${nombre.trim()}'"
            val cursor = base.query(consulta)
            cursor.use {
                if(cursor.count > 0){
                    cursor.moveToFirst()
                    servidor = ServidoresModel(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4)
                    )
                }
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER EL SERVIDOR -> " + e.message)
        }
        return servidor
    }

    //-----------------------------------------
    //Funcion para eliminar un servidor
    //-----------------------------------------
    fun eliminarServidor(context: Context, idServidor: Int) : Boolean{
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        var eliminado : Boolean = false

        try {
            val consulta = "DELETE FROM servidores WHERE Id = $idServidor"
            base.execSQL(consulta)

            eliminado = true

        }catch (e:Exception){
            println("ERROR AL ELIMINAR EL SERVIDOR -> " + e.message)

            eliminado = false

        }

        return eliminado

    }

}