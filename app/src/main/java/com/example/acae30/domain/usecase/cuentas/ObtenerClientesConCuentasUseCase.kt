package com.example.acae30.domain.usecase.cuentas

import com.example.acae30.data.repository.CuentasRepository
import com.example.acae30.modelos.Cliente

/**
 * REFACTORIZACIÓN MVVM: Caso de Uso para obtener el listado de clientes que poseen deudas.
 */
class ObtenerClientesConCuentasUseCase(
    private val repository: CuentasRepository
) {

    suspend fun ejecutar(filtroNombre: String = ""): List<Cliente> {
        // 1. Obtenemos las entidades desde el repositorio (Room)
        val entities = if (filtroNombre.isEmpty()) {
            repository.obtenerClientesConCuentasPendientes()
        } else {
            repository.buscarClientesConCuentasPendientes(filtroNombre)
        }

        // 2. Mapeamos de ClientesEntity a el modelo Cliente (UI) para mantener compatibilidad
        return entities.map { entity ->
            Cliente(
                Id = entity.id,
                Codigo = entity.codigo,
                Cliente = entity.cliente,
                Dui = entity.dui,
                Nit = entity.nit,
                Nrc = entity.nrc,
                Giro = entity.giro,
                Categoria_cliente = entity.categoriaCliente,
                Terminos_cliente = entity.terminosCliente,
                Plazo_credito = entity.plazoCredito ?: 0,
                Limite_credito = entity.limiteCredito?.toFloat() ?: 0f,
                Balance = entity.balance?.toFloat() ?: 0f,
                Estado_credito = entity.estadoCredito,
                Direccion = entity.direccion,
                Municipio = entity.municipio,
                Departamento = entity.departamento,
                Telefono_1 = entity.telefono1,
                Telefono_2 = entity.telefono2,
                Correo = entity.correo,
                Contacto = entity.contacto,
                Id_ruta = entity.idRuta,
                Id_vendedor = entity.idVendedor,
                Vendedor = entity.vendedor,
                Status = entity.status,
                Ultima_venta = entity.ultimaVenta,
                Aporte_mensual = entity.aporteMensual?.toFloat() ?: 0f,
                Firmar_pagare_app = if (entity.firmarPagareApp) 1 else 0,
                Persona_juridica = entity.personaJuridica,
                dteGiro = entity.dteGiro ?: "",
                Ruta = entity.ruta ?: "",
                DTEDireccion = entity.dteDireccion ?: "",
                DTECodDepto = entity.dteCodDepto ?: "",
                DTECodMunicipio = entity.dteCodMunicipio ?: "",
                DTECodPais = entity.dteCodPais ?: "",
                DTEPais = entity.dtePais ?: "",
                DTECorreo = entity.dteCorreo ?: "",
                DTETelefono = entity.dteTelefono ?: "",
                Latitud = entity.latitudApp ?: "0",
                Longitud = entity.longitudApp ?: "0",
                NombreComercial = entity.nombreComercial ?: "",
                DTECodGiro = entity.dteCodGiro ?: "",
                DTEDistrito = entity.dteDistrito ?: "",
                DTECodDistrito = entity.dteCodDistrito ?: "",
                Mayorista = entity.mayorista
            )
        }
    }
}
