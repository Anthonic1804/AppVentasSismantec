package com.example.acae30.ui.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.InventarioRepository
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.data.repository.TokenRepository
import com.example.acae30.data.remote.api.token.TokenApi
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.domain.usecase.inventario.CalcularBonificacionesUseCase
import com.example.acae30.domain.usecase.inventario.CalcularPrecioFinalUseCase
import com.example.acae30.domain.usecase.inventario.ObtenerStockDesglosadoUseCase
import com.example.acae30.domain.usecase.pedidos.GestionarDetallePedidoUseCase
import com.example.acae30.domain.usecase.token.ConfirmarTokenUseCase
import com.example.acae30.domain.usecase.token.ConsultarTokenUseCase
import com.example.acae30.ui.pedidos.ProductoAgregarViewModel

class ProductoAgregarViewModelFactory(
    private val inventarioRepository: InventarioRepository,
    private val clientesRepository: ClientesRepository,
    private val pedidosRepository: PedidosRepository,
    private val servidorUrl: String,
    private val context: android.content.Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductoAgregarViewModel::class.java)) {
            
            // Inicialización de la API de Tokens
            val tokenApi = RetrofitCliente.obtenerApi<TokenApi>(servidorUrl, context)
            val tokenRepository = TokenRepository(tokenApi)

            // Inyectamos los Casos de Uso necesarios
            val gestionarDetalleUseCase = GestionarDetallePedidoUseCase(pedidosRepository)
            val calcularPrecioUseCase = CalcularPrecioFinalUseCase(clientesRepository, inventarioRepository)
            val calcularBonificacionesUseCase = CalcularBonificacionesUseCase(clientesRepository, inventarioRepository)
            val obtenerStockUseCase = ObtenerStockDesglosadoUseCase()
            val consultarTokenUseCase = ConsultarTokenUseCase(tokenRepository)
            val confirmarTokenUseCase = ConfirmarTokenUseCase(tokenRepository)
            
            return ProductoAgregarViewModel(
                inventarioRepository,
                gestionarDetalleUseCase,
                calcularPrecioUseCase,
                calcularBonificacionesUseCase,
                obtenerStockUseCase,
                consultarTokenUseCase,
                confirmarTokenUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
