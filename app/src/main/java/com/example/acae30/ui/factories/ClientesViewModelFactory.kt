package com.example.acae30.ui.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.domain.usecase.CrearPedidoDirectoUseCase
import com.example.acae30.domain.usecase.clientes.ObtenerListaClientesUseCase
import com.example.acae30.ui.clientes.ClientesViewModel

class ClientesViewModelFactory(
    private val repository: ClientesRepository,
    private val pedidosRepository: PedidosRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientesViewModel::class.java)) {
            val crearPedidoUseCase = CrearPedidoDirectoUseCase(repository, pedidosRepository)
            val obtenerListaUseCase = ObtenerListaClientesUseCase(repository)
            
            return ClientesViewModel(repository, crearPedidoUseCase, obtenerListaUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
