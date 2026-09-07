package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.modelos.DetallePedido
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Caso de uso para obtener el detalle de un pedido de forma reactiva (Flow).
 * Convierte automáticamente la vista de Room al modelo de dominio DetallePedido.
 */
class GetDetallePedidoFlowUseCase(private val repository: PedidosRepository) {
    operator fun invoke(idPedido: Int): Flow<List<DetallePedido>> {
        return repository.obtenerDetallePedidoFlow(idPedido).map { lista ->
            lista.map { view ->
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
        }
    }
}
