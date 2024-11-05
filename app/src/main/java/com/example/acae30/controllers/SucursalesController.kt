package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.InformacionSucursal
import com.example.acae30.modelos.SucursalModel
import com.example.acae30.modelos.SucursalesTarjetaModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SucursalesController {

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var funciones = Funciones()

    //FUNCION PARA OBTENER LAS SUCURSALES POR CLIENTE.
    fun obtenerSucursalesporIdCliente(context: Context, idCliente:Int): ArrayList<String> {
        val db = funciones.getDataBase(context).readableDatabase
        val listaSucursales = ArrayList<String>()
        try {
            val dataSucursal = db.rawQuery("SELECT nombre_sucursal FROM cliente_sucursal WHERE id_cliente='$idCliente'", null)
            if(dataSucursal.count > 0){
                dataSucursal.moveToFirst()
                listaSucursales.add("-- SELECCIONES UNA SUCURSAL --")
                do{
                    listaSucursales.add(dataSucursal.getString(0))
                }while (dataSucursal.moveToNext())
            }
            dataSucursal.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listaSucursales
    }

    //FUNCION PARA OBTENER LA INFORMACION DE LA SUCURSAL DEL CLIENTE
    fun obtenerInformacionSucursal(context: Context, sucursal: String, idCliente: Int) : InformacionSucursal?{
        val db = funciones.getDataBase(context).readableDatabase
        var datosSucursal : InformacionSucursal? = null

        try {
            val cursor = db.rawQuery("SELECT Id, id_cliente, codigo_sucursal, nombre_sucursal, direccion_sucursal, " +
                    "municipio_sucursal, depto_sucursal, telefono_1, correo_sucursal, " +
                    "Id_ruta, Ruta, DTECodDepto, DTECodMunicipio, DTECodPais, DTEPais  FROM cliente_sucursal " +
                    "WHERE nombre_sucursal='$sucursal' AND id_cliente=$idCliente", null)

            if(cursor.count > 0){
                cursor.moveToFirst()
                datosSucursal = InformacionSucursal(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getInt(9),
                    cursor.getString(10),
                    cursor.getString(11),
                    cursor.getString(12),
                    cursor.getString(13),
                    cursor.getString(14)
                )
            }else{
                println("NO SE ENCONTRARON DATOS DE LA SUCURSAL")
            }
            cursor.close()
        }catch (e:Exception){
            throw Exception("ERROR AL OBTENER LA INFORMACION DE LAS SUCURSALES -> " + e.message)
        }finally {
            db.close()
        }
        return datosSucursal
    }

    //FUNCION PARA OBTENER LAS SUCURSALES POR CLIENTE PARA LISTADO
    fun obtenerInfoSucursalesPorCliente(context: Context, idCliente:Int) : ArrayList<SucursalesTarjetaModel>{
        val db = funciones.getDataBase(context).readableDatabase
        val listaSucursales = ArrayList<SucursalesTarjetaModel>()
        try {
            val dataSucursal = db.rawQuery("SELECT codigo_sucursal, nombre_sucursal, depto_sucursal, municipio_sucursal, " +
                    "direccion_sucursal, telefono_1, Ruta, DTECorreo FROM cliente_sucursal WHERE id_cliente='$idCliente' LIMIT 30", null)
            if(dataSucursal.count > 0){
                dataSucursal.moveToFirst()
                do{
                    val escalas = SucursalesTarjetaModel(
                        dataSucursal.getString(0),
                        dataSucursal.getString(1),
                        dataSucursal.getString(2),
                        dataSucursal.getString(3),
                        dataSucursal.getString(4),
                        dataSucursal.getString(5),
                        dataSucursal.getString(6),
                        dataSucursal.getString(7),
                    )
                    listaSucursales.add(escalas)

                }while (dataSucursal.moveToNext())
            }
            dataSucursal.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listaSucursales
    }

    //FUNCION PARA REGISTRAR EL CLIENTE EN EL SERVIDOR
    suspend fun enviarRegistroSucursalAlServidor(context: Context, sucursal: SucursalModel) : Boolean {
        var envio = false
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val sucursalJson = convertirSucursalToJson(context, sucursal)
        val servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString())
        try {
            val objecto =
                Gson().toJson(sucursalJson)
            val ruta: String = servidor + "sucursales/registrar"
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
                                if (res.getInt("idSucursal") > 0 && !res.isNull("respuesta")) {
                                    val idSucursal: Int = res.getInt("idSucursal")
                                    if(idSucursal == 0){
                                        println("ERROR")
                                    }else{
                                        println("RESPUESTA DEL SERIVDOR -> $idSucursal")
                                        registrarSucursalDataBase(sucursal, idSucursal, context)
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
    private fun convertirSucursalToJson(context: Context, sucursal: SucursalModel): JsonObject {
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        val json = JsonObject()
        json.addProperty("Id", sucursal.Id)
        json.addProperty("id_cliente", sucursal.id_cliente)
        json.addProperty("codigo_sucursal", sucursal.codigo_sucursal)
        json.addProperty("nombre_sucursal", sucursal.nombre_sucursal)
        json.addProperty("direccion", sucursal.direccion)
        json.addProperty("municipio", sucursal.municipio)
        json.addProperty("departamento", sucursal.departamento)
        json.addProperty("telefono1", sucursal.telefono1)
        json.addProperty("telefono2", sucursal.telefono2)
        json.addProperty("correo", sucursal.correo)
        json.addProperty("contacto", sucursal.contacto)
        json.addProperty("id_ruta", sucursal.id_ruta)
        json.addProperty("ruta", sucursal.ruta)
        json.addProperty("DTECodDepto", sucursal.DTECodDepto)
        json.addProperty("DTECodMunicipio", sucursal.DTECodMunicipio)
        json.addProperty("DTECodPais", sucursal.DTECodPais)
        json.addProperty("DTEDireccion", sucursal.DTEDireccion)
        json.addProperty("DTEPais", sucursal.DTEPais)
        json.addProperty("DTETelefono", sucursal.DTETelefono)
        json.addProperty("DTECodDistrito", sucursal.DTECodDistrito)
        json.addProperty("DTEDistrito", sucursal.DTEDistrito)

        return json
    }

    //FUNINON PARA REGISTRAR EL NUEVO CLIENTE EN SLITE
    private fun registrarSucursalDataBase(sucursal: SucursalModel, idSucursal: Int, context: Context) {
        val bd = funciones.getDataBase(context).writableDatabase
        try {
            bd!!.beginTransaction()

            val data = ContentValues()
            data.put("Id", idSucursal)
            data.put("id_cliente", funciones.validate(sucursal.id_cliente))
            data.put("codigo_sucursal", funciones.validate(sucursal.codigo_sucursal))
            data.put("nombre_sucursal", funciones.validate(sucursal.nombre_sucursal))
            data.put("direccion_sucursal", funciones.validate(sucursal.direccion))
            data.put("municipio_sucursal", funciones.validate(sucursal.municipio))
            data.put("depto_sucursal", funciones.validate(sucursal.departamento))
            data.put("telefono_1", funciones.validate(sucursal.telefono1))
            data.put("telefono_2", funciones.validate(sucursal.telefono2))
            data.put("correo_sucursal", funciones.validate(sucursal.correo))
            data.put("contacto_sucursal", funciones.validate(sucursal.contacto))
            data.put("Id_ruta", funciones.validate(sucursal.id_ruta))
            data.put("Ruta", funciones.validate(sucursal.ruta))
            data.put("DTECodDepto", funciones.validate(sucursal.DTECodDepto))
            data.put("DTECodMunicipio", funciones.validate(sucursal.DTECodMunicipio))
            data.put("DTECodPais", funciones.validate(sucursal.DTECodPais))
            data.put("DTEPais", funciones.validate(sucursal.DTEPais))
            data.put("DTECorreo", funciones.validate(sucursal.DTECorreo))

            bd.insert("cliente_sucursal", null, data)
            bd.setTransactionSuccessful()

        } catch (e: Exception) {
            println("ERROR AL REGISTRAR LA SUCURSAL EN SQLITE -> " + e.message)
        } finally {
            bd!!.endTransaction()
            bd.close()
        }
    }

}