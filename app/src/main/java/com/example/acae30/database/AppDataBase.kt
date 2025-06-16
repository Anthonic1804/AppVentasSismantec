package com.example.acae30.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.acae30.DAO.InventarioDao
import com.example.acae30.Entities.InventarioEntity
import com.example.acae30.Entities.InventarioPreciosEntity

@Database(entities = [InventarioEntity::class, InventarioPreciosEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventarioDao(): InventarioDao

    companion object{

        @Volatile
        private  var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context) : AppDatabase{

            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "Acae.db"
                ).build().also {
                    INSTANCE = it
                }
            }

        }


    }
}