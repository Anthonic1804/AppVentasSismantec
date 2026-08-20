package com.example.acae30.domain.usecase.inventario

import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.InventarioRepository

/**
 * REFACTORIZACIÓN MVVM: Caso de Uso para determinar el precio final de un producto.
 * Prioriza el precio personalizado del cliente sobre el precio de lista o escalas.
 */
class CalcularPrecioFinalUseCase(
    private val clientesRepository: ClientesRepository,
    private val inventarioRepository: InventarioRepository
) {

    suspend fun ejecutar(
        idCliente: Int,
        idProducto: Int,
        unidadSeleccionada: String,
        precioListaIva: Float
    ): Float {
        // REGLA DE NEGOCIO: El precio personalizado SOLO aplica para ventas en Unidades (UNI).
        if (unidadSeleccionada != "UNI") {
            return precioListaIva
        }

        // 1. Verificamos si el cliente tiene un precio personalizado para este producto
        val precioPersonalizado = clientesRepository.obtenerPrecioPersonalizado(idCliente, idProducto)
        
        // LOG DE DEPURACIÓN PARA BASE DE DATOS
        if (precioPersonalizado != null && precioPersonalizado > 0) {
            timber.log.Timber.d("[PRECIO_USECASE] ENCONTRADO PERSONALIZADO: $precioPersonalizado para Cliente $idCliente y Producto $idProducto")
            return precioPersonalizado
        } else {
            timber.log.Timber.d("[PRECIO_USECASE] NO HAY PERSONALIZADO para Cliente $idCliente y Producto $idProducto. Usando Lista: $precioListaIva")
            return precioListaIva
        }
    }
}
