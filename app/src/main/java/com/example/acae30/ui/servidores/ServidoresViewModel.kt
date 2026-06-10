package com.example.acae30.ui.servidores

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.data.repository.ServidoresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServidoresViewModel(
    private val repository: ServidoresRepository
) : ViewModel() {

    val servidores = repository.obtenerServidores()

    private val _resultadoConexion = MutableStateFlow("")
    val resultadoConexion = _resultadoConexion.asStateFlow()

    fun verificarConexion(ip: String, puerto: String, sslActivo: Int, context: Context){
        viewModelScope.launch {
            _resultadoConexion.value = repository.verificarConexionServidor(ip, puerto, sslActivo, context)
        }
    }

}