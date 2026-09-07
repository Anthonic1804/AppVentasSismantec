package com.example.acae30.domain.usecase

import com.example.acae30.data.local.entity.VisitasEntity
import com.example.acae30.data.remote.dto.FinalizarVisitaDto
import com.example.acae30.data.remote.dto.VisitaRequestDto
import com.example.acae30.data.repository.VisitasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class IniciarVisitaUseCase(private val repository: VisitasRepository) {
    suspend operator fun invoke(
        idCliente: Int,
        nombreCliente: String,
        idVendedor: Int,
        gps: String,
        fecha: String
    ): Long = withContext(Dispatchers.IO) {
        // Guardar localmente
        val visitaLocal = VisitasEntity(
            id = 0,
            idCliente = idCliente,
            nombreCliente = nombreCliente,
            gpsIn = gps,
            fechaInicial = fecha,
            gpsOut = gps,
            fechaFinal = fecha,
            idVisita = 0,
            comentario = "",
            imagenUrl = "",
            imagen = "",
            abierta = true,
            enviado = false,
            enviadoFinal = false
        )
        val idLocal = repository.guardarCheckInLocal(visitaLocal)

        // Intentar enviar al servidor
        try {
            val request = VisitaRequestDto(
                idAppVisita = 0,
                fechaHoraCheckin = fecha,
                latitudCheckin = gps.split(",")[0],
                longitudCheckin = gps.split(",")[1],
                idCliente = idCliente,
                cliente = nombreCliente,
                idVendedor = idVendedor,
                fechaHoraCheckout = fecha,
                latitudCheckout = gps.split(",")[0],
                longitudCheckout = gps.split(",")[1],
                comentarios = ""
            )
            val response = repository.registrarVisitaRemota(request)
            if (response.isSuccessful && response.body() != null) {
                val idServidor = response.body()!!.idServidor
                if (idServidor > 0) {
                    repository.confirmarEnvioCheckInLocal(idLocal.toInt(), idServidor)
                }
            }
        } catch (e: Exception) {
            Timber.e("Error en IniciarVisitaUseCase: ${e.message}")
        }

        return@withContext idLocal
    }
}

class FinalizarVisitaUseCase(private val repository: VisitasRepository) {
    suspend operator fun invoke(
        idLocal: Int,
        idVendedor: Int,
        gps: String,
        fecha: String
    ) = withContext(Dispatchers.IO) {
        val visita = repository.obtenerVisitaLocalPorId(idLocal) ?: return@withContext

        // Actualizar localmente
        val visitaActualizada = visita.copy(
            gpsOut = gps,
            fechaFinal = fecha,
            abierta = false
        )
        repository.actualizarVisitaLocal(visitaActualizada)

        // Intentar enviar al servidor
        try {
            // Si no se envió el Check-in (idVisita == 0), intentar enviar todo
            if (visitaActualizada.idVisita == 0) {
                val request = VisitaRequestDto(
                    idAppVisita = 0,
                    fechaHoraCheckin = visitaActualizada.fechaInicial,
                    latitudCheckin = visitaActualizada.gpsIn?.split(",")?.get(0) ?: "0",
                    longitudCheckin = visitaActualizada.gpsIn?.split(",")?.get(1) ?: "0",
                    idCliente = visitaActualizada.idCliente,
                    cliente = visitaActualizada.nombreCliente,
                    idVendedor = idVendedor,
                    fechaHoraCheckout = fecha,
                    latitudCheckout = gps.split(",")[0],
                    longitudCheckout = gps.split(",")[1],
                    comentarios = ""
                )
                val response = repository.registrarVisitaRemota(request)
                if (response.isSuccessful && response.body() != null) {
                    val idServidor = response.body()!!.idServidor
                    if (idServidor > 0) {
                        repository.confirmarEnvioCheckInLocal(idLocal, idServidor)
                        repository.confirmarEnvioCheckOutLocal(idLocal)
                    }
                }
            } else {
                // Si ya tenía ID de servidor, solo enviar el fin_visita
                val finalizeRequest = FinalizarVisitaDto(
                    idVisitaServidor = visitaActualizada.idVisita,
                    fechaFinal = fecha,
                    latitud = gps.split(",")[0],
                    longitud = gps.split(",")[1],
                    idVendedor = idVendedor
                )
                val response = repository.finalizarVisitaRemota(finalizeRequest)
                if (response.isSuccessful) {
                    repository.confirmarEnvioCheckOutLocal(idLocal)
                }
            }
        } catch (e: Exception) {
            Timber.e("Error en FinalizarVisitaUseCase: ${e.message}")
        }
    }
}
