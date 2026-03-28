package com.example.acae30.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.acae30.DAO.InventarioDao
import com.example.acae30.Entities.InventarioEntity
import com.example.acae30.Entities.InventarioLotesEntity
import com.example.acae30.Entities.InventarioPreciosEntity
import com.example.acae30.Entities.InventarioUnidadesEntity

@Database(entities = [InventarioEntity::class,
    InventarioPreciosEntity::class,
    InventarioLotesEntity::class,
    InventarioUnidadesEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventarioDao(): InventarioDao
    companion object{

        @Volatile
        private  var INSTANCE: AppDatabase? = null

        private var tbl: Tablas = Tablas()

        fun getInstance(context: Context) : AppDatabase{

            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "Acae.db"
                ).addCallback(object  : RoomDatabase.Callback(){
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.query("PRAGMA journal_mode=WAL;")
                    }

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        db.execSQL(tbl.cliente())
                        db.execSQL(tbl.clienteSucursal())
                        db.execSQL(tbl.clientePrecios())
                        //db.execSQL(tbl.inventarioUnidades())
                        db.execSQL(tbl.hojaCarga())
                        db.execSQL(tbl.hojaCargaDetalle())
                        db.execSQL(tbl.hojaDetalleRecargas())
                        db.execSQL(tbl.pedidos())
                        db.execSQL(tbl.cuentas())
                        db.execSQL(tbl.visitas())
                        db.execSQL(tbl.detallePedidos())
                        db.execSQL(tbl.vistaDetallePedidos())
                        db.execSQL(tbl.empleados())
                        db.execSQL(tbl.preciosAutorizados())
                        db.execSQL(tbl.ventasTemp())
                        db.execSQL(tbl.ventasDetalleTemp())
                        db.execSQL(tbl.reporteTemp())
                        db.execSQL(tbl.abonosCxc())
                        db.execSQL(tbl.catalogoPais())
                        db.execSQL(tbl.catalogoDepartamento())
                        db.execSQL(tbl.catalogoMunicipio())
                        db.execSQL(tbl.catalogoDistrito())
                        db.execSQL(tbl.catalogoGiro())
                        db.execSQL(tbl.catalogoRuta())
                        db.execSQL(tbl.gastos())
                        db.execSQL(tbl.inventariosolicitudCarga())
                        db.execSQL(tbl.solicitudCarga())
                        db.execSQL(tbl.solicitudCargaDetalle())
                        db.execSQL(tbl.devolucion())
                        db.execSQL(tbl.devolucionDetalle())
                        db.execSQL(tbl.conexionServidores())

                    }
                }).build().also {
                    INSTANCE = it
                }
            }

        }


    }
}