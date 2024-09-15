package com.example.acae30.controllers

import android.content.Context
import com.example.acae30.Funciones

class SucursalesController {

    private var funciones = Funciones()

    //FUNCION PARA OBTENER LAS SUCURSALES POR CLIENTE.
    fun obtenerSucursalesporIdCliente(context: Context, idCliente:Int): ArrayList<String> {
        val db = funciones.getDataBase(context).readableDatabase
        val listaSucursales = ArrayList<String>()
        try {
            val dataSucursal = db.rawQuery("SELECT nombre_sucursal FROM cliente_sucursal WHERE id_cliente='$idCliente'", null)
            if(dataSucursal.count > 0){
                dataSucursal.moveToFirst()
                listaSucursales.add("-- SELECCIONES UNA SUCURSAL --")
                do{
                    listaSucursales.add(dataSucursal.getString(0))
                }while (dataSucursal.moveToNext())
            }
            dataSucursal.close()
        }catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            db!!.close()
        }
        return listaSucursales
    }


}