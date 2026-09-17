package com.example.acae30.ui.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.local.entity.InventarioLotesEntity
import com.example.acae30.data.local.entity.PedidoDetalleEntity
import com.example.acae30.data.local.models.Inventario
import com.example.acae30.data.repository.ClientesRepository
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class ProductoAgregarViewModel(
    private val inventarioRepository: InventarioRepository,
    private val clientesRepository: ClientesRepository,
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

    // Estado de Mayorista del cliente
    private val _esMayorista = MutableStateFlow(false)
    val esMayorista = _esMayorista.asStateFlow()

    // Listado de lotes para el producto
    private val _listaLotes = MutableStateFlow<List<InventarioLotesEntity>>(emptyList())
    val listaLotes = _listaLotes.asStateFlow()

    // Detalle del pedido si estamos en modo edición
    private val _detallePedido = MutableStateFlow<PedidoDetalleEntity?>(null)
    val detallePedido = _detallePedido.asStateFlow()

    // Precio Autorizado vía Token
    private val _precioAutorizado = MutableStateFlow<Float?>(null)
    val precioAutorizado = _precioAutorizado.asStateFlow()

    // Estados de Stock para la UI
    private val _stockUni = MutableStateFlow("0")
    val stockUni = _stockUni.asStateFlow()

    private val _stockFra = MutableStateFlow("0")
    val stockFra = _stockFra.asStateFlow()

    // Stock total normalizado para validaciones
    private val _stockTotalValidacion = MutableStateFlow(0f)
    val stockTotalValidacion = _stockTotalValidacion.asStateFlow()

    // REFACTORIZACIÓN: Cantidad ya reservada en el pedido actual (normalizada)
    private val _cantidadYaEnPedidoNormalizada = MutableStateFlow(0.0)
    val cantidadYaEnPedidoNormalizada = _cantidadYaEnPedidoNormalizada.asStateFlow()

    // Precio final calculado (Personalizado o Viñeta)
    private val _precioFinal = MutableStateFlow(0f)
    val precioFinal = _precioFinal.asStateFlow()

    // Cantidad mínima requerida según la escala seleccionada
    private val _cantidadMinimaEscala = MutableStateFlow(0f)
    val cantidadMinimaEscala = _cantidadMinimaEscala.asStateFlow()

    // Indica si el precio actual viene de la tabla cliente_precios
    private val _esPrecioPersonalizado = MutableStateFlow(false)
    val esPrecioPersonalizado = _esPrecioPersonalizado.asStateFlow()

    // Cantidad de bonificación calculada
    private val _bonificado = MutableStateFlow(0)
    val bonificado = _bonificado.asStateFlow()

    // Total de la línea (Precio * Cantidad)
    private val _totalLinea = MutableStateFlow(0f)
    val totalLinea = _totalLinea.asStateFlow()

    // ID de la escala seleccionada para guardar en el detalle
    private val _idEscalaSeleccionada = MutableStateFlow(0)
    val idEscalaSeleccionada = _idEscalaSeleccionada.asStateFlow()

    // Resultado de la validación integral para habilitar el botón
    data class ValidationResult(
        val isValid: Boolean,
        val error: String? = null
    )
    private val _validationResult = MutableStateFlow(ValidationResult(false))
    val validationResult = _validationResult.asStateFlow()

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
    fun cargarProducto(idProducto: Int, idCliente: Int, idPedido: Int, unidadInicial: String) {
        viewModelScope.launch {
            try {
                // 1. Cargar Info del Cliente primero para asegurar el flag de Mayorista
                val cliente = clientesRepository.obtenerClientePorId(idCliente)
                val mayorista = (cliente?.mayorista?.trim()?.uppercase() == "S")
                _esMayorista.value = mayorista

                // 2. Cargar Info Producto
                val p = inventarioRepository.obtenerProductoPorId(idProducto)
                _producto.value = p
                
                // 3. Cargar Lotes
                val lotes = inventarioRepository.obtenerLotesPorProducto(idProducto)
                _listaLotes.value = lotes

                if (p != null) {
                    actualizarStock(p)
                    
                    // 4. Calcular ocupación actual en el pedido (Omitimos el item actual si estamos editando)
                    val idItemEdicion = _detallePedido.value?.id ?: 0
                    calcularOcupacionEnPedido(idPedido, idProducto, p.Fraccion ?: 0f, idItemEdicion)
                }
            } catch (e: Exception) {
                Timber.e(e, "[PRODUCTO_AGREGAR_VM] ERROR CARGANDO PRODUCTO")
            }
        }
    }

    /**
     * Calcula cuántas unidades de este producto ya están en el pedido actual.
     */
    private suspend fun calcularOcupacionEnPedido(idPedido: Int, idProducto: Int, realFraccion: Float, idOmitir: Int = 0) {
        try {
            val ocupacion = gestionarDetalleUseCase.obtenerOcupacionTotal(idPedido, idProducto, realFraccion, idOmitir)
            _cantidadYaEnPedidoNormalizada.value = ocupacion
        } catch (e: Exception) {
            Timber.e(e, "Error calculando ocupación previa")
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

    fun recalcularValores(
        idCliente: Int,
        idProducto: Int,
        cantidad: Float,
        unidad: String,
        unidadBase: String, 
        factorEquivalencia: Float,
        tipoBonif: String,
        precioSeleccionadoUi: Float? = null,
        sinExistencias: Int = 0
    ) {
        viewModelScope.launch {
            val p = _producto.value ?: return@launch
            
            val precioBaseFicha = if (unidad == "FRA") p.Precio_u_iva ?: 0f else p.Precio_iva ?: 0f
            val precioConvenio = calcularPrecioUseCase.ejecutar(idCliente, idProducto, unidad, precioBaseFicha)
            val esPersonalizado = (precioConvenio != precioBaseFicha && unidad == "UNI")
            
            val precioFinalCalculado = if (esPersonalizado) {
                precioConvenio
            } else {
                precioSeleccionadoUi ?: precioBaseFicha
            }

            _esPrecioPersonalizado.value = esPersonalizado
            _precioFinal.value = precioFinalCalculado

            val escalas = inventarioRepository.obtenerEscalasPrecios(idProducto, unidad)
            val scale = escalas.find { 
                val diff = Math.abs(it.precio_iva - precioFinalCalculado)
                diff < 0.001 
            }
            
            val minEscala = scale?.cantidad ?: 0f
            _idEscalaSeleccionada.value = scale?.id ?: 0
            _cantidadMinimaEscala.value = minEscala

            val regalias = calcularBonificacionesUseCase.ejecutar(
                idCliente, idProducto, cantidad, unidadBase, factorEquivalencia, tipoBonif, p.Bonificado ?: 0f
            )
            _bonificado.value = regalias
            _totalLinea.value = precioFinalCalculado * cantidad

            validarEstado(cantidad, unidad, p.Fraccion ?: 0f, factorEquivalencia, minEscala, sinExistencias)
        }
    }

    private fun validarEstado(
        cantidad: Float,
        unidad: String,
        realFraccion: Float,
        factorEquivalencia: Float,
        minEscala: Float,
        sinExistencias: Int
    ) {
        val capacidadParaCalculo = if (realFraccion > 1f) realFraccion else 1f
        var cantidadNormalizada = 0.0
        
        when (unidad) {
            "UNI" -> cantidadNormalizada = if (realFraccion > 1f) (cantidad * capacidadParaCalculo).toDouble() else cantidad.toDouble()
            "FRA" -> cantidadNormalizada = cantidad.toDouble()
            else -> {
                cantidadNormalizada = if (realFraccion > 1f) ((cantidad * factorEquivalencia) * capacidadParaCalculo).toDouble() else (cantidad * factorEquivalencia).toDouble()
            }
        }

        // MULTIPLES PEDIDOS: Sumar bonificados al consumo de stock (Normalizados a la unidad base)
        val bonificados = _bonificado.value.toDouble()
        val bonificadosNormalizados = if (realFraccion > 1f) bonificados * capacidadParaCalculo else bonificados
        val consumoTotalPropuesto = cantidadNormalizada + bonificadosNormalizados

        val umbralEscala = if (realFraccion > 1f) minEscala * capacidadParaCalculo else minEscala
        val stockDisponible = _stockTotalValidacion.value.toDouble()
        val yaEnPedido = _cantidadYaEnPedidoNormalizada.value
        val precioActual = _precioFinal.value
        val esMayorista = _esMayorista.value

        val result = when {
            cantidad <= 0f -> ValidationResult(false, "CAMPO NO PUEDE QUEDAR VACIO")
            precioActual <= 0f -> ValidationResult(false, "EL PRECIO DEBE SER MAYOR A 0")
            (consumoTotalPropuesto + yaEnPedido > stockDisponible) && sinExistencias == 0 -> {
                val msg = if (yaEnPedido > 0) "STOCK INSUFICIENTE para cubrir Venta + Regalía (Ya tiene reservado en pedido)" 
                          else "STOCK INSUFICIENTE para cubrir Venta + Regalía"
                ValidationResult(false, msg)
            }
            (cantidadNormalizada < umbralEscala) && !esMayorista -> 
                ValidationResult(false, "LA CANTIDAD NO ES VÁLIDA PARA EL PRECIO SELECCIONADO")
            else -> ValidationResult(true)
        }
        
        _validationResult.value = result
    }

    fun buscarTokenAutorizacion(idVendedor: Int, codProducto: String) {
        viewModelScope.launch {
            val precio = consultarTokenUseCase.ejecutar(idVendedor, codProducto)
            _precioAutorizado.value = precio
            if (precio == null) {
                _uiEvent.value = UIEvent.Error("NO SE ENCONTRÓ PRECIO AUTORIZADO")
            }
        }
    }

    fun confirmarTokenYGuardar(idVendedor: Int, codProducto: String, detalle: PedidoDetalleEntity, sinExistencias: Int) {
        viewModelScope.launch {
            val ok = confirmarTokenUseCase.ejecutar(idVendedor, codProducto)
            if (ok) {
                guardarProducto(detalle, sinExistencias)
            } else {
                _uiEvent.value = UIEvent.Error("ERROR AL CONFIRMAR EL TOKEN EN EL SERVIDOR")
            }
        }
    }

    fun guardarProducto(detalle: PedidoDetalleEntity, sinExistencias: Int) {
        viewModelScope.launch {
            try {
                val p = _producto.value
                val stock = _stockTotalValidacion.value
                
                // Si llegamos aquí y el botón está habilitado, es porque la validación previa pasó
                // o estamos permitiendo vender sin existencias (si fuera el caso).
                // Pero como doble seguridad, el UseCase volverá a verificar la suma total.
                gestionarDetalleUseCase.agregarOActualizarProducto(
                    detalle, 
                    stock, 
                    p?.Fraccion ?: 0f,
                    sinExistencias // Por seguridad, el UseCase siempre valida stock real en esta versión
                )
                _uiEvent.value = UIEvent.ProductoGuardado
            } catch (e: Exception) {
                _uiEvent.value = UIEvent.Error(e.message ?: "ERROR AL GUARDAR PRODUCTO")
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
