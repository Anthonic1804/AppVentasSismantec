package com.example.acae30.data.local.appDatabase

import android.content.Context
import androidx.room.Room

class LimpiarBD {

    //-----------------------------------------
    //Funcion para Limiar (Eliminar) la bd al actualizar
    //-----------------------------------------
    fun limpiarBdAlActualizar(context: Context){
        context.deleteDatabase("Acae.db")
        context.deleteSharedPreferences("CONFIG_SERVIDOR")
    }

}