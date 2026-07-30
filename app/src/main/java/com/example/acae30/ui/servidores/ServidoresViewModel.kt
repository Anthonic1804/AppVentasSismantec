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

// REFACTORIZACIÓN MVVM: El ViewModel actúa como puente entre la UI y el Repositorio.
class ServidoresViewModel(
    private val repository: ServidoresRepository
) : ViewModel() {

    // REFACTORIZACIÓN ROOM: Obtenemos el listado como un Flow directamente desde el repositorio.
    // Esto permite que la UI se actualice automáticamente cuando cambien los datos en la BD.
    val servidores = repository.obtenerServidores()

    // Manejo de estado para el resultado de la conexión (Éxito o Error)
    private val _resultadoConexion = MutableStateFlow("")
    val resultadoConexion = _resultadoConexion.asStateFlow()

    // Manejo de estado para saber si una operación (Insert, Update, Delete) fue exitosa
    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa = _operacionExitosa.asStateFlow()

    // Estado para la actualización de la App
    private val _updateInfo = MutableStateFlow<UpdateAppDto?>(null)
    val updateInfo = _updateInfo.asStateFlow()

    // ARQUITECTURA LIMPIA: La lógica de red se delega al repositorio.
    fun verificarConexion(ip: String, puerto: String, sslActivo: Int, context: Context){
        // BUG FIX: Reseteamos el valor a vacío antes de iniciar.
        // Esto asegura que el StateFlow detecte un cambio incluso si el resultado es el mismo que el anterior.
        _resultadoConexion.value = ""

        viewModelScope.launch {
            _resultadoConexion.value = repository.verificarConexionServidor(ip, puerto, sslActivo, context)
        }
    }

    // Nueva función para buscar actualización de la App
    fun buscarActualizacion(baseUrl: String, context: Context) {
        // BUG FIX: Reseteamos a null para que el StateFlow detecte el cambio 
        // cuando se vuelva a asignar el mismo DTO de actualización.
        _updateInfo.value = null

        viewModelScope.launch {
            _updateInfo.value = repository.obtenerActualizacionApp(baseUrl, context)
        }
    }

    // CRUD: Registro de servidor usando la entidad de Room
    fun registrarServidor(nombre: String, ip: String, puerto: String, ssl: Int) {
        viewModelScope.launch {
            val entity = ServidoresEntity(0, nombre, ip, puerto, ssl)
            repository.registrarServidor(entity)
            _operacionExitosa.value = true
        }
    }

    // CRUD: Actualización de servidor
    fun actualizarServidor(id: Int, nombre: String, ip: String, puerto: String, ssl: Int) {
        viewModelScope.launch {
            val entity = ServidoresEntity(id, nombre, ip, puerto, ssl)
            repository.actualizarServidor(entity)
            _operacionExitosa.value = true
        }
    }

    // CRUD: Eliminación de servidor
    fun eliminarServidor(id: Int) {
        viewModelScope.launch {
            repository.eliminarServidor(id)
            _operacionExitosa.value = true
        }
    }

    // Función para resetear el estado de la operación y evitar ejecuciones repetidas en la UI
    fun resetOperacion() {
        _operacionExitosa.value = false
    }

}
