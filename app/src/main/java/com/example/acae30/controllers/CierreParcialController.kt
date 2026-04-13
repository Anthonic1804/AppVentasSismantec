package com.example.acae30.controllers

import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones
import com.example.acae30.Retrofit.RetrofitCliente
import com.example.acae30.database.AppDatabase
import com.example.acae30.modelos.cierreParcial.CierreParcialDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class CierreParcialController {
    private val funciones = Funciones()
    private lateinit var preferencias: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private lateinit var base : AppDatabase
    private lateinit var servidor : String
    private var vendedor = ""
    private var numeroCaja = 0

    //------------------------------------------------------------------
    //Funcion para inicializar las variables principales
    //------------------------------------------------------------------

    private fun iniciarlizarVariables(context: Context){
        base = AppDatabase.getInstance(context)
        preferencias = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        servidor = funciones.getServidor(preferencias.getString("ip", ""), preferencias.getInt("puerto", 0).toString(), context)
    }

    //------------------------------------------------------------------
    //Funcion para obtener los datos del Cierre Parcial
    //------------------------------------------------------------------

    suspend fun obtenerDatosCierreParcial(context: Context, numeroCaja: Int, fecha: LocalDate) : List<CierreParcialDTO>? {
        withContext(Dispatchers.Main){
            iniciarlizarVariables(context)
        }

        var respuesta : List<CierreParcialDTO>? = null

        withContext(Dispatchers.IO){
            val baseUrl = servidor
            val api = RetrofitCliente.obtenerApi(baseUrl, context)

            try {
                respuesta = api.obtenerDatosCierreParcialCaja(numeroCaja, fecha)
            }catch (e: Exception){
                println("ERROR AL OBTENER LOS DATOS DEL CIERRE PARCIAL -> ${e.message}")
            }

        }

        return respuesta

    }

}