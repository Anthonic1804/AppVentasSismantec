package com.example.acae30.Utilidades

import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.example.acae30.domain.models.TicketData
import java.time.LocalDate
import java.util.Locale

/**
 * REFACTORIZACIÓN ARQUITECTURA LIMPIA: Clase de utilidad para formatear tickets de venta.
 * Mantiene el formato original (tags EscPosPrinter) pero desacoplado de la Activity.
 */
class TicketFormatter {

    fun formatTicket(printer: EscPosPrinter, data: TicketData, logo: Bitmap, charsPerLine: Int): String {
        val empresa = data.empresa
        val pedido = data.pedido
        val cliente = data.cliente
        
        val logoRedimensionado = redimensionarLogo(logo, 384)
        val direccionFormateada = dividirEnLineas(empresa.direccion, charsPerLine)
        val empresaFormateada = dividirEnLineas(empresa.nombre, charsPerLine)
        val giroFormateada = dividirEnLineas(empresa.giro, charsPerLine)
        val textoPieFormateado = dividirEnLineas("ESTE DOCUMENTO NO TIENE VALIDEZ FISCAL", charsPerLine)
        val giroCliente = dividirEnLineas(cliente.dteGiro ?: "", charsPerLine)
        val direccionCliente = dividirEnLineas(pedido.Sucursal_Direccion ?: "", charsPerLine)

        val codigoGeneracion = dividirEnLineas(pedido.dteCodigoGeneracion ?: "", charsPerLine)
        val numeroControl = dividirEnLineas(pedido.dteNumeroControl ?: "", charsPerLine)
        val selloRecepcion = dividirEnLineas(pedido.dteSelloRecibido ?: "", charsPerLine)

        val fechaP = pedido.Fecha_creado?.substring(0, 10).orEmpty()
        val documento = when(pedido.Tipo_documento) {
            "CF" -> "CREDITO FISCAL"
            "FC" -> "FACTURA"
            "RE" -> "REMISIÓN"
            else -> "RECIBO"
        }

        val qrHacienda = data.dteSettings.urlQrHacienda + "${pedido.dteAmbiente}&codGen=${pedido.dteCodigoGeneracion}&fechaEmi=$fechaP"
        val qrEmpresa = data.dteSettings.urlQrEmpresa + "${pedido.dteCodigoGeneracion}"
        val textoVerificacion = dividirEnLineas("Verificacion con ${empresa.nombre}", charsPerLine)

        val qr = if (data.dteSettings.urlQrEmpresa != "0") {
            ("[C]<qrcode size='30'>$qrHacienda</qrcode>\n" +
            "[C] Qr Hacienda \n\n" +
            "[C]<qrcode size='30'>$qrEmpresa</qrcode>\n" +
            "[C] $textoVerificacion \n")
        } else {
            "[C]<qrcode size='30'>$qrHacienda</qrcode>\n \n[C] Qr Hacienda \n"
        }

        val detalleBuilder = StringBuilder()
        data.detalle.forEach { item ->
            val descripcionPartes = if ((item.Bonificado ?: 0) > 0) {
                if (cliente.Nrc == "193-7" || cliente.Nrc == "1937") {
                    dividirDescripcion("${item.Codigo_de_barra} - ${item.Cantidad} ${item.Descripcion} - BONIFICADOS: ${item.Bonificado}")
                } else {
                    dividirDescripcion("${item.Cantidad} ${item.Descripcion} - BONIFICADOS: ${item.Bonificado}")
                }
            } else {
                if (cliente.Nrc == "193-7" || cliente.Nrc == "1937") {
                    dividirDescripcion("${item.Codigo_de_barra} - ${item.Cantidad} ${item.Descripcion}")
                } else {
                    dividirDescripcion("${item.Cantidad} ${item.Descripcion}")
                }
            }

            val totalVenta = if (documento == "CREDITO FISCAL") {
                (item.Total_iva ?: 0f).toDouble() / 1.13
            } else {
                (item.Total_iva ?: 0f).toDouble()
            }

            descripcionPartes.forEachIndexed { index, parte ->
                if (index == 0) {
                    detalleBuilder.append("[L]- $parte [R]$ ${String.format(Locale.getDefault(), "%.4f", totalVenta)}\n")
                } else {
                    detalleBuilder.append("[L]$parte\n")
                }
            }
        }

        val ticket = StringBuilder()
        if (data.esDte) {
            ticket.append("[C]<img>").append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logoRedimensionado)).append("</img>\n")
                .append("[C]$empresaFormateada\n").append("[C]$direccionFormateada\n")
                .append("[C]NIT: ${empresa.nit}\n").append("[C]NRC: ${empresa.nrc}\n").append("[C]$giroFormateada\n")
                .append("[L]------------------------------\n").append("[C]DATOS DEL CLIENTE\n")
                .append("[L]------------------------------\n").append("[L]NOMBRE:\n").append("[C]${cliente.Cliente}\n")
                .append("[L]DOCUMENTO: \n").append("[C]${cliente.Nit} / ${cliente.Dui} \n")
                .append("[L]N.R.C: ${cliente.Nrc} \n").append("[L]ACTIVIDAD ECONOMICA: \n").append("[C]$giroCliente \n")
                .append("[L]NOMBRE SUCURSAL: \n").append("[C]${pedido.Nombre_sucursal}\n")
                .append("[L]DIRECCION: \n").append("[C]$direccionCliente\n")
                .append("[L]------------------------------\n").append("[C]DOCUMENTO TRIBUTARIO ELECTRONICO\n")
                .append("[L]--------------------------------\n").append("[L]TIPO DOCUMENTO:\n").append("[C]$documento \n")
                .append("[L]FECHA DE EMISIÓN\n").append("[C]${pedido.Fecha_creado} \n")
                .append("[L]CODIGO DE GENERACION \n").append("[C]$codigoGeneracion \n")
                .append("[L]NUMERO DE CONTROL \n").append("[C]$numeroControl \n")
                .append("[L]SELLO DE RECEPCION\n").append("[C]$selloRecepcion \n")
                .append("[C]TERMINOS: ${pedido.Terminos}\n")
                .append("[L]--------------------------------\n").append(qr).append("[L]--------------------------------\n")
                .append("[C]DETALLE DEL DOCUMENTO\n").append("[L]--------------------------------\n")
                .append(detalleBuilder.toString()).append("[L]--------------------------------\n")
                .append("[L]SUB-TOTAL: [R] $ ${String.format(Locale.getDefault(), "%.2f", pedido.Suma)}\n")
                .append("[L]IVA: [R] $ ${String.format(Locale.getDefault(), "%.2f", pedido.Iva)}\n")
                .append("[L]IVA RET: [R] $ ${String.format(Locale.getDefault(), "%.2f", pedido.Iva_Percibido)}\n")
                .append("[L]TOTAL: [R] $ ${String.format(Locale.getDefault(), "%.2f", data.totalFacturado)}\n")
                .append("[L]VENDIDO POR: ${empresa.vendedor}\n").append("[L]FECHA: ${LocalDate.now()}\n")
                .append("[C]¡GRACIAS POR SU COMPRA! \n").append("[C]<b>$textoPieFormateado</b>\n")
                .append(" \n")
        } else {
            ticket.append("[C]<img>").append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logoRedimensionado)).append("</img>\n")
                .append("[C]$empresaFormateada\n").append("[C]$direccionFormateada\n")
                .append("[C]NIT: ${empresa.nit}\n").append("[C]NRC: ${empresa.nrc}\n").append("[C]$giroFormateada\n")
                .append("[L]--------------------------------\n").append("[C]DATOS DEL CLIENTE\n")
                .append("[L]--------------------------------\n").append("[L]NOMBRE:\n").append("[C]${cliente.Cliente}\n")
                .append("[L]DOCUMENTO: \n").append("[C]${cliente.Nit} / ${cliente.Dui} \n")
                .append("[L]N.R.C: ${cliente.Nrc} \n").append("[L]ACTIVIDAD ECONOMICA: \n").append("[C]$giroCliente \n")
                .append("[L]NOMBRE SUCURSAL: \n").append("[C]${pedido.Nombre_sucursal}\n")
                .append("[L]DIRECCION: \n").append("[C]$direccionCliente\n")
                .append("[L]TIPO DOCUMENTO:\n").append("[C]$documento \n")
                .append("[L]--------------------------------\n").append("[C]DETALLE DEL DOCUMENTO\n")
                .append("[L]--------------------------------\n").append(detalleBuilder.toString())
                .append("[L]--------------------------------\n")
                .append("[L]SUB-TOTAL: [R] $ ${String.format(Locale.getDefault(), "%.2f", pedido.Suma ?: 0f)}\n")
                .append("[L]IVA: [R] $ ${String.format(Locale.getDefault(), "%.2f", pedido.Iva ?: 0f)}\n")
                .append("[L]IVA RET: [R] $ ${String.format(Locale.getDefault(), "%.2f", pedido.Iva_Percibido ?: 0f)}\n")
                .append("[L]TOTAL: [R] $ ${String.format(Locale.getDefault(), "%.2f", data.totalFacturado)}\n")
                .append("[L]VENDIDO POR: ${empresa.vendedor}\n").append("[L]FECHA: ${LocalDate.now()}\n")
                .append("[C]¡GRACIAS POR SU COMPRA! \n").append("[C]<b>$textoPieFormateado</b>\n")
                .append(" \n")
        }

        return normalizarTexto(ticket.toString())
    }

    private fun redimensionarLogo(bitmap: Bitmap, anchoMaximo: Int): Bitmap {
        val proporcion = anchoMaximo.toFloat() / bitmap.width
        val altoNuevo = (bitmap.height * proporcion).toInt()
        return bitmap.scale(anchoMaximo, altoNuevo)
    }

    private fun dividirEnLineas(texto: String, maxCaracteres: Int): String {
        if (texto.isEmpty()) return ""
        return texto.chunked(maxCaracteres).joinToString("\n[C]")
    }

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

    private fun normalizarTexto(texto: String): String {
        val original = "ÁÀÂÄáàâäÉÈÊËéèêëÍÌÎÏíìîïÓÒÔÖóòôöÚÙÛÜúùûüÑñÇç"
        val reemplazo = "AAAAaaaaEEEEeeeeIIIIiiiiOOOOooooUUUUuuuuNnCc"
        var resultado = texto
        for (i in original.indices) {
            resultado = resultado.replace(original[i], reemplazo[i])
        }
        return resultado.replace(Regex("[^\\x00-\\x7F]"), "")
    }
}
