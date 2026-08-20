package com.example.acae30.data.repository

import com.example.acae30.data.local.dao.InventarioDao

class InventarioRepository(
    private val dao: InventarioDao
) {

    //---------------------------------------------------------------------------
    //Obtiene la información detallada de un producto por su ID.
    //---------------------------------------------------------------------------
    suspend fun obtenerProductoPorId(idProducto: Int) = dao.obtenerInformacionProductoPorId(idProducto)

    //---------------------------------------------------------------------------
    //Obtiene las escalas de precios vinculadas a un producto y una unidad de medida.
    //---------------------------------------------------------------------------
    suspend fun obtenerEscalasPrecios(idProducto: Int, unidad: String) = dao.obtenerEscalasPrecios(idProducto, unidad)

    //---------------------------------------------------------------------------
    //Obtiene los lotes activos para un producto específico.
    //---------------------------------------------------------------------------
    suspend fun obtenerLotesPorProducto(idProducto: Int) = dao.obtenerLotesPorProducto(idProducto)

    //---------------------------------------------------------------------------
    //Obtiene la configuración de una unidad de medida personalizada.
    //---------------------------------------------------------------------------
    suspend fun obtenerUnidadMedida(idProducto: Int, nombreUnidad: String) = 
        dao.obtenerIdUnidadMedidaSeleccionada(idProducto, nombreUnidad)
}
