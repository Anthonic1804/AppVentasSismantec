package com.example.acae30.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * REFACTORIZACIÓN ARQUITECTURA LIMPIA: Repositorio para gestionar la configuración de la aplicación.
 * Centraliza el acceso a SharedPreferences para evitar dependencias directas en la UI.
 */
class SettingsRepository(private val context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("CONFIG_SERVIDOR", Context.MODE_PRIVATE)

    fun getTipoImpresora(): String = preferences.getString("tipoImpresora", "") ?: ""
    
    fun getImpresorIntegrado(): String = preferences.getString("impresorIntegrado", "sinNombre") ?: "sinNombre"

    fun getEmpresaInfo(): CompanySettings {
        return CompanySettings(
            nombre = preferences.getString("empresa", "") ?: "",
            direccion = preferences.getString("direccion", "") ?: "",
            nrc = preferences.getString("nrc", "") ?: "",
            nit = preferences.getString("nit", "") ?: "",
            giro = preferences.getString("giro", "") ?: "",
            vendedor = preferences.getString("Vendedor", "") ?: "",
            puntoVenta = preferences.getString("puntoVenta", "") ?: "",
            numeroCaja = preferences.getInt("numeroCaja", 0)
        )
    }

    fun getDteSettings(): DteSettings {
        return DteSettings(
            urlQrHacienda = preferences.getString("dteUrlQRHacienda", "") ?: "",
            urlQrEmpresa = preferences.getString("dteUrlQRempresa", "") ?: ""
        )
    }

    fun getDecimalSettings(): DecimalSettings {
        return DecimalSettings(
            precios = preferences.getInt("decPrecios", 2),
            totales = preferences.getInt("decTotales", 2)
        )
    }

    fun isImpresionTicketVentaActiva(): Boolean = preferences.getBoolean("P_Imprimir_TK_Venta", false)

    data class CompanySettings(
        val nombre: String,
        val direccion: String,
        val nrc: String,
        val nit: String,
        val giro: String,
        val vendedor: String,
        val puntoVenta: String,
        val numeroCaja: Int
    )

    data class DteSettings(
        val urlQrHacienda: String,
        val urlQrEmpresa: String
    )

    data class DecimalSettings(
        val precios: Int,
        val totales: Int
    )
}
