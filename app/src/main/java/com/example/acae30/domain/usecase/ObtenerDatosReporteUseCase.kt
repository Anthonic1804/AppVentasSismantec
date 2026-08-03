package com.example.acae30.domain.usecase

import android.content.Context
import com.example.acae30.data.local.entity.ReporteTempEntity
import com.example.acae30.data.repository.PedidosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
class ObtenerDatosReporteUseCase(
    private val repository: PedidosRepository
) {

    sealed class ReportStatus {
        object Iniciando : ReportStatus()
        object Descargando : ReportStatus()
        object Guardando : ReportStatus()
        data class Exito(val datos: List<ReporteTempEntity>) : ReportStatus()
        data class Error(val mensaje: String) : ReportStatus()
    }

    suspend fun ejecutar(
        idVendedor: Int,
        fecha: String,
        context: Context
    ): Flow<ReportStatus> = flow {
        emit(ReportStatus.Iniciando)

        try {
            emit(ReportStatus.Descargando)
            val datosRemote = repository.obtenerReporteDiarioRemote(idVendedor, fecha, context)

            if (datosRemote != null) {
                if (datosRemote.isNotEmpty()) {
                    emit(ReportStatus.Guardando)
                    repository.actualizarTablaReporteLocal(datosRemote)
                    
                    val datosFinales = repository.obtenerDatosReporteLocal()
                    emit(ReportStatus.Exito(datosFinales))
                } else {
                    emit(ReportStatus.Error("NO SE ENCONTRARON PEDIDOS DE ESTE DÍA"))
                }
            } else {
                emit(ReportStatus.Error("ERROR DE CONEXIÓN CON EL SERVIDOR"))
            }
        } catch (e: Exception) {
            emit(ReportStatus.Error("ERROR AL PROCESAR EL REPORTE: ${e.message}"))
        }
    }
}
