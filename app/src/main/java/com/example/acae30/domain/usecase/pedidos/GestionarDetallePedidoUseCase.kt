package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.data.local.entity.PedidoDetalleEntity
import com.example.acae30.data.repository.PedidosRepository

/**
 * REFACTORIZACIÓN MVVM: Caso de Uso para insertar, actualizar o eliminar productos del pedido.
 */
class GestionarDetallePedidoUseCase(
    private val repository: PedidosRepository
) {

    /**
     * Agrega o actualiza un producto en el pedido.
     */
    suspend fun agregarOActualizarProducto(detalle: PedidoDetalleEntity) {
        // 1. Verificamos si el producto con la misma unidad ya existe en el pedido
        val existente = repository.buscarProductoEnDetalle(detalle.idPedido, detalle.idProducto, detalle.unidad ?: "")

        if (existente != null && detalle.id == 0) {
            // REGLA DE NEGOCIO: Si ya existe y estamos agregando uno nuevo, sumamos cantidades
            val nuevaCantidad = existente.cantidad + detalle.cantidad
            val nuevoTotalIva = existente.totalIva + detalle.totalIva
            val nuevoTotal = existente.total + detalle.total
            
            val actualizado = existente.copy(
                cantidad = nuevaCantidad,
                total = nuevoTotal,
                totalIva = nuevoTotalIva
            )
            repository.insertarDetallePedido(actualizado)
        } else {
            // Inserción directa (incluye actualización por ID si id != 0)
            repository.insertarDetallePedido(detalle)
        }

        // 2. Recalculamos el total de la cabecera del pedido
        repository.recalcularTotalPedido(detalle.idPedido)
    }

    /**
     * Elimina un ítem del detalle y actualiza el total del pedido.
     */
    suspend fun eliminarProducto(idDetalle: Int, idPedido: Int) {
        repository.eliminarDetallePedido(idDetalle)
        repository.recalcularTotalPedido(idPedido)
    }

    /**
     * Obtiene un ítem del detalle por su ID.
     */
    suspend fun obtenerDetallePorId(idDetalle: Int): PedidoDetalleEntity? {
        return repository.obtenerDetallePedidoPorId(idDetalle)
    }
}
