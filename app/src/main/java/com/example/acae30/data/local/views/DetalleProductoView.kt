package com.example.acae30.data.local.views

import androidx.room.DatabaseView

/**
 * REFACTORIZACIÓN MVVM: Vista para el detalle de productos.
 * IMPORTANTE: El nombre de los campos debe coincidir EXACTAMENTE con el casing del SQL.
 */
@DatabaseView(
    viewName = "detalle_producto",
    value = """
        SELECT
            dp.Id AS Id,
            dp.Id_pedido AS Id_pedido,
            dp.Id_producto AS Id_producto,
            inv.codigo AS Codigo,
            dp.Descripcion AS Descripcion,
            inv.costo AS Costo,
            inv.costo_iva AS Costo_iva,
            inv.precio AS Precio,
            inv.precio_iva AS Precio_iva,
            inv.precio_u AS Precio_u,
            inv.precio_u_iva AS Precio_u_iva,
            dp.Cantidad AS Cantidad,
            dp.Precio AS Precio_venta_siva,
            dp.Precio_iva AS Precio_venta,
            dp.Total AS Total,
            dp.Total_iva AS Total_iva,
            dp.unidad AS Unidad,
            dp.Bonificado AS Bonificado,
            dp.Descuento AS Descuento,
            dp.Precio_editado AS Precio_editado,
            dp.Idunidad AS Idunidad,
            dp.Codigo_de_barra AS Codigo_de_barra,
            dp.EquivaleUni AS EquivaleUni,
            dp.EquivaleFra AS EquivaleFra,
            dp.UniEquivale AS UniEquivale,
            dp.Tipo AS Tipo,
            dp.IdMarca AS IdMarca,
            dp.IdSku AS IdSku,
            dp.IdLinea AS IdLinea,
            dp.IdSubLinea AS IdSubLinea,
            dp.IdRubro AS IdRubro,
            dp.IdProductor AS IdProductor,
            dp.IdProveedor AS IdProveedor,
            dp.Metodo_gestion AS MetodoGestion,
            dp.Tipo_fiscal AS TipoFiscal,
            dp.IdLote AS IdLote,
            dp.Lote AS Lote,
            dp.FechaVencimiento AS FechaVencimiento,
            dp.IdBodega AS IdBodega,
            dp.CodBodega AS CodBodega,
            dp.Bodega AS Bodega,
            dp.Orden_despacho AS OrdenDespacho
        FROM detalle_pedidos dp
        LEFT JOIN inventario inv
            ON inv.Id = dp.Id_producto
    """
)
data class DetalleProductoView(
    val Id: Int,
    val Id_pedido: Int,
    val Id_producto: Int,
    val Codigo: String?,
    val Descripcion: String?,
    val Costo: Double?,
    val Costo_iva: Double?,
    val Precio: Double?,
    val Precio_iva: Double?,
    val Precio_u: Double?,
    val Precio_u_iva: Double?,
    val Cantidad: Double?,
    val Precio_venta_siva: Double?,
    val Precio_venta: Double?,
    val Total: Double?,
    val Total_iva: Double?,
    val Unidad: String?,
    val Bonificado: Int?,
    val Descuento: Double?,
    val Precio_editado: String?,
    val Idunidad: Int?,
    val Codigo_de_barra: String?,
    val EquivaleUni: Double?,
    val EquivaleFra: Double?,
    val UniEquivale: String?,
    val Tipo: String?,
    val IdMarca: Int?,
    val IdSku: Int?,
    val IdLinea: Int?,
    val IdSubLinea: Int?,
    val IdRubro: Int?,
    val IdProductor: Int?,
    val IdProveedor: Int?,
    val MetodoGestion: String?,
    val TipoFiscal: String?,
    val IdLote: Int?,
    val Lote: String?,
    val FechaVencimiento: String?,
    val IdBodega: Int?,
    val CodBodega: String?,
    val Bodega: String?,
    val OrdenDespacho: Int?
)
