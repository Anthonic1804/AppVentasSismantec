package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.PedidosRepository

/**
 * REFACTORIZACIÓN MULTIPLES PEDIDOS: Caso de uso centralizado para crear un nuevo pedido.
 */
class CrearPedidoUseCase(
    private val pedidosRepository: PedidosRepository,
    private val clientesRepository: ClientesRepository
) {
    suspend operator fun invoke(
        idCliente: Int,
        nombreCliente: String,
        idVisitaLocal: Int,
        gps: String
    ): Int? {
        val cliente = clientesRepository.obtenerClientePorId(idCliente) ?: return null
        
        var tipoDocumento = "FC"
        if ((cliente.nrc?.length ?: 0) > 2 && !cliente.nrc.isNullOrBlank()) {
            tipoDocumento = "CF"
        }

        return pedidosRepository.crearNuevoPedidoLocal(
            idCliente = idCliente,
            nombreCliente = nombreCliente,
            terminos = cliente.terminosCliente ?: "",
            idRuta = cliente.idRuta,
            ruta = cliente.ruta ?: "",
            tipoDocumento = tipoDocumento,
            dteDireccion = cliente.dteDireccion ?: "",
            dteCodDepto = cliente.dteCodDepto ?: "",
            dteCodMunicipio = cliente.dteCodMunicipio ?: "",
            dteCodPais = cliente.dteCodPais ?: "",
            dtePais = cliente.dtePais ?: "",
            dteCorreo = cliente.dteCorreo ?: "",
            dteTelefono = cliente.dteTelefono ?: "",
            idVisitaGlobal = idVisitaLocal,
            gps = gps
        )
    }
}
