package com.example.acae30.domain.usecase.token

import com.example.acae30.data.repository.TokenRepository

class ConsultarTokenUseCase(
    private val repository: TokenRepository
) {
    suspend fun ejecutar(idVendedor: Int, codProducto: String): Float? {
        return repository.buscarPrecioAutorizado(idVendedor, codProducto)
    }
}
