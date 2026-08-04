package com.example.acae30.domain.usecase.cuentas

import com.example.acae30.data.repository.CuentasRepository
import com.example.acae30.modelos.Cuenta

/**
 * REFACTORIZACIÓN MVVM: Caso de Uso para obtener el detalle de facturas de un cliente.
 */
class ObtenerDetalleCuentasUseCase(
    private val repository: CuentasRepository
) {

    suspend fun ejecutar(idCliente: Int, filtro: String): List<Cuenta> {
        // 1. Obtenemos las entidades según el filtro (Vencidas, Vigentes, Todas)
        val entities = when (filtro) {
            "Vencidas" -> repository.obtenerCuentasVencidas(idCliente)
            "Vigentes" -> repository.obtenerCuentasVigentes(idCliente)
            else -> repository.obtenerCuentasTodas(idCliente)
        }

        // 2. Mapeamos de CuentasEntity a el modelo Cuenta (UI)
        return entities.map { entity ->
            Cuenta(
                Id = entity.id,
                Id_cliente = entity.idCliente,
                Codigo_cliente = entity.codigoCliente,
                Documento = entity.documento,
                Fecha = entity.fecha,
                Valor = entity.valor.toFloat(),
                Abono_inicial = entity.abonoInicial.toFloat(),
                Saldo_inicial = entity.saldoInicial.toFloat(),
                Plazo = entity.plazo.toFloat(),
                Fecha_vencimiento = entity.fechaVencimiento,
                Saldo_actual = entity.saldoActual.toFloat(),
                Fecha_ult_pago = entity.fechaUltPago,
                Valor_pago = entity.valorPago?.toFloat() ?: 0f,
                Relacionado = entity.relacionado,
                Status = entity.status,
                Fecha_cancelado = entity.fechaCancelado
            )
        }
    }
}
