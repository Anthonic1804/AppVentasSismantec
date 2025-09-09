package com.example.acae30.controllers

import android.content.Context
import android.content.SharedPreferences
import com.example.acae30.Funciones
import com.example.acae30.modelos.PrecioPersonalizado

class PreciosAutorizadosController {
    private var funciones = Funciones()


    //OBTENIEDO LOS PRECIOS AUTORIZADOS POR FECHA
    fun obtenerPrecioAutorizadoPorFecha(context: Context): ArrayList<PrecioPersonalizado>{
        val data = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val fechanow = funciones.obtenerFecha()
        val list = ArrayList<PrecioPersonalizado>()

        try {
            val consulta = "SELECT T.Id, T.cod_producto, I.Descripcion, E.nombre_empleado, T.precio_asig FROM preciosAutorizados T " +
                    "INNER JOIN inventario I ON I.Codigo = T.cod_producto " +
                    "INNER JOIN empleado E ON E.id_empleado = T.Id_vendedor " +
                    "WHERE fecha_registrado='$fechanow'"

            val cursor = data.query(consulta)
            if(cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val dataToken = PrecioPersonalizado(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getFloat(4)
                    )
                    list.add(dataToken)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        }
        return list
    }


}