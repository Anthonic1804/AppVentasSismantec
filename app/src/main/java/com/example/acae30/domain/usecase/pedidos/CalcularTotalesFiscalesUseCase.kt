package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.modelos.DetallePedido
import timber.log.Timber

/**
 * Caso de uso que encapsula la lógica de cálculo de IVA, Exentos, No Sujetos, IVA Percibido e IVA Retenido.
 */
class CalcularTotalesFiscalesUseCase {
    
    data class ResultadoTotales(
        val sumas: Double,
        val iva: Double,
        val ventaExenta: Double,
        val ventaNoSujeta: Double,
        val ivaPerci: Double,
        val ivaReteni: Double,
        val totalFinal: Double
    )

    operator fun invoke(
        listaDetalle: List<DetallePedido>, 
        tipoDocumento: String, 
        categoriaEmpresa: String,
        categoriaCliente: String
    ): ResultadoTotales {
        if (listaDetalle.isEmpty()) {
            return ResultadoTotales(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }

        var acumSumas = 0.0
        var acumIva = 0.0
        var acumExenta = 0.0
        var acumNoSujeta = 0.0

        // Paso 1: Recorrer cada ítem del detalle y sumar según tipo fiscal
        listaDetalle.forEach { item ->
            val tipo = item.TipoFiscal.trim().uppercase()
            val totalIvaLine = item.Total_iva?.toDouble() ?: 0.0
            val totalSinIvaLine = item.Total?.toDouble() ?: 0.0

            when {
                tipo in listOf("E", "EXENTO", "EXENTA") || tipo.contains("EXENT") -> {
                    acumExenta += totalIvaLine
                }
                tipo in listOf("NS", "NO SUJETO") || tipo.contains("NO SUJETO") -> {
                    acumNoSujeta += totalIvaLine
                }
                else -> {
                    when (tipoDocumento) {
                        "CF", "RE" -> {
                            acumSumas += totalSinIvaLine
                            acumIva += (totalIvaLine - totalSinIvaLine)
                        }
                        else -> {
                            acumSumas += totalIvaLine
                        }
                    }
                }
            }
        }

        // Paso 2: Calcular IVA Percibido o IVA Retenido
        var ivaPerci = 0.0
        var ivaReteni = 0.0

        val funciones = com.example.acae30.Funciones()
        val empresaCat = funciones.normalizarTexto(categoriaEmpresa).trim().uppercase()
        val clienteCat = funciones.normalizarTexto(categoriaCliente).trim().uppercase()

        Timber.d("FISCAL_CALC: tipoDoc=$tipoDocumento, acumSumas=$acumSumas, empresaCat='$empresaCat', clienteCat='$clienteCat'")

        if (tipoDocumento == "CF" && acumSumas > 100.0) {
            val esEmpresaGran = empresaCat.contains("GRAN")
            val esClienteGran = clienteCat.contains("GRAN")
            // Respaldo robusto: si empresaCat está vacía pero el cliente es Gran Contribuyente, asumimos Pequeño Contribuyente para retención
            val esEmpresaPequeno = empresaCat.contains("PEQUE") || empresaCat.contains("PEQUEN") || (empresaCat.isEmpty() && esClienteGran)
            val esClientePequenoMediano = clienteCat.contains("PEQUE") || clienteCat.contains("PEQUEN") || clienteCat.contains("MEDIANO")

            Timber.d("FISCAL_CALC: esEmpresaGran=$esEmpresaGran, esEmpresaPequeno=$esEmpresaPequeno, esClienteGran=$esClienteGran, esClientePequenoMediano=$esClientePequenoMediano")

            if (esEmpresaGran && esClientePequenoMediano) {
                ivaPerci = acumSumas * 0.01
            } else if (esEmpresaPequeno && esClienteGran) {
                ivaReteni = acumSumas * 0.01
            }
        }

        // Paso 3: Total Final = Gravado + IVA + Exenta + No Sujeta + IVA Percibido - IVA Retenido
        val rawTotal = acumSumas + acumIva + acumExenta + acumNoSujeta + ivaPerci - ivaReteni
        val totalFinal = Math.round(rawTotal * 100.0) / 100.0

        return ResultadoTotales(
            sumas = acumSumas,
            iva = acumIva,
            ventaExenta = acumExenta,
            ventaNoSujeta = acumNoSujeta,
            ivaPerci = ivaPerci,
            ivaReteni = ivaReteni,
            totalFinal = totalFinal
        )
    }
}
