package com.example.acae30.ui.pedidos

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.AlertDialogo
import com.example.acae30.Funciones
import com.example.acae30.Inicio
import com.example.acae30.R
import com.example.acae30.Utilidades.CrearSslNoSeguro
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.domain.usecase.SincronizarPedidosUseCase
import com.example.acae30.listas.PedidosAdapter
import com.example.acae30.modelos.JSONmodels.BusquedaReporteJSON
import com.example.acae30.modelos.JSONmodels.DatosReporteJSON
import com.example.acae30.modelos.Pedidos
import com.example.acae30.ui.factories.PedidosViewModelFactory
import com.example.acae30.ui.clientes.Clientes
import com.example.acae30.data.remote.dto.PedidoTransmitidoDTO
import com.example.acae30.controllers.PedidosController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import com.example.acae30.databinding.ActivityPedidoBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

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
    private val tituloText = "DETALLE DE PEDIDOS ENVIADOS"
    private var alert: AlertDialogo? = null
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var lblMensaje: TextView
    private lateinit var lblTitulo: TextView

    // REFACTORIZACIÓN MVVM: Declaración del ViewModel y el Adaptador
    private lateinit var viewModel: PedidosViewModel
    private lateinit var adapter: PedidosAdapter

    private var tipoVentaLocal: Boolean = false

    private val utilidades = CrearSslNoSeguro()

    //Variable de control de accion
    private var inventarioTiempoReal: Boolean = false
    private var eliminarPedidosAutomaticos: Boolean = false

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
        val dao = AppDatabase.getInstance(this).pedidosDao()
        val repository = PedidosRepository(dao)
        val useCase = SincronizarPedidosUseCase(repository)
        val factory = PedidosViewModelFactory(repository, useCase)
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

        // Observar estado de la sincronización
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.syncStatus.collect { status ->
                    status?.let {
                        manejarEstadoSincronizacion(it)
                    }
                }
            }
        }
    }

    //Funcion para mostrar el listado de pedidos
    private fun mostrarListado(list: ArrayList<Pedidos>) {
        adapter.submitList(list)
    }

    //Funcion para manejar los estados de la Sincronización
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


    //Configurando el RecyvlerView
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

    //Funcion para mostrar la alerta de tener activo el GPS
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
            obtenerPedidos(idvendedor, fecha, view)
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }
    //FUNCION PARA OBTENER LOS PEDIDOS DESDE EL SERVIDOR
    private fun obtenerPedidos(Idvendedor: Int, Fecha: String, view: View) {
        try {
            val datos = BusquedaReporteJSON(
                Idvendedor,
                Fecha
            )
            val objecto =
                Gson().toJson(datos)

            val servidor = funciones.getServidor(ip, puerto.toString(), this@Pedido)
            val ruta: String = servidor + "pedido/reporte"
            val url = URL(ruta)

            val sslContext = utilidades.crearSslInseguro()

            with(url.openConnection() as HttpURLConnection) {

                if(this is HttpsURLConnection){
                    sslSocketFactory = sslContext.socketFactory
                    hostnameVerifier = HostnameVerifier { _, _ -> true }
                }

                try {
                    connectTimeout = 20000
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    )
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto) //SE ESCRIBE EL OBJ JSON
                    or.flush() //SE ENVIA EL OBJ JSON
                    when (responseCode) {
                        200 -> {
                            BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                                try {
                                    val respuesta = StringBuffer()
                                    var inpuline = it.readLine()
                                    while (inpuline != null) {
                                        respuesta.append(inpuline)
                                        inpuline = it.readLine()
                                    }
                                    it.close()
                                    val res = JSONArray(respuesta.toString())
                                    if (res.length() > 0) {
                                        cargarPedidos(res, view)
                                    } else {
                                        runOnUiThread {
                                            Toast.makeText(this@Pedido, "NO SE ENCONTRARON PEDIDOS DE ESTE DIA", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }

                        400 -> {
                            runOnUiThread { Toast.makeText(this@Pedido, "PARAMETROS ERRONEOS", Toast.LENGTH_LONG).show() }
                        }

                        404 -> {
                            runOnUiThread { Toast.makeText(this@Pedido, "NO SE ENCONTRARON PEDIDOS ENVIADOS", Toast.LENGTH_LONG).show() }
                        }

                        else -> {
                            runOnUiThread { Toast.makeText(this@Pedido, "ERROR DE CONEXION CON EL SERVIDOR", Toast.LENGTH_LONG).show() }
                        }
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                funciones.mensaje(this@Pedido, "ERROR EN LA CONEXION CON EL SERVIDOR -> " + e.message)
            }
        }
    }
    //FUNCION PARA CARGAR LOS PEDIDOS ENVIADOS EN LA BD
    private fun cargarPedidos(json: JSONArray, view: View) {
        val bd = funciones.obtenerInstancia(this@Pedido).openHelper.writableDatabase
        try {
            bd.beginTransaction() //INICIANDO TRANSACCION DE REGISTRO
            bd.delete("reporteTemp", null, null) //LIMPIANDO LA TABLA VENTASTEMP

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Cliente", funciones.validateJsonIsnullString(dato, "cliente"))
                valor.put("Sucursal", funciones.validateJsonIsnullString(dato, "sucursal"))
                valor.put("Total", funciones.validate(dato.getString("total").toFloat()))

                bd.insert("reporteTemp", SQLiteDatabase.CONFLICT_REPLACE, valor) //INSERTANDO EN VENTASDETALLE
            } //FINALIZANDO ITERACION FOR
            bd.setTransactionSuccessful() //TRANSACCION COMPLETA
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd.endTransaction()

            generarPDF()
            //verificarPermisos(view)
        }
    }

    //FUNCION PARA GENERAR EL REPORTE EN PDF
    private fun generarPDF() {
        try {
            val carpeta = "/reportespdf"
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + carpeta

            val dir = File(path)
            if(!dir.exists()){
                dir.mkdirs()
                Toast.makeText(this, "CARPETA CREADA CON EXITO", Toast.LENGTH_LONG).show()
            }

            val archivo = File(dir, vendedor + "_$fechaDoc.pdf")
            val fos = FileOutputStream(archivo)

            val documento = Document(PageSize.LETTER, 2.5f, 2.5f, 3.5f, 3.5f)
            PdfWriter.getInstance(documento, fos)

            documento.open()

            //ESPACIOS
            val espaciosDocumento = Paragraph(
                "\n\n\n"
            )
            documento.add(espaciosDocumento)

            //AGREGANDO TITULO PEDIDO
            val fechaDocumento = Paragraph(
                "$tituloText\n\n",
                FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
            )
            fechaDocumento.alignment = Element.ALIGN_CENTER
            documento.add(fechaDocumento)

            //DATOS DEL VENDEDOR
            val tablaCliente = PdfPTable(1)
            tablaCliente.widthPercentage = 80f
            val cellInforCliente = PdfPCell(
                Paragraph(
                    "VENDEDOR: $vendedor\n" +
                            "FECHA: $fecha\n\n\n",
                    FontFactory.getFont("arial", 12f, Font.NORMAL, BaseColor.BLACK)
                )
            )
            cellInforCliente.horizontalAlignment = Element.ALIGN_LEFT
            cellInforCliente.border = 0
            tablaCliente.addCell(cellInforCliente)
            documento.add(tablaCliente)

            //DATOS DEL PEDIDO
            val tablaPedido = PdfPTable(3)
            tablaPedido.widthPercentage = 80f

            val cellReferencia = PdfPCell(
                Paragraph(
                    "CLIENTE",
                    FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
                )
            )
            cellReferencia.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellReferencia)

            val cellDescripcion = PdfPCell(
                Paragraph(
                    "SUCURSAL",
                    FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
                )
            )
            cellDescripcion.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellDescripcion)

            val cellTotal = PdfPCell(
                Paragraph(
                    "TOTAL",
                    FontFactory.getFont("arial", 12f, Font.BOLD, BaseColor.BLACK)
                )
            )
            cellTotal.horizontalAlignment = Element.ALIGN_CENTER
            tablaPedido.addCell(cellTotal)

            //AGREGANDO EL CONTENIDO DEL PEDIDO
            val lista = getReporte()
            var total = 0f

            for(data in lista){

                val cellReferenciaP = PdfPCell(
                    Paragraph(
                        "" + data.Cliente,
                        FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                    )
                )
                cellReferenciaP.horizontalAlignment = Element.ALIGN_CENTER
                tablaPedido.addCell(cellReferenciaP)

                val cellDescripcionP = PdfPCell(
                    Paragraph(
                        "" + data.Sucursal,
                        FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                    )
                )
                cellDescripcionP.horizontalAlignment = Element.ALIGN_CENTER
                tablaPedido.addCell(cellDescripcionP)

                val cellTotalP = PdfPCell(
                    Paragraph(
                        "$ " + data.Total,
                        FontFactory.getFont("arial", 10f, Font.NORMAL, BaseColor.BLACK)
                    )
                )
                cellTotalP.horizontalAlignment = Element.ALIGN_RIGHT
                tablaPedido.addCell(cellTotalP)

                total += data.Total
            }

            val cellReferenciaP = PdfPCell(Paragraph(""))
            cellReferenciaP.border = 0
            tablaPedido.addCell(cellReferenciaP)

            val cellCantidadP = PdfPCell(
                Paragraph(
                    "TOTAL",
                    FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
                )
            )
            cellCantidadP.horizontalAlignment = Element.ALIGN_RIGHT
            tablaPedido.addCell(cellCantidadP)

            val cellTotalP = PdfPCell(
                Paragraph(
                    "$ " + total,
                    FontFactory.getFont("arial", 14f, Font.BOLD, BaseColor.BLACK)
                )
            )
            cellTotalP.horizontalAlignment = Element.ALIGN_RIGHT
            tablaPedido.addCell(cellTotalP)
            documento.add(tablaPedido)

            documento.close()

            val alert: Snackbar = Snackbar.make(binding.lienzo, "REPORTE GENERADO CORRECTAMENTE", Snackbar.LENGTH_LONG)
            alert.view.setBackgroundColor(ContextCompat.getColor(this@Pedido, R.color.btnVerde))
            alert.show()

        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: DocumentException){
            e.printStackTrace()
        }
    }
    //FUNCION PARA OBTENER LOS DATOS PARA EL REPORTE
    private fun getReporte(): ArrayList<DatosReporteJSON> {
        val base = funciones.obtenerInstancia(this@Pedido).openHelper.readableDatabase
        try {
            val cursor = base.query("SELECT *  FROM reporteTemp")
            val lista = ArrayList<DatosReporteJSON>()
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    do {
                        val detalle = DatosReporteJSON(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getFloat(2)
                        )
                        lista.add(detalle)
                    } while (cursor.moveToNext())
                }
            }
            return lista
        } catch (e: Exception) {
            throw Exception(e.message)
        }

    }

}