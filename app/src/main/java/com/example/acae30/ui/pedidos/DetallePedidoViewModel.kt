package com.example.acae30.ui.pedidos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.data.local.entity.ClienteSucursalEntity
import com.example.acae30.domain.usecase.ActualizarSucursalPedidoUseCase
import com.example.acae30.domain.usecase.GetSucursalesUseCase
import com.example.acae30.domain.models.TicketData
import com.example.acae30.domain.usecase.pedidos.ActualizarTotalesFiscalesUseCase
import com.example.acae30.domain.usecase.pedidos.CalcularTotalesFiscalesUseCase
import com.example.acae30.domain.usecase.pedidos.EliminarPedidoUseCase
import com.example.acae30.domain.usecase.pedidos.EnviarPedidoUseCase
import com.example.acae30.domain.usecase.pedidos.GetDetallePedidoFlowUseCase
import com.example.acae30.domain.usecase.pedidos.GetTicketDataUseCase
import com.example.acae30.domain.usecase.pedidos.ObtenerCantidadItemsUseCase
import com.example.acae30.modelos.DetallePedido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * REFACTORIZACIÓN MVVM: ViewModel para Detallepedido.
 * Gestiona sucursales, conteo de items y cálculos fiscales de forma asíncrona.
 * Optimizado para evitar ANR mediante manejo de hilos y reducción de emisiones redundantes.
 */
class DetallePedidoViewModel(
    private val getSucursalesUseCase: GetSucursalesUseCase,
    private val actualizarSucursalPedidoUseCase: ActualizarSucursalPedidoUseCase,
    private val obtenerCantidadItemsUseCase: ObtenerCantidadItemsUseCase,
    private val getDetallePedidoFlowUseCase: GetDetallePedidoFlowUseCase,
    private val calcularTotalesFiscalesUseCase: CalcularTotalesFiscalesUseCase,
    private val actualizarTotalesFiscalesUseCase: ActualizarTotalesFiscalesUseCase,
    private val enviarPedidoUseCase: EnviarPedidoUseCase,
    private val eliminarPedidoUseCase: EliminarPedidoUseCase,
    private val getTicketDataUseCase: GetTicketDataUseCase
) : ViewModel() {

    // Estados de UI
    private val _sucursales = MutableLiveData<List<ClienteSucursalEntity>>()
    val sucursales: LiveData<List<ClienteSucursalEntity>> = _sucursales

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _cantidadItems = MutableLiveData<Int>()
    val cantidadItems: LiveData<Int> = _cantidadItems

    private val _detallePedido = MutableLiveData<List<DetallePedido>>()
    val detallePedido: LiveData<List<DetallePedido>> = _detallePedido

    private val _totalesFiscales = MutableLiveData<CalcularTotalesFiscalesUseCase.ResultadoTotales>()
    val totalesFiscales: LiveData<CalcularTotalesFiscalesUseCase.ResultadoTotales> = _totalesFiscales

    private val _infoPedido = MutableLiveData<com.example.acae30.modelos.Pedidos?>()
    val infoPedido: LiveData<com.example.acae30.modelos.Pedidos?> = _infoPedido

    private val _infoCliente = MutableLiveData<com.example.acae30.modelos.Cliente?>()
    val infoCliente: LiveData<com.example.acae30.modelos.Cliente?> = _infoCliente

    private val _envioExitoso = MutableLiveData<Boolean?>()
    val envioExitoso: LiveData<Boolean?> = _envioExitoso

    private val _eliminacionExitosa = MutableLiveData<Boolean?>()
    val eliminacionExitosa: LiveData<Boolean?> = _eliminacionExitosa

    private val _ticketData = MutableLiveData<TicketData?>()
    val ticketData: LiveData<TicketData?> = _ticketData

    // Estado de validación de saldo para evitar bloqueos en la Activity
    private val _validacionSaldo = MutableLiveData<ResultadoValidacionSaldo?>()
    val validacionSaldo: LiveData<ResultadoValidacionSaldo?> = _validacionSaldo

    data class ResultadoValidacionSaldo(
        val esValido: Boolean,
        val mensajeError: String? = null
    )

    /**
     * Carga las sucursales de un cliente.
     */
    fun cargarSucursales(idCliente: Int) {
        viewModelScope.launch {
            _cargando.value = true
            val lista = getSucursalesUseCase(idCliente)
            _sucursales.value = lista
            _cargando.value = false
        }
    }

    /**
     * Actualiza la sucursal del pedido actual.
     */
    fun seleccionarSucursal(idPedido: Int, idCliente: Int, nombreSucursal: String) {
        viewModelScope.launch {
            actualizarSucursalPedidoUseCase(idPedido, idCliente, nombreSucursal)
        }
    }

    /**
     * Obtiene la cantidad de productos en el pedido.
     */
    fun cargarCantidadItems(idPedido: Int) {
        viewModelScope.launch {
            val cantidad = obtenerCantidadItemsUseCase(idPedido)
            _cantidadItems.value = cantidad
        }
    }

    /**
     * Carga la información de cabecera del pedido (vendedor, términos, etc.) de forma asíncrona.
     */
    fun cargarInfoPedido(idPedido: Int, idCliente: Int, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val pController = com.example.acae30.controllers.PedidosController()
            val cController = com.example.acae30.controllers.ClientesController()
            
            val infoP = pController.obtenerInformacionPedido(idPedido, context)
            val infoC = cController.obtenerInformacionCliente(context, idCliente)
            
            _infoPedido.postValue(infoP)
            _infoCliente.postValue(infoC)
        }
    }

    /**
     * Inicia la observación del detalle del pedido.
     * OPTIMIZACIÓN: Se usa distinctUntilChanged para evitar procesar actualizaciones de stock que no alteran el pedido.
     */
    fun observarDetallePedido(idPedido: Int) {
        viewModelScope.launch {
            getDetallePedidoFlowUseCase(idPedido)
                .distinctUntilChanged { old, new -> 
                    // Solo emitimos si cambia la cantidad de items o el ID del detalle (evita ruidos por stock)
                    old.size == new.size && old.zip(new).all { (o, n) -> o.Id == n.Id && o.Cantidad == n.Cantidad && o.Precio_iva == n.Precio_iva }
                }
                .collectLatest { lista ->
                    _detallePedido.value = lista
                    recalcularTotales(idPedido, lista)
                }
        }
    }

    /**
     * Verifica el saldo del cliente contra el servidor (con fallback local) antes de proceder.
     * REFACTORIZACIÓN: Implementa fallback a datos locales de Room si falla la conexión.
     */
    fun verificarSaldoYProceder(context: android.content.Context, idCliente: Int, totalPedido: Float, terminos: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                var balanceActual = 0f
                var limiteCredito = 0f
                var verificadoConServidor = false

                val controller = com.example.acae30.controllers.ClientesController()
                // 1. Intentar obtener balance fresco del servidor
                val balanceFresh = try {
                    controller.obtenerBalacenClientePorId(context, idCliente)
                } catch (e: Exception) {
                    null
                }
                
                if (balanceFresh != null) {
                    balanceActual = balanceFresh.balance
                    limiteCredito = balanceFresh.limiteCredito
                    verificadoConServidor = true
                } else {
                    // 2. FALLBACK: Usar datos locales de Room si no hay conexión
                    val db = com.example.acae30.data.local.appDatabase.AppDatabase.getInstance(context)
                    val clienteLocal = db.clienteDao().obtenerClientePorId(idCliente)
                    if (clienteLocal != null) {
                        balanceActual = clienteLocal.balance?.toFloat() ?: 0f
                        limiteCredito = clienteLocal.limiteCredito?.toFloat() ?: 0f
                        Timber.w("[VIEWMODEL] USANDO SALDO LOCAL (FALLBACK)")
                    } else {
                        _validacionSaldo.value = ResultadoValidacionSaldo(false, "NO SE ENCONTRARON DATOS DEL CLIENTE PARA VALIDAR SALDO")
                        return@launch
                    }
                }

                val nuevoBalanceReal = balanceActual + totalPedido

                // Validación de crédito
                if (terminos.equals("Credito", ignoreCase = true) && nuevoBalanceReal > limiteCredito) {
                    val prefijo = if (verificadoConServidor) "LÍMITE EXCEDIDO (ONLINE):" else "LÍMITE EXCEDIDO (LOCAL):"
                    val mensaje = String.format(
                        java.util.Locale.getDefault(),
                        "%s Saldo ($%.2f) + Pedido ($%.2f) = $%.2f. El límite es de $%.2f",
                        prefijo, balanceActual, totalPedido, nuevoBalanceReal, limiteCredito
                    )
                    _validacionSaldo.value = ResultadoValidacionSaldo(false, mensaje)
                } else {
                    _validacionSaldo.value = ResultadoValidacionSaldo(true)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error crítico al verificar saldo")
                _validacionSaldo.value = ResultadoValidacionSaldo(false, "ERROR AL VALIDAR CRÉDITO: ${e.message}")
            } finally {
                _cargando.value = false
            }
        }
    }

    fun resetValidacionSaldo() {
        _validacionSaldo.value = null
    }

    /**
     * Calcula y guarda los totales fiscales.
     */
    fun actualizarTotalesFiscales(
        idPedido: Int,
        totalBase: Double,
        tipoDocumento: String,
        esGranContribuyente: Boolean
    ) {
        viewModelScope.launch {
            val resultado = calcularTotalesFiscalesUseCase(totalBase, tipoDocumento, esGranContribuyente)
            _totalesFiscales.value = resultado
            
            // Persistir en la base de datos (en hilo IO)
            actualizarTotalesFiscalesUseCase(idPedido, resultado.sumas, resultado.iva, resultado.ivaPerci)
        }
    }

    private fun recalcularTotales(idPedido: Int, lista: List<DetallePedido>) {
        val totalBase = lista.sumOf { it.Total_iva?.toDouble() ?: 0.0 }
        val currentP = _infoPedido.value
        val currentC = _infoCliente.value
        
        val tipoDoc = currentP?.Tipo_documento ?: "FC"
        val categoria = currentC?.Categoria_cliente ?: ""
        val esGranContribuyente = categoria.contains("Gran contribuyente", ignoreCase = true)
        
        actualizarTotalesFiscales(idPedido, totalBase, tipoDoc, esGranContribuyente)
    }

    /**
     * Envia el pedido al servidor de forma asíncrona.
     * REFACTORIZACIÓN: Refuerzo de seguridad con try-catch-finally para asegurar cierre de diálogos.
     */
    fun enviarPedido(idPedido: Int) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val éxito = enviarPedidoUseCase(idPedido)
                // Mantener la animación de envío por al menos 3 segundos si fue exitoso
                if (éxito) delay(3000)
                _envioExitoso.value = éxito
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al enviar pedido")
                _envioExitoso.value = false
            } finally {
                _cargando.value = false
            }
        }
    }

    fun resetEnvioStatus() {
        _envioExitoso.value = null
    }

    /**
     * Elimina el pedido y sus detalles de forma local.
     */
    fun eliminarPedido(idPedido: Int) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                eliminarPedidoUseCase(idPedido)
                _eliminacionExitosa.value = true
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar pedido")
                _eliminacionExitosa.value = false
            } finally {
                _cargando.value = false
            }
        }
    }

    fun resetEliminacionStatus() {
        _eliminacionExitosa.value = null
    }

    /**
     * REFACTORIZACIÓN MVVM: Recolecta todos los datos necesarios para imprimir un ticket.
     */
    fun obtenerDatosImpresion(idPedido: Int) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val data = getTicketDataUseCase(idPedido)
                _ticketData.value = data
            } catch (e: Exception) {
                Timber.e(e, "Error al obtener datos de impresión")
            } finally {
                _cargando.value = false
            }
        }
    }

    fun resetTicketData() {
        _ticketData.value = null
    }
}
