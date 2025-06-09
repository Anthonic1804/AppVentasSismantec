package com.example.acae30.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "detalle_pedidos",
    foreignKeys = [
        ForeignKey(
            entity = PedidosEntity::class,
            parentColumns = ["Id"],
            childColumns = ["Id_pedido"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class DetallePedidoEntity(
    @PrimaryKey(autoGenerate = true)
    val Id: Int = 0,

    val Id_pedido: Int,
    val Id_producto: Int,

    val Cantidad: Double = 0.0,
    val Unidad: String? = null,
    val Idunidad: Int = 0,

    val Precio: Double = 0.0,
    val Precio_iva: Double = 0.0,
    val Total: Double = 0.0,
    val Total_iva: Double = 0.0,

    val Precio_oferta: Double,

    val Bonificado: Int = 0,
    val Descuento: Double = 0.0,
    val Precio_editado: String = "",

    val Id_talla: Int = 0,
    val Id_Inventario_Precios: Int = 0,
    val Codigo_de_barra: String = ""
)