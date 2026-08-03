package com.example.acae30.Utilidades

import android.os.Environment
import com.example.acae30.data.local.entity.ReporteTempEntity
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
class PdfReportManager {

    fun generarPdfReporteDiario(
        vendedor: String,
        fecha: String,
        fechaDoc: String,
        datos: List<ReporteTempEntity>
    ): File? {
        return try {
            val tituloText = "DETALLE DE PEDIDOS ENVIADOS"
            val carpeta = "/reportespdf"
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta

            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val archivo = File(dir, "${vendedor}_$fechaDoc.pdf")
            val fos = FileOutputStream(archivo)

            val documento = Document(PageSize.LETTER, 2.5f, 2.5f, 3.5f, 3.5f)
            PdfWriter.getInstance(documento, fos)

            documento.open()

            // ESPACIOS
            documento.add(Paragraph("\n\n\n"))

            // TÍTULO
            val titulo = Paragraph(
                "$tituloText\n\n",
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            )
            titulo.alignment = Element.ALIGN_CENTER
            documento.add(titulo)

            // DATOS DEL VENDEDOR
            val tablaVendedor = PdfPTable(1).apply {
                widthPercentage = 80f
            }
            val cellVendedor = PdfPCell(
                Paragraph(
                    "VENDEDOR: $vendedor\nFECHA: $fecha\n\n\n",
                    FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
                )
            ).apply {
                horizontalAlignment = Element.ALIGN_LEFT
                border = 0
            }
            tablaVendedor.addCell(cellVendedor)
            documento.add(tablaVendedor)

            // TABLA DE PEDIDOS
            val tablaPedido = PdfPTable(3).apply {
                widthPercentage = 80f
            }

            // ENCABEZADOS DE TABLA
            val fontBold = FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
            tablaPedido.addCell(crearCeldaEncabezado("CLIENTE", fontBold))
            tablaPedido.addCell(crearCeldaEncabezado("SUCURSAL", fontBold))
            tablaPedido.addCell(crearCeldaEncabezado("TOTAL", fontBold))

            // CONTENIDO
            val fontNormal = FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
            var totalGeneral = 0.0

            datos.forEach { item ->
                tablaPedido.addCell(crearCeldaContenido(item.cliente, fontNormal, Element.ALIGN_CENTER))
                tablaPedido.addCell(crearCeldaContenido(item.sucursal, fontNormal, Element.ALIGN_CENTER))
                tablaPedido.addCell(crearCeldaContenido("$ ${String.format("%.2f", item.total)}", fontNormal, Element.ALIGN_RIGHT))
                totalGeneral += item.total
            }

            // TOTAL GENERAL
            val fontTotal = FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            tablaPedido.addCell(PdfPCell(Paragraph("")).apply { border = 0 })
            tablaPedido.addCell(crearCeldaContenido("TOTAL", fontTotal, Element.ALIGN_RIGHT))
            tablaPedido.addCell(crearCeldaContenido("$ ${String.format("%.2f", totalGeneral)}", fontTotal, Element.ALIGN_RIGHT))

            documento.add(tablaPedido)
            documento.close()
            
            archivo
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun crearCeldaEncabezado(texto: String, font: Font): PdfPCell {
        return PdfPCell(Paragraph(texto, font)).apply {
            horizontalAlignment = Element.ALIGN_CENTER
        }
    }

    private fun crearCeldaContenido(texto: String, font: Font, alineacion: Int): PdfPCell {
        return PdfPCell(Paragraph(texto, font)).apply {
            horizontalAlignment = alineacion
        }
    }
}
