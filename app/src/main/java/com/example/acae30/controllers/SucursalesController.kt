package com.example.acae30.controllers

import android.content.Context
import com.example.acae30.Funciones
import com.example.acae30.modelos.InformacionSucursal
import com.example.acae30.modelos.InventarioPrecios
import com.example.acae30.modelos.SucursalesModel

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

    //FUNCION PARA OBTENER LA INFORMACION DE LA SUCURSAL DEL CLIENTE
    fun obtenerInformacionSucursal(context: Context, sucursal: String, idCliente: Int) : InformacionSucursal?{
        val db = funciones.getDataBase(context).readableDatabase
        var datosSucursal : InformacionSucursal? = null

        try {
            val cursor = db.rawQuery("SELECT Id, id_cliente, codigo_sucursal, nombre_sucursal, direccion_sucursal, " +
                    "municipio_sucursal, depto_sucursal, telefono_1, correo_sucursal, " +
                    "Id_ruta, Ruta, DTECodDepto, DTECodMunicipio, DTECodPais, DTEPais  FROM cliente_sucursal " +
                    "WHERE nombre_sucursal='$sucursal' AND id_cliente=$idCliente", null)

            if(cursor.count > 0){
                cursor.moveToFirst()
                datosSucursal = InformacionSucursal(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getInt(9),
                    cursor.getString(10),
                    cursor.getString(11),
                    cursor.getString(12),
                    cursor.getString(13),
                    cursor.getString(14)
                )
            }else{
                println("NO SE ENCONTRARON DATOS DE LA SUCURSAL")
            }
            cursor.close()
        }catch (e:Exception){
            throw Exception("ERROR AL OBTENER LA INFORMACION DE LAS SUCURSALES -> " + e.message)
        }finally {
            db.close()
        }
        return datosSucursal
    }

    //FUNCION PARA OBTENER LAS SUCURSALES POR CLIENTE PARA LISTADO
    fun obtenerInfoSucursalesPorCliente(context: Context, idCliente:Int) : ArrayList<SucursalesModel>{
        val db = funciones.getDataBase(context).readableDatabase
        val listaSucursales = ArrayList<SucursalesModel>()
        try {
            val dataSucursal = db.rawQuery("SELECT codigo_sucursal, nombre_sucursal, depto_sucursal, municipio_sucursal, " +
                    "direccion_sucursal, telefono_1, Ruta, DTECorreo FROM cliente_sucursal WHERE id_cliente='$idCliente' LIMIT 30", null)
            if(dataSucursal.count > 0){
                dataSucursal.moveToFirst()
                do{
                    val escalas = SucursalesModel(
                        dataSucursal.getString(0),
                        dataSucursal.getString(1),
                        dataSucursal.getString(2),
                        dataSucursal.getString(3),
                        dataSucursal.getString(4),
                        dataSucursal.getString(5),
                        dataSucursal.getString(6),
                        dataSucursal.getString(7),
                    )
                    listaSucursales.add(escalas)

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