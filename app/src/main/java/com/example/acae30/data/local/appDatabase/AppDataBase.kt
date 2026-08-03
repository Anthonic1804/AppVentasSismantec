package com.example.acae30.data.local.appDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.acae30.data.local.dao.ClientesDao
import com.example.acae30.data.local.dao.InventarioDao
import com.example.acae30.data.local.dao.InventarioSolicitudDao
import com.example.acae30.data.local.dao.PedidosDao
import com.example.acae30.data.local.dao.ReporteDao
import com.example.acae30.data.local.dao.ServidoresDao
import com.example.acae30.data.local.entity.AbonosEntity
import com.example.acae30.data.local.entity.CatalogoDepartamentoEntity
import com.example.acae30.data.local.entity.CatalogoDistritoEntity
import com.example.acae30.data.local.entity.CatalogoGiroEntity
import com.example.acae30.data.local.entity.CatalogoMunicipioEntity
import com.example.acae30.data.local.entity.CatalogoPaisEntity
import com.example.acae30.data.local.entity.CatalogoRutaEntity
import com.example.acae30.data.local.entity.ClientePreciosEntity
import com.example.acae30.data.local.entity.ClienteSucursalEntity
import com.example.acae30.data.local.entity.ClientesEntity
import com.example.acae30.data.local.entity.CuentasEntity
import com.example.acae30.data.local.entity.DevolucionDetalleEntity
import com.example.acae30.data.local.entity.DevolucionEntity
import com.example.acae30.data.local.entity.EmpleadosEntity
import com.example.acae30.data.local.entity.GastosEntity
import com.example.acae30.data.local.entity.HojaCargaDetalleEntity
import com.example.acae30.data.local.entity.HojaCargaEntity
import com.example.acae30.data.local.entity.HojaDetalleRecargasEntity
import com.example.acae30.data.local.entity.InventarioEntity
import com.example.acae30.data.local.entity.InventarioFTSEntity
import com.example.acae30.data.local.entity.InventarioLotesEntity
import com.example.acae30.data.local.entity.InventarioPreciosEntity
import com.example.acae30.data.local.entity.InventarioSolicitudCargaEntity
import com.example.acae30.data.local.entity.InventarioUnidadesEntity
import com.example.acae30.data.local.entity.LineasEntity
import com.example.acae30.data.local.entity.PedidoDetalleEntity
import com.example.acae30.data.local.entity.PedidosEntity
import com.example.acae30.data.local.entity.PreciosAutorizadosEntity
import com.example.acae30.data.local.entity.ReporteTempEntity
import com.example.acae30.data.local.entity.ServidoresEntity
import com.example.acae30.data.local.entity.SolicitudCargaDetalleEntity
import com.example.acae30.data.local.entity.SolicitudCargaEntity
import com.example.acae30.data.local.entity.VentasDetalleTempEntity
import com.example.acae30.data.local.entity.VentasTempEntity
import com.example.acae30.data.local.entity.VisitasEntity
import com.example.acae30.data.local.views.DetalleProductoView

//InventarioFTSEntity::class,
@Database(entities = [
    InventarioEntity::class,
    InventarioPreciosEntity::class,
    InventarioLotesEntity::class,
    InventarioUnidadesEntity::class,
    LineasEntity::class,
    CatalogoPaisEntity::class,
    CatalogoDepartamentoEntity::class,
    CatalogoMunicipioEntity::class,
    CatalogoDistritoEntity::class,
    CatalogoGiroEntity::class,
    CatalogoRutaEntity::class,
    ServidoresEntity::class,
    HojaCargaEntity::class,
    HojaCargaDetalleEntity::class,
    HojaDetalleRecargasEntity::class,
    EmpleadosEntity::class,
    ClientesEntity::class,
    ClientePreciosEntity::class,
    ClienteSucursalEntity::class,
    CuentasEntity::class,
    GastosEntity::class,
    AbonosEntity::class,
    PreciosAutorizadosEntity::class,
    VentasTempEntity::class,
    VentasDetalleTempEntity::class,
    ReporteTempEntity::class,
    VisitasEntity::class,
    SolicitudCargaEntity::class,
    SolicitudCargaDetalleEntity::class,
    DevolucionEntity::class,
    DevolucionDetalleEntity::class,
    InventarioSolicitudCargaEntity::class,
    PedidosEntity::class,
    PedidoDetalleEntity::class],
    views = [
        DetalleProductoView::class
            ],
    version = 1,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventarioDao(): InventarioDao
    abstract fun clienteDao(): ClientesDao
    abstract fun inventarioSolicitudDao(): InventarioSolicitudDao
    abstract fun servidoresDao(): ServidoresDao
    abstract fun pedidosDao() : PedidosDao
    abstract fun reporteDao() : ReporteDao

    companion object{

        @Volatile
        private  var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context) : AppDatabase{

            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "Acae.db"
                ).addCallback(object  : Callback(){
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.query("PRAGMA journal_mode=WAL;")
                    }
                }).build().also {
                    INSTANCE = it
                }
            }

        }


    }
}