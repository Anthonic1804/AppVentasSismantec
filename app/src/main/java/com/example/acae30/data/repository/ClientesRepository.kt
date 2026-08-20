package com.example.acae30.data.repository

import com.example.acae30.data.local.dao.ClientesDao
import com.example.acae30.data.local.entity.ClientesEntity
import com.example.acae30.data.remote.api.clientes.ClientesApi
import com.example.acae30.modelos.JSONmodels.ActualizarPagareFirmadoCliente
import timber.log.Timber

class ClientesRepository(
    private val dao: ClientesDao,
    private val api: ClientesApi
) {

    //--------------------------------------------------------
    // Obtener un cliente por su ID desde Room
    //--------------------------------------------------------
    suspend fun obtenerClientePorId(idCliente: Int): ClientesEntity? {
        return dao.obtenerClientePorId(idCliente)
    }

    //--------------------------------------------------------
    //FUNCION PARA OBTENER EL BALANCE DEL CLIENTE
    //--------------------------------------------------------
    /*suspend fun obtenerBalancePorIdCliente(
        idCliente: Int
    ) : BalanceClienteDTO{
        return api.obtenerBalancePorIdCliente(idCliente)
    }*/

    //--------------------------------------------------------------
    // Obtiene la lista de clientes filtrada desde Room.
    //--------------------------------------------------------------
    suspend fun obtenerListaClientesLocal(filtro: String, idRuta: Int): List<ClientesEntity> {
        return dao.obtenerListaClientes(filtro, idRuta)
    }

    //--------------------------------------------------------------
    // Sincroniza la firma del pagaré con el servidor y luego actualiza localmente.
    //--------------------------------------------------------------
    suspend fun actualizarPagareFirmado(idCliente: Int): Boolean {
        return try {
            // Intentamos actualizar el servidor vía API enviando el ID en el cuerpo
            val datos = ActualizarPagareFirmadoCliente(idCliente)
            val respuesta = api.actualizarEstadoPagare(datos)
            
            if (respuesta.isSuccessful) {
                // Si el servidor aceptó, actualizamos la base de datos local (Room)
                dao.actualizarEstadoPagareFirmado(idCliente)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            // Si hay error de red, devolvemos false para que la UI informe al usuario
            Timber.e(e,"[CLIENTE_REPO] ERROR AL ACTUALIZAR EL PAGARÉ EN EL SERVIDOR")
            false
        }
    }

    //---------------------------------------------------------
    // REFACTORIZACIÓN MVVM: MÉTODOS DE PRECIO Y BONIFICACIÓN
    //---------------------------------------------------------

    // Obtiene el precio personalizado del cliente para un producto.
    suspend fun obtenerPrecioPersonalizado(idCliente: Int, idProducto: Int) = 
        dao.obtenerPrecioPersonalizadoCliente(idCliente, idProducto)

    // Obtiene la bonificación (regalía) configurada para el cliente.
    suspend fun obtenerBonificacionCliente(idCliente: Int, idProducto: Int) = 
        dao.obtenerCantidadBonificadoClientePorIdProducto(idCliente, idProducto)

}