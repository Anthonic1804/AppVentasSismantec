package com.example.acae30.ui.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.domain.usecase.clientes.ActualizarPagareUseCase
import com.example.acae30.ui.clientes.FirmarPagareViewModel

class FirmarPagareViewModelFactory(
    private val repository: ClientesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FirmarPagareViewModel::class.java)) {
            val useCase = ActualizarPagareUseCase(repository)
            return FirmarPagareViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
