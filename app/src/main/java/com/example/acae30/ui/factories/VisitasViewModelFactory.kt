package com.example.acae30.ui.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.CuentasRepository
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.data.repository.VisitasRepository
import com.example.acae30.ui.pedidos.VisitasViewModel

class VisitasViewModelFactory(
    private val visitasRepository: VisitasRepository,
    private val pedidosRepository: PedidosRepository,
    private val clientesRepository: ClientesRepository,
    private val cuentasRepository: CuentasRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitasViewModel::class.java)) {
            return VisitasViewModel(
                visitasRepository,
                pedidosRepository,
                clientesRepository,
                cuentasRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
