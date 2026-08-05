package com.example.acae30.ui.configuracion

import androidx.lifecycle.ViewModel
import com.example.acae30.data.repository.BluetoothRepository
import com.example.acae30.domain.usecase.bluetooth.ObtenerImpresorasVinculadasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfiguracionViewModel(
    private val bluetoothRepository: BluetoothRepository,
    private val obtenerImpresorasUseCase: ObtenerImpresorasVinculadasUseCase
) : ViewModel() {

    // Lista de nombres de impresoras para el Spinner
    private val _listaImpresoras = MutableStateFlow<List<String>>(emptyList())
    val listaImpresoras = _listaImpresoras.asStateFlow()

    // Estado del Bluetooth
    private val _bluetoothHabilitado = MutableStateFlow(false)
    val bluetoothHabilitado = _bluetoothHabilitado.asStateFlow()

    //----------------------------------------------------------
    //Solicita al Caso de Uso la lista de impresoras emparejadas.
    //----------------------------------------------------------
    fun cargarImpresoras() {
        _bluetoothHabilitado.value = bluetoothRepository.isBluetoothEnabled()
        if (_bluetoothHabilitado.value) {
            val impresoras = obtenerImpresorasUseCase.ejecutar()
            _listaImpresoras.value = impresoras
        } else {
            _listaImpresoras.value = emptyList()
        }
    }
}
