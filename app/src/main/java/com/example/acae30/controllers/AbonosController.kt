package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout.DispatchChangeEvent
import com.example.acae30.AbonosCxc
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.modelos.Abono
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class AbonosController {

    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    //FUNCION PARA INSERTAR LOS ABONOS EN SQLITE
    fun insertarAbonoCxc(context: Context, abono: Abono, tipo: String, idAbonoServer: Int) : Boolean{
        var guardado : Boolean = false
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        var enviado = 1
        if(tipo == "GUARDAR"){
            enviado = 0
        }
        try {
            bd.beginTransaction()

            val data = ContentValues()
            data.put("fecha", abono.Fecha)
            data.put("idCliente", abono.IdCliente)
            data.put("codigoCliente", abono.codigoCliente)
            data.put("cliente", abono.Cliente)
            data.put("idSucursal", abono.IdSucursal)
            data.put("sucursal", abono.Sucursal)
            data.put("abono", abono.Abono)
            data.put("tipoPago", abono.Tipo_pago)
            data.put("numeroCheque", abono.Numero_cheque)
            data.put("cuenta", abono.Cuenta)
            data.put("banco", abono.Banco)
            data.put("idVendedor", abono.IdVendedor)
            data.put("vendedor", abono.Vendedor)
            data.put("fecha_hora_proceso", abono.Fecha_hora_proceso)
            data.put("idVisitaServer", abono.Id_app_visita)
            data.put("idAbonoServer", idAbonoServer)
            data.put("abonoEnviado", enviado)

            bd.insert("abonos", SQLiteDatabase.CONFLICT_REPLACE, data)
            bd.setTransactionSuccessful()
            guardado = true
        }catch (e:Exception){
            println("ERROR: INSERTAR ABONO -> ${e.message}")
        }finally {
            bd.endTransaction()
        }
        return guardado
    }

    //FUNCION PARA SELECCIONAR TODOS LOS ABONOS POR FECHA
    fun obtenerAbonosSQLite(context: Context, fecha : String) : ArrayList<Abono>{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val listaAbonos = ArrayList<Abono>()

        try{
            val cursor = bd.query("SELECT * FROM abonos WHERE fecha = ? AND borradoLogico = 0 ORDER BY id DESC", arrayOf(fecha))
            if(cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val abono = Abono(
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getString(6),
                        cursor.getFloat(7),
                        cursor.getString(8),
                        cursor.getString(9),
                        cursor.getString(10),
                        cursor.getString(11),
                        cursor.getInt(12),
                        cursor.getString(13),
                        cursor.getString(14),
                        cursor.getInt(15),
                        cursor.getInt(18),
                        cursor.getInt(16)
                    )

                    listaAbonos.add(abono)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e:Exception){
            funciones.mensaje(context,"ERROR: OBTENER LOS ABONOS -> ${e.message}")
        }
        return listaAbonos
    }

    //FUNCION PARA EL ENVIO DEL ABONO AL SERVIDOR
    suspend fun enviarAbonoAlServidor(context: Context, abono: Abono, tipo: String) : Boolean {
        var envio = false
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val abonoJson = convertirAbonoAJson(context, abono)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())
        try {
            val objecto =
                Gson().toJson(abonoJson)
            val ruta: String = servidor + "abonos"
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
                                if (res.getInt("idAbono") > 0 && !res.isNull("respuesta")) {
                                    val idAbono: Int = res.getInt("idAbono")
                                    if(idAbono == 0){
                                        println("ERROR")
                                    }else{
                                        when(tipo){
                                            "SINCRONIZANDO" -> {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    actualizandoAbonoCxc(context, abono, idAbono)
                                                }
                                            }
                                            else -> {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    insertarAbonoCxc(context, abono, "ENVIAR", idAbono)
                                                }
                                            }
                                        }
                                        envio = true
                                    }
                                }
                            } catch (e: Exception) {
                                println("ERROR DE LECTURA EN LA RESPUESTA 201 " + e.message)
                            }
                        }
                    }else {
                        println("ERROR NO SE LOGRO REGISTRAR EL ABONO EN EL SERVIDOR")
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

    //FUNCION PARA ACTUALIZAR EL ESTADO DE ENVIO DEL ABONO
    private fun actualizandoAbonoCxc(context: Context, abono: Abono, idAbono: Int) {
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE abonos SET idAbonoServer = $idAbono, abonoEnviado = 1 " +
                    "WHERE idCliente = ${abono.IdCliente} AND idSucursal = ${abono.IdSucursal} AND fecha = '${abono.Fecha}'")
        } catch (e: Exception) {
            println("ERROR AL ACTUALIZAR EL ABONO EN SQLITE -> " + e.message)
        }
    }

    //FUNCION PARA CONVERTIR EL OBJETO DEL ABONO EN JSON
    private fun convertirAbonoAJson(context: Context, abono: Abono): JsonObject {

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val puntoVenta = preferences.getString("puntoVenta", "").toString()

        val json = JsonObject()
        json.addProperty("Fecha", abono.Fecha)
        json.addProperty("IdCliente", abono.IdCliente)
        json.addProperty("Cliente", abono.Cliente)
        json.addProperty("IdSucursal", abono.IdSucursal)
        json.addProperty("Sucursal", abono.Sucursal)
        json.addProperty("Abono", abono.Abono)
        json.addProperty("Tipo_pago", abono.Tipo_pago)
        json.addProperty("Numero_cheque", abono.Numero_cheque)
        json.addProperty("Cuenta", abono.Cuenta)
        json.addProperty("Banco", abono.Banco)
        json.addProperty("IdVendedor", abono.IdVendedor)
        json.addProperty("Vendedor", abono.Vendedor)
        json.addProperty("Fecha_hora_proceso", abono.Fecha_hora_proceso)
        json.addProperty("Id_app_visita", abono.Id_app_visita)
        json.addProperty("Punto_venta", puntoVenta)

        return json

    }

    //FUNCION PARA SELECCIONAR LOS ABONO NO ENVIADOS
    fun obtenerAbonosNoEnviados(context: Context) : ArrayList<Abono>{
        val base = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val abonos = ArrayList<Abono>()
        val fecha = funciones.obtenerFecha()
        try {
            val cursor = base.query("SELECT * FROM abonos WHERE abonoEnviado = 0 AND fecha=?", arrayOf(fecha))
            if(cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val item = Abono(
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getString(6),
                        cursor.getFloat(7),
                        cursor.getString(8),
                        cursor.getString(9),
                        cursor.getString(10),
                        cursor.getString(11),
                        cursor.getInt(12),
                        cursor.getString(13),
                        cursor.getString(14),
                        cursor.getInt(15),
                        cursor.getInt(18),
                        cursor.getInt(16)
                    )
                    abonos.add(item)
                }while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e:Exception){
            println("ERROR AL OBTENER LOS ABONOS NO ENVIADOS -> " + e.message)
        }
        return abonos
    }

    //FUNCION PARA ANULAR UN ABONO ENVIADO
    private suspend fun anularAbonoEnviado(context: Context, idAbonoServer: Int) : Boolean{
        var anulado = false
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val abonoJson = convertirIdAbonoServerAJson(idAbonoServer)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())
        try {
            val objecto =
                Gson().toJson(abonoJson)
            val ruta: String = servidor + "abonos/anularAbono"
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
                                if (!res.isNull("respuesta")) {
                                    val resServidor : String = res.getString("respuesta")
                                    if(resServidor == "ABONO_ANULADO"){
                                        actualizarEstadoAbonoAnulado(context, idAbonoServer)
                                        anulado = true

                                        val intento = Intent(context, AbonosCxc::class.java)
                                        context.startActivity(intento)
                                    }
                                }
                            } catch (e: Exception) {
                                println("ERROR DE LECTURA EN LA RESPUESTA 201 " + e.message)
                            }
                        }
                    }else {
                        println("ERROR NO SE LOGRO REGISTRAR EL ABONO EN EL SERVIDOR")
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
        return anulado
    }

    //FUNCION PARA CONVERTIR EL IDABONOSERVER EN JSON
    private fun convertirIdAbonoServerAJson(idAbonoServer: Int) : JsonObject{
        val json = JsonObject()
        json.addProperty("IdAbono", idAbonoServer)

        return json
    }

    //FUNCION ACTUALIZAR ESTADO DE ABONO ANULADO
    private fun actualizarEstadoAbonoAnulado(context: Context, idAbonoServer: Int){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE abonos SET borradoLogico = 1 WHERE idAbonoServer = $idAbonoServer")
        }catch (e:Exception){
            println("ERROR AL ACTUALIZAR EL ESTADO DEL ABONO " + e.message)
        }
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    fun mensajeAnulacion(context: Context, cliente: String, idAbonoServer: Int){
        val dialog = AlertDialog.Builder(context)
            .setTitle("INFORMACION")
            .setMessage("DESEA ANULAR EL ABONO DEL CLIENTE : $cliente")
            .setNegativeButton("CANCELAR"){view, _ ->
                view.dismiss()
            }
            .setPositiveButton("ACEPTAR") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                  anularAbonoEnviado(context, idAbonoServer)
                }
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

}