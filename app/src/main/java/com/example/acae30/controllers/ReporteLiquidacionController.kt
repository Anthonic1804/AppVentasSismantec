package com.example.acae30.controllers

import android.content.Context
import com.example.acae30.Funciones
import com.example.acae30.modelos.ReporteLiquidacion.VentaContado

class ReporteLiquidacionController {

    private var funciones = Funciones()

    //FUNCION PARA OBTENER EL TOTAL POR TIPO DE PAGO EN ABONOS
    fun obtenerCobros(context: Context) : ArrayList<VentaContado>{
        val bd = funciones.getDataBase(context).readableDatabase
        val fecha = funciones.obtenerFecha()
        val ventasLista = ArrayList<VentaContado>()

        try {
            val cursor = bd.rawQuery("SELECT tipoPago, SUM(abono) AS 'TOTAL' FROM abonos " +
                    "WHERE fecha = '$fecha' AND borradoLogico = 0 GROUP BY tipoPago", null)

            if(cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val lista = VentaContado(
                        cursor.getString(0),
                        cursor.getFloat(1)
                    )
                    ventasLista.add(lista)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER LOS TOTALES AL CONTADO")
        }finally {
            bd.close()
        }

        return ventasLista
    }

    //FUNCION PARA OBTENER EL TOTAL DE VENTA AL CONTADO POR FOMRA DE PAGO
    fun obtenerVentaContado(context: Context) : ArrayList<VentaContado>{
        val bd = funciones.getDataBase(context).readableDatabase
        val fecha = funciones.obtenerFecha()
        val ventasLista = ArrayList<VentaContado>()

        try {
            val cursor = bd.rawQuery("SELECT formaPago, SUM(Total) AS 'TOTAL' FROM pedidos " +
                    "WHERE substr(Fecha_creado,0,11) = '${fecha.toString()}' AND pedido_dte = 0 AND pedido_dte_error = 0 AND Terminos='Contado' " +
                    "GROUP BY formaPago", null)

            if(cursor.count > 0){
                cursor.moveToFirst()
                do {
                    val lista = VentaContado(
                        cursor.getString(0),
                        cursor.getFloat(1)
                    )
                    ventasLista.add(lista)
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER LOS TOTALES AL CONTADO")
        }finally {
            bd.close()
        }

        return ventasLista
    }

    //FUNCION PARA OBTENER EL TOTAL DE VENTA AL CONTADO
    fun obtenerTotalVentaContado(context: Context) : Float{
        val bd = funciones.getDataBase(context).readableDatabase
        val fecha = funciones.obtenerFecha()
        var total = 0f
        try {
            val cursor = bd.rawQuery("SELECT SUM(Total) FROM pedidos " +
                    "WHERE substr(Fecha_creado,0,11) = '${fecha.toString()}' AND pedido_dte = 0 AND pedido_dte_error = 0 AND Terminos='Contado' ", null)

            if(cursor.count > 0){
                cursor.moveToFirst()
                total = cursor.getFloat(0)
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER TOTAL DE VENTA AL CONTADO " + e.message)
        }finally {
            bd.close()
        }
        return total
    }

    //FUNCION PARA OBTENER EL TOTAL DE VENTA AL CREDITO
    fun obtenerTotalVentaCredito(context: Context) : Float{
        val bd = funciones.getDataBase(context).readableDatabase
        val fecha = funciones.obtenerFecha()
        var total = 0f
        try {
            val cursor = bd.rawQuery("SELECT SUM(Total) FROM pedidos " +
                    "WHERE substr(Fecha_creado,0,11) = '${fecha.toString()}' AND pedido_dte = 0 AND pedido_dte_error = 0 AND Terminos='Credito' ", null)

            if(cursor.count > 0){
                cursor.moveToFirst()
                total = cursor.getFloat(0)
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER TOTAL DE VENTA AL CREDITO " + e.message)
        }finally {
            bd.close()
        }
        return total
    }

    //FUNCION PARA OBTENER EL TOTAL DE ABONOS DIARIOS
    fun obtenerTotalVentaCobros(context: Context) : Float{
        val bd = funciones.getDataBase(context).readableDatabase
        val fecha = funciones.obtenerFecha()
        var total = 0f
        try {
            val cursor = bd.rawQuery("SELECT SUM(abono) FROM abonos " +
                    "WHERE fecha = '$fecha' AND borradoLogico = 0", null)

            if(cursor.count > 0){
                cursor.moveToFirst()
                total = cursor.getFloat(0)
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER TOTAL DE ABONOS DIARIOS " + e.message)
        }finally {
            bd.close()
        }
        return total
    }

    //FUNCION PARA OBTENER EL TOTAL DE GASTOS DIARIOS
    fun obtenerTotalGastosDiarios(context: Context) : Float{
        val bd = funciones.getDataBase(context).readableDatabase
        val fecha = funciones.obtenerFecha()
        var total = 0f

        try{
            val cursor = bd.rawQuery("SELECT SUM(valor) AS Total FROM gastos WHERE Fecha='$fecha' AND gastoEnviado= 1", null)
            if(cursor.count > 0){
                cursor.moveToFirst()
                total = cursor.getFloat(0)
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR EN OBTENER EL TOTAL DE GASTOS DIARIOS " + e.message)
        }finally {
            bd.close()
        }
        return total
    }

}