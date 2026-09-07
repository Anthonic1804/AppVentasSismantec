package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.data.repository.SettingsRepository
import com.example.acae30.domain.models.TicketData
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.Pedidos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * REFACTORIZACIÓN ARQUITECTURA LIMPIA: Caso de uso para recolectar todos los datos necesarios para un ticket.
 * Combina información del pedido, del cliente y de la configuración de la empresa.
 */
class GetTicketDataUseCase(
    private val pedidosRepository: PedidosRepository,
    private val clientesRepository: ClientesRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(idPedido: Int): TicketData? = withContext(Dispatchers.IO) {
        
        // 1. Obtener Entidad del Pedido
        val pedidoEntity = pedidosRepository.obtenerPedidoPorIdSync(idPedido) ?: return@withContext null
        
        // 2. Obtener Entidad del Cliente
        val clienteEntity = clientesRepository.obtenerClientePorId(pedidoEntity.idCliente) ?: return@withContext null
        
        // 3. Obtener Detalle del Pedido (Vista de Room)
        val detalleViews = pedidosRepository.obtenerDetallePedidoListSync(idPedido)
        
        // 4. Mapear a modelos de dominio antiguos para compatibilidad con TicketFormatter
        val pedido = Pedidos(
            Id = pedidoEntity.id,
            Id_cliente = pedidoEntity.idCliente,
            Nombre_cliente = pedidoEntity.nombreCliente,
            Total = pedidoEntity.total.toFloat(),
            Descuento = pedidoEntity.descuento.toFloat(),
            Enviado = if (pedidoEntity.enviado) 1 else 0,
            Fecha_enviado = pedidoEntity.fechaEnviado,
            Id_pedido_sistema = pedidoEntity.idPedidoSistema,
            Gps = pedidoEntity.gps,
            Cerrado = pedidoEntity.cerrado,
            Idvisita = pedidoEntity.idVisita,
            Fecha_creado = pedidoEntity.fechaCreado,
            Suma = pedidoEntity.sumas.toFloat(),
            Iva = pedidoEntity.iva.toFloat(),
            Iva_Percibido = pedidoEntity.ivaPercibido.toFloat(),
            pedido_dte = pedidoEntity.pedidoDte,
            pedido_dte_error = pedidoEntity.pedidoDteError,
            dteAmbiente = pedidoEntity.dteAmbiente,
            dteCodigoGeneracion = pedidoEntity.dteCodigoGeneracion,
            dteSelloRecibido = pedidoEntity.dteSelloRecibido,
            dteNumeroControl = pedidoEntity.dteNumeroControl,
            Tipo_documento = pedidoEntity.tipoDocumento,
            Terminos = pedidoEntity.terminos,
            Nombre_sucursal = pedidoEntity.nombreSucursal,
            Sucursal_Direccion = pedidoEntity.dteDireccion,
            IdPedidoApp = pedidoEntity.idPedidoApp
        )

        val cliente = Cliente(
            Id = clienteEntity.id,
            Codigo = clienteEntity.codigo,
            Cliente = clienteEntity.cliente,
            Dui = clienteEntity.dui,
            Nit = clienteEntity.nit,
            Nrc = clienteEntity.nrc,
            Giro = clienteEntity.giro,
            Categoria_cliente = clienteEntity.categoriaCliente,
            Terminos_cliente = clienteEntity.terminosCliente,
            Plazo_credito = clienteEntity.plazoCredito,
            Limite_credito = clienteEntity.limiteCredito?.toFloat(),
            Balance = clienteEntity.balance?.toFloat(),
            Estado_credito = clienteEntity.estadoCredito,
            Direccion = clienteEntity.direccion,
            Municipio = clienteEntity.municipio,
            Departamento = clienteEntity.departamento,
            Telefono_1 = clienteEntity.telefono1,
            Telefono_2 = clienteEntity.telefono2,
            Correo = clienteEntity.correo,
            Contacto = clienteEntity.contacto,
            Id_ruta = clienteEntity.idRuta,
            Id_vendedor = clienteEntity.idVendedor,
            Vendedor = clienteEntity.vendedor,
            Status = clienteEntity.status,
            Ultima_venta = clienteEntity.ultimaVenta,
            Aporte_mensual = clienteEntity.aporteMensual?.toFloat(),
            Firmar_pagare_app = if (clienteEntity.firmarPagareApp) 1 else 0,
            Persona_juridica = clienteEntity.personaJuridica,
            dteGiro = clienteEntity.dteGiro,
            Ruta = clienteEntity.ruta,
            DTEDireccion = clienteEntity.dteDireccion,
            DTECodDepto = clienteEntity.dteCodDepto,
            DTECodMunicipio = clienteEntity.dteCodMunicipio,
            DTECodPais = clienteEntity.dteCodPais,
            DTEPais = clienteEntity.dtePais,
            DTECorreo = clienteEntity.dteCorreo,
            DTETelefono = clienteEntity.dteTelefono,
            Latitud = clienteEntity.latitudApp,
            Longitud = clienteEntity.longitudApp,
            NombreComercial = clienteEntity.nombreComercial,
            DTECodGiro = clienteEntity.dteCodGiro,
            DTEDistrito = clienteEntity.dteDistrito,
            DTECodDistrito = clienteEntity.dteCodDistrito,
            Mayorista = clienteEntity.mayorista
        )

        val detalle = detalleViews.map { view ->
            DetallePedido(
                Id = view.Id,
                Id_pedido = view.Id_pedido,
                Id_producto = view.Id_producto,
                Codigo = view.Codigo,
                Descripcion = view.Descripcion,
                Costo = view.Costo?.toFloat(),
                Costo_iva = view.Costo_iva?.toFloat(),
                Precio = view.Precio?.toFloat(),
                Precio_iva = view.Precio_iva?.toFloat(),
                Precio_u = view.Precio_u?.toFloat(),
                Precio_u_iva = view.Precio_u_iva?.toFloat(),
                Cantidad = view.Cantidad?.toFloat(),
                Precio_venta_siva = view.Precio_venta_siva?.toFloat(),
                Precio_venta = view.Precio_venta?.toFloat(),
                Total = view.Total?.toFloat(),
                Total_iva = view.Total_iva?.toFloat(),
                Unidad = view.Unidad,
                Bonificado = view.Bonificado,
                Descuento = view.Descuento?.toFloat(),
                Precio_editado = view.Precio_editado,
                Idunidad = view.Idunidad,
                Codigo_de_barra = view.Codigo_de_barra,
                EquivaleUni = view.EquivaleUni?.toFloat() ?: 0f,
                EquivaleFra = view.EquivaleFra?.toFloat() ?: 0f,
                UniEquivale = view.UniEquivale,
                Tipo = view.Tipo ?: "",
                IdMarca = view.IdMarca,
                IdSku = view.IdSku,
                IdLinea = view.IdLinea,
                IdSubLinea = view.IdSubLinea,
                IdRubro = view.IdRubro,
                IdProductor = view.IdProductor,
                IdProveedor = view.IdProveedor,
                MetodoGestion = view.MetodoGestion ?: "",
                TipoFiscal = view.TipoFiscal ?: "",
                IdLote = view.IdLote,
                Lote = view.Lote,
                FechaVencimiento = view.FechaVencimiento,
                IdBodega = view.IdBodega,
                CodBodega = view.CodBodega,
                Bodega = view.Bodega,
                OrdenDespacho = view.OrdenDespacho ?: 0
            )
        }

        // 5. Devolver objeto consolidado
        TicketData(
            empresa = settingsRepository.getEmpresaInfo(),
            dteSettings = settingsRepository.getDteSettings(),
            decimalSettings = settingsRepository.getDecimalSettings(),
            pedido = pedido,
            cliente = cliente,
            detalle = detalle,
            esDte = pedido.Enviado == 1 && pedido.pedido_dte == 1,
            totalFacturado = (pedido.Total ?: 0f) - (pedido.Iva_Percibido ?: 0f)
        )
    }
}
