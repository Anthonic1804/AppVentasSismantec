package com.example.acae30.domain.usecase.clientes

import com.example.acae30.data.repository.ClientesRepository

class ActualizarPagareUseCase(
    private val repository: ClientesRepository
) {

    suspend fun ejecutar(idCliente: Int): Boolean {
        return repository.actualizarPagareFirmado(idCliente)
    }
}
