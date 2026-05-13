package com.example.acae30.Utilidades

import com.example.acae30.modelos.cierreParcial.CierreItem
import com.example.acae30.modelos.cierreParcial.CierreParcialDTO

class Mapper {

    fun mapToUI(data: List<CierreParcialDTO>?): List<CierreItem> {

        if (data.isNullOrEmpty()) return emptyList()

        val lista = mutableListOf<CierreItem>()

        //CORRELATIVOS
        lista.add(CierreItem.Header("CORRELATIVOS"))

        data.forEach {

            lista.add(CierreItem.TipoDocumento(tipoDocumento(it.tipo)))
            lista.add(
                CierreItem.Correlativo(
                    it.correlativoInicial,
                    it.correlativoFinal
                )
            )
        }

        //VENTA AL CONTADO
        lista.add(CierreItem.Header("VENTA AL CONTADO"))

        data.forEach {

            lista.add(CierreItem.TipoDocumento(tipoDocumento(it.tipo)))

            val esContado = it.terminos.any { termino ->
                termino.termino.equals("Contado", ignoreCase = true)
            }

            if (esContado) {
                it.formasPago.forEach { pago ->
                    lista.add(
                        CierreItem.FormaPago(
                            pago.formaPago,
                            pago.total
                        )
                    )
                }
            }

        }

        //VENTA AL CRÉDITO
        lista.add(CierreItem.Header("VENTA AL CRÉDITO"))

        data.forEach {

            lista.add(CierreItem.TipoDocumento(tipoDocumento(it.tipo)))

            it.terminos.forEach { termino ->
                if (termino.termino.equals("Credito", ignoreCase = true)) {

                    lista.add(
                        CierreItem.Termino(
                            termino.termino,
                            termino.total
                        )
                    )
                }
            }
        }

        //RESUMEN
        lista.add(CierreItem.Header("RESUMEN DE VENTAS"))

        var totalContado = 0.0
        var totalCredito = 0.0

        data.forEach {

            it.terminos.forEach { termino ->

                if (termino.termino.equals("Contado", ignoreCase = true)) {
                    totalContado += termino.total
                }

                if (termino.termino.equals("Credito", ignoreCase = true)) {
                    totalCredito += termino.total
                }
            }
        }

        val totalGeneral = totalContado + totalCredito

        // Items de resumen
        lista.add(CierreItem.Resumen("VENTA AL CONTADO", totalContado))
        lista.add(CierreItem.Resumen("VENTA AL CRÉDITO", totalCredito))
        lista.add(CierreItem.Resumen("TOTAL", totalGeneral))

        return lista
    }

    private fun tipoDocumento(tipo: String) : String{
        val tipoDocumento = when(tipo){
            "FC" -> "CONSUMIDOR FINAL"
            "CF" -> "CRÉDITO FISCAL"
            "RC" -> "RECIBO"
            "NC" -> "NOTA DE CRÉDITO"
            "ND" -> "NOTA DE DEBITO"
            else -> "NO DEFINIDO"
        }

        return tipoDocumento
    }
}
