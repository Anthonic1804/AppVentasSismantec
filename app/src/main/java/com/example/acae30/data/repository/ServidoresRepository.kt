package com.example.acae30.data.repository

import android.content.Context
import com.example.acae30.Funciones
import com.example.acae30.data.local.dao.ServidoresDao
import com.example.acae30.data.local.entity.ServidoresEntity
import com.example.acae30.data.remote.api.conexion.ConexionApi
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.data.remote.dto.UpdateAppDto
import timber.log.Timber

private val funciones = Funciones()
class ServidoresRepository(
    private val dao: ServidoresDao
) {

    fun obtenerServidores() = dao.obtenerListadoServidores()

    suspend fun registrarServidor(item: ServidoresEntity) = dao.registrarNuevoServidor(item)

    suspend fun actualizarServidor(item: ServidoresEntity) = dao.actualizarServidor(item)

    suspend fun eliminarServidor(id: Int) = dao.eliminarServidor(id)

    suspend fun verificarConexionServidor(ip: String, puerto: String, sslActivo: Int, context: Context) : String{

        val servidor = funciones.verificarServidor(ip, puerto, sslActivo)

        val api = RetrofitCliente.obtenerApi<ConexionApi>(servidor, context)

        return try {

            val respuesta = api.conectarServidor()

            respuesta.firstOrNull()
                ?.respuestaConexion
                ?: "SIN_RESPUESTA"

        } catch (e: Exception) {
            "ERROR_CONEXION -> ${e.message}"
        }
    }

    // ARQUITECTURA LIMPIA: Nueva función para obtener la actualización de la App
    suspend fun obtenerActualizacionApp(baseUrl: String, context: Context): UpdateAppDto? {
        val api = RetrofitCliente.obtenerApi<ConexionApi>(baseUrl, context)
        return try {
            val respuesta = api.obtenerActualizacionApp()
            if (respuesta.isSuccessful) {
                respuesta.body()
            } else {
                Timber.e("Error en respuesta de actualización: ${respuesta.code()}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al conectar para actualización")
            null
        }
    }

}
