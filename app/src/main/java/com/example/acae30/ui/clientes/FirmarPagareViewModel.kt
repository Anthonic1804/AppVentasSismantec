package com.example.acae30.ui.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.domain.usecase.clientes.ActualizarPagareUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirmarPagareViewModel(
    private val actualizarPagareUseCase: ActualizarPagareUseCase
) : ViewModel() {

    // Estado del proceso de guardado
    sealed class SaveStatus {
        object Guardando : SaveStatus()
        object Exito : SaveStatus()
        data class Error(val mensaje: String) : SaveStatus()
    }

    private val _saveStatus = MutableStateFlow<SaveStatus?>(null)
    val saveStatus = _saveStatus.asStateFlow()

    //--------------------------------------------------------------
    // Sincroniza la firma con el servidor y actualiza la base de datos local.
    //--------------------------------------------------------------
    fun confirmarFirma(idCliente: Int) {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Guardando
            
            val exito = actualizarPagareUseCase.ejecutar(idCliente)
            
            if (exito) {
                _saveStatus.value = SaveStatus.Exito
            } else {
                _saveStatus.value = SaveStatus.Error("NO SE LOGRÓ SINCRONIZAR LA FIRMA CON EL SERVIDOR")
            }
        }
    }

    fun resetStatus() {
        _saveStatus.value = null
    }
}
