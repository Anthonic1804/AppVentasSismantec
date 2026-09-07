package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.data.repository.PedidosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Caso de uso para persistir los totales calculados en el pedido.
 */
class ActualizarTotalesFiscalesUseCase(private val repository: PedidosRepository) {
    suspend operator fun invoke(idPedido: Int, sumas: Double, iva: Double, ivaPerci: Double) = withContext(Dispatchers.IO) {
        repository.actualizarTotalesFiscalesLocal(idPedido, sumas, iva, ivaPerci)
    }
}
