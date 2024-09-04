package com.example.acae30.controllers

import android.content.Context
import com.example.acae30.Funciones

class HojaCargaController {

    private var funciones = Funciones()

    //BUSCAR ID DE LA RECARGA EN LA TBL RECARGAS
    fun obtenerRecargasRealizadas(context: Context, id: Int) : Int{
        val db = funciones.getDataBase(context).readableDatabase
        var respuesta = 0
        try {
            val cursor = db.rawQuery("SELECT * FROM hoja_detalle_recargas WHERE id=$id AND recargado=1", null)
            if(cursor.count > 0){
                respuesta = 1
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR: NO SE LOGRO REALIZAR LA CONSULTA " + e.message)
        }finally {
            db.close()
        }

        return respuesta
    }

    //INSERTAR RECARGA EN LA TBL RECARGAS DETALLE
    fun insertarRecargaProducto(context: Context, id: Int, id_hoja: Int, id_producto: Int, codigo: String, cantidad: Float){
        val db = funciones.getDataBase(context).writableDatabase
        try {
            db.execSQL("INSERT INTO hoja_detalle_recargas(id, id_hoja, id_producto, codigo_producto, cantidad) VALUES(" +
                    "$id, $id_hoja, $id_producto, '$codigo', $cantidad)")
        }catch (e:Exception){
            println("ERROR: NO SE LOGRO REALIZAR LA INSERCION " + e.message)
        }finally {
            db.close()

        }
    }


}