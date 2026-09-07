package com.example.acae30.domain.usecase

import com.example.acae30.data.local.entity.ClienteSucursalEntity
import com.example.acae30.data.repository.ClientesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSucursalesUseCase(private val repository: ClientesRepository) {
    suspend operator fun invoke(idCliente: Int): List<ClienteSucursalEntity> = withContext(Dispatchers.IO) {
        repository.obtenerSucursalesPorCliente(idCliente)
    }
}
