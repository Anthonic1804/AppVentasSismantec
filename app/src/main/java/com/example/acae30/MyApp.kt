package com.example.acae30

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber

class MyApp : Application() {

    private val instancia = "CONFIG_SERVIDOR"
    private var preferencias: SharedPreferences? = null

    override fun onCreate() {

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        val modoDesarrollo = preferencias!!.getBoolean("modoDesarrollo", false)

        super.onCreate()

        if(modoDesarrollo){

            Timber.plant(Timber.DebugTree())

            /*startKoin {
                androidContext(this@MyApp)

                analytics() //Kotzilla Analytics
            }*/
        }

    }


}