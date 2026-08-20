package com.example.acae30.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.acae30.data.local.entity.InventarioEntity
import com.example.acae30.data.local.entity.InventarioFTSEntity
import com.example.acae30.data.local.entity.InventarioLotesEntity
import com.example.acae30.data.local.entity.InventarioPreciosEntity
import com.example.acae30.data.local.entity.InventarioUnidadesEntity
import com.example.acae30.data.local.models.Inventario
import com.example.acae30.data.local.models.UnidadMedidaModelo

@Dao
interface InventarioDao {

    //Insertando Inventario
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarTodos(items: List<InventarioEntity>)

    //Insertando Inventario FTS
    /*@Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarInventarioFTS(items: List<InventarioFTSEntity>)*/

    //Insertando Escalas de Precios
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarEscalas(items : List<InventarioPreciosEntity>)

    //Insertando Lotes
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarLotes(items: List<InventarioLotesEntity>)

    //Insertando Unidades
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertarUnidades(items: List<InventarioUnidadesEntity>)

    //Mostrando el listado de inventario
    @Query("SELECT * FROM Inventario LIMIT 50")
    suspend fun mostrarListadoInventario(): List<InventarioEntity>

    //Busqueda en Inventario Normal
    @Query("""
        SELECT 
            * 
        FROM Inventario 
        WHERE descripcion LIKE :busqueda OR codigo LIKE :busqueda
        LIMIT 50
    """)
    suspend fun busquedaInventarioNormal(busqueda: String) : List<InventarioEntity>

    //Busqueda en Inventario FTS
    /*@Query("""
        SELECT i.*
        FROM inventario i
        INNER JOIN InventarioFTS f
            ON i.id = f.id
        WHERE InventarioFTS MATCH :busqueda
        LIMIT 50
    """)
    suspend fun busquedaInventarioFTS(busqueda: String) : List<InventarioEntity>*/

    //-------------------------------------------------------
    // Obtener unidades de Medida por idProducto  06/07/2026
    //-------------------------------------------------------
    @Query("SELECT * " +
            "FROM inventario_unidades " +
            "WHERE id_inventario = :idInventario " +
            "AND Nombre_unidad = :unidadMedida")
    suspend fun obtenerIdUnidadMedidaSeleccionada(
        idInventario: Int,
        unidadMedida: String
    ): UnidadMedidaModelo?

    //--------------------------------------------------------
    // Obtener el informacion de un producto por su id
    //--------------------------------------------------------
    @Query("""
        SELECT 
             Id, codigo, tipo, descripcion, unidad_medida, fraccion, nombre_fraccion,
             existencia, costo, costo_iva, precio_iva, precio, precio_u, precio_u_iva,
             fecha_inventario, bonificado, existencia_u, codigo_de_barra, condicion_mercado,
             id_marca, id_sku, id_rubro, id_linea, id_sublinea, id_productor,
             id_proveedor, metodo_gestion, tipo_fiscal
        FROM Inventario
        WHERE id = :idInventario
    """)
    suspend fun obtenerInformacionProductoPorId(
        idInventario: Int
    ) : Inventario?

    //-------------------------------------------------------
    // Obtener escalas de precios por producto y unidad
    //-------------------------------------------------------
    @Query("""
        SELECT * FROM inventario_precios 
        WHERE id_inventario = :idInventario 
        AND (unidad = :unidad OR unidad = '')
        ORDER BY cantidad ASC
    """)
    suspend fun obtenerEscalasPrecios(idInventario: Int, unidad: String): List<InventarioPreciosEntity>

    //-------------------------------------------------------
    // Obtener lotes por producto
    //-------------------------------------------------------
    @Query("SELECT * FROM inventario_lotes WHERE idProducto = :idProducto")
    suspend fun obtenerLotesPorProducto(idProducto: Int): List<InventarioLotesEntity>
}