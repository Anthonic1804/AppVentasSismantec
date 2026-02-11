package com.example.acae30.controllers

import android.content.Context
import com.example.acae30.Funciones
import com.example.acae30.modelos.ReporteLiquidacion.VentaContado

class ReporteLiquidacionController {

    private var funciones = Funciones()

    //FUNCION PARA OBTENER EL TOTAL POR TIPO DE PAGO EN ABONOS
    fun obtenerCobros(context: Context) : ArrayList<VentaContado>{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val fecha = funciones.obtenerFecha()
        val ventasLista = ArrayList<VentaContado>()

        try {
            val consulta = "SELECT tipoPago, SUM(abono) AS 'TOTAL' FROM abonos " +
                    "WHERE fecha = '$fecha' AND borradoLogico = 0 GROUP BY tipoPago"

            val cursor = bd.query(consulta)

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
        }

        return ventasLista
    }

    //FUNCION PARA OBTENER EL TOTAL DE VENTA AL CONTADO POR FOMRA DE PAGO
    fun obtenerVentaContado(context: Context) : ArrayList<VentaContado>{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val fecha = funciones.obtenerFecha()
        val ventasLista = ArrayList<VentaContado>()

        try {
            val consulta = "SELECT formaPago, SUM(Total) AS 'TOTAL' FROM pedidos " +
                    "WHERE fecha = '${fecha.toString()}' AND pedido_dte_error != 2 AND Terminos='Contado' " +
                    "GROUP BY formaPago"

            val cursor = bd.query(consulta)

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
        }

        return ventasLista
    }

    //FUNCION PARA OBTENER EL TOTAL DE VENTA AL CONTADO
    fun obtenerTotalVentaContado(context: Context) : Float{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val fecha = funciones.obtenerFecha()
        var total = 0f
        try {
            val consulta = "SELECT SUM(Total) FROM pedidos " +
                    "WHERE fecha = '${fecha.toString()}' AND pedido_dte_error != 2 AND Terminos='Contado' "
            val cursor = bd.query(consulta)

            if(cursor.count > 0){
                cursor.moveToFirst()
                total = cursor.getFloat(0)
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER TOTAL DE VENTA AL CONTADO " + e.message)
        }
        return total
    }

    //FUNCION PARA OBTENER EL TOTAL DE VENTA AL CREDITO
    fun obtenerTotalVentaCredito(context: Context) : Float{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val fecha = funciones.obtenerFecha()
        var total = 0f
        try {
            val consulta = "SELECT SUM(Total) FROM pedidos " +
                    "WHERE fecha = '${fecha.toString()}' AND pedido_dte_error != 2 AND Terminos='Credito' "
            val cursor = bd.query(consulta)

            if(cursor.count > 0){
                cursor.moveToFirst()
                total = cursor.getFloat(0)
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER TOTAL DE VENTA AL CREDITO " + e.message)
        }
        return total
    }

    //FUNCION PARA OBTENER EL TOTAL DE ABONOS DIARIOS
    fun obtenerTotalVentaCobros(context: Context) : Float{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val fecha = funciones.obtenerFecha()
        var total = 0f
        try {
            val consulta = "SELECT SUM(abono) FROM abonos " +
                    "WHERE fecha = '$fecha' AND borradoLogico = 0"

            val cursor = bd.query(consulta)

            if(cursor.count > 0){
                cursor.moveToFirst()
                total = cursor.getFloat(0)
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR AL OBTENER TOTAL DE ABONOS DIARIOS " + e.message)
        }
        return total
    }

    //FUNCION PARA OBTENER EL TOTAL DE GASTOS DIARIOS
    fun obtenerTotalGastosDiarios(context: Context) : Float{
        val bd = funciones.obtenerInstancia(context).openHelper.readableDatabase
        val fecha = funciones.obtenerFecha()
        var total = 0f

        try{
            val consulta = "SELECT SUM(valor) AS Total FROM gastos WHERE Fecha='$fecha' AND gastoEnviado= 1"
            val cursor = bd.query(consulta)
            if(cursor.count > 0){
                cursor.moveToFirst()
                total = cursor.getFloat(0)
            }
            cursor.close()
        }catch (e:Exception){
            println("ERROR EN OBTENER EL TOTAL DE GASTOS DIARIOS " + e.message)
        }
        return total
    }

}