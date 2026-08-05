package com.example.acae30.ui.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acae30.data.repository.BluetoothRepository
import com.example.acae30.domain.usecase.bluetooth.ObtenerImpresorasVinculadasUseCase
import com.example.acae30.ui.configuracion.ConfiguracionViewModel

class ConfiguracionViewModelFactory(
    private val bluetoothRepository: BluetoothRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfiguracionViewModel::class.java)) {
            val useCase = ObtenerImpresorasVinculadasUseCase(bluetoothRepository)
            return ConfiguracionViewModel(bluetoothRepository, useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
