package com.example.acae30.database

import android.content.Context
import androidx.room.Room

class LimpiarBD {

    //-----------------------------------------
    //Funcion para Limiar (Eliminar) la bd al actualizar
    //-----------------------------------------
    fun limpiarBdAlActualizar(context: Context){
        Room.databaseBuilder(context,AppDatabase::class.java, "Acae.db")
            .fallbackToDestructiveMigration()
            .build()
    }

}