package com.example.acae30.data.repository

import com.example.acae30.data.local.dao.CuentasDao

class CuentasRepository(
    private val dao: CuentasDao
) {

    suspend fun obtenerClientesConCuentasPendientes() = dao.obtenerClientesConCuentasPendientes()

    suspend fun buscarClientesConCuentasPendientes(nombre: String) = dao.buscarClientesConCuentasPendientes(nombre)

    suspend fun contarCuentasPendientesPorCliente(idCliente: Int) = dao.contarCuentasPendientesPorCliente(idCliente)

    suspend fun obtenerCuentasTodas(idCliente: Int) = dao.obtenerCuentasTodas(idCliente)

    suspend fun obtenerCuentasVencidas(idCliente: Int) = dao.obtenerCuentasVencidas(idCliente)

    suspend fun obtenerCuentasVigentes(idCliente: Int) = dao.obtenerCuentasVigentes(idCliente)
}
