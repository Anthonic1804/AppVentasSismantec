package com.example.acae30.data.repository

import android.content.Context
import com.example.acae30.Funciones
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.local.entity.VisitasEntity
import com.example.acae30.data.remote.api.pedidos.VisitasApiService
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.data.remote.dto.FinalizarVisitaDto
import com.example.acae30.data.remote.dto.VisitaRequestDto
import com.example.acae30.data.remote.dto.VisitaResponseDto
import retrofit2.Response

class VisitasRepository(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val visitasDao = database.visitasDao()
    private val funciones = Funciones()

    private fun getApiService(): VisitasApiService {
        val preferencias = context.getSharedPreferences("CONFIG_SERVIDOR", Context.MODE_PRIVATE)
        val ip = preferencias.getString("ip", "") ?: ""
        val puerto = preferencias.getInt("puerto", 0).toString()
        val baseUrl = funciones.getServidor(ip, puerto, context)
        return RetrofitCliente.obtenerApi(baseUrl, context)
    }

    // --- Operaciones Locales (Room) ---

    suspend fun guardarCheckInLocal(visita: VisitasEntity): Long {
        return visitasDao.registrarCheckIn(visita)
    }

    suspend fun obtenerVisitaLocalPorId(id: Int): VisitasEntity? {
        return visitasDao.obtenerVisitaPorId(id)
    }

    suspend fun actualizarVisitaLocal(visita: VisitasEntity) {
        visitasDao.actualizarVisita(visita)
    }

    suspend fun confirmarEnvioCheckInLocal(idLocal: Int, idServidor: Int) {
        visitasDao.confirmarEnvioCheckIn(idLocal, idServidor)
    }

    suspend fun confirmarEnvioCheckOutLocal(idLocal: Int) {
        visitasDao.confirmarEnvioCheckOut(idLocal)
    }

    suspend fun obtenerVisitaPorIdCliente(idCliente: Int) = 
        visitasDao.obtenerVisitaPorIdCliente(idCliente)

    // --- Operaciones Remotas (Retrofit) ---

    suspend fun registrarVisitaRemota(request: VisitaRequestDto): Response<VisitaResponseDto> {
        return getApiService().registrarVisita(request)
    }

    suspend fun finalizarVisitaRemota(request: FinalizarVisitaDto): Response<VisitaResponseDto> {
        return getApiService().finalizarVisita(request)
    }
}
