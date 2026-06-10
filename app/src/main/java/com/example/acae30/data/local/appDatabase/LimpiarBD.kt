package com.example.acae30.data.local.appDatabase

import android.content.Context
import androidx.room.Room

class LimpiarBD {

    //-----------------------------------------
    //Funcion para Limiar (Eliminar) la bd al actualizar
    //-----------------------------------------
    fun limpiarBdAlActualizar(context: Context){
        Room.databaseBuilder(context, AppDatabase::class.java, "Acae.db")
            .fallbackToDestructiveMigration()
            .build()
    }

}