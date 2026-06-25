package com.example.acae30.data.repository

import com.example.acae30.data.local.dao.ClientesDao
import com.example.acae30.data.remote.api.clientes.ClientesApi
import com.example.acae30.data.remote.dto.BalanceClienteDTO

class ClientesRepository(
    private val dao: ClientesDao,
    private val api: ClientesApi
) {

    //--------------------------------------------------------
    //FUNCION PARA OBTENER EL BALANCE DEL CLIENTE
    //--------------------------------------------------------
    /*suspend fun obtenerBalancePorIdCliente(
        idCliente: Int
    ) : BalanceClienteDTO{
        return api.obtenerBalancePorIdCliente(idCliente)
    }*/

}