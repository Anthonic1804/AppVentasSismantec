package com.example.acae30.domain.usecase.pedidos

import com.example.acae30.modelos.DetallePedido

/**
 * Caso de uso que encapsula la lógica de cálculo de IVA, Exentos, No Sujetos e IVA Percibido.
 */
class CalcularTotalesFiscalesUseCase {
    
    /* CÓDIGO VIEJO:
    data class ResultadoTotales(
        val sumas: Double,
        val iva: Double,
        val ivaPerci: Double,
        val totalFinal: Double
    )
    */
    // CÓDIGO NUEVO: Estructura extendida con desgloses de ventaExenta y ventaNoSujeta
    data class ResultadoTotales(
        val sumas: Double,
        val iva: Double,
        val ventaExenta: Double,
        val ventaNoSujeta: Double,
        val ivaPerci: Double,
        val totalFinal: Double
    )

    /* CÓDIGO VIEJO:
    operator fun invoke(totalBase: Double, tipoDocumento: String, esGranContribuyente: Boolean): ResultadoTotales { ... }
    */
    // CÓDIGO NUEVO: Función para calcular totales recorriendo los ítems del pedido según su tipo fiscal
    operator fun invoke(
        listaDetalle: List<DetallePedido>, 
        tipoDocumento: String, 
        esGranContribuyente: Boolean
    ): ResultadoTotales {
        if (listaDetalle.isEmpty()) {
            return ResultadoTotales(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
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

            when (tipo) {
                "E", "EXENTO" -> {
                    // Ítem Exento: Suma directa a ventas exentas
                    acumExenta += totalIvaLine
                }
                "NS", "NO SUJETO" -> {
                    // Ítem No Sujeto: Suma directa a ventas no sujetas
                    acumNoSujeta += totalIvaLine
                }
                else -> {
                    // Ítem Gravado ("G")
                    when (tipoDocumento) {
                        "CF", "RE" -> {
                            // En Crédito Fiscal o Recibo, desglosar Net y IVA
                            acumSumas += totalSinIvaLine
                            acumIva += (totalIvaLine - totalSinIvaLine)
                        }
                        else -> {
                            // En Factura (FC), el total de la línea incluye IVA
                            acumSumas += totalIvaLine
                        }
                    }
                }
            }
        }

        // Paso 2: Calcular IVA Percibido si aplica (Gran contribuyente en CF con sumas > $100)
        var ivaPerci = 0.0
        if (tipoDocumento == "CF" && esGranContribuyente && acumSumas > 100.0) {
            ivaPerci = acumSumas * 0.01
        }

        // Paso 3: Total Final = Gravado + IVA + Exenta + No Sujeta - IVA Percibido
        val totalFinal = acumSumas + acumIva + acumExenta + acumNoSujeta - ivaPerci

        return ResultadoTotales(
            sumas = acumSumas,
            iva = acumIva,
            ventaExenta = acumExenta,
            ventaNoSujeta = acumNoSujeta,
            ivaPerci = ivaPerci,
            totalFinal = totalFinal
        )
    }
}
