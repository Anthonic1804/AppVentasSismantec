package com.example.acae30.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.acae30.data.local.entity.PedidosEntity

@Entity(tableName = "detalle_pedidos",
    foreignKeys = [
        ForeignKey(
            entity = PedidosEntity::class,
            parentColumns = ["Id"],
            childColumns = ["Id_pedido"]
        )
    ],
    indices = [Index("Id_pedido")]
)
data class PedidoDetalleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    val id: Int,

    @ColumnInfo(name = "Id_pedido", defaultValue = "0")
    val idPedido: Int,

    @ColumnInfo(name = "Id_producto", defaultValue = "0")
    val idProducto: Int,

    @ColumnInfo(name = "Descripcion", defaultValue = "")
    val descripcion: String,

    @ColumnInfo(name = "Cantidad", defaultValue = "0")
    val cantidad: Double,

    @ColumnInfo(name = "Unidad")
    val unidad: String? = null,

    @ColumnInfo(name = "Idunidad", defaultValue = "0")
    val idUnidad: Int,

    @ColumnInfo(name = "Precio", defaultValue = "0.0")
    val precio: Double,

    @ColumnInfo(name = "Precio_iva", defaultValue = "0.0")
    val precioIva: Double,

    @ColumnInfo(name = "Total", defaultValue = "0.0")
    val total: Double,

    @ColumnInfo(name = "Total_iva", defaultValue = "0.0")
    val totalIva: Double,

    @ColumnInfo(name = "Precio_oferta", defaultValue = "0.0")
    val precioOferta: Double,

    @ColumnInfo(name = "Bonificado", defaultValue = "0")
    val bonificado: Int,

    @ColumnInfo(name = "Descuento", defaultValue = "0.0")
    val descuento: Double,

    @ColumnInfo(name = "Precio_editado", defaultValue = "")
    val precioEditado: String,

    @ColumnInfo(name = "Id_talla")
    val idTalla: Int? = null,

    @ColumnInfo(name = "Id_Inventario_Precios", defaultValue = "0")
    val idInventarioPrecios: Int,

    @ColumnInfo(name = "Codigo_de_barra", defaultValue = "")
    val codigoBarra: String,

    @ColumnInfo(name = "EquivaleUni", defaultValue = "0.0")
    val equivaleUni: Double,

    @ColumnInfo(name = "EquivaleFra", defaultValue = "0.0")
    val equivaleFra: Double,

    @ColumnInfo(name = "UniEquivale")
    val uniEquivale: String? = null,

    @ColumnInfo(name = "Comentario", defaultValue = "0")
    val comentario: Int,

    @ColumnInfo(name = "Tipo", defaultValue = "")
    val tipo: String,

    @ColumnInfo(name = "IdMarca")
    val idMarca: Int? = null,

    @ColumnInfo(name = "IdSku")
    val idSku: Int? = null,

    @ColumnInfo(name = "IdLinea")
    val idLinea: Int? = null,

    @ColumnInfo(name = "IdSubLinea")
    val idSubLinea: Int? = null,

    @ColumnInfo(name = "IdRubro")
    val idRubro: Int? = null,

    @ColumnInfo(name = "IdProductor")
    val idProductor: Int? = null,

    @ColumnInfo(name = "IdProveedor")
    val idProveedor: Int? = null,

    @ColumnInfo(name = "IdRuta")
    val idRuta: Int? = null,

    @ColumnInfo(name = "IdVendedor")
    val idVendedor: Int? = null,

    @ColumnInfo(name = "Metodo_gestion", defaultValue = "NINGUNO")
    val metodoGestion: String,

    @ColumnInfo(name = "Tipo_fiscal", defaultValue = "")
    val tipoFiscal: String,

    @ColumnInfo(name = "Departamento")
    val departamento: String? = null,

    @ColumnInfo(name = "IdLote")
    val idLote: Int? = null,

    @ColumnInfo(name = "Lote")
    val lote: String? = null,

    @ColumnInfo(name = "FechaVencimiento")
    val fechaVencimiento: String? = null,

    @ColumnInfo(name = "IdBodega")
    val idBodega: Int? = null,

    @ColumnInfo(name = "CodBodega")
    val codBodega: String? = null,

    @ColumnInfo(name = "Bodega")
    val bodega: String? = null,

    @ColumnInfo(name = "Orden_despacho", defaultValue = "0")
    val ordenDespacho: Int,


)