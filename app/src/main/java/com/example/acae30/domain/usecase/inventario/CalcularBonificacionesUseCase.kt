package com.example.acae30.domain.usecase.inventario

import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.InventarioRepository

/**
 * REFACTORIZACIÓN MVVM: Caso de Uso para calcular la cantidad de bonificación (regalía).
 */
class CalcularBonificacionesUseCase(
    private val clientesRepository: ClientesRepository,
    private val inventarioRepository: InventarioRepository
) {

    /**
     * Calcula los bonificados basándose en la cantidad total de unidades físicas compradas.
     * @param unidadBase: "UNI" o "FRA" (determina si aplica bonificación)
     * @param factorEquivalencia: Cuántas UNI o FRA representa 1 unidad de esta medida (ej: Sixpack = 6)
     */
    suspend fun ejecutar(
        idCliente: Int,
        idProducto: Int,
        cantidadIngresada: Float,
        unidadBase: String,
        factorEquivalencia: Float,
        tipoBonificacion: String,
        bonificadoProducto: Float
    ): Int {
        // REGLA DE NEGOCIO 1: Solo se bonifica por la compra de Unidades, NO por Fracciones.
        if (unidadBase != "UNI") return 0

        // Calculamos cuántas unidades físicas representa la compra (ej: Sixpack = 6)
        val totalUnidadesFisicas = cantidadIngresada * factorEquivalencia

        // REGLA DE NEGOCIO 2: Obtener el umbral según el tipo de bonificación configurado
        val bonificadoCliente = clientesRepository.obtenerBonificacionCliente(idCliente, idProducto) ?: 0f

        val umbralBonificacion = when (tipoBonificacion) {
            "T" -> {
                // T -> TODOS: Prioridad Cliente, si no tiene, usa Producto
                if (bonificadoCliente > 0) bonificadoCliente else bonificadoProducto
            }
            "BC" -> {
                // BC -> SOLO FICHA CLIENTE
                bonificadoCliente
            }
            "BP" -> {
                // BP -> SOLO FICHA PRODUCTO
                bonificadoProducto
            }
            "SB" -> {
                // SB -> SIN BONIFICACIÓN
                0f
            }
            else -> 0f
        }

        // 3. Calculamos la regalía (Cada 'umbral' unidades compradas, se regala 1)
        return if (umbralBonificacion > 0 && totalUnidadesFisicas >= umbralBonificacion) {
            (totalUnidadesFisicas / umbralBonificacion).toInt()
        } else {
            0
        }
    }
}
