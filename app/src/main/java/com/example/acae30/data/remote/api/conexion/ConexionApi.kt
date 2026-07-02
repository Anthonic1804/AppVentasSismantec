package com.example.acae30.data.remote.api.conexion

import com.example.acae30.data.remote.dto.RespuestaConexionDto
import com.example.acae30.data.remote.dto.UpdateAppDto
import com.example.acae30.modelos.Login.LoginModel
import com.example.acae30.modelos.Login.RespuestaLogin
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ConexionApi {

    //Conexion con el Servidor
    @GET("conexion")
    suspend fun conectarServidor() : List<RespuestaConexionDto>

    //-------------------------------------------------------
    //Buscar Actualizacion App
    //-------------------------------------------------------
    @GET("updateapp/v2/buscar")
    suspend fun obtenerActualizacionApp(): Response<UpdateAppDto>

    //--------------------------------------
    //EndPoints Login
    //11-03-2026
    //--------------------------------------

    @POST("login")
    suspend fun login(
        @Body credenciales : LoginModel
    ) : Response<RespuestaLogin>

}