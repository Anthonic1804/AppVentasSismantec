package com.example.acae30.ui.pedidos

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.AlertDialogo
import com.example.acae30.Funciones
import com.example.acae30.Inicio
import com.example.acae30.R
import com.example.acae30.Utilidades.PdfReportManager
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.databinding.ActivityPedidoBinding
import com.example.acae30.domain.usecase.ObtenerDatosReporteUseCase
import com.example.acae30.domain.usecase.SincronizarPedidosUseCase
import com.example.acae30.listas.PedidosAdapter
import com.example.acae30.modelos.Pedidos
import com.example.acae30.ui.clientes.Clientes
import com.example.acae30.ui.factories.PedidosViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Pedido : AppCompatActivity() {

    private var funciones = Funciones()
    lateinit var preferencias: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var idvendedor = 0
    private var vendedor = ""
    private var ip = ""
    private var puerto = 0
    private var proviene: String? = ""
    private var fechaDoc = ""
    val fecha: String = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    private var alert: AlertDialogo? = null
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var lblMensaje: TextView
    private lateinit var lblTitulo: TextView

    // REFACTORIZACIÓN MVVM: Declaración del ViewModel y el Adaptador
    private lateinit var viewModel: PedidosViewModel
    private lateinit var adapter: PedidosAdapter

    //Variable de control de accion
    private var inventarioTiempoReal: Boolean = false
    private var eliminarPedidosAutomaticos: Boolean = false
    private var tipoVentaLocal: Boolean = false

    //Inicializando Binding
    private lateinit var binding: ActivityPedidoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(this.instancia, MODE_PRIVATE)
        idvendedor = preferencias.getInt("Idvendedor", 0)
        vendedor = preferencias.getString("Vendedor", "").toString()
        ip = preferencias.getString("ip", "").toString()
        puerto = preferencias.getInt("puerto", 0)
        proviene = intent.getStringExtra("proviene")

        alert = AlertDialogo(this@Pedido, this@Pedido)

        inventarioTiempoReal= preferencias.getBoolean("inventarioTiempoReal", false)
        eliminarPedidosAutomaticos = preferencias.getBoolean("eliminarPedidosAutomaticos", false)
        tipoVentaLocal = preferencias.getBoolean("tipoVentaLocal", false)

        // REFACTORIZACIÓN MVVM: Inicialización de Arquitectura Limpia
        val db = AppDatabase.getInstance(this)
        val pedidosDao = db.pedidosDao()
        val reporteDao = db.reporteDao()
        
        val repository = PedidosRepository(pedidosDao, reporteDao)
        val sincronizarUseCase = SincronizarPedidosUseCase(repository)
        val reporteUseCase = ObtenerDatosReporteUseCase(repository)
        
        val factory = PedidosViewModelFactory(repository, sincronizarUseCase, reporteUseCase)
        viewModel = ViewModelProvider(this, factory)[PedidosViewModel::class.java]

        observarViewModel()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // REFACTORIZACIÓN MVVM: Inicialización única del Adaptador
        setupRecyclerView()

        binding.nuevoPedido.setOnClickListener { view ->
            preferencias.edit {
                putBoolean("busqueda", true)
                putBoolean("visita", true)
            }

            val intento = Intent(this, Clientes::class.java)
            startActivity(intento)
            finish()
        }

        //sincronizar los datos que no se han enviado manualmente desde el botón
        binding.btnsincronizar.setOnClickListener {
            // Refrescar valor justo antes de llamar a la sincronización manual
            eliminarPedidosAutomaticos = preferencias.getBoolean("eliminarPedidosAutomaticos", false)
            tipoVentaLocal = preferencias.getBoolean("tipoVentaLocal", false)
            
            viewModel.sincronizarPedidos(this@Pedido, false, eliminarPedidosAutomaticos, tipoVentaLocal)
        }

        binding.imbtnatras.setOnClickListener {
            val intento = Intent(this, Inicio::class.java)
            startActivity(intento)
            finish()

        }

        // SOLICITAR PERMISOS DE GPS
        solicitarPermisos()

        //Verificando si la configuracion es tipo de venta local
        if(tipoVentaLocal){
            // Sincronización automática al inicio si es venta local
            viewModel.sincronizarPedidos(this@Pedido, true, eliminarPedidosAutomaticos, tipoVentaLocal)

        }else{
            // MOSTRAR MENSAJE DE GPS
            if (proviene == "inicio") {
                alertaGPS()
            }
        }

        if(inventarioTiempoReal){
            funciones.limpiarHojaCarga(this@Pedido)
        }
    }

    override fun onStart() {
        super.onStart()
        
        // REFACTORIZACIÓN MVVM: Refrescar preferencias para asegurar valores actualizados
        inventarioTiempoReal = preferencias.getBoolean("inventarioTiempoReal", false)
        eliminarPedidosAutomaticos = preferencias.getBoolean("eliminarPedidosAutomaticos", false)
        tipoVentaLocal = preferencias.getBoolean("tipoVentaLocal", false)
        
        //BOTON PARA GENERAR EL REPORTE DE PEDIDOS EN PDF
        binding.btnReporte.setOnClickListener {
            fechaDoc = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
            mensajeReporte(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.cancel()
    }

    //-----------------------------------
    // REFACTORIZACIÓN MVVM: Observar cambios en el ViewModel
    //-----------------------------------
    private fun observarViewModel() {
        // Observar listado de pedidos para el RecyclerView
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pedidos.collect { listaEntities ->
                    // Mapear de PedidosEntity (Room) al modelo Pedidos (Adapter)
                    val listaPedidos = ArrayList<Pedidos>()
                    listaEntities.forEach { entity ->
                        listaPedidos.add(
                            Pedidos(
                                entity.id,
                                entity.idCliente,
                                entity.nombreCliente,
                                entity.total.toFloat(),
                                entity.descuento.toFloat(),
                                if (entity.enviado) 1 else 0,
                                entity.fechaEnviado,
                                entity.idPedidoSistema,
                                entity.gps,
                                entity.cerrado,
                                entity.idVisita,
                                entity.fechaCreado,
                                entity.sumas.toFloat(),
                                entity.iva.toFloat(),
                                entity.ivaPercibido.toFloat(),
                                entity.pedidoDte,
                                entity.pedidoDteError,
                                entity.dteAmbiente,
                                entity.dteCodigoGeneracion,
                                entity.dteSelloRecibido,
                                entity.dteNumeroControl,
                                entity.tipoDocumento,
                                entity.terminos,
                                entity.nombreSucursal,
                                entity.dteDireccion,
                                entity.idPedidoApp
                            )
                        )
                    }
                    mostrarListado(listaPedidos)
                }
            }
        }

        //---------------------------------------------------------
        // Observar estado de la sincronización
        //---------------------------------------------------------
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.syncStatus.collect { status ->
                    status?.let {
                        manejarEstadoSincronizacion(it)
                    }
                }
            }
        }

        //---------------------------------------------------------
        // REFACTORIZACIÓN MVVM: Observar estado de la generación de reporte PDF
        //---------------------------------------------------------
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reportStatus.collect { status ->
                    status?.let {
                        manejarEstadoReporte(it)
                    }
                }
            }
        }
    }

    //---------------------------------------------------------
    //Funcion para el Manejo de Estado de Generación del Reporte
    //---------------------------------------------------------
    private fun manejarEstadoReporte(status: ObtenerDatosReporteUseCase.ReportStatus) {
        when (status) {
            is ObtenerDatosReporteUseCase.ReportStatus.Iniciando -> {
                alert?.Cargando()
                messageAsync("PREPARANDO REPORTE...")
            }
            is ObtenerDatosReporteUseCase.ReportStatus.Descargando -> {
                // Si el estado llega muy rápido y el diálogo no se mostró, lo forzamos
                if (alert?.isShowing() == false) alert?.Cargando()
                messageAsync("DESCARGANDO DATOS DEL SERVIDOR...")
            }
            is ObtenerDatosReporteUseCase.ReportStatus.Guardando -> {
                if (alert?.isShowing() == false) alert?.Cargando()
                messageAsync("PROCESANDO INFORMACIÓN...")
            }
            is ObtenerDatosReporteUseCase.ReportStatus.Exito -> {
                // Generar el archivo PDF
                val manager = PdfReportManager()
                val archivo = manager.generarPdfReporteDiario(vendedor, fecha, fechaDoc, status.datos)

                if (archivo != null) {
                    val snack = Snackbar.make(binding.lienzo, "REPORTE GENERADO CORRECTAMENTE", Snackbar.LENGTH_LONG)
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.btnVerde))
                    snack.show()
                } else {
                    funciones.mensaje(this, "ERROR AL ESCRIBIR EL ARCHIVO PDF")
                }
                
                alert?.dismisss()
                viewModel.resetReportStatus()
            }
            is ObtenerDatosReporteUseCase.ReportStatus.Error -> {
                alert?.dismisss()
                funciones.mensaje(this, status.mensaje)
                viewModel.resetReportStatus()
            }
        }
    }

    //---------------------------------------------------------
    //Funcion para mostrar el listado de pedidos
    //---------------------------------------------------------
    private fun mostrarListado(list: ArrayList<Pedidos>) {
        adapter.submitList(list)
    }

    //---------------------------------------------------------
    //Funcion para manejar los estados de la Sincronización
    //---------------------------------------------------------
    private fun manejarEstadoSincronizacion(status: SincronizarPedidosUseCase.SyncProgress) {
        val esSegundoPlano = viewModel.esSegundoPlano.value

        when (status) {
            is SincronizarPedidosUseCase.SyncProgress.Iniciando -> {
                if (!esSegundoPlano) {
                    alert?.Cargando()
                    messageAsync("INICIANDO SINCRONIZACIÓN...")
                }
            }
            is SincronizarPedidosUseCase.SyncProgress.Procesando -> {
                if (!esSegundoPlano) {
                    messageAsync(status.mensaje)
                }
            }
            is SincronizarPedidosUseCase.SyncProgress.Exito -> {
                if (!esSegundoPlano) {
                    messageAsync("SINCRONIZACIÓN COMPLETADA CON ÉXITO")
                }
            }
            is SincronizarPedidosUseCase.SyncProgress.Error -> {
                if (!esSegundoPlano) {
                    funciones.mensaje(this, status.error)
                }
            }
            is SincronizarPedidosUseCase.SyncProgress.Finalizado -> {
                if (!esSegundoPlano) {
                    lifecycleScope.launch {
                        delay(1000)
                        alert?.dismisss()
                    }
                }
                viewModel.resetSyncStatus()
            }
        }
    }

    //MENSANJE ASINCRONO
    private fun messageAsync(mensaje: String) {
        if (alert != null) {
            alert!!.changeText(mensaje)
        }
    }


    //---------------------------------------------------------
    //Configurando el RecyvlerView
    //---------------------------------------------------------
    private fun setupRecyclerView() {
        adapter = PedidosAdapter(this@Pedido) { position ->
            val data = adapter.currentList[position]
            val intento = Intent(this@Pedido, Detallepedido::class.java).apply {
                putExtra("nombrecliente", data.Nombre_cliente)
                putExtra("idcliente", data.Id_cliente!!)
                putExtra("codigo", "")
                putExtra("idpedido", data.Id)
                putExtra("from", "ver")
            }
            startActivity(intento)
            finish()
        }
        
        binding.listadoPedidos.layoutManager = LinearLayoutManager(this@Pedido, LinearLayoutManager.VERTICAL, false)
        binding.listadoPedidos.adapter = adapter
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
//        super.onBackPressed();

    }//anula el boton atras

    private fun solicitarPermisos() {
        // SOLICITAR
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
    }

    //---------------------------------------------------------
    //Funcion para mostrar la alerta de tener activo el GPS
    //---------------------------------------------------------
    private fun alertaGPS() {
        val dialogo = Dialog(this@Pedido)
        dialogo.setContentView(R.layout.alerta_gps)
        dialogo.findViewById<Button>(R.id.btnok).setOnClickListener {
            dialogo.dismiss()
        }
        dialogo.show()
    }

    //FUNCIONES PARA REPORTE DE PEDIDOS ENVIADOS DIARIMENTE DESDE LA APP
    //MODIFICACION 21/06/2023
    //FUNCION PARA EL MENSAJE DE ADVERTENCIA DE REPORTE

    //REFACTORIZADO 03/08/2026
    private fun mensajeReporte(view: View){
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cargar_empleados)
        lblMensaje = updateDialog.findViewById(R.id.lblMensaje)
        lblTitulo = updateDialog.findViewById(R.id.lblTitulo)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)

        lblTitulo.text = "REPORTE DE PEDIDOS DIARIOS"
        lblMensaje.text = "¿Desea generar el Reporte de Pedidos?"
        tvUpdate.text = "ACEPTAR"

        tvUpdate.setOnClickListener {
            updateDialog.dismiss()
            viewModel.obtenerDatosReporte(idvendedor, fecha, this@Pedido)
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

}