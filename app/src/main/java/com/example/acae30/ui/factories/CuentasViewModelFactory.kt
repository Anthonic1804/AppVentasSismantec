package com.example.acae30.ui.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acae30.data.repository.CuentasRepository
import com.example.acae30.domain.usecase.cuentas.ObtenerClientesConCuentasUseCase
import com.example.acae30.domain.usecase.cuentas.ObtenerDetalleCuentasUseCase
import com.example.acae30.ui.clientes.CuentasViewModel

/**
 * REFACTORIZACIÓN MVVM: Fábrica para instanciar CuentasViewModel con sus dependencias inyectadas.
 */
class CuentasViewModelFactory(
    private val repository: CuentasRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CuentasViewModel::class.java)) {
            val obtenerClientesUseCase = ObtenerClientesConCuentasUseCase(repository)
            val obtenerDetalleUseCase = ObtenerDetalleCuentasUseCase(repository)
            
            return CuentasViewModel(obtenerClientesUseCase, obtenerDetalleUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
