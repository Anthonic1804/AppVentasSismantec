package com.example.acae30.data.remote.api.pedidos

import com.example.acae30.data.remote.dto.FinalizarVisitaDto
import com.example.acae30.data.remote.dto.VisitaRequestDto
import com.example.acae30.data.remote.dto.VisitaResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VisitasApiService {

    @POST("visitas/registrar_visita")
    suspend fun registrarVisita(
        @Body request: VisitaRequestDto
    ): Response<VisitaResponseDto>

    @POST("visitas/fin_visita")
    suspend fun finalizarVisita(
        @Body request: FinalizarVisitaDto
    ): Response<VisitaResponseDto>
}
