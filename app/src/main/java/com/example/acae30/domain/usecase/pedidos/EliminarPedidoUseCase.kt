package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.data.repository.PedidosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Caso de uso para eliminar un pedido y sus detalles de forma local.
 */
class EliminarPedidoUseCase(private val repository: PedidosRepository) {
    suspend operator fun invoke(idPedido: Int) = withContext(Dispatchers.IO) {
        repository.eliminarPedidoLocal(idPedido)
    }
}
