package com.example.acae30.services

import android.content.Context
import android.database.sqlite.SQLiteException
import com.example.acae30.Funciones

class InventarioService {

    private val funciones = Funciones()

    //----------------------------------------------------
    //Funciones para Reintegrar el Inventario ROOM
    //----------------------------------------------------

    //Funcion para Reintegrar Unidades
    fun reintegrarUnidades(context: Context, idProducto: Int, cantidad: Float){

        val base = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            base.execSQL("UPDATE Inventario SET Existencia = (Existencia + ${cantidad}) WHERE Id=$idProducto")
        }catch (e:SQLiteException){
            println("ERROR DE SQLITE AL REINTEGRAR UNIDADES -> " + e.message)
        }catch (e:Exception){
            println("ERROR GENERAL AL REINTEGRAR UNIDADES -> " + e.message)
        }
    }

    //Funcion para Reintegrar Fracciones
    fun reintegrarFracciones(context: Context, idProducto: Int, cantidad: Float){

        val base = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {
            val consulta = "SELECT Existencia, Existencia_u, Fraccion FROM Inventario WHERE Id=$idProducto"
            val cursor = base.query(consulta)

            cursor.use {

                var existenciaActual: Float = 0f
                var existenciaUActual: Float = 0f
                var fraccionamiento: Int = 0
                var totalFraccionesActuales: Float = 0f

                var existenciaFinal: Float = 0f
                var existenciaUFinal: Float = 0f
                var totalFracionesFinal: Float = 0f


                if(cursor.count > 0){
                    cursor.moveToFirst()
                    existenciaActual = cursor.getFloat(0)
                    existenciaUActual = cursor.getFloat(1)
                    fraccionamiento = cursor.getInt(2)
                }

                //Calculado
                totalFraccionesActuales = (existenciaActual * fraccionamiento) + existenciaUActual

                totalFracionesFinal = totalFraccionesActuales + cantidad

                existenciaFinal = totalFracionesFinal / fraccionamiento // Unidades Completas
                existenciaUFinal = totalFracionesFinal % fraccionamiento // Fracciones Restantes

                base.execSQL("UPDATE Inventario SET Existencia = ${existenciaFinal.toInt()}, Existencia_u = ${existenciaUFinal.toInt()} WHERE Id = $idProducto")
            }

        }catch (e:SQLiteException){
            println("ERROR DE SQLITE AL REINTEGRAR FRACCIONES -> " + e.message)
        }catch (e:Exception){
            println("ERROR GENERAL AL REINTEGRAR FRACCIONES -> " + e.message)
        }

    }

    //Funcion para reintegrar Unidades de Medida
    fun reintegrarUnidadesMedida(context: Context, idProducto: Int, cantidad: Float, unidadMedida: String){

        val base = funciones.obtenerInstancia(context).openHelper.writableDatabase
        try {

            val consulta = "SELECT Equivale, Unidades FROM inventario_unidades WHERE id_inventario = $idProducto AND Nombre_unidad = '$unidadMedida'"
            val cursor = base.query(consulta)

            cursor.use {

                var cantidadDEscargar : Float = 0f
                if(cursor.count > 0){
                    cantidadDEscargar = cantidad * cursor.getFloat(0)

                    when(cursor.getString(1)){
                        "UNI" -> reintegrarUnidades(context, idProducto, cantidadDEscargar)
                        "FRA" -> reintegrarFracciones(context, idProducto, cantidadDEscargar)
                    }
                }
            }
        }catch (e:SQLiteException){
            println("ERROR DE SQLITE AL REINTEGRAR UNIDADES DE MEDIDA-> " + e.message)
        }catch (e:Exception){
            println("ERROR GENERAL AL REINTEGRAR UNIDADES DE MEDIDA" + e.message)
        }

    }

}