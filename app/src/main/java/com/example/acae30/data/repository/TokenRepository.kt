package com.example.acae30.data.repository

import com.example.acae30.data.remote.api.token.TokenApi
import com.example.acae30.modelos.JSONmodels.ActualizarPrecioPersonalizadoJSON
import timber.log.Timber

class TokenRepository(
    private val api: TokenApi
) {

    //---------------------------------------------------------------------------------
     // Consulta si existe un precio autorizado para un vendedor y producto específicos.
    //---------------------------------------------------------------------------------
    suspend fun buscarPrecioAutorizado(idVendedor: Int, codProducto: String): Float? {
        return try {
            val request = ActualizarPrecioPersonalizadoJSON(idVendedor, codProducto)
            val response = api.buscarPrecioAutorizado(request)
            if (response.isSuccessful) {
                response.body()?.precioAsignado
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "[TOKEN_REPO] ERROR AL BUSCAR PRECIO AUTORIZADO")
            null
        }
    }

    //---------------------------------------------------------------------------------
     // Notifica al servidor que un token de autorización ha sido utilizado.
    //---------------------------------------------------------------------------------
    suspend fun confirmarUsoToken(idVendedor: Int, codProducto: String): Boolean {
        return try {
            val request = ActualizarPrecioPersonalizadoJSON(idVendedor, codProducto)
            val response = api.confirmarUsoToken(request)
            response.isSuccessful && response.code() == 201
        } catch (e: Exception) {
            Timber.e(e, "[TOKEN_REPO] ERROR AL CONFIRMAR TOKEN")
            false
        }
    }
}
