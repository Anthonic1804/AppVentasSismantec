package com.example.acae30.domain.usecase.token

import com.example.acae30.data.repository.TokenRepository

class ConfirmarTokenUseCase(
    private val repository: TokenRepository
) {
    suspend fun ejecutar(idVendedor: Int, codProducto: String): Boolean {
        return repository.confirmarUsoToken(idVendedor, codProducto)
    }
}
