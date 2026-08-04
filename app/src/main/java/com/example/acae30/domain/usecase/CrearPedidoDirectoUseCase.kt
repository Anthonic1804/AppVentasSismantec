package com.example.acae30.domain.usecase

import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.PedidosRepository

class CrearPedidoDirectoUseCase(
    private val clientesRepository: ClientesRepository,
    private val pedidosRepository: PedidosRepository
) {

    suspend fun ejecutar(idCliente: Int): Int {
        // Obtenemos los datos completos del cliente desde Room
        val cliente = clientesRepository.obtenerClientePorId(idCliente)
            ?: throw Exception("Cliente no encontrado")

        // Determinamos el tipo de documento (Lógica de negocio migrada)
        var tipoDocumento = "FC"
        val nrc = cliente.nrc ?: ""
        if (nrc.length > 2 && nrc.isNotBlank()) {
            tipoDocumento = "CF"
        }

        // Solicitamos la creación del pedido al repositorio con valores por defecto para Local
        // idVisitaGlobal = 0, gps = "0,0"
        return pedidosRepository.crearNuevoPedidoLocal(
            idCliente = idCliente,
            nombreCliente = cliente.cliente ?: "",
            terminos = cliente.terminosCliente ?: "Contado",
            idRuta = cliente.idRuta,
            ruta = cliente.ruta ?: "",
            tipoDocumento = tipoDocumento,
            dteDireccion = cliente.dteDireccion ?: "",
            dteCodDepto = cliente.dteCodDepto ?: "",
            dteCodMunicipio = cliente.dteCodMunicipio ?: "",
            dteCodPais = cliente.dteCodPais ?: "",
            dtePais = cliente.dtePais ?: "",
            dteCorreo = cliente.dteCorreo ?: "",
            dteTelefono = cliente.dteTelefono ?: ""
        )
    }
}
