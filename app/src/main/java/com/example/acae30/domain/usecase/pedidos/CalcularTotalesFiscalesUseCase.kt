package com.example.acae30.domain.usecase.pedidos

/**
 * Caso de uso que encapsula la lógica de cálculo de IVA e IVA Percibido.
 */
class CalcularTotalesFiscalesUseCase {
    
    data class ResultadoTotales(
        val sumas: Double,
        val iva: Double,
        val ivaPerci: Double,
        val totalFinal: Double
    )

    operator fun invoke(totalBase: Double, tipoDocumento: String, esGranContribuyente: Boolean): ResultadoTotales {
        return if (totalBase > 0) {
            when (tipoDocumento) {
                "CF", "RE" -> {
                    val sumas = totalBase / 1.13
                    val iva = sumas * 0.13
                    var ivaPerci = 0.0
                    
                    if (esGranContribuyente && sumas > 100.0) {
                        ivaPerci = sumas * 0.01
                    }
                    
                    val totalFinal = totalBase - ivaPerci
                    
                    ResultadoTotales(
                        sumas = sumas,
                        iva = iva,
                        ivaPerci = ivaPerci,
                        totalFinal = totalFinal
                    )
                }
                else -> {
                    ResultadoTotales(
                        sumas = totalBase,
                        iva = 0.0,
                        ivaPerci = 0.0,
                        totalFinal = totalBase
                    )
                }
            }
        } else {
            ResultadoTotales(0.0, 0.0, 0.0, 0.0)
        }
    }
}
