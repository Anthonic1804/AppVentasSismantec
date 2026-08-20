package com.example.acae30.ui.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.data.local.entity.InventarioLotesEntity
import com.example.acae30.data.local.entity.PedidoDetalleEntity
import com.example.acae30.data.local.models.Inventario
import com.example.acae30.data.repository.InventarioRepository
import com.example.acae30.domain.usecase.inventario.CalcularBonificacionesUseCase
import com.example.acae30.domain.usecase.inventario.CalcularPrecioFinalUseCase
import com.example.acae30.domain.usecase.inventario.ObtenerStockDesglosadoUseCase
import com.example.acae30.domain.usecase.pedidos.GestionarDetallePedidoUseCase
import com.example.acae30.domain.usecase.token.ConfirmarTokenUseCase
import com.example.acae30.domain.usecase.token.ConsultarTokenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ProductoAgregarViewModel(
    private val inventarioRepository: InventarioRepository,
    private val gestionarDetalleUseCase: GestionarDetallePedidoUseCase,
    private val calcularPrecioUseCase: CalcularPrecioFinalUseCase,
    private val calcularBonificacionesUseCase: CalcularBonificacionesUseCase,
    private val obtenerStockUseCase: ObtenerStockDesglosadoUseCase,
    private val consultarTokenUseCase: ConsultarTokenUseCase,
    private val confirmarTokenUseCase: ConfirmarTokenUseCase
) : ViewModel() {

    // Información del producto actual
    private val _producto = MutableStateFlow<Inventario?>(null)
    val producto = _producto.asStateFlow()

    // Listado de lotes para el producto
    private val _listaLotes = MutableStateFlow<List<InventarioLotesEntity>>(emptyList())
    val listaLotes = _listaLotes.asStateFlow()

    // Detalle del pedido si estamos en modo edición
    private val _detallePedido = MutableStateFlow<PedidoDetalleEntity?>(null)
    val detallePedido = _detallePedido.asStateFlow()

    // Precio Autorizado vía Token
    private val _precioAutorizado = MutableStateFlow<Float?>(null)
    val precioAutorizado = _precioAutorizado.asStateFlow()

    // Estados de Stock para la UI (Representados como String para soportar decimales o enteros)
    private val _stockUni = MutableStateFlow("0")
    val stockUni = _stockUni.asStateFlow()

    private val _stockFra = MutableStateFlow("0")
    val stockFra = _stockFra.asStateFlow()

    // Stock total normalizado para validaciones
    private val _stockTotalValidacion = MutableStateFlow(0f)
    val stockTotalValidacion = _stockTotalValidacion.asStateFlow()

    // Precio final calculado (Personalizado o Viñeta)
    private val _precioFinal = MutableStateFlow(0f)
    val precioFinal = _precioFinal.asStateFlow()

    // Indica si el precio actual viene de la tabla cliente_precios
    private val _esPrecioPersonalizado = MutableStateFlow(false)
    val esPrecioPersonalizado = _esPrecioPersonalizado.asStateFlow()

    // Cantidad de bonificación calculada
    private val _bonificado = MutableStateFlow(0)
    val bonificado = _bonificado.asStateFlow()

    // Total de la línea (Precio * Cantidad)
    private val _totalLinea = MutableStateFlow(0f)
    val totalLinea = _totalLinea.asStateFlow()

    // Estado de la operación (Cerrar pantalla o Error)
    sealed class UIEvent {
        object ProductoGuardado : UIEvent()
        data class Error(val mensaje: String) : UIEvent()
    }
    private val _uiEvent = MutableStateFlow<UIEvent?>(null)
    val uiEvent = _uiEvent.asStateFlow()

    //---------------------------------------------------------------------------
    //Carga la información inicial del producto y calcula el precio base y stock.
    //---------------------------------------------------------------------------
    fun cargarProducto(idProducto: Int, idCliente: Int, unidadInicial: String) {
        viewModelScope.launch {
            try {
                // Cargar Info Producto
                val p = inventarioRepository.obtenerProductoPorId(idProducto)
                _producto.value = p
                
                // Cargar Lotes
                val lotes = inventarioRepository.obtenerLotesPorProducto(idProducto)
                _listaLotes.value = lotes

                if (p != null) {
                    // Calculamos el stock inicial (sin lote específico)
                    actualizarStock(p)
                    
                    recalcularValores(
                        idCliente = idCliente,
                        idProducto = idProducto,
                        cantidad = 1f,
                        unidad = unidadInicial,
                        unidadBase = "UNI",
                        factorEquivalencia = 1f,
                        tipoBonif = ""
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "[PRODUCTO_AGREGAR_VM] ERROR CARGANDO PRODUCTO")
            }
        }
    }

    //---------------------------------------------------------------------------
    //Carga el detalle de un pedido existente por su ID.
    //---------------------------------------------------------------------------
    fun cargarDetallePedido(idDetalle: Int) {
        viewModelScope.launch {
            try {
                Timber.d("[PRODUCTO_AGREGAR_VM] Intentando cargar detalle ID: $idDetalle")
                val detalle = gestionarDetalleUseCase.obtenerDetallePorId(idDetalle)
                _detallePedido.value = detalle
                if (detalle == null) {
                    Timber.e("[PRODUCTO_AGREGAR_VM] No se encontró el detalle con ID: $idDetalle")
                    _uiEvent.value = UIEvent.Error("ERROR AL BUSCAR EN EL DETALLE DEL PEDIDO")
                }
            } catch (e: Exception) {
                Timber.e(e, "[PRODUCTO_AGREGAR_VM] ERROR CARGANDO DETALLE")
                _uiEvent.value = UIEvent.Error("ERROR AL BUSCAR EN EL DETALLE DEL PEDIDO")
            }
        }
    }

    //---------------------------------------------------------------------------
    //Actualiza el desglose de stock cuando se selecciona un lote o cambia el producto.
    //---------------------------------------------------------------------------
    fun actualizarStock(p: Inventario, unidadesLote: Float? = null, fraccionesLote: Float? = null) {
        val stock = obtenerStockUseCase.ejecutar(p, unidadesLote, fraccionesLote)
        _stockUni.value = stock.unidades
        _stockFra.value = stock.fracciones
        _stockTotalValidacion.value = stock.stockTotalValidacion
    }

    //---------------------------------------------------------------------------
    //Recalcula precio, bonificaciones y totales cada vez que cambia la cantidad o unidad.
    //---------------------------------------------------------------------------
    fun recalcularValores(
        idCliente: Int,
        idProducto: Int,
        cantidad: Float,
        unidad: String,
        unidadBase: String, // "UNI" o "FRA"
        factorEquivalencia: Float,
        tipoBonif: String
    ) {
        viewModelScope.launch {
            val p = _producto.value ?: return@launch
            
            // Obtener el precio base según la unidad (UNI o FRA)
            val precioBaseFicha = if (unidad == "FRA") p.Precio_u_iva ?: 0f else p.Precio_iva ?: 0f

            // Obtener el precio final (Prioridad: Personalizado > Lista/Escala)
            val precioCalculado = calcularPrecioUseCase.ejecutar(idCliente, idProducto, unidad, precioBaseFicha)
            
            // Determinar si el precio es personalizado
            _esPrecioPersonalizado.value = (precioCalculado != precioBaseFicha && unidad == "UNI")

            _precioFinal.value = precioCalculado

            // Calcular Bonificación
            val regalias = calcularBonificacionesUseCase.ejecutar(
                idCliente, idProducto, cantidad, unidadBase, factorEquivalencia, tipoBonif, p.Bonificado ?: 0f
            )
            _bonificado.value = regalias

            // Calcular Total con máxima precisión
            _totalLinea.value = precioCalculado * cantidad
            
            Timber.d("[PRODUCTO_AGREGAR_VM] CALCULO: $precioCalculado * $cantidad = ${_totalLinea.value}")
        }
    }

    //---------------------------------------------------------------------------
    //Consulta el servidor para ver si existe un precio autorizado para este producto.
    //---------------------------------------------------------------------------
    fun buscarTokenAutorizacion(idVendedor: Int, codProducto: String) {
        viewModelScope.launch {
            val precio = consultarTokenUseCase.ejecutar(idVendedor, codProducto)
            _precioAutorizado.value = precio
            if (precio == null) {
                _uiEvent.value = UIEvent.Error("NO SE ENCONTRÓ PRECIO AUTORIZADO")
            }
        }
    }

    //---------------------------------------------------------------------------
    //Confirma el uso del token y luego guarda el producto.
    //---------------------------------------------------------------------------
    fun confirmarTokenYGuardar(idVendedor: Int, codProducto: String, detalle: PedidoDetalleEntity) {
        viewModelScope.launch {
            val ok = confirmarTokenUseCase.ejecutar(idVendedor, codProducto)
            if (ok) {
                guardarProducto(detalle)
            } else {
                _uiEvent.value = UIEvent.Error("ERROR AL CONFIRMAR EL TOKEN EN EL SERVIDOR")
            }
        }
    }

    //---------------------------------------------------------------------------
    //Inserta o actualiza el producto en el pedido.
    //---------------------------------------------------------------------------
    fun guardarProducto(detalle: PedidoDetalleEntity) {
        viewModelScope.launch {
            try {
                gestionarDetalleUseCase.agregarOActualizarProducto(detalle)
                _uiEvent.value = UIEvent.ProductoGuardado
            } catch (e: Exception) {
                _uiEvent.value = UIEvent.Error("ERROR AL GUARDAR PRODUCTO: ${e.message}")
            }
        }
    }

    fun eliminarProducto(idDetalle: Int, idPedido: Int) {
        viewModelScope.launch {
            try {
                gestionarDetalleUseCase.eliminarProducto(idDetalle, idPedido)
                _uiEvent.value = UIEvent.ProductoGuardado
            } catch (e: Exception) {
                _uiEvent.value = UIEvent.Error("ERROR AL ELIMINAR PRODUCTO")
            }
        }
    }

    fun resetEvents() { _uiEvent.value = null }
}
