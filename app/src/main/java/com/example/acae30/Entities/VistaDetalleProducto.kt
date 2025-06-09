package com.example.acae30.Entities

import androidx.room.DatabaseView

@DatabaseView(
    viewName = "detalle_producto",
    value = """
        SELECT 
            detalle_pedidos.id,
            detalle_pedidos.id_pedido,
            detalle_pedidos.id_producto,
            inventario.codigo,
            inventario.descripcion,
            inventario.costo,
            inventario.costo_iva,
            inventario.precio,
            inventario.precio_iva,
            inventario.precio_u,
            inventario.precio_u_iva,
            detalle_pedidos.cantidad,
            detalle_pedidos.precio AS Precio_venta_siva,
            detalle_pedidos.precio_iva AS precio_venta,
            detalle_pedidos.total,
            detalle_pedidos.total_iva,
            detalle_pedidos.unidad,
            detalle_pedidos.bonificado,
            detalle_pedidos.descuento,
            detalle_pedidos.precio_editado,
            detalle_pedidos.idunidad,
            detalle_pedidos.Codigo_de_barra
        FROM detalle_pedidos 
        INNER JOIN inventario 
        ON inventario.Id = detalle_pedidos.Id_producto
    """
)
data class VistaDetalleProducto(
    val id: Int,
    val id_pedido: Int,
    val id_producto: Int,
    val codigo: String,
    val descripcion: String,
    val costo: Double,
    val costo_iva: Double,
    val precio: Double,
    val precio_iva: Double,
    val precio_u: Double,
    val precio_u_iva: Double,
    val cantidad: Double,
    val Precio_venta_siva: Double,
    val precio_venta: Double,
    val total: Double,
    val total_iva: Double,
    val unidad: String?,
    val bonificado: Int,
    val descuento: Double,
    val precio_editado: String,
    val idunidad: Int,
    val Codigo_de_barra: String
)
