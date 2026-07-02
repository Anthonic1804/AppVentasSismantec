package com.example.acae30.data.repository

import com.example.acae30.data.local.dao.PedidosDao

class PedidosRepository(
    private val dao: PedidosDao
) {

    fun obtenerPedidosNoTransmitidos() = dao.obtenerListadoPedidosNoTransmitidos()



}