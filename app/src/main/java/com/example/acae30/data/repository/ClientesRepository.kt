package com.example.acae30.data.repository

import com.example.acae30.data.local.dao.ClientesDao
import com.example.acae30.data.local.entity.ClientesEntity
import com.example.acae30.data.remote.api.clientes.ClientesApi

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

}