package com.example.acae30.ui.cuentas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.domain.usecase.cuentas.ObtenerClientesConCuentasUseCase
import com.example.acae30.domain.usecase.cuentas.ObtenerDetalleCuentasUseCase
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.Cuenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CuentasViewModel(
    private val obtenerClientesUseCase: ObtenerClientesConCuentasUseCase,
    private val obtenerDetalleUseCase: ObtenerDetalleCuentasUseCase
) : ViewModel() {

    // Listado de clientes con deudas
    private val _clientesConCuentas = MutableStateFlow<List<Cliente>>(emptyList())
    val clientesConCuentas = _clientesConCuentas.asStateFlow()

    // Listado de facturas (detalle) del cliente seleccionado
    private val _detalleCuentas = MutableStateFlow<List<Cuenta>>(emptyList())
    val detalleCuentas = _detalleCuentas.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    //--------------------------------------------------------
    //Carga o busca clientes que tienen facturas pendientes.
    //--------------------------------------------------------
    fun cargarClientesConCuentas(nombre: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val listado = obtenerClientesUseCase.ejecutar(nombre)
                _clientesConCuentas.value = listado
            } catch (e: Exception) {
                Timber.e(e,"[CUENTAS_VIEWMODEL] ERROR AL CARGAR TODAS LAS CUENTAS PENDIENTES")
            } finally {
                _isLoading.value = false
            }
        }
    }

    //--------------------------------------------------------
    // Carga el detalle de facturas de un cliente según el filtro.
    //--------------------------------------------------------
    fun cargarDetalleCuentas(idCliente: Int, filtro: String = "Todas") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val detalle = obtenerDetalleUseCase.ejecutar(idCliente, filtro)
                _detalleCuentas.value = detalle
            } catch (e: Exception) {
                Timber.e(e,"[CUENTAS_VIEWMODEL] ERROR AL MOSTRAR EL DETALLE DE FATURAS POR CLIENTE")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
