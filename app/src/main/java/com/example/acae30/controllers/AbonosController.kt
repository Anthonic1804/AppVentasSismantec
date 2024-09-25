package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones
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
        val bd = funciones.getDataBase(context).writableDatabase
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

            bd.insert("abonos", null, data)
            bd.setTransactionSuccessful()
            guardado = true
        }catch (e:Exception){
            println("ERROR: INSERTAR ABONO -> ${e.message}")
        }finally {
            bd.endTransaction()
            bd.close()
        }
        return guardado
    }

    //FUNCION PARA SELECCIONAR TODOS LOS ABONOS POR FECHA
    fun obtenerAbonosSQLite(context: Context, fecha : String) : ArrayList<Abono>{
        val bd = funciones.getDataBase(context).readableDatabase
        val listaAbonos = ArrayList<Abono>()

        try{
            val cursor = bd.rawQuery("SELECT * FROM abonos WHERE fecha = '$fecha' AND borradoLogico = 0 ORDER BY id DESC", null)
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
        }finally {
            bd.close()
        }
        return listaAbonos
    }

    //FUNCION PARA EL ENVIO DEL ABONO AL SERVIDOR
    suspend fun enviarAbonoAlServidor(context: Context, abono: Abono) : Boolean {
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
                    connectTimeout = 20000
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
                                        CoroutineScope(Dispatchers.IO).launch {
                                            insertarAbonoCxc(context, abono, "ENVIAR", idAbono)
                                        }
                                        envio = true
                                    }
                                }
                            } catch (e: Exception) {
                                funciones.mensaje(context, "ERROR DE LECTURA EN LA RESPUESTA 201 " + e.message)
                            }
                        }
                    }else {
                        funciones.mensaje(context, "ERROR NO SE LOGRO REGISTRAR EL ABONO EN EL SERVIDOR")
                    }

                } catch (e: Exception) {
                    funciones.mensaje(context, "ERROR DE CONEXION CON EL SERVIDOR 1 " + e.message)
                }
            }
        } catch (e: Exception) {
            funciones.mensaje(context, "ERROR DE CONEXION CON EL SERVIDOR 2 " + e.message)
        }
        return envio
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
        val base = funciones.getDataBase(context).readableDatabase
        val abonos = ArrayList<Abono>()
        val fecha = funciones.obtenerFecha()
        try {
            val cursor = base.rawQuery("SELECT * FROM abonos WHERE abonoEnviado = 0 AND fecha='$fecha'", null)
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
        }finally {
            base.close()
        }
        return abonos
    }

}