package com.example.acae30.ui.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.domain.usecase.ObtenerDatosReporteUseCase
import com.example.acae30.domain.usecase.SincronizarPedidosUseCase
import com.example.acae30.ui.pedidos.PedidosViewModel

/**
 * REFACTORIZACIÓN MVVM: Fábrica para instanciar el PedidosViewModel con sus dependencias.
 */
class PedidosViewModelFactory(
    private val repository: PedidosRepository,
    private val sincronizarUseCase: SincronizarPedidosUseCase,
    private val obtenerReporteUseCase: ObtenerDatosReporteUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PedidosViewModel::class.java)) {
            return PedidosViewModel(repository, sincronizarUseCase, obtenerReporteUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
