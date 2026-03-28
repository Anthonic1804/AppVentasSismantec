package com.example.acae30.controllers

import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.DAO.InventarioDao
import com.example.acae30.Entities.InventarioEntity
import com.example.acae30.Entities.InventarioLotesEntity
import com.example.acae30.Entities.InventarioPreciosEntity
import com.example.acae30.Entities.InventarioUnidadesEntity
import com.example.acae30.Funciones
import com.example.acae30.Retrofit.RetrofitCliente
import com.example.acae30.database.AppDatabase
import com.example.acae30.modelos.InventarioTiempoRealModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class InventarioTiempoRealController {

    private val funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    private lateinit var base : AppDatabase
    private lateinit var servidor : String
    private lateinit var inventarioDao : InventarioDao

    //------------------------------------------------------------------
    //Funcion para inicializar las variables principales
    //------------------------------------------------------------------

    private fun iniciarlizarVariables(context: Context){
        base = AppDatabase.getInstance(context)
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        servidor = funciones.getServidor(preferences.getString("ip", ""), preferences.getInt("puerto", 0).toString(), context)

    }

    //------------------------------------------------------------------
    //Funcion para realizar la busqueda en tiempo real por descripcion o codigo
    //------------------------------------------------------------------

    suspend fun obtenerInventarioPorDescripcion(context: Context, busqueda: String) : List<InventarioTiempoRealModel>{

        withContext(Dispatchers.Main){
            iniciarlizarVariables(context)
        }

        val listaInventario = mutableListOf<InventarioTiempoRealModel>()

        withContext(Dispatchers.IO){
            val baseUrl = servidor
            val api = RetrofitCliente.obtenerApi(baseUrl, context)

            try {
                val respuesta = api.obtenerProductoPorString(busqueda)
                listaInventario.clear()
                listaInventario.addAll(respuesta)

            }catch (e:Exception){
                println("ERROR AL OBTENER EL LISTADO DE PRODUCTOS -> " + e.message)
            }

        }

        return listaInventario
    }

    //------------------------------------------------------------------
    //Funcion para buscar el producto por id
    //------------------------------------------------------------------
    suspend fun obtenerProductoPorId(context: Context, idProducto: Int){

        withContext(Dispatchers.Main){
            iniciarlizarVariables(context)
        }

        withContext(Dispatchers.IO){

            val baseUrl = servidor
            val api = RetrofitCliente.obtenerApi(baseUrl, context)
            inventarioDao = base.inventarioDao()

            try {
                val respuesta = api.obtenerProductoPorId(idProducto)
                if(respuesta.isNotEmpty() && respuesta.last().id != 0){
                    val item = respuesta.map {
                        InventarioEntity(
                            id = it.id,
                            codigo = it.codigo ?: "",
                            codigo_de_barra = it.codigo_de_barra ?: " ",
                            tipo = it.tipo ?: "",
                            descripcion = it.descripcion ?: "",
                            unidad_medida = it.unidad_medida ?: " ",
                            fraccion = it.fraccion ?: 0f,
                            nombre_fraccion = it.nombre_fraccion ?: " ",
                            costo = it.costo ?: 0f,
                            costo_iva = it.costo_iva ?: 0f,
                            ult_costo = it.ult_costo ?: 0f,
                            ult_costo_iva = it.ult_costo_iva ?: 0f,
                            existencia = it.existencia ?: 0f,
                            existencia_u = it.existencia_u ?: 0f,
                            precio = it.precio ?: 0f,
                            precio_u = it.precio_u ?: 0f,
                            precio_u_iva = it.precio_u_iva ?: 0f,
                            precio_iva = it.precio_iva ?: 0f,
                            bonificado = it.bonificado ?: 0f,
                            lote = it.lote ?: " ",
                            fecha_vencimiento = it.fecha_vencimiento ?: " ",
                            precio2 = it.precio2 ?: 0f,
                            precio2_iva = it.precio2_iva ?: 0f,
                            precio_u2 = it.precio_u2 ?: 0f,
                            precio_u2_iva = it.precio_u2_iva ?: 0f,
                            precio_viñeta = it.precio_viñeta ?: 0f,
                            precio_viñeta_iva = it.precio_viñeta_iva ?: 0f,
                            fecha_inventario = LocalDate.now().toString(),
                            validadoHoja = 1,
                            condicion_mercado = it.condicion_mercado ?: "NORMAL",
                            id_marca = it.id_marca,
                            marca = it.marca,
                            id_sku = it.id_sku,
                            Sku = it.Sku,
                            id_rubro = it.id_rubro,
                            rubro = it.rubro,
                            id_linea = it.id_linea,
                            linea = it.linea,
                            id_sublinea = it.id_sublinea,
                            sublinea = it.sublinea,
                            id_productor = it.id_productor,
                            productor = it.productor,
                            id_proveedor = it.id_proveedor,
                            proveedor = it.proveedor,
                            metodo_gestion = it.metodo_gestion,
                            tipo_fiscal = it.tipo_fiscal
                        )
                    }

                    inventarioDao.insertarTodos(item)
                }
            }catch (e:Exception){
                println("ERROR AL INSERTAR EL PRODUCTO EN TIEMPO REAL -> " + e.message)
            }
        }

    }

    //-------------------------------------------
    //Funcion para obtener Inventario Precios por Id
    //-------------------------------------------
    suspend fun obtenerInventarioPreciosPorId(context: Context, idProducto: Int) {

        withContext(Dispatchers.Main){
            iniciarlizarVariables(context)
        }

        withContext(Dispatchers.IO){

            val baseUrl = servidor
            inventarioDao = base.inventarioDao()
            val api = RetrofitCliente.obtenerApi(baseUrl, context)

            try {

                val respuesta = api.obtenerProductoPreciosPorId(idProducto)

                if (respuesta.isNotEmpty() && respuesta.last().id != 0) {

                    val entidades = respuesta.map {
                        InventarioPreciosEntity(
                            id = it.id,
                            id_inventario = it.id_inventario ?: 0,
                            codigo_producto = it.codigo_producto ?: " ",
                            nombre = it.nombre ?: "",
                            terminos = it.terminos ?: "",
                            plazo = it.plazo ?: 0f,
                            unidad = it.unidad ?: " ",
                            cantidad = it.cantidad ?: 0f,
                            porcentaje = it.porcentaje ?: 0f,
                            precio = it.precio ?: 0f,
                            precio_iva = it.precio_iva ?: 0f,
                            id_inventario_unidad = it.id_inventario_unidad ?: 0
                        )
                    }

                    inventarioDao.insertarEscalas(entidades)

                }

            }catch (e:Exception){
                println("Error de Escalas General: ${e.message}")
            }

        }
    }

    //------------------------------------------
    //Funcion para cargar Lotes por Id
    //------------------------------------------
    suspend fun obtenerInventarioLotesPorId(context: Context, idProducto: Int){

        withContext(Dispatchers.Main){
            iniciarlizarVariables(context)
        }

        withContext(Dispatchers.IO){
            val baseUrl: String = servidor
            inventarioDao = base.inventarioDao()
            val api = RetrofitCliente.obtenerApi(baseUrl, context)

            try {

                val respuesta = api.obtenerProductoLotesPorId(idProducto)

                if(respuesta.isNotEmpty() && respuesta.last().id != 0){
                    val entidades = respuesta.map {
                        InventarioLotesEntity(
                            id = it.id,
                            idProducto = it.idProducto,
                            codigoProducto = it.codigoProducto ?: "",
                            lote = it.lote ?: "",
                            fechaVencimiento = it.fechaVencimiento ?: "",
                            unidades = it.unidades ?: 0f,
                            fracciones = it.fracciones ?: 0f
                        )
                    }

                    inventarioDao.insertarLotes(entidades)

                }
            }catch (e:Exception){
                println("ERROR AL OBTENER INVENTARIO LOTES GENERAL: ${e.message}")
            }
        }

    }

    //---------------------------------------------
    //Funcion para cargar las unidades de medida por Id
    //---------------------------------------------
    suspend fun obtenerInventarioUnidadesPorId( context: Context, idProducto: Int){

        withContext(Dispatchers.Main){
            iniciarlizarVariables(context)
        }

        withContext(Dispatchers.IO){
            val baseUrl = servidor

            inventarioDao = base.inventarioDao()

            val api = RetrofitCliente.obtenerApi(baseUrl, context)

            try {

                val respuesta = api.obtenerProductoUnidadesPorId(idProducto)

                if(respuesta.isNotEmpty() && respuesta.last().Id != 0){
                    val entidades = respuesta.map {
                        InventarioUnidadesEntity(
                            Id = it.Id,
                            Id_inventario = it.Id_inventario,
                            Nombre_unidad = it.Nombre_unidad ?: "",
                            Equivale = it.Equivale ?: 0f,
                            Unidades = it.Unidades ?: ""
                        )
                    }

                    inventarioDao.insertarUnidades(entidades)
                }

            }catch (e:Exception){
                println("ERROR GENERAL DE INVENTARIO UNIDADES -> ${e.message}")
            }

        }
    }


}