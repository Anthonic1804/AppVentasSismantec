package com.example.acae30.ui.pedidos

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.PedidosController
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.local.entity.ClienteSucursalEntity
import com.example.acae30.data.local.entity.PedidosEntity
import com.example.acae30.domain.usecase.ActualizarSucursalPedidoUseCase
import com.example.acae30.domain.usecase.GetSucursalesUseCase
import com.example.acae30.domain.models.TicketData
import com.example.acae30.domain.usecase.pedidos.ActualizarTotalesFiscalesUseCase
import com.example.acae30.domain.usecase.pedidos.ActualizarNombreClienteUseCase
import com.example.acae30.domain.usecase.pedidos.CalcularTotalesFiscalesUseCase
import com.example.acae30.domain.usecase.pedidos.EliminarPedidoUseCase
import com.example.acae30.domain.usecase.pedidos.CrearPedidoUseCase
import com.example.acae30.domain.usecase.pedidos.EnviarPedidoUseCase
import com.example.acae30.domain.usecase.pedidos.GetDetallePedidoFlowUseCase
import com.example.acae30.domain.usecase.pedidos.GetPedidosBorradoresUseCase
import com.example.acae30.domain.usecase.pedidos.GetTicketDataUseCase
import com.example.acae30.domain.usecase.pedidos.ObtenerCantidadItemsUseCase
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.Pedidos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale

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
    private val getTicketDataUseCase: GetTicketDataUseCase,
    private val getPedidosBorradoresUseCase: GetPedidosBorradoresUseCase,
    private val crearPedidoUseCase: CrearPedidoUseCase,
    private val actualizarNombreClienteUseCase: ActualizarNombreClienteUseCase
) : ViewModel() {

    // Jobs para cancelar observaciones previas al cambiar de pedido
    private var jobDetalle: Job? = null
    private var jobBorradores: Job? = null

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

    private val _infoPedido = MutableLiveData<Pedidos?>()
    val infoPedido: LiveData<Pedidos?> = _infoPedido

    private val _infoCliente = MutableLiveData<Cliente?>()
    val infoCliente: LiveData<Cliente?> = _infoCliente

    private val _envioExitoso = MutableLiveData<Boolean?>()
    val envioExitoso: LiveData<Boolean?> = _envioExitoso

    private val _eliminacionExitosa = MutableLiveData<Boolean?>()
    val eliminacionExitosa: LiveData<Boolean?> = _eliminacionExitosa

    private val _ticketData = MutableLiveData<TicketData?>()
    val ticketData: LiveData<TicketData?> = _ticketData

    // MULTIPLES PEDIDOS: Evento para notificar a la Activity que se cambió de pedido exitosamente
    private val _pedidoCambiadoContexto = MutableLiveData<PedidosEntity?>()
    val pedidoCambiadoContexto: LiveData<PedidosEntity?> = _pedidoCambiadoContexto

    // MULTIPLES PEDIDOS: Lista de borradores globales
    private val _pedidosBorradores = MutableLiveData<List<PedidosEntity>>()
    val pedidosBorradores: LiveData<List<PedidosEntity>> = _pedidosBorradores

    // Estado de validación de saldo para evitar bloqueos en la Activity
    private val _validacionSaldo = MutableLiveData<ResultadoValidacionSaldo?>()
    val validacionSaldo: LiveData<ResultadoValidacionSaldo?> = _validacionSaldo

    data class ResultadoValidacionSaldo(
        val esValido: Boolean,
        val mensajeError: String? = null
    )

    fun cargarSucursales(idCliente: Int) {
        viewModelScope.launch {
            // MULTIPLES PEDIDOS: Solo mostramos carga si no es un cambio de contexto rápido que ya gestiona su propia carga
            _cargando.value = true
            try {
                val lista = getSucursalesUseCase(idCliente)
                _sucursales.value = lista
            } catch (e: Exception) {
                Timber.e(e, "Error al cargar sucursales")
            } finally {
                _cargando.value = false
            }
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
    fun cargarInfoPedido(idPedido: Int, idCliente: Int, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val pController = PedidosController()
            val cController = ClientesController()
            
            val infoP = pController.obtenerInformacionPedido(idPedido, context)
            val infoC = cController.obtenerInformacionCliente(context, idCliente)
            
            _infoPedido.postValue(infoP)
            _infoCliente.postValue(infoC)
        }
    }

    /**
     * MULTIPLES PEDIDOS: Inicia la observación de borradores globales.
     */
    fun observarBorradores() {
        jobBorradores?.cancel()
        jobBorradores = viewModelScope.launch {
            getPedidosBorradoresUseCase().collectLatest { lista ->
                _pedidosBorradores.value = lista
            }
        }
    }

    /**
     * MULTIPLES PEDIDOS: Cambia el pedido activo sin recargar la Activity.
     */
    fun cambiarPedidoActivo(pedido: PedidosEntity, context: Context) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                // MULTIPLES PEDIDOS: Reset de estados de procesos anteriores para evitar colisiones
                _envioExitoso.value = null
                _eliminacionExitosa.value = null

                // 1. Cargar nueva cabecera e info de cliente
                cargarInfoPedidoSync(pedido.id, pedido.idCliente, context)
                // 2. Cargar sucursales del nuevo cliente para gestionar visibilidad
                val listaSucursales = getSucursalesUseCase(pedido.idCliente)
                _sucursales.value = listaSucursales
                // 3. Reiniciar observación del detalle de productos
                observarDetallePedido(pedido.id)
                // 4. Actualizar conteo de items
                cargarCantidadItems(pedido.id)
                // 5. Notificar cambio exitoso
                _pedidoCambiadoContexto.value = pedido
            } catch (e: Exception) {
                Timber.e(e, "Error al cambiar pedido")
            } finally {
                _cargando.value = false
            }
        }
    }

    private suspend fun cargarInfoPedidoSync(idPedido: Int, idCliente: Int, context: Context) = withContext(Dispatchers.IO) {
        val pController = PedidosController()
        val cController = ClientesController()
        
        val infoP = pController.obtenerInformacionPedido(idPedido, context)
        val infoC = cController.obtenerInformacionCliente(context, idCliente)
        
        _infoPedido.postValue(infoP)
        _infoCliente.postValue(infoC)
    }

    fun resetPedidoCambiadoContexto() {
        _pedidoCambiadoContexto.value = null
    }

    /**
     * MULTIPLES PEDIDOS: Actualiza el nombre del cliente en el pedido actual (para código 01).
     */
    fun actualizarNombreCliente(idPedido: Int, nombre: String) {
        viewModelScope.launch {
            actualizarNombreClienteUseCase(idPedido, nombre)
        }
    }

    /**
     * MULTIPLES PEDIDOS: Crea un nuevo borrador vacío para el cliente actual.
     */
    fun crearNuevoBorrador(idCliente: Int, nombre: String, idVisita: Int, gps: String, context: Context) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val nuevoId = crearPedidoUseCase(idCliente, nombre, idVisita, gps)
                if (nuevoId != null && nuevoId > 0) {
                    // Cargar el pedido recién creado
                    val db = AppDatabase.getInstance(context)
                    val pedidoNuevo = db.pedidosDao().obtenerPedidoPorIdSync(nuevoId)
                    pedidoNuevo?.let { cambiarPedidoActivo(it, context) }
                } else {
                    Timber.e("Error al crear nuevo borrador: ID devuelto inválido")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error crítico al crear borrador")
            } finally {
                _cargando.value = false
            }
        }
    }

    /**
     * Inicia la observación del detalle del pedido.
     * OPTIMIZACIÓN: Se usa distinctUntilChanged para evitar procesar actualizaciones de stock que no alteran el pedido.
     */
    fun observarDetallePedido(idPedido: Int) {
        jobDetalle?.cancel()
        jobDetalle = viewModelScope.launch {
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
    fun verificarSaldoYProceder(context: Context, idCliente: Int, totalPedido: Float, terminos: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                var balanceActual = 0f
                var limiteCredito = 0f
                var verificadoConServidor = false

                val controller = ClientesController()
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
                    val db = AppDatabase.getInstance(context)
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
                        Locale.getDefault(),
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
            
            // Persistir en la base de datos (en hilo IO) incluyendo el Total Final recalculado
            actualizarTotalesFiscalesUseCase(idPedido, resultado.sumas, resultado.iva, resultado.ivaPerci, resultado.totalFinal)
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
            var exitoInterno = false
            try {
                exitoInterno = enviarPedidoUseCase(idPedido)
                if (exitoInterno) delay(3000)
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al enviar pedido")
            } finally {
                _cargando.value = false
                _envioExitoso.value = exitoInterno
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
            var exitoInterno = false
            try {
                eliminarPedidoUseCase(idPedido)
                exitoInterno = true
            } catch (e: Exception) {
                Timber.e(e, "Error al eliminar pedido")
            } finally {
                _cargando.value = false
                _eliminacionExitosa.value = exitoInterno
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
