package com.example.acae30.data.remote.api.token

import com.example.acae30.data.remote.dto.TokenResponseDTO
import com.example.acae30.modelos.JSONmodels.ActualizarPrecioPersonalizadoJSON
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenApi {

    @POST("token/search")
    suspend fun buscarPrecioAutorizado(
        @Body request: ActualizarPrecioPersonalizadoJSON
    ): Response<TokenResponseDTO>

    @POST("token/update")
    suspend fun confirmarUsoToken(
        @Body request: ActualizarPrecioPersonalizadoJSON
    ): Response<Void>
}
