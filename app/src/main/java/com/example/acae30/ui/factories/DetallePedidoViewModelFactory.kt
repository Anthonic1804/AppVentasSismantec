package com.example.acae30.ui.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acae30.domain.usecase.ActualizarSucursalPedidoUseCase
import com.example.acae30.domain.usecase.GetSucursalesUseCase
import com.example.acae30.domain.usecase.pedidos.ActualizarTotalesFiscalesUseCase
import com.example.acae30.domain.usecase.pedidos.CalcularTotalesFiscalesUseCase
import com.example.acae30.domain.usecase.pedidos.EliminarPedidoUseCase
import com.example.acae30.domain.usecase.pedidos.EnviarPedidoUseCase
import com.example.acae30.domain.usecase.pedidos.GetDetallePedidoFlowUseCase
import com.example.acae30.domain.usecase.pedidos.GetTicketDataUseCase
import com.example.acae30.domain.usecase.pedidos.ObtenerCantidadItemsUseCase
import com.example.acae30.ui.pedidos.DetallePedidoViewModel

class DetallePedidoViewModelFactory(
    private val getSucursalesUseCase: GetSucursalesUseCase,
    private val actualizarSucursalPedidoUseCase: ActualizarSucursalPedidoUseCase,
    private val obtenerCantidadItemsUseCase: ObtenerCantidadItemsUseCase,
    private val getDetallePedidoFlowUseCase: GetDetallePedidoFlowUseCase,
    private val calcularTotalesFiscalesUseCase: CalcularTotalesFiscalesUseCase,
    private val actualizarTotalesFiscalesUseCase: ActualizarTotalesFiscalesUseCase,
    private val enviarPedidoUseCase: EnviarPedidoUseCase,
    private val eliminarPedidoUseCase: EliminarPedidoUseCase,
    private val getTicketDataUseCase: GetTicketDataUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetallePedidoViewModel::class.java)) {
            return DetallePedidoViewModel(
                getSucursalesUseCase,
                actualizarSucursalPedidoUseCase,
                obtenerCantidadItemsUseCase,
                getDetallePedidoFlowUseCase,
                calcularTotalesFiscalesUseCase,
                actualizarTotalesFiscalesUseCase,
                enviarPedidoUseCase,
                eliminarPedidoUseCase,
                getTicketDataUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
