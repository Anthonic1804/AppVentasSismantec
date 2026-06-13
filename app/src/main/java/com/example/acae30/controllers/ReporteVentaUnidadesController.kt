package com.example.acae30.controllers

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.print.PrintManager
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.graphics.scale
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.usb.UsbConnection
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.remote.api.reportes.ReportesApi
import com.example.acae30.modelos.reporteUnidadesVendidas.UnidadesVendidasPorProducto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import kotlin.text.StringBuilder

class ReporteVentaUnidadesController {
    private val funciones = Funciones()
    private lateinit var preferencias: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private lateinit var base : AppDatabase
    private lateinit var servidor : String
    private var vendedor = ""
    private var numeroCaja = 0

    //------------------------------------------------------------------
    //Funcion para inicializar las variables principales
    //------------------------------------------------------------------

    private fun iniciarlizarVariables(context: Context){
        base = AppDatabase.getInstance(context)
        preferencias = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)
        servidor = funciones.getServidor(preferencias.getString("ip", ""), preferencias.getInt("puerto", 0).toString(), context)
    }

    //------------------------------------------------------------------
    //Funcion para realizar la busqueda de las ventas por producto diarias
    //------------------------------------------------------------------
    suspend fun obtenerUnidadesVendidasPorProducto(context: Context, numeroCaja: Int, fecha: LocalDate) : List<UnidadesVendidasPorProducto> {

        withContext(Dispatchers.Main){
            iniciarlizarVariables(context)
        }

        val lista = mutableListOf<UnidadesVendidasPorProducto>()

        withContext(Dispatchers.IO){
            val baseUrl = servidor
            val api = RetrofitCliente.obtenerApi<ReportesApi>(baseUrl, context)

            try {
                val respuesta = api.obtenerUnidadesVendidasPorProducto(numeroCaja, fecha)
                lista.clear()
                lista.addAll(respuesta)
            }catch (e: Exception){
                println("ERROR AL OBTENER EL LISTADO DE VENTAS DETALLE POR PRODUCTO")
            }

        }

        return lista

    }

    //------------------------------------------------------------------
    //Funcion para Imprimir el Ticket del reporte de ventas de unidades
    //------------------------------------------------------------------
    fun imprimirTicket(connection: Any, context: Context, lista: List<UnidadesVendidasPorProducto>, fechaReporte: String) {

        vendedor = preferencias.getString("Vendedor", "").toString()
        val empresa = preferencias.getString("empresa", "").orEmpty()
        val direccion = preferencias.getString("direccion", "").orEmpty()
        val nrc = preferencias.getString("nrc", "").orEmpty()
        val nit = preferencias.getString("nit", "").orEmpty()
        val giro = preferencias.getString("giro", "").orEmpty()
        val tituloReporte = "REPORTE DE UNIDADES VENDIDAS POR PRODUCTO"
        numeroCaja = preferencias.getInt("numeroCaja", 0)

        //val infoCliente = clientesController.obtenerInformacionCliente(this@Detallepedido, idcliente)

        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        val printer = when(connection) {
            is UsbDevice -> EscPosPrinter(UsbConnection(usbManager, connection), 160, 48f, 32)
            is BluetoothConnection -> EscPosPrinter(connection, 160, 48f, 32)
            else -> null
        } ?: return

        // ===============================
        // Preparar logo y texto
        // ===============================
        val prefs = context.getSharedPreferences("MisImagenes", MODE_PRIVATE)
        val filePath = prefs.getString("imagenFile", null)

        // Variable para el logo final
        val logoOriginal: Bitmap = if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.nologo)
            }
        } else {
            BitmapFactory.decodeResource(context.resources, R.drawable.nologo)
        }

        // Redimensionar
        val logoRedimensionado = redimensionarLogo(logoOriginal, 384)

        val direccionFormateada = dividirEnLineas(direccion, 32)
        val empresaFormateada = dividirEnLineas(empresa, 32)
        val giroFormateada = dividirEnLineas(giro, 32)
        val tituloFormateado = dividirEnLineas(tituloReporte, 32)

        val detalleBuilder = StringBuilder()

        // ===============================
        // Concatenando a la Descripcion, la Cantidad
        // ===============================
        lista.forEach { item ->
            val descripcionPartes = dividirDescripcion(
                item.descripcion
            )

            //Funcion para cortar la descripcion en varias lineas
            descripcionPartes.forEachIndexed { index, parte ->
                if (index == 0) {
                    detalleBuilder.append("[L]- $parte [R]${String.format("%.0f", item.cantidad)} UNI\n")
                } else {
                    detalleBuilder.append("[L]$parte\n")
                }
            }

        }

        val fecha = funciones.getFechaHoraProceso()

        val ticket = StringBuilder()
            .append("[C]<img>")
            .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logoRedimensionado))
            .append("</img>\n")
            .append("[C]$empresaFormateada\n")
            .append("[C]$direccionFormateada\n")
            .append("[C]NIT: $nit\n")
            .append("[C]NRC: $nrc\n")
            .append("[C]$giroFormateada\n")
            .append("[L]--------------------------------\n")
            .append("[C]$tituloFormateado\n")
            .append("[C]FECHA: $fechaReporte\n")
            .append("[C]NUM. CAJA: $numeroCaja\n\n")
            .append("[L]   DESCRIPCION [R]CANTIDAD\n")
            .append("[L]--------------------------------\n")
            .append(detalleBuilder.toString())
            .append("[L]--------------------------------\n")
            .append("[C]VENDEDOR:\n")
            .append("[C]${vendedor.trim()}\n")
            .append("[C]FECHA IMPRESIÓN: \n")
            .append("[C]$fecha\n")
            .append(" \n\n\n")

        val textoImprmir = normalizarTexto(ticket.toString())
        printer.printFormattedText(textoImprmir)
    }

    //------------------------------------------------------------------
    //Funcion para Redimencionar el Logo
    //------------------------------------------------------------------
    private fun redimensionarLogo(bitmap: Bitmap, anchoMaximo: Int) : Bitmap {
        val proporcion = anchoMaximo.toFloat() / bitmap.width
        val altoNuevo = (bitmap.height * proporcion).toInt()

        return bitmap.scale(anchoMaximo, altoNuevo)
    }

    //------------------------------------------------------------------
    //Funcion para dividir en lineas
    //------------------------------------------------------------------
    private fun dividirEnLineas(texto: String, maxCaracteres: Int): String {
        return texto.chunked(maxCaracteres).joinToString("\n[C]")
    }

    //------------------------------------------------------------------
    //Funcion para dividir en lineas la descripcion
    //------------------------------------------------------------------
    private fun dividirDescripcion(texto: String, maxLength: Int = 16): List<String> {
        val lineas = mutableListOf<String>()
        var inicio = 0
        while (inicio < texto.length) {
            val fin = (inicio + maxLength).coerceAtMost(texto.length)
            lineas.add(texto.substring(inicio, fin))
            inicio += maxLength
        }
        return lineas
    }

    //------------------------------------------------------------------
    //Funcion para Imprimir el Ticket del reporte de ventas de unidades Integrado
    //------------------------------------------------------------------
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun imprimirReciboIntegrado(context: Context, lista: List<UnidadesVendidasPorProducto>, fechaReporte: String){
        val empresa = preferencias.getString("empresa", "").orEmpty()
        val direccion = preferencias.getString("direccion", "").orEmpty()
        val nrc = preferencias.getString("nrc", "").orEmpty()
        val nit = preferencias.getString("nit", "").orEmpty()
        val giro = preferencias.getString("giro", "").orEmpty()
        val tituloReporte = "REPORTE DE UNIDADES VENDIDAS POR PRODUCTO"
        numeroCaja = preferencias.getInt("numeroCaja", 0)


        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val impresorIntegrado = preferencias.getString("impresorIntegrado", "sinNombre")

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device : BluetoothDevice? = bluetoothAdapter.bondedDevices.firstOrNull {
            it.name.contains(impresorIntegrado.toString())
        }

        if(device != null){
            val connection = BluetoothConnection(device)

            connection.connect()

            val printer = EscPosPrinter(connection, 160, 48f, 28)


            // ===============================
            // Preparar logo y texto
            // ===============================
            val prefs = context.getSharedPreferences("MisImagenes", MODE_PRIVATE)
            val filePath = prefs.getString("imagenFile", null)

            // Variable para el logo final
            val logoOriginal: Bitmap = if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    BitmapFactory.decodeResource(context.resources, R.drawable.nologo)
                }
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.nologo)
            }

            // Redimensionar
            val logoRedimensionado = redimensionarLogo(logoOriginal, 384)

            val direccionFormateada = dividirEnLineas(direccion, 32)
            val empresaFormateada = dividirEnLineas(empresa, 32)
            val giroFormateada = dividirEnLineas(giro, 32)
            val tituloFormateado = dividirEnLineas(tituloReporte, 32)

            val detalleBuilder = StringBuilder()

            // ===============================
            // Concatenando a la Descripcion, la Cantidad
            // ===============================
            lista.forEach { item ->
                val descripcionPartes = dividirDescripcion(
                    item.descripcion
                )

                //Funcion para cortar la descripcion en varias lineas
                descripcionPartes.forEachIndexed { index, parte ->
                    if (index == 0) {
                        detalleBuilder.append("[L]- $parte [R]${String.format("%.0f", item.cantidad)} UNI\n")
                    } else {
                        detalleBuilder.append("[L]$parte\n")
                    }
                }

            }

            val fecha = funciones.getFechaHoraProceso()

            val ticket = StringBuilder()
                .append("[C]<img>")
                .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logoRedimensionado))
                .append("</img>\n")
                .append("[C]$empresaFormateada\n")
                .append("[C]$direccionFormateada\n")
                .append("[C]NIT: $nit\n")
                .append("[C]NRC: $nrc\n")
                .append("[C]$giroFormateada\n")
                .append("[L]--------------------------------\n")
                .append("[C]$tituloFormateado\n")
                .append("[C]FECHA: $fechaReporte\n")
                .append("[C]NUM. CAJA: $numeroCaja\n\n")
                .append("[L]   DESCRIPCION [R]CANTIDAD\n")
                .append("[L]--------------------------------\n")
                .append(detalleBuilder.toString())
                .append("[L]--------------------------------\n")
                .append("[C]VENDEDOR:\n")
                .append("[C]${vendedor.trim()}\n")
                .append("[C]FECHA IMPRESIÓN: \n")
                .append("[C]$fecha\n")
                .append(" \n\n\n")

            val textoImprmir = normalizarTexto(ticket.toString())
            printer.printFormattedText(textoImprmir)

        }else{
            Toast.makeText(context, "NO ENCONTRADO", Toast.LENGTH_SHORT)
                .show()
        }

    }

    //------------------------------------------------------------------
    //Funcion para Normalizar Texo, eliminar tildes, caracteres especiales, etc.
    //------------------------------------------------------------------
    private fun normalizarTexto(texto: String): String {
        val original = "ÁÀÂÄáàâäÉÈÊËéèêëÍÌÎÏíìîïÓÒÔÖóòôöÚÙÛÜúùûüÑñÇç"
        val reemplazo = "AAAAaaaaEEEEeeeeIIIIiiiiOOOOooooUUUUuuuuNnCc"

        var resultado = texto
        for (i in original.indices) {
            resultado = resultado.replace(original[i], reemplazo[i])
        }

        // Elimina caracteres no ASCII
        resultado = resultado.replace(Regex("[^\\x00-\\x7F]"), "")
        return resultado
    }
}