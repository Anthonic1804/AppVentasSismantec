package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.data.local.entity.PedidosEntity
import kotlinx.coroutines.flow.Flow

/**
 * REFACTORIZACIÓN MULTIPLES PEDIDOS: Caso de uso para obtener borradores reactivos de un cliente.
 */
class GetPedidosBorradoresUseCase(
    private val repository: PedidosRepository
) {
    operator fun invoke(): Flow<List<PedidosEntity>> {
        return repository.obtenerTodosLosPedidosBorradores()
    }
}
