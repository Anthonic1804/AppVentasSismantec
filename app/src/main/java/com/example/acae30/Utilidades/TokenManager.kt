package com.example.acae30.Utilidades

import android.content.Context

class TokenManager(context: Context) {

    private var instancia = "CONFIG_SERVIDOR"

    private val prefs = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

    fun obtenerToken(): String?{
        return prefs.getString("token", "")
    }

}