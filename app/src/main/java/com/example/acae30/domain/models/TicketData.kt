package com.example.acae30.domain.models

import com.example.acae30.data.repository.SettingsRepository
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.Pedidos

/**
 * REFACTORIZACIÓN ARQUITECTURA LIMPIA: Modelo de dominio que agrupa todos los datos necesarios para imprimir un ticket.
 * Independiente de la plataforma y de la librería de impresión.
 */
data class TicketData(
    val empresa: SettingsRepository.CompanySettings,
    val dteSettings: SettingsRepository.DteSettings,
    val decimalSettings: SettingsRepository.DecimalSettings,
    val pedido: Pedidos,
    val cliente: Cliente,
    val detalle: List<DetallePedido>,
    val esDte: Boolean,
    val totalFacturado: Float
)
