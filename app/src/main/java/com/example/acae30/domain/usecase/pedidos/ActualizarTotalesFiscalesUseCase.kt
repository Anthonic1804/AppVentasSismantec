package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.data.repository.PedidosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Caso de uso para persistir los totales calculados en el pedido.
 */
class ActualizarTotalesFiscalesUseCase(private val repository: PedidosRepository) {
    /* CÓDIGO VIEJO:
    suspend operator fun invoke(idPedido: Int, sumas: Double, iva: Double, ivaPerci: Double, totalFinal: Double) = withContext(Dispatchers.IO) {
        repository.actualizarTotalesFiscalesLocal(idPedido, sumas, iva, ivaPerci, totalFinal)
    }
    */
    // CÓDIGO NUEVO: Incluye ventaExenta y ventaNoSujeta para persistencia en Room
    suspend operator fun invoke(
        idPedido: Int, 
        sumas: Double, 
        iva: Double, 
        ventaExenta: Double, 
        ventaNoSujeta: Double, 
        ivaPerci: Double, 
        totalFinal: Double
    ) = withContext(Dispatchers.IO) {
        repository.actualizarTotalesFiscalesLocal(idPedido, sumas, iva, ventaExenta, ventaNoSujeta, ivaPerci, totalFinal)
    }
}

/**
 * CÓDIGO NUEVO: Caso de uso para actualizar el tipo de documento del pedido en Room.
 */
class ActualizarTipoDocumentoUseCase(private val repository: PedidosRepository) {
    suspend operator fun invoke(idPedido: Int, tipoDoc: String) = withContext(Dispatchers.IO) {
        repository.actualizarTipoDocumentoLocal(idPedido, tipoDoc)
    }
}
