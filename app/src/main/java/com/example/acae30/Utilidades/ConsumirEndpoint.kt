package com.example.acae30.Utilidades

import android.content.Context
import com.example.acae30.Funciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class ConsumirEndpoint {

    private var instancia = "CONFIG_SERVIDOR"

    private val funciones = Funciones()
    private val sslNoSeguro = CrearSslNoSeguro()
    private val agregarHeaders = AgregarHeaders()

    //---------------------------------------
    //Función Generica para consumir los Endpoints HTTP Metodo GET
    //para reducir el codigo repetido en las funciones
    //09-03-2026
    //---------------------------------------

    suspend fun consumirEndpoint(
        context: Context,
        endpoint: String,
        metodo: String = "GET"
    ) : String? {

        val preferencias = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        val servidor = funciones.getServidor(
            preferencias.getString("ip", ""),
            preferencias.getInt("puerto", 0).toString(),
            context
        )

        val direccion = servidor + endpoint
        val token = preferencias.getString("token", "")
        val sslContext = sslNoSeguro.crearSslInseguro()

        return try {

            val url = URL(direccion)

            with(withContext(Dispatchers.IO) {
                url.openConnection()
            } as HttpURLConnection) {

                requestMethod = metodo

                agregarHeaders.agregarHeaders(this, token, sslContext)

                connectTimeout = 10000

                when(responseCode){

                    200 -> {
                        inputStream.bufferedReader().readText()
                    }
                    401 -> {
                        println("TOKEN INVALIDO O EXPIRADO: $responseCode")
                        null
                    }
                    else -> {
                        println("ERROR SERVIDOR: $responseCode")
                        null
                    }

                }

            }

        }catch (e: Exception){
            println("ERROR CONEXION: ${e.message}")
            null
        }


    }


}