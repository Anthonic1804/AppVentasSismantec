package com.example.acae30.controllers

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import com.example.acae30.Funciones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class HojaCargaController {

    private var funciones = Funciones()
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    //-----------------------------------------
    //NUEVAS FUNCIONES DE RECARGAS PARA HOJA DE CARGA
    //-----------------------------------------

    //Funcion para comparar la nueva hoja con la actual
    fun compararActualizarInventarioYHojaDeCarga(context: Context, json: JSONArray) : Int{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        preferences = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val idHojaCarga = preferences.getInt("idHojaCarga", 0)
        var registrosActualizados : Int = 0

        try{

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)

                val idProducto = dato.getInt("id")
                val cantidadNuevaProducto = funciones.validateJsonIsNullFloat(dato, "existencia")

                val consulta = "SELECT Id_hojaCarga, Id_inventario, Cantidad FROM hoja_carga_detalle WHERE Id_hojaCarga = $idHojaCarga AND Id_inventario = $idProducto"
                val cursor = bd.query(consulta)

                cursor.use {

                    if(cursor.count > 0){
                        cursor.moveToFirst()

                        //ACTUALIZANDO REGISTRO SOLO SI LA CANTIDAD ES MAYOR A LA HOJA ORIGINAL
                        val existenciaOriginal = cursor.getInt(2)
                        val diferenciaCantidad = cantidadNuevaProducto - existenciaOriginal
                        if(diferenciaCantidad > 0){

                            CoroutineScope(Dispatchers.IO).launch {
                                //ACTUALIZANDO MI INVENTARIO

                                bd.execSQL("UPDATE inventario SET existencia = (existencia + $diferenciaCantidad) WHERE id = $idProducto")

                                //ACTTUALIZANDO MI HOJA_DETALLE

                                bd.execSQL("UPDATE hoja_carga_detalle SET Cantidad = (Cantidad + $diferenciaCantidad) WHERE Id_inventario = $idProducto")
                            }

                            registrosActualizados += 1

                        }else{
                            registrosActualizados += 0
                        }

                    }else{
                        //AGREGANDO EL PRODUCTO NUEVO
                        try{
                            CoroutineScope(Dispatchers.IO).launch {
                                guardarProductoEnInventarioSQLite(context, dato)
                            }

                            registrosActualizados += 1
                        }catch (e:Exception){
                            throw Exception("ERROR AL INTENTAR ALMACENAR EL PRODUCTO EN SQLITE -> " + e.message)
                        }
                    }

                }

            }
        }catch (e:Exception){
            println("ERROR AL COMPARAR LA HOJA DE CARGA -> " + e.message)
        }

        return registrosActualizados
    }

    //ALMACENAR EN SQLITE EL PRODUCTO QUE NO EXISTIA EN LA HOJA DE CARGA INICIAL
    private fun guardarProductoEnInventarioSQLite(context: Context, dato: JSONObject){
        val bd = funciones.obtenerInstancia(context).openHelper.writableDatabase

        try {
            val data = ContentValues()
            data.put("id", dato.getInt("id"))
            data.put("codigo", funciones.validateJsonIsnullString(dato, "codigo"))
            data.put("codigo_de_barra", funciones.validateJsonIsnullString(dato, "codigo_de_barra"))
            data.put("tipo", funciones.validateJsonIsnullString(dato, "tipo"))
            data.put("descripcion", funciones.validateJsonIsnullString(dato, "descripcion"))
            data.put(
                "unidad_medida",
                funciones.validateJsonIsnullString(dato, "unidad_medida")
            )
            data.put("fraccion", funciones.validateJsonIsNullFloat(dato, "fraccion"))
            data.put(
                "nombre_fraccion", funciones.validateJsonIsnullString(
                    dato,
                    "nombre_fraccion"
                )
            )
            data.put("costo", funciones.validateJsonIsNullFloat(dato, "costo"))
            data.put("costo_iva", funciones.validateJsonIsNullFloat(dato, "costo_iva"))
            data.put("ult_costo", funciones.validateJsonIsNullFloat(dato, "ult_costo"))
            data.put("ult_costo_iva", funciones.validateJsonIsNullFloat(dato, "ult_costo_iva"))
            data.put("existencia", funciones.validateJsonIsNullFloat(dato, "existencia"))
            data.put("existencia_u", funciones.validateJsonIsNullFloat(dato, "existencia_u"))
            data.put("precio", funciones.validateJsonIsNullFloat(dato, "precio"))
            data.put("precio_u", funciones.validateJsonIsNullFloat(dato, "precio_u"))
            data.put("precio_u_iva", funciones.validateJsonIsNullFloat(dato, "precio_u_iva"))
            data.put("precio_iva", funciones.validateJsonIsNullFloat(dato, "precio_iva"))
            data.put("bonificado", funciones.validateJsonIsNullFloat(dato, "bonificado"))
            data.put("lote", funciones.validateJsonIsnullString(dato, "lote"))
            data.put("fecha_vencimiento", funciones.validateJsonDate(dato, "fecha_vencimiento"))
            data.put("precio2", funciones.validateJsonIsNullFloat(dato, "precio2"))
            data.put("precio2_iva", funciones.validateJsonIsNullFloat(dato, "precio2_iva"))
            data.put("precio_u2", funciones.validateJsonIsNullFloat(dato, "precio_u2"))
            data.put("precio_u2_iva", funciones.validateJsonIsNullFloat(dato, "precio_u2_iva"))
            data.put("precio_viñeta", funciones.validateJsonIsNullFloat(dato, "precio_viñeta"))
            data.put("precio_viñeta_iva", funciones.validateJsonIsNullFloat(dato, "precio_viñeta_iva"))
            data.put("fecha_inventario", LocalDate.now().toString())

            bd.insert("inventario", SQLiteDatabase.CONFLICT_REPLACE, data)
        }catch (e:Exception){
            println("ERROR AL INSERTAR EL PRODUCTO EN INVENTARIO -> " + e.message)
        }finally {
            //INSERTANDO EL PRODUCTO NUEVO EN LA TBL HOJA_CARGA_DETALLE
            almacenarProductoEnHojaDetalle(context, dato)
        }
    }

    //FUNCION PARA ALMACENAR EL NUEVO PRODUCTO EN HOJA_CARGA_DETALL
    private fun almacenarProductoEnHojaDetalle(context: Context, dato: JSONObject){
        val db = funciones.obtenerInstancia(context).openHelper.writableDatabase
        val idHojaCarga = preferences.getInt("idHojaCarga", 0)
        val idProducto = dato.getInt("id")
        val codigoInventario = funciones.validateJsonIsnullString(dato, "codigo")
        val cantidad = funciones.validateJsonIsNullFloat(dato, "existencia")

        try {
            db.execSQL("INSERT INTO hoja_carga_detalle(Id_hojaCarga, Id_inventario, Codigo_inventario, Cantidad) VALUES($idHojaCarga, $idProducto, $codigoInventario, $cantidad)")
        }catch (e:Exception){
            println("ERROR AL INSERTAR EL PRODUCTO EN HOJA DETALLE -> " + e.message)
        }
    }

}