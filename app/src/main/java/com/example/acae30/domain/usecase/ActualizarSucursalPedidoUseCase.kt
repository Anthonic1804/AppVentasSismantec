package com.example.acae30.domain.usecase

import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.PedidosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActualizarSucursalPedidoUseCase(
    private val clientesRepository: ClientesRepository,
    private val pedidosRepository: PedidosRepository
) {
    suspend operator fun invoke(idPedido: Int, idCliente: Int, nombreSucursal: String) = withContext(Dispatchers.IO) {
        val sucursal = clientesRepository.obtenerSucursalPorNombre(idCliente, nombreSucursal)
        
        if (sucursal != null) {
            
            // Actualizamos el pedido en la base de datos local
            pedidosRepository.actualizarSucursalEnPedido(
                idPedido = idPedido,
                idSucursal = sucursal.id,
                codigoSucursal = sucursal.codigoSucursal ?: "",
                nombreSucursal = sucursal.nombreSucursal ?: "",
                idRuta = sucursal.idRuta,
                ruta = sucursal.ruta ?: "",
                dteDireccion = sucursal.direccionSucursal ?: "",
                dteCodDepto = sucursal.dteCodDepto ?: "",
                dteCodMunicipio = sucursal.dteCodMunicipio ?: "",
                dteCodPais = sucursal.dteCodPais ?: "",
                dtePais = sucursal.dtePais ?: "",
                dteCorreo = sucursal.dteCorreo ?: "",
                dteTelefono = sucursal.telefono1 ?: ""
            )
        }
    }
}
