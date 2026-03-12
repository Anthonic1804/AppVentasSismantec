package com.example.acae30.Utilidades

import android.content.Context
import android.provider.Settings

class GenerarIdPorDispositivo {

    private val instancia = "CONFIG_SERVIDOR"

    fun generarIdPorDispositivo(context: Context) : String{

        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        val prefs = context.getSharedPreferences(instancia, Context.MODE_PRIVATE)

        var puntoVenta = prefs.getString("puntoVenta", "")

        /*if (uuid == null) {

            uuid = UUID.randomUUID().toString()

            prefs.edit {
                putString("device_uuid", uuid)
            }

        } */

        return "$androidId-$puntoVenta"

    }

}