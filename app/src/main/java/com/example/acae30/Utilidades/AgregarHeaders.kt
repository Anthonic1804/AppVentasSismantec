package com.example.acae30.Utilidades

import java.net.HttpURLConnection
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext


//------------------------------
//Clase utilizada para agregar los Headers
//para el uso de JWT en los Endpoints
//09-03-2026
//------------------------------

class AgregarHeaders {

    fun agregarHeaders(conexion: HttpURLConnection, token: String?, sslContext: SSLContext){

        conexion.setRequestProperty("Content-Type", "application/json")

        if(!token.isNullOrEmpty()){
            conexion.setRequestProperty("Authorization", "Bearer $token")
        }

        //Configuracion SSL
        if(conexion is HttpsURLConnection && sslContext != null){
            conexion.sslSocketFactory = sslContext.socketFactory
            conexion.hostnameVerifier = HostnameVerifier{_, _ -> true}
        }

    }

}