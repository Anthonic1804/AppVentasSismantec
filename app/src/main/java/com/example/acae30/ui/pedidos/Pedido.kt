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
import com.example.acae30.controllers.PedidosController
import com.example.acae30.data.remote.dto.PedidoTransmitidoDTO
import com.example.acae30.listas.PedidosAdapter
import com.example.acae30.modelos.JSONmodels.BusquedaReporteJSON
import com.example.acae30.modelos.JSONmodels.DatosReporteJSON
import com.example.acae30.modelos.Pedidos
import com.example.acae30.ui.clientes.Clientes
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

    private var funciones: Funciones? = null
    private var reciclado: RecyclerView? = null
    private var lienzo: ConstraintLayout? = null
    private var btnsincronizar: Button? = null
    lateinit var preferencias: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var idvendedor = 0
    private var btnatras: ImageButton? = null
    private var vendedor = ""
    private var ip = ""
    private var puerto = 0
    private var proviene: String? = ""
    private var fechaDoc = ""

    val fecha: String = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    private val tituloText = "DETALLE DE PEDIDOS ENVIADOS"

    private var alert: AlertDialogo? = null

    private lateinit var btnReporte: FloatingActionButton
    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var lblMensaje: TextView
    private lateinit var lblTitulo: TextView


    private var pedidosController = PedidosController()

    private var tipoVentaLocal: Boolean = false

    private val utilidades = CrearSslNoSeguro()

    //Variable de control de accion
    private var isProcessing = false

    private var inventarioTiempoReal: Boolean = false
    private var eliminarPedidosAutomaticos: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pedido)
        btnsincronizar = findViewById(R.id.btnsincronizar)
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

        btnatras = findViewById(R.id.imbtnatras)
        btnReporte = findViewById(R.id.btnReporte)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        funciones = Funciones()
        reciclado = findViewById(R.id.recicler)


        lienzo = findViewById(R.id.lienzo)
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            preferencias.edit {
                putBoolean("busqueda", true)
                putBoolean("visita", true)
            }

            val intento = Intent(this, Clientes::class.java)
            startActivity(intento)
            finish()
        }

        //sincronizar los datos que no se han enviado
        btnsincronizar!!.setOnClickListener {
            sincronizacionDePedidos()
        }

        btnatras!!.setOnClickListener {
            val intento = Intent(this, Inicio::class.java)
            startActivity(intento)
            finish()

        } //regresa al menu principal

        // SOLICITAR PERMISOS DE GPS
        solicitarPermisos()
        if(tipoVentaLocal){

            sincronizacionDePedidos()

        }else{
            // MOSTRAR MENSAJE DE GPS
            if (proviene == "inicio") {
                AlertaGPS(this@Pedido)
            }
        }

        if(inventarioTiempoReal){
            funciones!!.limpiarHojaCarga(this@Pedido)
        }
    }

    //FUNCION PARA SINCRONIZAR LOS PEDIDOS AUTOMATICAMENTE
    private fun sincronizacionDePedidos(){

        //Si ya esta activa sale de la funcion
        if(isProcessing) return

        //Si no esta activa, deshabilitamos los controller
        isProcessing = true
        btnsincronizar!!.isEnabled = false
        btnatras!!.isEnabled = false
        btnReporte.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {

            val hayInternet = funciones!!.isInternetAvailable(this@Pedido)
            if(hayInternet){

                runOnUiThread {
                    alert!!.Cargando()
                    messageAsync("SINCRONIZANDO PEDIDOS")
                }

                val pedidosNoTransmitidos : ArrayList<Pedidos> = pedidosController.obtenerPedidosNoTransmitidos(this@Pedido)
                //var idPedidoDTE = 0
                delay(1000)
                if(pedidosNoTransmitidos.isNotEmpty()){
                    for(i in 0 until pedidosNoTransmitidos.size){
                        val item = pedidosNoTransmitidos[i]

                        runOnUiThread {
                            messageAsync("SINCRONIZANDO PEDIDO DEL CLIENTE: \n ${item.Nombre_cliente} \n IdPedido: ${item.IdPedidoApp}")
                        }

                        delay(1000)

                        //obtenerPedidosDTEServidor(item.Id_pedido_sistema!!)
                        
                        /*
                         * CÓDIGO ANTERIOR (Comentado para comparación):
                         * val pedido: PedidoTransmitidoDTO = pedidosController.obtenerPedidosTransmitidos(this@Pedido, item.IdPedidoApp!!)
                         * if(pedido.encontrado){
                         *     val pedidoDTE = if(pedido.pedidoDte!!) 1 else 0
                         *     val pedidoDteError = if(pedido.pedidoDteError!!) 1 else 0
                         *     if(pedido.pedidoDte) {
                         *         pedidosController.actualizarInformacionPedidoTransmitido(this@Pedido, item.Id, pedidoDTE, pedidoDteError, pedido.dteAmbiente!!, ...)
                         *     } else {
                         *         pedidosController.actualizarEstadoPedidoEnviado(this@Pedido, pedido.idPedido!!, item.Id)
                         *     }
                         * }
                         */

                        /*
                         * NUEVO CÓDIGO:
                         * Realizamos validaciones seguras para evitar el java.lang.NullPointerException (NPE).
                         * Reemplazamos el operador '!!' por comparaciones seguras y valores por defecto.
                         */
                        val idPedidoApp = item.IdPedidoApp ?: ""
                        if (idPedidoApp.isNotEmpty()) {
                            
                            val pedido: PedidoTransmitidoDTO = pedidosController.obtenerPedidosTransmitidos(this@Pedido, idPedidoApp)

                            if (pedido.encontrado) {
                                // Evitamos el crash usando '== true' en lugar de '!!'
                                val pedidoDTE = if (pedido.pedidoDte == true) 1 else 0
                                val pedidoDteError = if (pedido.pedidoDteError == true) 1 else 0

                                if (pedido.pedidoDte == true) {
                                    // Actualizando informacion DTE del Pedido Transmitido de forma segura
                                    pedidosController.actualizarInformacionPedidoTransmitido(
                                        this@Pedido, 
                                        item.Id, 
                                        pedidoDTE, 
                                        pedidoDteError, 
                                        pedido.dteAmbiente ?: "",
                                        pedido.dteCodigoGeneracion ?: "", 
                                        pedido.dteSelloRecibido ?: "", 
                                        pedido.dteNumeroControl ?: "", 
                                        pedido.idDocTransmitido ?: 0
                                    )
                                } else {
                                    // Cerrando Pedido no transmitido con ID seguro
                                    pedidosController.actualizarEstadoPedidoEnviado(this@Pedido, pedido.idPedido ?: 0, item.Id)
                                }
                            }
                        }

                    }

                    runOnUiThread {
                        messageAsync("PEDIDOS SINCRONIZADOS CORRECTAMENTE")
                    }

                    delay(1000)

                    //VERIFICANDO SI VENTA LOCAL ESTA ACTIVO PARA ELIMINAR LOS PEDIDOS YA TRANSMITIDOS
                    if(tipoVentaLocal && eliminarPedidosAutomaticos){
                        pedidosController.eliminarPedidosAntiguos(this@Pedido, true)
                    }

                    delay(1000)

                    runOnUiThread {
                        actualizarVistaDTE()
                    }

                    delay(1000)

                    runOnUiThread {
                        alert!!.dismisss()

                        isProcessing = false
                        btnsincronizar!!.isEnabled = true
                        btnatras!!.isEnabled = true
                        btnReporte.isEnabled = true
                    }

                }else{

                    //VERIFICANDO SI VENTA LOCAL ESTA ACTIVO PARA ELIMINAR LOS PEDIDOS YA TRANSMITIDOS
                    if(tipoVentaLocal && eliminarPedidosAutomaticos){
                        pedidosController.eliminarPedidosAntiguos(this@Pedido, true)
                    }

                    delay(1000)

                    runOnUiThread {
                        actualizarVistaDTE()
                    }

                    delay(1000)

                    runOnUiThread {
                        messageAsync("NO SE ENCONTRARON PEDIDOS NO SINCRONIZADOS")
                    }

                    delay(1000)

                    runOnUiThread {
                        alert!!.dismisss()

                        isProcessing = false
                        btnsincronizar!!.isEnabled = true
                        btnatras!!.isEnabled = true
                        btnReporte.isEnabled = true
                    }
                }
            }else{
                runOnUiThread {
                    funciones!!.mensaje(this@Pedido, "NO TIENE CONEXION A INTENET")

                    isProcessing = false
                    btnsincronizar!!.isEnabled = true
                    btnatras!!.isEnabled = true
                    btnReporte.isEnabled = true

                }
            }

        }


        /*if (funciones!!.isInternetAvailable(this@Pedido)){
            alert!!.Cargando()
            messageAsync("SINCRONIZANDO PEDIDOS")

            CoroutineScope(Dispatchers.IO).launch {
                val pedidosNoTransmitidos : ArrayList<Pedidos> = pedidosController.obtenerPedidosNoTransmitidos(this@Pedido)
                //var idPedidoDTE = 0
                delay(1000)
                if(pedidosNoTransmitidos.size > 0){
                    for(i in 0 until pedidosNoTransmitidos.size){
                        val item = pedidosNoTransmitidos[i]

                        withContext(Dispatchers.Main){
                            messageAsync("SINCRONIZANDO PEDIDO DEL CLIENTE: \n ${item.Nombre_cliente}")
                        }

                        delay(1000)

                        obtenerPedidosDTEServidor(item.Id_pedido_sistema!!)

                    }

                    withContext(Dispatchers.Main){
                        messageAsync("PEDIDOS SINCRONIZADOS CORRECTAMENTE")
                    }

                    delay(1000)

                    //VERIFICANDO SI VENTA LOCAL ESTA ACTIVO PARA ELIMINAR LOS PEDIDOS YA TRANSMITIDOS
                    if(tipoVentaLocal){
                        pedidosController.eliminarPedidosAntiguos(this@Pedido, true)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        actualizarVistaDTE()
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.dismisss()
                    }

                }else{

                    //VERIFICANDO SI VENTA LOCAL ESTA ACTIVO PARA ELIMINAR LOS PEDIDOS YA TRANSMITIDOS
                    if(tipoVentaLocal){
                        pedidosController.eliminarPedidosAntiguos(this@Pedido, true)
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        actualizarVistaDTE()
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        messageAsync("NO SE ENCONTRARON PEDIDOS NO SINCRONIZADOS")
                    }

                    delay(1000)

                    withContext(Dispatchers.Main){
                        alert!!.dismisss()
                    }
                }

                /*
                if(pedidoDTE != null)
                {
                    idPedidoDTE = pedidoDTE.Id_pedido_sistema!!
                }

                if (idPedidoDTE > 0) {
                    obtenerPedidosDTEServidor(idPedidoDTE)
                }*/
            }
        }else{
            funciones!!.mensaje(this@Pedido, "NO TIENE CONEXION A INTENET")
        }*/
    }

    //MENSANJE ASINCRONO
    private fun messageAsync(mensaje: String) {
        if (alert != null) {
            alert!!.changeText(mensaje)
        }
    }

    override fun onStart() {
        super.onStart()
        //GlobalScope.launch(Dispatchers.IO) {
        this@Pedido.lifecycleScope.launch {
            try {
                val lista = GetPedido()
                if (lista.size > 0) {
                    ShowList(lista)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val alert: Snackbar = Snackbar.make(
                        lienzo!!,
                        e.message.toString(),
                        Snackbar.LENGTH_LONG
                    )
                    alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                    alert.show()
                }
            }
        }

        //BOTON PARA GENERAR EL REPORTE DE PEDIDOS EN PDF
        btnReporte.setOnClickListener {
            fechaDoc = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"))
            mensajeReporte(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.cancel()
    }

    private fun ShowList(list: ArrayList<Pedidos>) {
        var mLayoutManager = LinearLayoutManager(this@Pedido, LinearLayoutManager.VERTICAL, false)
        reciclado!!.layoutManager = mLayoutManager
        val adapter = PedidosAdapter(list, this@Pedido) { position ->

            //GlobalScope.launch(Dispatchers.Main) {
            this@Pedido.lifecycleScope.launch {

                val data = list.get(position)

                val intento = Intent(this@Pedido, Detallepedido::class.java)
                intento.putExtra("nombrecliente", data.Nombre_cliente)
                intento.putExtra("idcliente", data.Id_cliente!!)
                intent.putExtra("codigo", "")
                intento.putExtra("idpedido", data.Id)
                intento.putExtra("from", "ver")
                startActivity(intento)
                finish()

            }

        }
        reciclado!!.adapter = adapter

    }

    private fun GetPedido(): ArrayList<Pedidos> {
        val base = funciones!!.obtenerInstancia(this@Pedido).openHelper.readableDatabase
        try {
            val cursor = base.query(
                "SELECT Id," +
                        " Id_cliente," +
                        " Nombre_cliente," +
                        " Total," +
                        " Descuento," +
                        " Enviado," +
                        " Fecha_enviado," +
                        " Id_pedido_sistema," +
                        " Gps," +
                        " Cerrado," +
                        " Idvisita," +
                        " strftime('%d/%m/%Y %H:%M'," +
                        " fecha_creado) as fecha_creado," +
                        "Sumas," +
                        "Iva," +
                        "Iva_percibido, " +
                        "pedido_dte, " +
                        "pedido_dte_error," +
                        "Tipo_documento FROM pedidos " +
                        "order by id desc"
            )
            var lista = ArrayList<Pedidos>()
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    do {

                        val pedido = Pedidos(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getString(2),
                            cursor.getFloat(3),
                            cursor.getFloat(4),
                            cursor.getInt(5),
                            cursor.getString(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getInt(9),
                            cursor.getInt(10),
                            cursor.getString(11),
                            cursor.getFloat(12),
                            cursor.getFloat(13),
                            cursor.getFloat(14),
                            cursor.getInt(15),
                            cursor.getInt(16),
                            "",
                            "",
                            "",
                            "",
                            cursor.getString(17),
                            "",
                            "",
                            "",
                            ""
                        )
                        lista.add(pedido)

                    } while (cursor.moveToNext())
                }
            }
            return lista
        } catch (e: Exception) {
            throw Exception(e.message)
        }

    }//obtiene el listado de los pedidos

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
            ),  /* Este codigo es para identificar tu request */
            1
        )
    }

    private fun AlertaGPS(contexto: Pedido) {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alerta_gps)

        // Acccion de click al boton OK
        dialogo.findViewById<Button>(R.id.btnok).setOnClickListener {
            dialogo.dismiss()
        }//boton eliminar

        dialogo.show()

    } //muestra la alerta para agregar precio

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

            val servidor = funciones!!.getServidor(ip, puerto.toString(), this@Pedido)
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
                funciones!!.mensaje(this@Pedido, "ERROR EN LA CONEXION CON EL SERVIDOR -> " + e.message)
            }
        }
    }
    //FUNCION PARA CARGAR LOS PEDIDOS ENVIADOS EN LA BD
    private fun cargarPedidos(json: JSONArray, view: View) {
        val bd = funciones!!.obtenerInstancia(this@Pedido).openHelper.writableDatabase
        try {
            bd.beginTransaction() //INICIANDO TRANSACCION DE REGISTRO
            bd.delete("reporteTemp", null, null) //LIMPIANDO LA TABLA VENTASTEMP

            for (i in 0 until json.length()) {
                val dato = json.getJSONObject(i)
                val valor = ContentValues()
                valor.put("Cliente", funciones!!.validateJsonIsnullString(dato, "cliente"))
                valor.put("Sucursal", funciones!!.validateJsonIsnullString(dato, "sucursal"))
                valor.put("Total", funciones!!.validate(dato.getString("total").toFloat()))

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

            val alert: Snackbar = Snackbar.make(lienzo!!, "REPORTE GENERADO CORRECTAMENTE", Snackbar.LENGTH_LONG)
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
        val base = funciones!!.obtenerInstancia(this@Pedido).openHelper.readableDatabase
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

    //FUNCION PARA OBTENER LOS PEDIDOS DESDE EL SERVIDOR
    /*private fun obtenerPedidosDTEServidor(Id_pedido:Int) {
        try {
            val datos = PedidoDTE(
                Id_pedido
            )
            val objecto =
                Gson().toJson(datos)

            val servidor = funciones!!.getServidor(ip, puerto.toString(), this@Pedido)

            val ruta: String = servidor + "pedido/dte"
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
                                    val res = JSONObject(respuesta.toString())
                                    if (res.length() > 0) {
                                        //cargarPedidos(res, view)
                                        val res_pedido_dte: String = res.getString("pedido_dte")
                                        val res_pedido_dte_error: String = res.getString("pedido_dte_error")
                                        val dteAmbiente : String = res.getString("dteAmbiente")
                                        val dteCodigoGeneracion : String = res.getString("dteCodigoGeneracion")
                                        val dteSelloRecibido: String = res.getString("dteSelloRecibido")
                                        val dteNumeroControl: String = res.getString("dteNumeroControl")
                                        val idDocTransmitido : Int = res.getInt("idDocTransmitido")
                                        var pedido_dte = 0
                                        var pedido_dte_error = 0

                                        if(res_pedido_dte == "true"){
                                            pedido_dte = 1
                                        }

                                        if(res_pedido_dte_error == "true"){
                                            pedido_dte_error = 1
                                        }

                                        pedidosController.actualizarEstadoTransmisionPedido(this@Pedido, Id_pedido,pedido_dte, pedido_dte_error, dteAmbiente, dteCodigoGeneracion,
                                            dteSelloRecibido, dteNumeroControl, idDocTransmitido)

                                    } else {
                                        //runOnUiThread { Toast.makeText(this@Pedido, "NO SE ENCONTRARON PEDIDOS DE ESTE DIA", Toast.LENGTH_LONG).show() }
                                    }
                                } catch (e: Exception) {
                                    throw Exception(e.message)
                                }
                            }
                        }

                        400 -> {
                            //runOnUiThread { Toast.makeText(this@Pedido, "PARAMETROS ERRONEOS", Toast.LENGTH_LONG).show() }
                        }

                        404 -> {
                            //runOnUiThread { Toast.makeText(this@Pedido, "NO SE ENCONTRARON PEDIDOS ENVIADOS", Toast.LENGTH_LONG).show() }
                        }

                        else -> {
                            //runOnUiThread { Toast.makeText(this@Pedido, "ERROR DE CONEXION CON EL SERVIDOR", Toast.LENGTH_LONG).show() }
                        }
                    }
                } catch (e: Exception) {
                    throw Exception(e.message)
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                funciones!!.mensaje(this@Pedido, "ERROR EN LA CONEXION CON EL SERVIDOR -> " + e.message)
            }
        }
    }*/

    private fun actualizarVistaDTE(){
        try {
            val lista = GetPedido()
            ShowList(lista)
        } catch (e: Exception) {
            runOnUiThread {
                val alert: Snackbar = Snackbar.make(
                    lienzo!!,
                    e.message.toString(),
                    Snackbar.LENGTH_LONG
                )
                alert.view.setBackgroundColor(resources.getColor(R.color.moderado))
                alert.show()
            }
        }
    }

}