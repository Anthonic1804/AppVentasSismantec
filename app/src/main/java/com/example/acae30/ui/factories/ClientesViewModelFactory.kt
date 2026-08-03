package com.example.acae30.ui.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.domain.usecase.CrearPedidoDirectoUseCase
import com.example.acae30.ui.clientes.ClientesViewModel

class ClientesViewModelFactory(
    private val repository: ClientesRepository,
    private val pedidosRepository: PedidosRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if(modelClass.isAssignableFrom(ClientesViewModel::class.java)){
            val useCase = CrearPedidoDirectoUseCase(repository, pedidosRepository)
            return ClientesViewModel(repository, useCase) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )

    }

}