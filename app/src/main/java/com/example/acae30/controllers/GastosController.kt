package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones
import com.example.acae30.modelos.Abono
import com.example.acae30.modelos.GastoModel
import com.google.gson.Gson
import com.google.gson.JsonArray
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

class GastosController {

    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    //FUNCION PARA EL ENVIO DEL ABONO AL SERVIDOR
    suspend fun enviarGastoAlServidor(context: Context, gasto : GastoModel) : Boolean {
        var envio = false
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val abonoJson = convertirGastoAJson(context, gasto)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())
        try {
            val objecto =
                Gson().toJson(abonoJson)
            println(objecto)
            val ruta: String = servidor + "movimientosvarios/registrar"
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
                                if (res.getInt("idVarios") > 0 && !res.isNull("respuesta")) {
                                    val idGasto: Int = res.getInt("idVarios")
                                    if(idGasto == 0){
                                        println("ERROR")
                                    }else{
                                        println("RESPUESTA DEL SERVIDOR : $idGasto")
                                        CoroutineScope(Dispatchers.IO).launch {
                                            insertarGastoSQLite(context, gasto, idGasto)
                                        }
                                        envio = true
                                    }
                                }
                            } catch (e: Exception) {
                                println("ERROR DE LECTURA EN LA RESPUESTA 201 " + e.message)
                            }
                        }
                    }else {
                        println("ERROR NO SE LOGRO REGISTRAR EL GASTO EN EL SERVIDOR")
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

    //FUNCION PARA CONVERTIR EL OBJETO DEL ABONO EN JSON
    private fun convertirGastoAJson(context: Context, gasto: GastoModel): JsonObject {

        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        val json = JsonObject()
        json.addProperty("Fecha", gasto.fecha)
        json.addProperty("Tipo_movimiento", gasto.tipoMovimiento)
        json.addProperty("Concepto", gasto.concepto)
        json.addProperty("Cuenta_bco", gasto.cuentaBanco)
        json.addProperty("Banco", gasto.banco)
        json.addProperty("Num_Cheque_Tarjeta", gasto.numCheque)
        json.addProperty("Mas_info", gasto.mas_info)
        json.addProperty("Valor", gasto.valor)
        json.addProperty("Forma", gasto.forma)
        json.addProperty("Persona", gasto.persona)
        json.addProperty("Numero_Caja", gasto.numeroCaja)

        return json

    }

    //FUNCION PARA INSERTAR LOS ABONOS EN SQLITE
    private fun insertarGastoSQLite(context: Context, gasto : GastoModel, idGastoServer: Int) : Boolean{
        var guardado : Boolean = false
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd.beginTransaction()

            val data = ContentValues()
            data.put("Fecha", gasto.fecha)
            data.put("Tipo_movimiento", gasto.tipoMovimiento)
            data.put("Concepto", gasto.concepto)
            data.put("Cuenta_bco", gasto.cuentaBanco)
            data.put("Banco", gasto.banco)
            data.put("numero_cheque", gasto.numCheque)
            data.put("Mas_infor", gasto.mas_info)
            data.put("Valor", gasto.valor)
            data.put("Forma", gasto.forma)
            data.put("Persona", gasto.persona)
            data.put("NumeroCaja", gasto.numeroCaja)
            data.put("gastoEnviado", 1)
            data.put("idServidor", idGastoServer)

            bd.insert("gastos", null, data)
            bd.setTransactionSuccessful()
            guardado = true
        }catch (e:Exception){
            println("ERROR: INSERTAR EL GASTO -> ${e.message}")
        }finally {
            bd.endTransaction()
            bd.close()
        }
        return guardado
    }

    //FUNCION PARA OBTENER EL LISTADO DE GASTOS POR DIA
    fun obtenerGastosSQLite(context: Context) : ArrayList<GastoModel>{
        val bd = funciones.getDataBase(context).readableDatabase
        val listadoGastos = ArrayList<GastoModel>()
        val fecha = funciones.obtenerFecha()
        try{
            val cursor = bd.rawQuery("SELECT * FROM gastos WHERE fecha = '$fecha'", null)
            if(cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val gasto = GastoModel(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getFloat(8),
                        cursor.getString(9),
                        cursor.getString(10),
                        cursor.getInt(11)
                    )
                    listadoGastos.add(gasto)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e : Exception){
            funciones.mensaje(context,"ERROR: OBTENER LOS GASTOS -> ${e.message}")
        }finally {
            bd.close()
        }
        return listadoGastos
    }
}