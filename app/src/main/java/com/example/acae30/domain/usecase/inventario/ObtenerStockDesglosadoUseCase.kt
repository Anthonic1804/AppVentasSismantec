package com.example.acae30.domain.usecase.inventario

import com.example.acae30.data.local.models.Inventario
import kotlin.math.floor

/**
 * REFACTORIZACIÓN MVVM: Caso de Uso para calcular el stock desglosado en Unidades y Fracciones.
 * Evita mostrar decimales y asegura que la suma sea consistente con la capacidad de fracción del producto.
 */
class ObtenerStockDesglosadoUseCase {

    data class StockDesglosado(
        val unidades: String,
        val fracciones: String,
        val stockTotalValidacion: Float
    )

    fun ejecutar(producto: Inventario, unidadesLote: Float? = null, fraccionesLote: Float? = null): StockDesglosado {
        val capacidadFraccion = producto.Fraccion ?: 0f
        val existencia = unidadesLote ?: (producto.Existencia ?: 0f)
        val existenciaU = fraccionesLote ?: producto.Existencia_u

        return if (capacidadFraccion > 1f) {
            // CASO 1: PRODUCTO FRACCIONADO (Ej: Cajas y Unidades)
            // Se muestra como enteros. No permite decimales en la visualización.
            val totalEnFracciones = (existencia * capacidadFraccion) + existenciaU
            val uni = floor(totalEnFracciones / capacidadFraccion).toInt()
            val fra = (totalEnFracciones % capacidadFraccion).toInt()
            
            StockDesglosado(
                unidades = uni.toString(),
                fracciones = fra.toString(),
                stockTotalValidacion = totalEnFracciones
            )
        } else {
            // CASO 2: PRODUCTO NO FRACCIONADO O DECIMAL (Fraccion 0 o 1)
            // Se muestra el total con decimales en el campo de Unidades.
            val totalDecimal = existencia + existenciaU
            
            StockDesglosado(
                unidades = String.format("%.2f", totalDecimal),
                fracciones = "0",
                stockTotalValidacion = totalDecimal
            )
        }
    }
}
