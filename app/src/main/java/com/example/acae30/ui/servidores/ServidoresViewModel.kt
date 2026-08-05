package com.example.acae30.ui.servidores

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acae30.data.local.entity.ServidoresEntity
import com.example.acae30.data.remote.dto.UpdateAppDto
import com.example.acae30.data.repository.ServidoresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServidoresViewModel(
    private val repository: ServidoresRepository
) : ViewModel() {

    // Obtenemos el listado como un Flow directamente desde el repositorio.
    val servidores = repository.obtenerServidores()

    // Manejo de estado para el resultado de la conexión
    private val _resultadoConexion = MutableStateFlow("")
    val resultadoConexion = _resultadoConexion.asStateFlow()

    // Manejo de estado para saber si una operación (Insert, Update, Delete) fue exitosa
    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa = _operacionExitosa.asStateFlow()

    // Estado para la actualización de la App
    private val _updateInfo = MutableStateFlow<UpdateAppDto?>(null)
    val updateInfo = _updateInfo.asStateFlow()

    // La lógica de red se delega al repositorio.
    fun verificarConexion(ip: String, puerto: String, sslActivo: Int, context: Context){
        _resultadoConexion.value = ""

        viewModelScope.launch {
            _resultadoConexion.value = repository.verificarConexionServidor(ip, puerto, sslActivo, context)
        }
    }

    // Nueva función para buscar actualización de la App
    fun buscarActualizacion(baseUrl: String, context: Context) {
        _updateInfo.value = null

        viewModelScope.launch {
            _updateInfo.value = repository.obtenerActualizacionApp(baseUrl, context)
        }
    }

    // Registro de servidor
    fun registrarServidor(nombre: String, ip: String, puerto: String, ssl: Int) {
        viewModelScope.launch {
            val entity = ServidoresEntity(0, nombre, ip, puerto, ssl)
            repository.registrarServidor(entity)
            _operacionExitosa.value = true
        }
    }

    // Actualización de servidor
    fun actualizarServidor(id: Int, nombre: String, ip: String, puerto: String, ssl: Int) {
        viewModelScope.launch {
            val entity = ServidoresEntity(id, nombre, ip, puerto, ssl)
            repository.actualizarServidor(entity)
            _operacionExitosa.value = true
        }
    }

    // Eliminación de servidor
    fun eliminarServidor(id: Int) {
        viewModelScope.launch {
            repository.eliminarServidor(id)
            _operacionExitosa.value = true
        }
    }

    // Función para resetear el estado de la operación
    fun resetOperacion() {
        _operacionExitosa.value = false
    }

}
