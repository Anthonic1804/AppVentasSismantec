package com.example.acae30.ui.pedidos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.Funciones
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.CuentasRepository
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.data.repository.VisitasRepository
import com.example.acae30.domain.usecase.FinalizarVisitaUseCase
import com.example.acae30.domain.usecase.IniciarVisitaUseCase
import kotlinx.coroutines.launch

/**
 * REFACTORIZACIÓN MVVM: ViewModel para gestionar la lógica de Visitas.
 * Recibe dependencias por constructor siguiendo el patrón del proyecto.
 */
class VisitasViewModel(
    private val visitasRepository: VisitasRepository,
    private val pedidosRepository: PedidosRepository,
    private val clientesRepository: ClientesRepository,
    private val cuentasRepository: CuentasRepository
) : ViewModel() {

    private val funciones = Funciones()

    // Casos de Uso
    private val iniciarVisitaUseCase = IniciarVisitaUseCase(visitasRepository)
    private val finalizarVisitaUseCase = FinalizarVisitaUseCase(visitasRepository)

    // Estados de la UI
    private val _estadoVisita = MutableLiveData<EstadoVisita>()
    val estadoVisita: LiveData<EstadoVisita> = _estadoVisita

    private val _navegacion = MutableLiveData<EventoNavegacion?>()
    val navegacion: LiveData<EventoNavegacion?> = _navegacion

    private val _clienteMoroso = MutableLiveData<Boolean>()
    val clienteMoroso: LiveData<Boolean> = _clienteMoroso

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    sealed class EstadoVisita {
        data object SinIniciar : EstadoVisita()
        data class Activa(val idLocal: Int, val idServidor: Int) : EstadoVisita()
        data object Finalizada : EstadoVisita()
    }

    sealed class EventoNavegacion {
        data class IrADetallePedido(val idPedido: Int, val idVisita: Int, val idApi: Int) : EventoNavegacion()
        data object IrAPedidoPrincipal : EventoNavegacion()
        data object IrAClientes : EventoNavegacion()
    }

    fun verificarVisitaActiva(idCliente: Int) {
        viewModelScope.launch {
            val visita = visitasRepository.obtenerVisitaPorIdCliente(idCliente)
            if (visita != null && visita.abierta) {
                _estadoVisita.value = EstadoVisita.Activa(visita.id, visita.idVisita)
            } else {
                _estadoVisita.value = EstadoVisita.SinIniciar
            }
        }
    }
    
    fun cargarVisitaPorId(idLocal: Int) {
        viewModelScope.launch {
            val visita = visitasRepository.obtenerVisitaLocalPorId(idLocal)
            if (visita != null && visita.abierta) {
                _estadoVisita.value = EstadoVisita.Activa(visita.id, visita.idVisita)
            } else {
                _estadoVisita.value = EstadoVisita.SinIniciar
            }
        }
    }

    fun verificarMora(idCliente: Int) {
        viewModelScope.launch {
            val cuentas = cuentasRepository.obtenerCuentasVencidas(idCliente)
            _clienteMoroso.value = cuentas.isNotEmpty()
        }
    }

    fun iniciarVisita(idCliente: Int, nombreCliente: String, idVendedor: Int, gps: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val fecha = funciones.getFechaHoraProceso() ?: ""
                val idLocal = iniciarVisitaUseCase(idCliente, nombreCliente, idVendedor, gps, fecha)
                
                val visita = visitasRepository.obtenerVisitaLocalPorId(idLocal.toInt())
                _estadoVisita.value = EstadoVisita.Activa(idLocal.toInt(), visita?.idVisita ?: 0)
                _mensaje.value = "Visita Iniciada"
            } catch (e: Exception) {
                _mensaje.value = "Error al iniciar visita: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun finalizarVisita(idLocal: Int, idVendedor: Int, gps: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val fecha = funciones.getFechaHoraProceso() ?: ""
                finalizarVisitaUseCase(idLocal, idVendedor, gps, fecha)
                _estadoVisita.value = EstadoVisita.Finalizada
                _mensaje.value = "Visita Finalizada"
                _navegacion.value = EventoNavegacion.IrAPedidoPrincipal
            } catch (e: Exception) {
                _mensaje.value = "Error al finalizar visita: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun crearPedido(idCliente: Int, nombreCliente: String, idVisitaLocal: Int, idVisitaApi: Int, gps: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val cliente = clientesRepository.obtenerClientePorId(idCliente)
                if (cliente != null) {
                    var tipoDocumento = "FC"
                    if ((cliente.nrc?.length ?: 0) > 2 && !cliente.nrc.isNullOrBlank()) {
                        tipoDocumento = "CF"
                    }

                    val idPedido = pedidosRepository.crearNuevoPedidoLocal(
                        idCliente = idCliente,
                        nombreCliente = nombreCliente,
                        terminos = cliente.terminosCliente ?: "",
                        idRuta = cliente.idRuta,
                        ruta = cliente.ruta ?: "",
                        tipoDocumento = tipoDocumento,
                        dteDireccion = cliente.dteDireccion ?: "",
                        dteCodDepto = cliente.dteCodDepto ?: "",
                        dteCodMunicipio = cliente.dteCodMunicipio ?: "",
                        dteCodPais = cliente.dteCodPais ?: "",
                        dtePais = cliente.dtePais ?: "",
                        dteCorreo = cliente.dteCorreo ?: "",
                        dteTelefono = cliente.dteTelefono ?: "",
                        idVisitaGlobal = idVisitaLocal,
                        gps = gps
                    )

                    _navegacion.value = EventoNavegacion.IrADetallePedido(idPedido, idVisitaLocal, idVisitaApi)
                } else {
                    _mensaje.value = "Error: No se encontró la información del cliente"
                }
            } catch (e: Exception) {
                _mensaje.value = "Error al crear pedido: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun resetNavegacion() {
        _navegacion.value = null
    }
}
