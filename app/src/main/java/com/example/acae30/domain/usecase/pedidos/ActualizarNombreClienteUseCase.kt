package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.data.repository.PedidosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Caso de uso para actualizar el nombre del cliente en un pedido (para código 01).
 */
class ActualizarNombreClienteUseCase(private val repository: PedidosRepository) {
    suspend operator fun invoke(idPedido: Int, nombre: String) = withContext(Dispatchers.IO) {
        repository.actualizarNombreClienteLocal(idPedido, nombre)
    }
}
