package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.data.repository.PedidosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Caso de uso para obtener la cantidad de items (productos) agregados a un pedido.
 */
class ObtenerCantidadItemsUseCase(private val repository: PedidosRepository) {
    suspend operator fun invoke(idPedido: Int): Int = withContext(Dispatchers.IO) {
        repository.obtenerCantidadItemsPedidoLocal(idPedido)
    }
}
