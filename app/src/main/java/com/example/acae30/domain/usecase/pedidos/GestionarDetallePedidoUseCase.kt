package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.data.local.entity.PedidoDetalleEntity
import com.example.acae30.data.repository.PedidosRepository

/**
 * Caso de Uso para insertar, actualizar o eliminar productos del pedido.
 */
class GestionarDetallePedidoUseCase(
    private val repository: PedidosRepository
) {

    /**
     * Agrega o actualiza un producto en el pedido.
     * REFACTORIZACIÓN: Incluye validación de stock integral considerando lo que ya está en el pedido.
     */
    suspend fun agregarOActualizarProducto(
        detalle: PedidoDetalleEntity, 
        stockDisponible: Float, 
        realFraccion: Float,
        permitirSinExistencia: Int = 0
    ) {
        // 1. Obtener todos los detalles actuales de este producto en el pedido
        val detallesActuales = repository.obtenerDetallesDeProductoEnPedidoLocal(detalle.idPedido, detalle.idProducto)
        
        // 2. Calcular la ocupación actual (normalizada a la unidad base de validación)
        var ocupacionActual = 0.0
        val capacidad = if (realFraccion > 1f) realFraccion.toDouble() else 1.0
        
        detallesActuales.forEach { item ->
            // Si estamos EDITANDO un item (id != 0), no sumamos su cantidad vieja al cálculo de ocupación
            if (item.id != detalle.id) {
                val cant = item.cantidad
                val bonif = item.bonificado.toDouble()
                val eqUni = item.equivaleUni
                val eqFra = item.equivaleFra
                
                if (realFraccion > 1f) {
                    // Sumamos (Cantidad Vendida + Bonificación) convertido a fracciones + Fracciones sueltas
                    ocupacionActual += ((cant * eqUni + bonif) * capacidad) + (cant * eqFra)
                } else {
                    ocupacionActual += cant + bonif + (cant * eqFra)
                }
            }
        }
        
        // 3. Calcular la ocupación de la nueva cantidad
        val nuevaOcupacion = if (realFraccion > 1f) {
            ((detalle.cantidad * detalle.equivaleUni + detalle.bonificado.toDouble()) * capacidad) + (detalle.cantidad * detalle.equivaleFra)
        } else {
            detalle.cantidad + detalle.bonificado.toDouble() + (detalle.cantidad * detalle.equivaleFra)
        }
        
        // 4. VALIDACIÓN FINAL: ¿Suma total excede el stock?
        if (permitirSinExistencia == 0 && (ocupacionActual + nuevaOcupacion) > stockDisponible.toDouble()) {
            throw Exception("EXISTENCIAS INSUFICIENTES: Ya tiene reservado lo disponible en otros ítems de este pedido.")
        }

        // 5. Lógica de inserción/actualización original
        // Verificamos si el producto con la misma unidad exacta ya existe para fundirlo
        val existenteMismaUnidad = repository.buscarProductoEnDetalle(detalle.idPedido, detalle.idProducto, detalle.unidad ?: "")

        if (existenteMismaUnidad != null && detalle.id == 0) {
            // Si ya existe la misma unidad y estamos agregando uno nuevo (no editando), sumamos cantidades
            val nuevaCantidad = existenteMismaUnidad.cantidad + detalle.cantidad
            val nuevoTotalIva = existenteMismaUnidad.totalIva + detalle.totalIva
            val nuevoTotal = existenteMismaUnidad.total + detalle.total
            val nuevaBonif = existenteMismaUnidad.bonificado + detalle.bonificado
            
            val actualizado = existenteMismaUnidad.copy(
                cantidad = nuevaCantidad,
                total = nuevoTotal,
                totalIva = nuevoTotalIva,
                bonificado = nuevaBonif
            )
            repository.insertarDetallePedido(actualizado)
        } else {
            // Inserción directa (incluye actualización por ID si id != 0)
            repository.insertarDetallePedido(detalle)
        }

        // Recalculamos el total de la cabecera del pedido
        repository.recalcularTotalPedido(detalle.idPedido)
    }

    /**
     * Calcula la ocupación total normalizada de un producto en un pedido.
     */
    suspend fun obtenerOcupacionTotal(idPedido: Int, idProducto: Int, realFraccion: Float, idOmitir: Int = 0): Double {
        val detalles = repository.obtenerDetallesDeProductoEnPedidoLocal(idPedido, idProducto)
        var total = 0.0
        val capacidad = if (realFraccion > 1f) realFraccion.toDouble() else 1.0
        
        detalles.forEach { item ->
            if (item.id != idOmitir) {
                val cant = item.cantidad
                val bonif = item.bonificado.toDouble()
                val eqUni = item.equivaleUni
                val eqFra = item.equivaleFra
                
                if (realFraccion > 1f) {
                    total += ((cant * eqUni + bonif) * capacidad) + (cant * eqFra)
                } else {
                    total += cant + bonif + (cant * eqFra)
                }
            }
        }
        return total
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
