package com.example.acae30


import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Editable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dantsu.escposprinter.EscPosCharsetEncoding
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.usb.UsbConnection
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.example.acae30.R
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.InventarioController
import com.example.acae30.controllers.PedidosController
import com.example.acae30.controllers.VisitaController
import com.example.acae30.databinding.ActivityDetallepedidoBinding
import com.example.acae30.listas.PedidoDetalleAdapter
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.InformacionSucursal
import com.example.acae30.modelos.JSONmodels.CabezeraPedidoSend
import com.example.acae30.modelos.Sucursales
import com.example.acae30.modelos.dataPedidos
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Timer
import kotlin.concurrent.schedule
import com.example.acae30.R as R1


class Detallepedido : AppCompatActivity() {

    //AGREGANDO EL SPINNER DE SUCURSALES
    private var idSucursal: Int? = null
    private var codigoSucursal: String? = null
    private var nombreSucursalPedido: String? = ""
    private var Id_ruta: Int? = 0
    private var Ruta: String? = ""
    private var DTEDireccion: String? = ""
    private var DTECodDepto: String? = ""
    private var DTECodMunicipio: String? = ""
    private var DTECodPais: String? = ""
    private var DTEPais: String? = ""
    private var DTECorreo: String? = ""
    private var DTETelefono: String? = ""

    private var pedidoEnviado: Boolean = false
    private var getSucursalPosition: Int? = null
    private var tipoEnvio: Int? = null
    private var terminosPedidos: String? = null

    private var tipoDocumento: String = ""

    private var idcliente: Int = 0
    private var nombre: String? = ""
    private var idpedido = 0
    private var visita_enviada: Boolean? = null
    private var from: String? = ""
    private var idvendedor = 0
    private var idvisita = 0
    private var vendedor = ""
    private var ip = ""
    private var puerto = 0
    private var alerta: AlertDialogo? = null
    private var codigo = ""
    private var idapi = 0
    var total = 0f

    private var envioSelec : String = ""
    private var documentoSelec : String = ""
    private var sucursalName: String = ""
    private var categoriaCliente: String = ""

    private var funciones = Funciones()
    private var pedidosController = PedidosController()
    private var inventarioController = InventarioController()
    private var clientesController = ClientesController()
    private var visitaController = VisitaController()
    private lateinit var preferencias: SharedPreferences

    private val instancia = "CONFIG_SERVIDOR"

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvTitulo : TextView
    private lateinit var tvMensaje : TextView
    private var enviandoPedido = false
    private var guardandoPedido = false

    private var FacturaExportacion = false
    private var precioConIVA = true

    private var idPedidoServidor = 0

    //private lateinit var infoSucursal : InformacionSucursal
    //private lateinit var infoCliente : Cliente

    val fecha: String = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

    private lateinit var binding: ActivityDetallepedidoBinding
    private var clienteMosoro = 0

    private var P_Imprimir_TK_Venta: Boolean = false

    //-----------
    // VARIABLES PARA VALIDACION DE LIMITE DE ITEMS POR DOCUMENTO
    //-----------
    private var cantidadItemsPedido : Int = 0
    private var limiteItemPedido : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityDetallepedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val intento = intent
        alerta = AlertDialogo(this, this)
        idcliente = intento.getIntExtra("idcliente", 0)
        nombre = intento.getStringExtra("nombrecliente")
        idpedido = intento.getIntExtra("idpedido", 0)
        idvisita = intento.getIntExtra("visitaid", 0)
        codigo = intento.getStringExtra("codigo").toString()
        idapi = intento.getIntExtra("idapi", 0)
        from = intento.getStringExtra("from").toString()
        FacturaExportacion = intento.getBooleanExtra("facturaExportacion", false)

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        idvendedor = preferencias.getInt("Idvendedor", 0)
        vendedor = preferencias.getString("Vendedor", "").toString()
        ip = preferencias.getString("ip", "").toString()
        puerto = preferencias.getInt("puerto", 0)
        clienteMosoro = preferencias.getInt("clienteMoroso", 0)
        P_Imprimir_TK_Venta = preferencias.getBoolean("P_Imprimir_TK_Venta", false)

        visita_enviada = false

        //OBTENIENDO LA CATEGORIA DEL CLIENTE
        categoriaCliente = clientesController.obtenerInformacionCliente(this@Detallepedido, idcliente)?.Categoria_cliente.toString()

        total = pedidosController.obtenerInformacionPedido(idpedido,this@Detallepedido)?.Total!!
        actualizarTotales()

        //DESHABILITAMOS LOS TEXTVIEWS DE INFORMACION ENVIADA
        binding.tvDocumentoSeleccionado.visibility = View.GONE
        binding.tvTipoenvio.visibility = View.GONE
        binding.sinSucursal.visibility = View.GONE

        //FUNCION PARA OBTENER LA INFORMACION DEL PEDIDO
        getTipoEnvio(idpedido)

        //FUNCION PARA CARGAR LAS SUCURSALES AL SPINNER
        cargarSucursales()

        //FUNCION PARA DESHABILITAR FUNCIONES SEGUN PROCESO
        validarProcesoPedidos(codigo)

        //COMPLETANDO SPINNER TERMINOS ENVIO
        terminosDelCliente()

        //CARGANDO LA CANTIDAD DE ITEMS EN EL PEDIDO
        obtenerCantidadItemsPedido()

        when(terminosPedidos){
            "Contado" -> {
                binding.spTipoEnvio.setSelection(0, true)
                binding.tvTipoenvio.text = "CONTADO"
            }
            else -> {
                binding.spTipoEnvio.setSelection(1, true)
                binding.tvTipoenvio.text = "CREDITO"
            }
        }

        //COMPLETANDO SPINNER DOCUMENTO

        val documentoAutorizados = funciones.documentosDeFacturacion(this@Detallepedido)

        val tipoDocumentoAdaptador = ArrayAdapter<String>(this@Detallepedido, android.R.layout.simple_spinner_dropdown_item)
        tipoDocumentoAdaptador.addAll(documentoAutorizados)
        binding.spDocumento.adapter = tipoDocumentoAdaptador

        //COMPLETANDO TVTIPODOCUMENTO
        when(tipoDocumento){
            "FC" -> {
                binding.tvDocumentoSeleccionado.text = getString(R.string.factura)
                binding.spDocumento.setSelection(0, true)
                limiteItemPedido = preferencias.getInt("numItemFactura", 0)

                actualizarVistaTotales()
                actualizarTotales()
            }
            "CF" -> {
                binding.tvDocumentoSeleccionado.text = getString(R.string.credito_fiscal)
                binding.spDocumento.setSelection(1, true)
                limiteItemPedido = preferencias.getInt("numItemCreFiscal", 0)

                actualizarVistaTotales()
                actualizarTotales()
            }
            "RC" -> {
                binding.tvDocumentoSeleccionado.text = getString(R.string.recibo)
                binding.spDocumento.setSelection(2, true)
                limiteItemPedido = preferencias.getInt("numItemRecibo", 0)

                actualizarVistaTotales()
                actualizarTotales()
            }
            "RE" -> {
                binding.tvDocumentoSeleccionado.text = getString(R.string.remisi_n)
                binding.spDocumento.setSelection(3, true)
                limiteItemPedido = preferencias.getInt("numItemRemision", 0)

                actualizarVistaTotales()
                actualizarTotales()
            }
//            "FE" -> {
//                binding.tvDocumentoSeleccionado.text = getString(R.string.factura_exportacion)
//                binding.spDocumento.setSelection(4, true)
//                actualizarVistaTotales()
//                actualizarTotales()
//            }
        }

        //CARTURANDO LA SUCURSAL SELECCIONADA
         getSucursalPosition = intento.getIntExtra("sucursalPosition", 0)
        //println("Sucursal desde Agregar Producto: $nombreSucursalPedido")
        if(getSucursalPosition != 0){
            binding.spSucursal.setSelection(getSucursalPosition!!, true)
        }

        // Consultar datos de visita
        if (idpedido > 0) {
            val base = funciones.obtenerInstancia(this@Detallepedido).openHelper.readableDatabase
            try {
                val sql = "select c.codigo as codigo, " +
                        "c.cliente as nombre, " +
                        "c.id as idcliente, " +
                        "v.id as idvisita, " +
                        "v.enviado as visita_enviada, " +
                        "strftime('%d/%m/%Y %H:%M', p.fecha_creado) as fecha_creado " +
                        "from pedidos p " +
                        "inner join clientes c " +
                        "on p.Id_cliente = c.Id " +
                        "inner join visitas v on p.idvisita = v.id " +
                        "where p.id = ${idpedido}"

                val cursor = base.query(sql)
                cursor.use {
                    if (cursor.count > 0) {
                        cursor.moveToFirst()
                        codigo = cursor.getString(0)
                        nombre = cursor.getString(1)
                        idcliente = cursor.getInt(2)
                        idvisita = cursor.getInt(3)
                        visita_enviada = cursor.getInt(4) == 1
                        binding.fechaCreacion.text = cursor.getString(5)
                    } else {
                        throw Exception("Error al obtener código de cliente")
                    }
                }
            } catch (e: Exception) {
                println("ERROR AL OBTENER INFORMACION DE LA VISITA DEL CLIENTE -> " + e.message)
            }
        }

        //MODIFICACION 04/12/2023
        // VERIFICAMOS SI TENEMOS CONEXION A INTERNET PARA PODER ENVIAR EL PEDIDO O ALMACENARLO
//        if(!funciones.isInternetAvailable(this@Detallepedido)){
//            binding.btnenviar.isEnabled = false
//            binding.btnenviar.setBackgroundResource(R1.drawable.border_btndisable)
//        }

        binding.imbtnatras.setOnClickListener {
            val intento = Intent(this, Pedido::class.java)
            startActivity(intento)
            finish()

        } //regresa al menu principal

        binding.imgbtnadd.setOnClickListener {
            if (cantidadItemsPedido < limiteItemPedido) {
                val intento = Intent(this, Inventario::class.java)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", binding.txtCliente.text.toString())
                intento.putExtra("busqueda", true)
                intento.putExtra("idpedido", idpedido)
                intento.putExtra("visitaid", idvisita)
                intento.putExtra("codigo", codigo)
                intento.putExtra("idapi", idapi)
                intento.putExtra("sucursalPosition", getSucursalPosition)
                intento.putExtra("facturaExportacion", FacturaExportacion)
                startActivity(intento)
                finish()
            }else{
                Toast.makeText(this@Detallepedido, "YA NO PUEDE AGREGAR MAS PRODUCTOS AL PEDIDO",
                    Toast.LENGTH_SHORT).show()
            }
        }
        //muestra el listado de los productos
        binding.btncancelar.setOnClickListener {
            AlertaEliminar()
        }

        //EVENTRO CLIC DEL BOTON ENVIAR
        binding.btnenviar.setOnClickListener {
            if(cantidadItemsPedido <= limiteItemPedido){
                if(codigo == "01"){
                    nombre = binding.txtCliente.text.toString()
                    CoroutineScope(Dispatchers.IO).launch {
                        pedidosController.actualizarNombreClientePedido(this@Detallepedido, nombre!!, idpedido)
                    }
                }

                if (ConfirmarDetallePedido() > 0) {
                    val pedidoInfo = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)
                    enviandoPedido = true

                    if(pedidoInfo?.Cerrado == 0 && pedidoInfo.Enviado == 0){

                        //MOSTRAR LA VENTA DE PAGO SI ESTÁ ACTIVA
                        val facturacionLocal = preferencias.getBoolean("tipoVentaLocal", false)
                        if(facturacionLocal){
                            alertaPago(binding.txttotal.text.toString().toFloat())
                        }else{
                            envioAlerta()
                        }
                    }else{
                        verificarConexionEnvio()
                    }
                } else {
                    funciones.mostrarAlerta("ERROR: NO HAY PRODUCTOS AGREGADOS AL PEDIDO", this@Detallepedido, binding.lienzo)
                }
                /*if(clienteMosoro == 1 && terminosPedidos != "Contado"){
                    funciones.mostrarAlerta("ERROR: NO PUEDE FACTURAR AL CREDITO A CLIENTE EN MORA", this@Detallepedido, binding.lienzo)
                }else{
                    if (ConfirmarDetallePedido() > 0) {
                        val pedidoInfo = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)
                        enviandoPedido = true

                        if(pedidoInfo?.Cerrado == 0 && pedidoInfo.Enviado == 0){
                            alertaPago(total)
                        }else{
                            verificarConexionEnvio()
                        }
                    } else {
                        funciones.mostrarAlerta("ERROR: NO HAY PRODUCTOS AGREGADOS AL PEDIDO", this@Detallepedido, binding.lienzo)
                    }
                }*/
            }else{
                Toast.makeText(this@Detallepedido, "CANTIDAD DE ITEMS PERMITIDOS POR EL TIPO DE DOCUMENTO -> $limiteItemPedido",
                    Toast.LENGTH_SHORT).show()
            }
        }

        //EVENTO CLIC DEL BOTON GUARDAR.
        binding.btnguardar.setOnClickListener {
            if(cantidadItemsPedido <= limiteItemPedido){
                if (ConfirmarDetallePedido() > 0) {
                    guardandoPedido = true
                    alertaPago(binding.txttotal.text.toString().toFloat())
                } else {
                    funciones.mostrarAlerta("ERROR: NO HAY PRODUCTOS AGREGADOS AL PEDIDO", this@Detallepedido, binding.lienzo)
                }
            }else{
                Toast.makeText(this@Detallepedido, "CANTIDAD DE ITEMS PERMITIDOS POR EL TIPO DE DOCUMENTO -> $limiteItemPedido",
                    Toast.LENGTH_SHORT).show()
            }
        }

        //BOTON DE EXPORTAR A PDF EL PEDIDO
        binding.btnexportar.setOnClickListener {
            imprimirRecibo()
        }

        //BOTON DE INVALIDAR PEDIDO
        binding.btnInvalidar.setOnClickListener {
            pedidosController.mensajeInvalidarDTE(this@Detallepedido, "¿Desea Invalidar este Pedido?", idPedidoServidor, idpedido)
        }

        //IMPLEMENTANDO LOGICA DE SUCURSAL SELECCIONADA EN SPINNER
        binding.spSucursal.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sucursalName = parent?.getItemAtPosition(position).toString()

                validarSelecciones(sucursalName)

                if (sucursalName != "-- SELECCIONE UNA SUCURSAL --") {
                    getSucursalPosition = binding.spSucursal.selectedItemPosition
                    updatePedidoSucursal(idcliente, sucursalName, idpedido)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DE TIPO ENVIO SELECCIONADA EN SPINNER
        binding.spTipoEnvio.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                envioSelec = parent?.getItemAtPosition(position).toString()

                when(envioSelec){
                    "CONTADO" -> {
                        pedidosController.actualizarTerminosEnvio("Contado", idpedido,this@Detallepedido)
                        terminosPedidos = "Contado"
                    }
                    "CREDITO" -> {
                        pedidosController.actualizarTerminosEnvio("Credito", idpedido, this@Detallepedido)
                        terminosPedidos = "Credito"
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        //IMPLEMENTANDO LOGICA DE TIPO DOCUMENTO SELECCIONADA EN SPINNER
        binding.spDocumento.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                documentoSelec = parent?.getItemAtPosition(position).toString()

                when(documentoSelec){
                    "FACTURA" -> {
                        pedidosController.updateTipoDocumento("FC", idpedido, this@Detallepedido)
                        tipoDocumento = "FC"
                        FacturaExportacion = false
                        precioConIVA = true

                        limiteItemPedido = preferencias.getInt("numItemFactura", 0)

                        pedidosController.actualizarTotalesPedido(this@Detallepedido,idpedido,precioConIVA)
                        actualizarVistaTotales()

                        actualizarTotales()
                    }
                    "CREDITO FISCAL" -> {
                        pedidosController.updateTipoDocumento("CF", idpedido, this@Detallepedido)
                        tipoDocumento = "CF"
                        FacturaExportacion = false
                        precioConIVA = true

                        limiteItemPedido = preferencias.getInt("numItemCreFiscal", 0)


                        pedidosController.actualizarTotalesPedido(this@Detallepedido,idpedido,precioConIVA)
                        actualizarVistaTotales()

                        actualizarTotales()
                    }
                    "FACTURA EXPORTACION" -> {
//                        Toast.makeText(this@Detallepedido, "OPCION EN REVISION", Toast.LENGTH_SHORT).show()

                        /*pedidosController.updateTipoDocumento("FE", idpedido, this@Detallepedido)
                        tipoDocumento = "FE"
                        FacturaExportacion = true
                        precioConIVA = false

                        pedidosController.actualizarTotalesPedido(this@Detallepedido,idpedido,precioConIVA)
                        actualizarVistaTotales()

                        actualizarTotales()*/
                    }
                    "RECIBO" -> {
                        pedidosController.updateTipoDocumento("RC", idpedido, this@Detallepedido)
                        tipoDocumento = "RC"
                        FacturaExportacion = false
                        precioConIVA = true

                        limiteItemPedido = preferencias.getInt("numItemRecibo", 0)

                        pedidosController.actualizarTotalesPedido(this@Detallepedido,idpedido,precioConIVA)
                        actualizarVistaTotales()

                        actualizarTotales()
                    }
                    "REMISIÓN" -> {
                        pedidosController.updateTipoDocumento("RE", idpedido, this@Detallepedido)
                        tipoDocumento = "RE"
                        FacturaExportacion = false
                        precioConIVA = true

                        limiteItemPedido = preferencias.getInt("numItemRemision", 0)


                        pedidosController.actualizarTotalesPedido(this@Detallepedido,idpedido,precioConIVA)
                        actualizarVistaTotales()

                        actualizarTotales()
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    //FUNCION PARA OBTENER LA CANTIDAD DE ITEMS Y SETEARLO EN PANTALLA
    private fun obtenerCantidadItemsPedido(){
        this@Detallepedido.lifecycleScope.launch {
            cantidadItemsPedido = pedidosController.obtenerCantidadItemsPedido(this@Detallepedido, idpedido)

            binding.cantidadItems.text = "CANT. ITEMS: $cantidadItemsPedido"
        }
    }

    private fun actualizarVistaTotales(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lista = pedidosController.obtenerDetallePedido(idpedido, this@Detallepedido)
                if(lista.size > 0){
                    runOnUiThread {
                        ArmarLista(lista)
                    }
                }
            }catch (e: Exception){
                runOnUiThread {
                    funciones.mostrarAlerta("NO SE PUEDO CARGAR EL DETALLE DEL PEDIDO", this@Detallepedido, binding.lienzo)
                }
            }
        }
    }

    //FUNCION PARA COMPLETAR LOS TERMINOS DEL CLIENTE
    private fun terminosDelCliente(){
        val terminosClientes = clientesController.obtenerInformacionCliente(this@Detallepedido, idcliente)!!.Terminos_cliente

        val listaTipoAdaptador = ArrayAdapter<String>(this@Detallepedido, android.R.layout.simple_spinner_dropdown_item)
        if(terminosClientes == "Contado"){
            listaTipoAdaptador.addAll(listOf("CONTADO"))
        }else{
            listaTipoAdaptador.addAll(listOf("CONTADO", "CREDITO"))
        }
        binding.spTipoEnvio.adapter = listaTipoAdaptador
    }

    //FUNCION PARA GUARDAR EL PEDIDO EN EL DISPOSITIVO
    private fun guardarPedido(){
        try {
            //DESCARGANDO INVENTARIO
            descargarInventario()

            pedidosController.actualizarEstadoAlGuardar(idpedido, this@Detallepedido, binding.lienzo) //FUNCION PARA ACTUALIZAR EL ESTADO
            alerta!!.pedidoGuardado()
            alerta!!.changeText("Guardando Pedido")

            Timer().schedule(2300){
                runOnUiThread {
                    alerta!!.dismisss()
                }

                pedidoEnviado()
            }
        } catch (e: Exception) {
            alerta!!.dismisss()
            funciones.mostrarAlerta("ERROR: ${e.message}", this@Detallepedido, binding.lienzo)
        }
    }

    //FUNCION PARA ACTUALIZAR TOTALES CUANDO ES CREDITO FISCAL
    private fun actualizarTotales(){
        if(total > 0){
            when(tipoDocumento) {
                "CF","RE" -> {
                    if(categoriaCliente == "Gran contribuyente"){
                        if((total/1.13f) > 100f){
                            binding.txtSumas.text = "${String.format("%.2f".format((total/1.13)))}"
                            binding.txtIva.text = "${String.format("%.2f".format(((total/1.13)*0.13)))}"
                            binding.txtIvaPerci.text = "${String.format("%.2f".format(((total/1.13)*0.01)))}"
                            binding.txttotal.text = "${String.format("%.2f".format((total - (total/1.13)*0.01)))}"
                        }else{
                            binding.txtSumas.text = "${String.format("%.2f".format((total/1.13)))}"
                            binding.txtIva.text = "${String.format("%.2f".format(((total/1.13)*0.13)))}"
                            binding.txtIvaPerci.text = "${String.format("%.2f".format(0f))}"
                            binding.txttotal.text = "${String.format("%.2f".format(total))}"
                        }
                    }else{
                        binding.txtSumas.text = "${String.format("%.2f".format((total/1.13)))}"
                        binding.txtIva.text = "${String.format("%.2f".format(((total/1.13)*0.13)))}"
                        binding.txtIvaPerci.text = "${String.format("%.2f".format(0f))}"
                    }
                }
                else -> {
                    binding.txtSumas.text = "${String.format("%.2f".format(total))}"
                    binding.txtIva.text = "${String.format("%.2f".format(0f))}"
                    binding.txtIvaPerci.text = "${String.format("%.2f".format(0f))}"
                    binding.txttotal.text = "${String.format("%.2f".format(total))}"
                }
            }
        }
        val sumas = binding.txtSumas.text.toString().toFloat()
        val iva = binding.txtIva.text.toString().toFloat()
        val ivaperci = binding.txtIvaPerci.text.toString().toFloat()

        pedidosController.actualizarTotalesFiscales(this@Detallepedido, idpedido,
            sumas, iva, ivaperci)

    }

    //FUNCION PARA VALIDAR OPCIONES SELECCIONADAS
    private fun validarSelecciones(sucursalSelec:String){
        if(sucursalSelec != "-- SELECCIONE UNA SUCURSAL --"){
            binding.btnguardar.isEnabled = true
            binding.btnenviar.isEnabled = true
            binding.btnenviar.setBackgroundResource(R1.drawable.border_btnactualizar)
            binding.btnguardar.setBackgroundResource(R1.drawable.border_btnenviar)
        }else{
            binding.btnguardar.isEnabled = false
            binding.btnenviar.isEnabled = false
            binding.btnenviar.setBackgroundResource(R1.drawable.border_btndisable)
            binding.btnguardar.setBackgroundResource(R1.drawable.border_btndisable)
        }
    }

    //FUNCION PARA ENVIAR EL PEDIDO AL SERVIDOR
    private suspend fun enviarPedidoaServidor(){
        try {
            Timer().schedule(2300){
                val pedido = getPedidoSend(idpedido) //retorna el pedido

                pedido!!.Idvendedor = idvendedor // ASIGNAMOS EL ID DEL VENDEDOR AL PEDIDO

                pedido.Vendedor = vendedor//agregamos los datos del vendedor

                val enviado = SendPedido(pedido, idpedido)//envia el pedido y actualiza el estado del pedido en el cel
                alerta!!.dismisss()

                if(enviado){
                    //SI EL PEDIDO YA HA FUE CERRADO NO REALIZA LA DESCARGA NUEVAMENTE
                    if(pedido.Cerrado!! == 0){
                        //DESCARGANDO INVENTARIO
                        descargarInventario()
                    }

                    pedidoEnviado()

                }else{
                    runOnUiThread {
                        Toast.makeText(this@Detallepedido,"DESEA ALMACENAR EL PEDIDO PARA LUEGO ENVIARLO", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                alerta!!.dismisss()

                funciones.mostrarAlerta("ERROR AL ENVIAR EL PEDIDO", this@Detallepedido, binding.lienzo)
            }
        }
    }

    //FUNCION PARA FINALIZAR EL ENVIO DEL PEDIDO
    private fun pedidoEnviado(){
        val visita = visitaController.obtenerVisitaPorID(idvisita, this@Detallepedido)
        if(visita!!.Abierta){
            val intento = Intent(this@Detallepedido, Visita::class.java)
            intento.putExtra("idcliente", idcliente)
            intento.putExtra("nombrecliente", nombre)
            intento.putExtra("idpedido", idpedido)
            intento.putExtra("visitaid", idvisita)
            intento.putExtra("codigo", codigo)
            intento.putExtra("idapi", idapi)
            startActivity(intento)
            finish()
        }else{
            val intento = Intent(this@Detallepedido, Pedido::class.java)
            startActivity(intento)
            finish()
        }

    }

    private fun envioAlerta(){
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMensaje = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        tvTitulo.text = "INFORMACIÓN"
        tvMensaje.text = "¿DESEA ENVIAR EL PEDIDO?"
        tvUpdate.text = "ACEPTAR"

        tvUpdate.setOnClickListener {
            updateDialog.dismiss()
            verificarConexionEnvio()
        }

        tvCancel.setOnClickListener {
            enviandoPedido = false
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    private fun verificarConexionEnvio() {
        if(funciones.isInternetAvailable(this@Detallepedido)){
            alerta!!.pedidoEnviado()

            CoroutineScope(Dispatchers.IO).launch {
                enviarPedidoaServidor()
            }
        }else{
            funciones.mostrarAlerta("ERROR: NO TIENES CONEXION A INTERNET", this@Detallepedido, binding.lienzo)
        }
    }

    //FUNCION PARA DESCARGAR EL PRODUCTO DE INVENTARIO APP
    private fun descargarInventario(){
        //REALIZANDO LA DESCAR DE INVENTARIO DE LA APP
        //SOLO SI SE USA HOJA DE CARGA DE ESCARRSA
        val hojaCarga = preferencias.getBoolean("Hoja_carga_inventario_app", false)
        if(hojaCarga){
            CoroutineScope(Dispatchers.IO).launch {
                inventarioController.descargarProductosInventario(idpedido, this@Detallepedido)
            }
        }
    }

    //OPTENIENDO INFORMACION DEL PEDIDO
    private fun getTipoEnvio(ipPedido: Int){
        val dataBase = funciones.obtenerInstancia(this@Detallepedido).openHelper.readableDatabase
        try {
            val sql = "SELECT Enviado, nombre_sucursal, tipo_envio, tipo_documento, terminos FROM pedidos WHERE id=$ipPedido"
            val getTipo = dataBase.query(sql)
            val getPedidoData = ArrayList<dataPedidos>()
            getTipo.use { c ->
                if(c.count > 0){
                    c.moveToFirst()
                    do {
                        val data = dataPedidos(
                            c.getInt(0) == 1,
                            c.getString(1),
                            c.getInt(2),
                            c.getString(3),
                            c.getString(4)
                        )
                        getPedidoData.add(data)
                    }while (c.moveToNext())
                }
            }

            for(data in getPedidoData){
                pedidoEnviado = data.envioPedido!!
                nombreSucursalPedido = data.nombreSucursalPedido!!.toString()
                tipoEnvio = data.tipoPedido!!.toInt()
                tipoDocumento = data.tipoDocumento!!.toString()
                terminosPedidos = data.terminosPedido!!.toString()
            }
        }catch (e: Exception) {
            println("ERROR AL OBTENER LA INFORMACION DEL PEDIDO -> " + e.message)
        }
    }

    //ACTUALIZANDO LA SUCURSAL DEL PEDIDO
    //CAMBIO EN EL TIPO DE DATO PARA EL CODIGO DE LA SUCURSAL, SE CAMBIO A STRING
    //09/10/2023
    private fun updatePedidoSucursal(idCliente:Int, nombreSucursal: String, idpedidos: Int){
        val db = funciones.obtenerInstancia(this@Detallepedido).openHelper.readableDatabase
        var sucursal = nombreSucursal.replace("'", "''", false)
        try {
            val sql = "SELECT Id, id_cliente, codigo_sucursal, nombre_sucursal, direccion_sucursal, " +
                    "municipio_sucursal, depto_sucursal, telefono_1, correo_sucursal, " +
                    "Id_ruta, Ruta, DTECodDepto, DTECodMunicipio, DTECodPais, DTEPais  FROM cliente_sucursal " +
                    "WHERE id_cliente=$idCliente and nombre_sucursal = '$sucursal'"

            val cursor = db.query(sql)
            //val cursor = db.rawQuery("SELECT * FROM cliente_sucursal WHERE id_cliente=$idCliente and nombre_sucursal like '%$sucursal%'", null)
            val listaSucursales = ArrayList<InformacionSucursal>()
            cursor.use { c ->
                if(c.count > 0){
                    c.moveToFirst()
                    do{
                        val data = InformacionSucursal(
                            c.getInt(0),
                            c.getInt(1),
                            c.getString(2),
                            c.getString(3),
                            c.getString(4),
                            c.getString(5),
                            c.getString(6),
                            c.getString(7),
                            c.getString(8),
                            c.getInt(9),
                            c.getString(10),
                            c.getString(11),
                            c.getString(12),
                            c.getString(13),
                            c.getString(14)
                        )
                        listaSucursales.add(data)
                    }while (c.moveToNext())
                }
            }

            for (data in listaSucursales) {
                idSucursal = data.id
                codigoSucursal = data.codigoSucursal
                Id_ruta = data.idRuta
                Ruta = data.ruta
                DTEDireccion = data.dteDireccion
                DTECodDepto = data.dteCodDepto
                DTECodMunicipio = data.dteCodMunicipio
                DTECodPais = data.dteCodPais
                DTEPais = data.dtePais
                DTECorreo = data.dteCorreo
                DTETelefono = data.dteTelefono
            }
            db.execSQL("UPDATE pedidos set id_sucursal=$idSucursal, " +
                    "codigo_sucursal='$codigoSucursal', " +
                    "nombre_sucursal='$sucursal'," +
                    "Id_ruta = $Id_ruta," +
                    "Ruta = '$Ruta'," +
                    "DTEDireccion = '$DTEDireccion'," +
                    "DTECodDepto = '$DTECodDepto'," +
                    "DTECodMunicipio = '$DTECodMunicipio'," +
                    "DTECodPais = '$DTECodPais'," +
                    "DTEPais = '$DTEPais'," +
                    "DTECorreo = '$DTECorreo'," +
                    "DTETelefono = '$DTETelefono' " +
                    "WHERE id=$idpedidos")

        }catch (e: Exception) {
            println("ERROR AL ACTUALIZAR LA INFORMACION DE LA SUCURSAL EN EL PEDIDO -> " + e.message)
        }
    }

    //CARGANDO EL NOMBRE DE LA SUCURSAL EN EL SPINNER
    private fun nombreSucursal(): ArrayList<String> {
        val nombreSucursal = arrayListOf<String>()
        nombreSucursal.add("-- SELECCIONE UNA SUCURSAL --")
        try {
            val list: ArrayList<com.example.acae30.modelos.Sucursales> = getSucursalesNombre(idcliente)
            if(list.isNotEmpty()){
                for(data in list){
                    nombreSucursal.add(data.nombreSucursal)
                }
            }
        } catch (e: Exception) {
            println("ERROR AL MOSTRAR LA TABLA CONFIG -> " + e.message)
        }
        return nombreSucursal
    }

    //FUNCION PARA CARGAR LAS SUCURSALES AL SPINNER
    private fun cargarSucursales() {
        val listSucursal = nombreSucursal().toMutableList()

        val adaptador = ArrayAdapter(this@Detallepedido, android.R.layout.simple_spinner_item, listSucursal)
        adaptador.setDropDownViewResource(R1.layout.support_simple_spinner_dropdown_item)
        binding.spSucursal.adapter = adaptador
    }

    //FUNCION PARA OBTENER LAS SUCURSALES POR CLIENTE.
    //03-02-2023
    private fun getSucursalesNombre(idCliente:Int): ArrayList<Sucursales> {
        val db = funciones.obtenerInstancia(this@Detallepedido).openHelper.readableDatabase
        val listaSucursales = ArrayList<Sucursales>()
        try {

            val sql = "SELECT * FROM cliente_sucursal WHERE id_cliente='$idCliente'"
            val dataSucursal = db.query(sql)
            dataSucursal.use {
                if(dataSucursal.count > 0){
                    dataSucursal.moveToFirst()
                    do{
                        val data = Sucursales(
                            dataSucursal.getString(0),
                            dataSucursal.getString(2),
                            dataSucursal.getString(3)
                        )
                        listaSucursales.add(data)
                    }while (dataSucursal.moveToNext())
                }else{
                    binding.spSucursal.visibility = View.GONE
                    binding.sinSucursal.visibility = View.VISIBLE
                }
            }
        }catch (e: Exception) {
            println("ERROR AL OBTENER LAS SUCURSALES POR CLIENTE -> " + e.message)
        }
        return listaSucursales
    }

    //FUNCION PARA DESHABILITAR OPCIONES SEGUN VISTA EN PEDIDOS
    private fun validarProcesoPedidos( codigoCliente: String){
        binding.txtCliente.setText(nombre)
        val pedido = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)
        this@Detallepedido.lifecycleScope.launch {
            try {
                val lista = pedidosController.obtenerDetallePedido(idpedido, this@Detallepedido)
                if(lista.size > 0){
                    ArmarLista(lista)
                }
            }catch (e: Exception){
                funciones.mostrarAlerta("NO SE PUEDO CARGAR EL DETALLE DEL PEDIDO", this@Detallepedido, binding.lienzo)
            }
        }
        when(from){
            "ver" -> {
                //MOSTRANDO EL NOMBRE DE LA SUCURSAL
                if (nombreSucursalPedido != "") {
                    binding.sinSucursal.text = nombreSucursalPedido
                } else {
                    binding.sinSucursal.text = getString(R.string.no_tiene_sucursal_registrada_)
                }

                if(pedido!!.Enviado == 1 && pedido.pedido_dte == 0){
                    binding.txtCliente.isEnabled = false
                    binding.imgbtnadd.visibility = View.GONE
                    binding.btnenviar.visibility = View.GONE
                    binding.btnguardar.visibility = View.GONE
                    binding.imbtnatras.visibility = View.VISIBLE
                    binding.btncancelar.visibility = View.GONE
                    binding.spDocumento.visibility = View.GONE
                    binding.spTipoEnvio.visibility = View.GONE
                    binding.spSucursal.visibility = View.GONE
                    binding.sinSucursal.visibility = View.VISIBLE
                    binding.tvDocumentoSeleccionado.visibility = View.VISIBLE
                    binding.tvTipoenvio.visibility = View.VISIBLE
                    binding.btnInvalidar.visibility = View.GONE

                    if(!P_Imprimir_TK_Venta){
                        binding.btnexportar.visibility = View.GONE //visible
                    }else{
                        binding.btnexportar.visibility = View.VISIBLE //visible
                    }

                }else if(pedido.pedido_dte_error == 2){
                    binding.txtCliente.isEnabled = false
                    binding.imgbtnadd.visibility = View.GONE
                    binding.btnenviar.visibility = View.GONE
                    binding.btnguardar.visibility = View.GONE
                    binding.imbtnatras.visibility = View.VISIBLE
                    binding.btncancelar.visibility = View.GONE
                    binding.spDocumento.visibility = View.GONE
                    binding.spTipoEnvio.visibility = View.GONE
                    binding.spSucursal.visibility = View.GONE
                    binding.sinSucursal.visibility = View.VISIBLE
                    binding.tvDocumentoSeleccionado.visibility = View.VISIBLE
                    binding.tvTipoenvio.visibility = View.VISIBLE
                    binding.btnInvalidar.visibility = View.GONE

                    if(!P_Imprimir_TK_Venta){
                        binding.btnexportar.visibility = View.GONE //visible
                    }else{
                        binding.btnexportar.visibility = View.VISIBLE //visible
                    }

                }else if(pedido.Enviado == 0 && pedido.Cerrado == 1){
                    binding.txtCliente.isEnabled = false
                    binding.imgbtnadd.visibility = View.GONE
                    binding.btnenviar.visibility = View.VISIBLE
                    binding.btnguardar.visibility = View.GONE
                    binding.imbtnatras.visibility = View.VISIBLE
                    binding.btncancelar.visibility = View.GONE
                    binding.btnexportar.visibility = View.GONE //VISIBLE
                    binding.spSucursal.visibility = View.GONE
                    binding.sinSucursal.visibility = View.VISIBLE
                    binding.btnInvalidar.visibility = View.GONE
                }else if(pedido.Enviado == 1 && pedido.pedido_dte == 1){
                    binding.txtCliente.isEnabled = false
                    binding.imgbtnadd.visibility = View.GONE
                    binding.btnenviar.visibility = View.GONE
                    binding.btnguardar.visibility = View.GONE
                    binding.imbtnatras.visibility = View.VISIBLE
                    binding.btncancelar.visibility = View.GONE
                    binding.spDocumento.visibility = View.GONE
                    binding.spTipoEnvio.visibility = View.GONE
                    binding.spSucursal.visibility = View.GONE
                    binding.sinSucursal.visibility = View.VISIBLE
                    binding.tvDocumentoSeleccionado.visibility = View.VISIBLE
                    binding.tvTipoenvio.visibility = View.VISIBLE
                    binding.btnInvalidar.visibility = View.VISIBLE //visible

                    if(!P_Imprimir_TK_Venta){
                        binding.btnexportar.visibility = View.GONE //visible
                    }else{
                        binding.btnexportar.visibility = View.VISIBLE //visible
                    }

                    idPedidoServidor = pedido.Id_pedido_sistema!!
                }


                permisosBluetooth()

            }
            "visita" -> {
                //RUTINA PARA AGREGAR NUEVO PEDIDO

                binding.imgbtnadd.visibility = View.VISIBLE
                binding.btnenviar.visibility = View.VISIBLE
                binding.imbtnatras.visibility = View.VISIBLE
                binding.btncancelar.visibility = View.VISIBLE
                binding.btnexportar.visibility = View.GONE
                binding.imbtnatras.visibility = View.GONE
                binding.btnInvalidar.visibility = View.GONE

                if(codigoCliente == "01"){
                    binding.txtCliente.isEnabled = true
                }

            }
        }
    }
    //MODIFICACION PARA AUMENTAR EL NUMERO DE DECIMALES A 4
    //MODIFICACION PARA LA PAPELERIA DM
    //23-08-2022
    private fun ArmarLista(lista: ArrayList<DetallePedido>) {
        //var total = 0.toFloat()
        val pedido = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)
        val mLayoutManager = LinearLayoutManager(
            this@Detallepedido,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.reciclerdetalle.layoutManager = mLayoutManager
        val adapter = PedidoDetalleAdapter(lista, this@Detallepedido) { i ->
            if(pedido!!.Enviado != 1 && from == "visita"){
                val data = lista[i]
                val intento = Intent(this@Detallepedido, Producto_agregar::class.java)

                intento.putExtra("idpedidodetalle", data.Id)
                intento.putExtra("idpedido", data.Id_pedido)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", binding.txtCliente.text.toString())
                intento.putExtra("idproducto", data.Id_producto)
                intento.putExtra("proviene", "editar")
                intento.putExtra("total_param", data.Total_iva)
                intento.putExtra("sucursalPosition", getSucursalPosition)
                intento.putExtra("facturaExportacion", FacturaExportacion)
                startActivity(intento)
                finish()
            }
        }
        //binding.txttotal.text = "$" + "${String.format("%.4f", total)}"
        binding.reciclerdetalle.adapter = adapter

    } //muestra el detalle del pedido

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();

    }//anula el boton atras

    private fun AlertaEliminar() {
        val dialogo = Dialog(this)
        dialogo.show()
        dialogo.setContentView(R1.layout.alert_eliminar)
        dialogo.findViewById<Button>(R1.id.btneliminar).setOnClickListener {
                try {
                    EliminarPedido(idpedido)

                    val intento = Intent(this@Detallepedido, Visita::class.java)
                    intento.putExtra("idcliente", idcliente)
                    intento.putExtra("nombrecliente", nombre)
                    intento.putExtra("visitaid", idvisita)
                    intento.putExtra("codigo", codigo)
                    intento.putExtra("idapi", idapi)
                    startActivity(intento)
                    finish()
                    dialogo.dismiss()
                } catch (e: Exception) {
                    dialogo.dismiss()
                    val alert: Snackbar = Snackbar.make(
                        binding.lienzo,
                        e.message.toString(),
                        Snackbar.LENGTH_LONG
                    )
                    alert.view.setBackgroundColor(resources.getColor(R1.color.moderado))
                    alert.show()
                }
        }//boton eliminar
        dialogo.findViewById<Button>(R1.id.btncancelar).setOnClickListener {
            dialogo.dismiss()
        }//boton eliminar
    } //muestra la alerta para eliminar

    private fun EliminarPedido(idpedido: Int) {
        val bd = funciones.obtenerInstancia(this@Detallepedido).openHelper.readableDatabase
        try {
            val sql = "SELECT * FROM pedidos where Id=$idpedido and Enviado=1"
            val cursor = bd.query(sql)
            cursor.use {
                if (cursor.count > 0) {
                    throw Exception("Este pedido ya fue enviado no se puede eliminar")
                } else {
                    bd.execSQL("DELETE FROM detalle_pedidos WHERE Id_pedido=$idpedido")
                    bd.execSQL("DELETE FROM pedidos where Id=$idpedido")
                }
            }
        } catch (e: Exception) {
            println("ERROR AL TRATAR DE ELIMINAR EL PEDIDO -> " + e.message)
        }
    }

    //AGREGANDO CAMPOS DE SUCURSAL Y TIPO DE ENVIO A LA CABECERA DEL PEDIDO
    private fun getPedidoSend(idpedido: Int): CabezeraPedidoSend? {
        val base = funciones.obtenerInstancia(this@Detallepedido).openHelper.readableDatabase
        try {
            val sql = "SELECT * FROM pedidos where Id=$idpedido"
            base.query(sql).use { pedido ->
                if (pedido.count == 0) return null
                pedido.moveToFirst()

                val envioLocal = CabezeraPedidoSend(
                    pedido.getInt(1),//id del cliente
                    pedido.getString(2), //nombre del cliente
                    pedido.getFloat(11), //POR EL MOMENTO TIENE EL DATO DEL TOTAL
                    pedido.getFloat(5),
                    pedido.getFloat(11),
                    pedido.getInt(12),
                    pedido.getInt(16),
                    pedido.getInt(19),
                    pedido.getString(20),
                    pedido.getString(21),
                    pedido.getInt(23),
                    pedido.getString(22),
                    0,
                    "",
                    pedido.getString(18),
                    pedido.getString(24),
                    pedido.getFloat(25),
                    pedido.getFloat(26),
                    pedido.getFloat(27),
                    pedido.getFloat(28),
                    pedido.getString(39),
                    pedido.getString(29),
                    pedido.getString(30),
                    pedido.getString(31),
                    pedido.getString(32),
                    pedido.getString(33),
                    pedido.getString(34),
                    pedido.getString(35),
                    pedido.getString(36),
                    pedido.getString(37),
                    pedido.getString(38),
                    pedido.getInt(47),
                    pedido.getString(48),
                    pedido.getString(49),
                    pedido.getString(50),
                    pedido.getString(51),
                    pedido.getString(52),
                    pedido.getString(53),
                    pedido.getString(54),
                    pedido.getString(55),
                    null
                )

                val consulta = "SELECT * FROM detalle_producto WHERE Id_pedido=$idpedido"
                base.query(consulta).use { cdetalle ->
                    if (cdetalle.count > 0) {
                        val list = ArrayList<DetallePedido>()
                        cdetalle.moveToFirst()
                        do {
                            val detalle = DetallePedido(
                                cdetalle.getInt(0),
                                cdetalle.getInt(1),
                                cdetalle.getInt(2),
                                cdetalle.getString(3),
                                cdetalle.getString(4),
                                cdetalle.getFloat(5),
                                cdetalle.getFloat(6),
                                cdetalle.getFloat(7),
                                cdetalle.getFloat(8),
                                cdetalle.getFloat(9),
                                cdetalle.getFloat(10),
                                cdetalle.getFloat(11),
                                cdetalle.getFloat(12),
                                cdetalle.getFloat(13),
                                cdetalle.getFloat(14),
                                cdetalle.getFloat(15),
                                cdetalle.getString(16),
                                cdetalle.getInt(17),
                                cdetalle.getFloat(18),
                                cdetalle.getString(19),
                                cdetalle.getInt(20),
                                cdetalle.getString(21),
                                cdetalle.getFloat(22),
                                cdetalle.getFloat(23),
                                cdetalle.getString(24)
                            )
                            list.add(detalle)
                        } while (cdetalle.moveToNext())

                        envioLocal.detalle = list
                    }
                }

                return envioLocal
            }
        } catch (e: Exception) {
            throw Exception(e)
        }
    }

    private fun SendPedido(pedido: CabezeraPedidoSend, idpedido: Int) : Boolean  {
        var enviado = false
        try {
            val objecto = convertToJson(pedido, idpedido) //convertimos a json el objecto pedido
            val ruta: String = "http://$ip:$puerto/pedido" //ruta para enviar el pedido

            println("JSON ENVIADO -> " + objecto )

            val url = URL(ruta)
            with(url.openConnection() as HttpURLConnection) {
                try {
                    setRequestProperty(
                        "Content-Type",
                        "application/json;charset=utf-8"
                    ) //definimos la cabezera
                    connectTimeout = 2000
                    requestMethod = "POST"
                    val or = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    or.write(objecto.toString()) //escribo el json
                    or.flush() //se envia el json
                    val errorcode = responseCode
                    BufferedReader(InputStreamReader(inputStream) as Reader?).use {
                        try {
                            val respuesta = StringBuffer()
                            var inpuline = it.readLine()
                            while (inpuline != null) {
                                respuesta.append(inpuline)
                                inpuline = it.readLine()
                            }
                            it.close()
                            val data: String? = respuesta.toString()
                            if (data != null && data.length > 0) {
                                val datosservidor = JSONObject(data)
                                if (!datosservidor.isNull("error") && !datosservidor.isNull("response")) {
                                    when (responseCode) {
                                        201 -> {
                                            val idpedidoS = datosservidor.getString("error").toInt()
                                            if (idpedidoS > 0) {
                                                enviado = true
                                                ConfirmarPedido(idpedido, idpedidoS)
                                            } else {
                                                enviado = false
                                                funciones.mostrarAlerta("ERROR: AL ENVIAR EL PEDIDO", this@Detallepedido, binding.lienzo)
                                            }
                                        }
                                        400 -> {
                                            enviado = false
                                            funciones.mostrarAlerta("ERROR: RESPUESTA NO ENCONTRADA", this@Detallepedido, binding.lienzo)
                                        }
                                        500 -> {
                                            enviado = false
                                            funciones.mostrarAlerta("ERROR INTERNO DEL SERVIDOR", this@Detallepedido, binding.lienzo)
                                        }
                                    }
                                } else {
                                    enviado = false
                                    funciones.mostrarAlerta("ERROR: NO HEY RESPUESTA DEL SERVIDOR 1", this@Detallepedido, binding.lienzo)
                                }
                            } else {
                                enviado = false
                                funciones.mostrarAlerta("ERROR: NO HAY RESPUESTA DEL SERVIDOR 2", this@Detallepedido, binding.lienzo)
                            }
                        } catch (e: Exception) {
                            enviado = false
                            funciones.mostrarAlerta("ERROR: AL LEER LA RESPUESTA DEL SERVER", this@Detallepedido, binding.lienzo)
                        }
                    } //se obtiene la respuesta del servidor
                } catch (e: Exception) {
                    enviado = false
                    //funciones.mostrarAlerta("ERROR: AL ENVIAR EL JSON DEL PEDIDO", this@Detallepedido, binding.lienzo)
                    println("ERROR AL ENVIAR EL PEDIDO -> ${e.message}")
                }

            }
        } catch (e: Exception) {
            enviado = false
            funciones.mostrarAlerta("ERROR: ENVIO DE PARAMETRO EQUIVOCADOS", this@Detallepedido, binding.lienzo)
        }
        return enviado
    } //funcion que envia el pedido a la bd

    private fun ConfirmarPedido(idpedido: Int, idservidor: Int) {
        val bd = funciones.obtenerInstancia(this@Detallepedido).openHelper.writableDatabase
        try {
            bd.execSQL("UPDATE pedidos set Id_pedido_sistema=$idservidor,Enviado=1,Cerrado=1 WHERE Id=$idpedido")
            //bd!!.execSQL("UPDATE pedidos set Enviado=1,Cerrado=1 WHERE Id=$idpedido")
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    } //actualiza el pedido y confirma que se envio

    private fun ConfirmarDetallePedido(): Int {
        val bd = funciones.obtenerInstancia(this@Detallepedido).openHelper.readableDatabase
        var cantidadDetallepedido = 0.toInt()
        try {
            val sql = "select count(id_pedido) as cantidad from detalle_pedidos where id_pedido = ${idpedido}"
            val cursor = bd.query(sql)
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    cantidadDetallepedido = cursor.getInt(0)
                } else {
                    throw Exception("Error al buscar productos del pedidos.")
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
        return cantidadDetallepedido
    } //actualiza el pedido y confirma que se envio

    private fun convertToJson(pedido: CabezeraPedidoSend, idpedido_param: Int): JsonObject {

        var idvisita_v = 0.toInt()
        val puntoVenta = preferencias.getString("puntoVenta", "").toString()
        val idHojaCarga = preferencias.getInt("idHojaCarga", 0)
        val hojaCarga = preferencias.getInt("hojaCarga", 0)

        var horaProceso = funciones.getFechaHoraProceso()

        val base = funciones.obtenerInstancia(this@Detallepedido).openHelper.readableDatabase
        try {
            val sql = "select v.Idvisita from visitas v inner join pedidos p on v.id = p.idvisita where p.id = ${idpedido_param}"
            val cursor = base.query(sql)
            cursor.use {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    idvisita_v = cursor.getInt(0)
                } else {
                    throw Exception("Error al obtener código de cliente")
                }
            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }

        val json = JsonObject()
        json.addProperty("Idcliente", pedido.Idcliente)
        json.addProperty("Cliente", pedido.Cliente)
        json.addProperty("Subtotal", pedido.Subtotal)
        json.addProperty("Descuento", pedido.Descuento)
        json.addProperty("Total", pedido.Total)
        json.addProperty("Envidado", false)
        json.addProperty("Cerrado", false)
        json.addProperty("IdSucursal", pedido.IdSucursal)
        json.addProperty("CodigoSucursal", pedido.CodigoSucursal)
        json.addProperty("NombreSucursal", pedido.NombreSucursal)
        json.addProperty("TipoEnvio", pedido.TipoEnvio)
        json.addProperty("Tipo_documento_app", pedido.TipoDocumento)
        json.addProperty("Idvendedor", pedido.Idvendedor)
        json.addProperty("Vendedor", pedido.Vendedor)
        json.addProperty("Terminos", pedido.Terminos)
        json.addProperty("fechaCreado", pedido.fechaCreado) /*ENVIANDO LA FECHA DESDE EL DISPOSITIVO MOVIL*/
        json.addProperty("HoraProceso", horaProceso)/*ENVIANDO EL TIMESTAMP DE CREACION DEL PEDIDO*/
        json.addProperty("Idapp", idvisita_v)
        //AGREGANDO NUEVO PARAMETROS
        json.addProperty("Id_hoja_de_carga", idHojaCarga)
        json.addProperty("num_hoja_de_carga", hojaCarga)
        json.addProperty("punto_venta",puntoVenta)

        //ENVIANDO FORMAS DE PAGO
        json.addProperty("Forma_pago", pedido.formaPago)
        json.addProperty("numero_orden",pedido.numeroOrden!!.toBigDecimal())
        json.addProperty("Efectivo_pago", pedido.pagoEfectivo)
        json.addProperty("Tarjeta_pago", pedido.pagoTarjeta)
        json.addProperty("Tarjeta_banco", pedido.bancoTarjeta)
        json.addProperty("Tarjeta_nombre", pedido.nombreTarjeta)
        json.addProperty("Tarjeta_numero", pedido.numTarjeta)
        json.addProperty("Cheque_pago", pedido.pagoCheque)
        json.addProperty("Cheque_banco", pedido.bancoCheque)
        json.addProperty("Cheque_cuenta", pedido.numCuentaCheque)
        json.addProperty("Cheque_numero", pedido.numCheque)
        json.addProperty("Deposito_pago", pedido.pagoDeposito)
        json.addProperty("Deposito_banco", pedido.bancoDeposito)
        json.addProperty("Deposito_cuenta", pedido.numCuentaDeposito)
        json.addProperty("Deposito_numero", pedido.numDeposito)

        //AGREGANDO LA INFORMACION DE DTE Y RUTA
        json.addProperty("Id_ruta", pedido.idRuta)
        json.addProperty("Ruta", pedido.ruta)
        json.addProperty("DTEDireccion", pedido.dteDireccion)
        json.addProperty("DTETelefono", pedido.dteTelefono)
        json.addProperty("DTECorreo",pedido.dteCorreo )
        json.addProperty("DTECodDepto", pedido.dteCodDepto)
        json.addProperty("DTECodMunicipio", pedido.dteCodMunicipio)
        json.addProperty("DTECodPais", pedido.dteCodPais)
        json.addProperty("DTEPais", pedido.dtePais)
        //json.addProperty("DTEGiro", infoCliente!!.dteGiro)

        //se ordena la cabezera
        val detalle = JsonArray()
        for (i in 0..(pedido.detalle!!.size - 1)) {
            val data = pedido.detalle!!.get(i)
            val d = JsonObject()

            d.addProperty("Id", data.Id)
            d.addProperty("Id_pedido", data.Id_pedido)
            d.addProperty("Id_producto", data.Id_producto)
            d.addProperty("Codigo", data.Codigo)
            d.addProperty("Codigo_de_barra", data.Codigo_de_barra)
            d.addProperty("Descripcion", data.Descripcion)
            d.addProperty("Costo", data.Costo)
            d.addProperty("Costo_iva", data.Costo_iva)
            d.addProperty("Precio", data.Precio)
            d.addProperty("Precio_iva", data.Precio_iva)
            d.addProperty("Precio_u", data.Precio_u)
            d.addProperty("Precio_u_iva", data.Precio_u_iva)
            d.addProperty("Cantidad", data.Cantidad)
            d.addProperty("Precio_venta", data.Precio_venta)
            d.addProperty("Total", data.Total_iva)
            d.addProperty("Total_iva", data.Total_iva)
            d.addProperty("Unidad", data.Unidad)
            d.addProperty("Bonificado", data.Bonificado!!.toFloat())
            d.addProperty("Descuento", data.Descuento)
            d.addProperty("Precio_editado", data.Precio_editado)
            d.addProperty("Idunidad", data.Idunidad)
            d.addProperty("EquivaleUni", data.EquivaleUni)
            d.addProperty("EquivaleFra", data.EquivaleFra)
            d.addProperty("UniEquivale", data.UniEquivale)
            d.addProperty("FechaCreado", pedido.fechaCreado) /*ENVIANDO LA MISMA FECHA DEL PEDIDO DESDE EL CEL*/
            detalle.add(d)
        }
        json.add("detalle", detalle)
        return json

    }
    //convierte el pedido a json

    //FUNCION PARA MOSTRAR VENTANA DE PAGO
    private fun alertaPago(total: Float){
        val dialogo = Dialog(this@Detallepedido)
        dialogo.show()
        dialogo.setContentView(R1.layout.vista_cobro)
        dialogo.setCancelable(false)

        val etTotal = dialogo.findViewById<TextInputEditText>(R1.id.txtTotalPago)
        etTotal.setText("$" + "${String.format("%.2f", total)}")

        val etCambio = dialogo.findViewById<TextInputEditText>(R1.id.txtCambioPago)
        var cambio = 0f

        val etPago = dialogo.findViewById<TextInputEditText>(R1.id.txtEfectivoPago)
        var pagoCliente = 0f

        //VARIABLES PARA MOSTRAR Y OCULTAR LOS LAYOUTS
        val spFormaPago = dialogo.findViewById<Spinner>(R1.id.spFormaPago)
        val lyPagoCheque = dialogo.findViewById<LinearLayout>(R1.id.lyContenedorCheque)
        val lyPagoTarjeta = dialogo.findViewById<LinearLayout>(R1.id.lyContenedorTarjeta)
        val lyPagoDeposito = dialogo.findViewById<LinearLayout>(R1.id.lyContenedorDeposito)

        var formaPagoSeleccionada : String = ""

        //VARIABLES PARA ALMACENAR LOS VALORES
        var pagoEfectivo : Float = 0f
        var pagoCheque : Float = 0f
        var pagoTarjeta : Float = 0f
        var pagoDeposito : Float = 0f

        var numeroOrden : String = ""

        var bancoCheque : String = ""
        var numCuentaCheque : String = ""
        var numCheque : String = ""

        var bancoTarjeta : String = ""
        var nombreTarjeta: String = ""
        var numTarjeta : String = ""

        var bancoDeposito : String = ""
        var numCuentaDeposito : String = ""
        var numDeposito : String = ""

        //IMPLEMENTANDO LOGICA DE TIPO DE PAGO SELECCIONADA EN SPINNER
        spFormaPago.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long) {

                formaPagoSeleccionada = parent?.getItemAtPosition(position).toString()

                CoroutineScope(Dispatchers.IO).launch {
                    when(formaPagoSeleccionada){
                        "TARJETA"->{
                            runOnUiThread{
                                lyPagoCheque.visibility = View.GONE
                                lyPagoDeposito.visibility = View.GONE
                                lyPagoTarjeta.visibility = View.VISIBLE
                            }
                        }
                        "CHEQUE" -> {
                            runOnUiThread{
                                lyPagoCheque.visibility = View.VISIBLE
                                lyPagoDeposito.visibility = View.GONE
                                lyPagoTarjeta.visibility = View.GONE
                            }
                        }
                        "DEPOSITO A CUENTA" -> {
                            runOnUiThread{
                                lyPagoCheque.visibility = View.GONE
                                lyPagoDeposito.visibility = View.VISIBLE
                                lyPagoTarjeta.visibility = View.GONE
                            }
                        }
                        else -> {
                            runOnUiThread{
                                lyPagoCheque.visibility = View.GONE
                                lyPagoDeposito.visibility = View.GONE
                                lyPagoTarjeta.visibility = View.GONE
                            }
                        }
                    }
                }

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        etPago.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //NADA QUE HACER
            }

            override fun onTextChanged(pago: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(pago.isNullOrEmpty()){
                    pagoCliente = 0f
                    cambio = pagoCliente - total
                    etCambio.setText("${String.format("%.2f", cambio)}")
                }else{
                    pagoCliente = pago.toString().toFloat()
                    cambio = pagoCliente - total
                    etCambio.setText("${String.format("%.2f", cambio)}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                //NADA QUE HACER
            }

        })

        //PROCESO DEL BOTON ACEPTAR
        dialogo.findViewById<Button>(R1.id.btnaceptar).setOnClickListener {
            numeroOrden = dialogo.findViewById<TextInputEditText>(R1.id.txtNumeroOrden).text.toString()
            /*if(terminosPedidos == "Contado" && etPago.text.toString().isEmpty()){
                Toast.makeText(this@Detallepedido, "DEBE DE INGRESAR EL PAGO DEL CLIENTE", Toast.LENGTH_SHORT)
                    .show()
            }else */
            if(codigo == "00037" && numeroOrden.isEmpty()){
                Toast.makeText(this@Detallepedido, "DEBE DE INGRESAR EL NUMERO DE ORDEN", Toast.LENGTH_SHORT)
                    .show()
            }
            else{

                when(formaPagoSeleccionada){
                    "TARJETA"->{
                        formaPagoSeleccionada = "Tarjeta"
                        pagoTarjeta = if(etPago.text.toString() == ""){
                            0f
                        }else{
                            etPago.text.toString().toFloat()
                        }
                    }
                    "CHEQUE" -> {
                        formaPagoSeleccionada = "Cheque"
                        pagoCheque = if(etPago.text.toString() == ""){
                            0f
                        }else{
                            etPago.text.toString().toFloat()
                        }
                    }
                    "DEPOSITO A CUENTA" -> {
                        formaPagoSeleccionada = "Depósito a Cta."
                        pagoDeposito = if(etPago.text.toString() == ""){
                            0f
                        }else{
                            etPago.text.toString().toFloat()
                        }
                    }
                    else -> {
                        formaPagoSeleccionada = "Efectivo"
                        pagoEfectivo = if(etPago.text.toString() == ""){
                            0f
                        }else{
                            etPago.text.toString().toFloat()
                        }
                    }
                }

                //numeroOrden = dialogo.findViewById<TextInputEditText>(R1.id.txtNumeroOrden).text.toString()

                bancoCheque = dialogo.findViewById<TextInputEditText>(R1.id.tvBanco).text.toString()
                numCuentaCheque = dialogo.findViewById<TextInputEditText>(R1.id.tvNumCuentaCheque).text.toString()
                numCheque = dialogo.findViewById<TextInputEditText>(R1.id.tvNumCheque).text.toString()

                bancoTarjeta = dialogo.findViewById<TextInputEditText>(R1.id.tvTarjeta).text.toString()
                nombreTarjeta = dialogo.findViewById<TextInputEditText>(R1.id.tvNombreTarjeta).text.toString()
                numTarjeta = dialogo.findViewById<TextInputEditText>(R1.id.tvNumTarjeta).text.toString()

                bancoDeposito = dialogo.findViewById<TextInputEditText>(R1.id.tvDeposito).text.toString()
                numCuentaDeposito = dialogo.findViewById<TextInputEditText>(R1.id.tvNumCuentaDeposito).text.toString()
                numDeposito = dialogo.findViewById<TextInputEditText>(R1.id.tvNumDeposito).text.toString()



                CoroutineScope(Dispatchers.IO).launch {
                    pedidosController.actualizarPagoCambioPedido(this@Detallepedido, idpedido,
                        pagoCliente, cambio, pagoEfectivo, pagoCheque, pagoTarjeta, pagoDeposito,
                        numeroOrden, bancoCheque, numCuentaCheque, numCheque, bancoTarjeta, nombreTarjeta,
                        numTarjeta, bancoDeposito, numCuentaDeposito, numDeposito, formaPagoSeleccionada
                    )
                }

                dialogo.dismiss()
                //imprimirRecibo()

                if(enviandoPedido){
                    envioAlerta()
                }

                if(guardandoPedido){
                    guardarPedido()
                }
            }
        }

        //PROCESO DEL BOTON CANCELAR
        dialogo.findViewById<Button>(R1.id.btncancelar).setOnClickListener {
            dialogo.dismiss()
        }

    }




//FUNCION PARA DETERMINAR LA CONEXION DE LA IMPRESORA
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun imprimirRecibo() {
        try {
            val tipoImpresora = preferencias.getString("tipoImpresora", "")
            when(tipoImpresora){
                "BT" -> {
                    // ===============================
                    // Si no hay USB, probar Bluetooth
                    // ===============================
                    val btConnection = BluetoothPrintersConnections.selectFirstPaired()
                    if (btConnection != null) {
                        imprimirTicket(btConnection)
                    } else {
                        Toast.makeText(this, "No se encontró impresora USB ni Bluetooth", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    // ===============================
                    // Detectar impresora Integrada
                    // ===============================
                    imprimirReciboIntegrado()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al imprimir: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    //FUNCION DEL FORMATO DEL TICKET
    private fun imprimirTicket(connection: Any) {

        val empresa = preferencias.getString("empresa", "").orEmpty()
        val direccion = preferencias.getString("direccion", "").orEmpty()
        val nrc = preferencias.getString("nrc", "").orEmpty()
        val nit = preferencias.getString("nit", "").orEmpty()
        val giro = preferencias.getString("giro", "").orEmpty()
        val dteUrlQRHacienda = preferencias.getString("dteUrlQRHacienda", "").orEmpty()
        val dteUrlQRempresa = preferencias.getString("dteUrlQRempresa", "").orEmpty()
        val textoPie = "ESTE DOCUMENTO NO TIENE VALIDEZ FISCAL"

        val infoPedido = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)
        val infoCliente = clientesController.obtenerInformacionCliente(this@Detallepedido, idcliente)

        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        val printer = when(connection) {
            is UsbDevice -> EscPosPrinter(UsbConnection(usbManager, connection), 160, 48f, 32)
            is BluetoothConnection -> EscPosPrinter(connection, 160, 48f, 32)
            else -> null
        } ?: return

        // ===============================
        // Preparar logo y texto
        // ===============================
        val prefs = getSharedPreferences("MisImagenes", MODE_PRIVATE)
        val filePath = prefs.getString("imagenFile", null)

        // Variable para el logo final
        val logoOriginal: Bitmap = if (filePath != null) {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                BitmapFactory.decodeResource(resources, R.drawable.nologo)
            }
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.nologo)
        }

        // Redimensionar
        val logoRedimensionado = redimensionarLogo(logoOriginal, 384)

        val direccionFormateada = dividirEnLineas(direccion, 32)
        val empresaFormateada = dividirEnLineas(empresa, 32)
        val giroFormateada = dividirEnLineas(giro, 32)
        val textoPieFormateado = dividirEnLineas(textoPie, 32)
        val giroCliente = dividirEnLineas(infoCliente!!.dteGiro!!, 32)
        val direccionCliente = dividirEnLineas(infoPedido!!.Sucursal_Direccion!!,32)

        // ===============================
        // Formateando Datos Fiscales DTE
        // ===============================

        val codigoGeneracion = dividirEnLineas(infoPedido.dteCodigoGeneracion!!, 32)
        val numeroControl = dividirEnLineas(infoPedido.dteNumeroControl!!, 32)
        val selloRecepcion = dividirEnLineas(infoPedido.dteSelloRecibido!!, 32)

        val fecha = infoPedido.Fecha_creado?.substring(0, 10).orEmpty()
        val documento = when(infoPedido.Tipo_documento){
            "CF" -> "CREDITO FISCAL"
            "FC" -> "FACTURA"
            "RE" -> "REMISIÓN"
            else -> "RECIBO"
        }

        // ===============================
        // Configurando la impresion de los Qr
        // ===============================
        val qrHacienda = dteUrlQRHacienda + "${infoPedido.dteAmbiente}&codGen=${infoPedido.dteCodigoGeneracion}&fechaEmi=$fecha"
        val qrEmpresa = dteUrlQRempresa + "${infoPedido.dteCodigoGeneracion}"

        val textoVerificacion = dividirEnLineas("Verificacion con $empresa",32)

            val qr =if(dteUrlQRempresa != "0") {("[C]<qrcode size='30'>$qrHacienda</qrcode>\n" +
                    "[C] Qr Hacienda \n" +
                    "\n" +
                    "[C]<qrcode size='30'>$qrEmpresa</qrcode>\n" +
                    "[C] $textoVerificacion \n")}
                    else{
                        "[C]<qrcode size='30'>$qrHacienda</qrcode>\n" +
                                " \n" +
                                "[C] Qr Hacienda \n"
                    }


        // ===============================
        // Detalle del pedido desde controlador
        // ===============================
        val listaDetalle = pedidosController.obtenerDetallePedido(idpedido, this@Detallepedido)
        var total = 0f
        val detalleBuilder = StringBuilder()

        // ===============================
        // Concatenando a la Descripcion, la Cantidad, Codigo de Barra y Bonificados
        // ===============================
        listaDetalle.forEach { item ->
            val descripcionPartes = if(item.Bonificado!! > 0){
                if(infoCliente.Nrc == "193-7" || infoCliente.Nrc == "1937"){
                    dividirDescripcion(
                        (item.Codigo_de_barra + " - " + item.Cantidad.toString() + " " + item.Descripcion + " - BONIFICADOS: " + item.Bonificado) ?: ""
                    )
                }else{
                    dividirDescripcion(
                        (item.Cantidad.toString() + " " + item.Descripcion + " - BONIFICADOS: " + item.Bonificado) ?: ""
                    )
                }
            }else{
                if(infoCliente.Nrc == "193-7" || infoCliente.Nrc == "1937"){
                    dividirDescripcion(
                        (item.Codigo_de_barra + " - " + item.Cantidad.toString() + " " + item.Descripcion) ?: ""
                    )
                }else{
                    dividirDescripcion(
                        (item.Cantidad.toString() + " " + item.Descripcion) ?: ""
                    )
                }
            }

            // ===============================
            // Calculo del detalle para mostrar precio sin iva
            // ===============================
            val totalVenta = if(documento.contentEquals("CREDITO FISCAL")){
                item.Total_iva!!.toDouble() / 1.13
            }else{
                item.Total_iva
            }

            //Funcion para cortar la descripcion en varias lineas
            descripcionPartes.forEachIndexed { index, parte ->
                if (index == 0) {
                    detalleBuilder.append("[L]- $parte [R]$ ${String.format("%.4f", totalVenta)}\n")
                } else {
                    detalleBuilder.append("[L]$parte\n")
                }
            }

            total += item.Total_iva ?: 0f
        }

        total -= infoPedido.Iva_Percibido!!

        if(infoPedido.Enviado == 1 && infoPedido.pedido_dte == 1){
            // ===============================
            // Construir ticket completo DTE
            // ===============================
            val ticket = StringBuilder()
                .append("[C]<img>")
                .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logoRedimensionado))
                .append("</img>\n")
                .append("[C]$empresaFormateada\n")
                .append("[C]$direccionFormateada\n")
                .append("[C]NIT: $nit\n")
                .append("[C]NRC: $nrc\n")
                .append("[C]$giroFormateada\n")
                .append("[L]--------------------------------\n")
                .append("[C]DATOS DEL CLIENTE\n")
                .append("[L]--------------------------------\n")
                .append("[L]NOMBRE:\n")
                .append("[C]${infoCliente.Cliente}\n")
                .append("[L]DOCUMENTO: \n")
                .append("[C]${infoCliente.Nit} / ${infoCliente.Dui} \n")
                .append("[L]N.R.C: ${infoCliente.Nrc} \n")
                .append("[L]ACTIVIDAD ECONOMICA: \n")
                .append("[C]$giroCliente \n")
                .append("[L]NOMBRE SUCURSAL: \n")
                .append("[C]${infoPedido.Nombre_sucursal}\n")
                .append("[L]DIRECCION: \n")
                .append("[C]$direccionCliente\n")
                .append("[L]--------------------------------\n")
                .append("[C]DOCUMENTO TRIBUTARIO ELECTRONICO\n")
                .append("[L]--------------------------------\n")
                .append("[L]TIPO DOCUMENTO:\n")
                .append("[C]$documento \n")
                .append("[L]FECHA DE EMISIÓN\n")
                .append("[C]${infoPedido.Fecha_creado} \n")
                .append("[L]CODIGO DE GENERACION \n")
                .append("[C]$codigoGeneracion \n")
                .append("[L]NUMERO DE CONTROL \n")
                .append("[C]$numeroControl \n")
                .append("[L]SELLO DE RECEPCION\n")
                .append("[C]$selloRecepcion \n")
                .append("[C]TERMINOS: ${infoPedido.Terminos}\n")
                .append("[L]--------------------------------\n")
                .append(qr)
                .append("[L]--------------------------------\n")
                .append("[C]DETALLE DEL DOCUMENTO\n")
                .append("[L]--------------------------------\n")
                .append(detalleBuilder.toString())
                .append("[L]--------------------------------\n")
                .append("[L]SUB-TOTAL: [R] $ ${String.format("%.2f", infoPedido.Suma)} \n")
                .append("[L]IVA: [R] $ ${String.format("%.2f", infoPedido.Iva)} \n")
                .append("[L]IVA RET: [R] $ ${String.format("%.2f", infoPedido.Iva_Percibido)} \n")
                .append("[L]TOTAL: [R] $ ${String.format("%.2f", total)} \n")
                .append("[L]VENDIDO POR: $vendedor\n")
                .append("[L]FECHA: $fecha \n")
                .append("[C]¡GRACIAS POR SU COMPRA! \n")
                .append("[C]<b>$textoPieFormateado</b>\n")
                .append(" \n")
                .append(" \n")
                .append(" \n")

            val textoImprmir = normalizarTexto(ticket.toString())
            printer.printFormattedText(textoImprmir)
        }else{
            // ===============================
            // Construir ticket Normal
            // ===============================
            val ticket = StringBuilder()
                .append("[C]<img>")
                .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logoRedimensionado))
                .append("</img>\n")
                .append("[C]$empresaFormateada\n")
                .append("[C]$direccionFormateada\n")
                .append("[C]NIT: $nit\n")
                .append("[C]NRC: $nrc\n")
                .append("[C]$giroFormateada\n")
                .append("[L]--------------------------------\n")
                .append("[C]DATOS DEL CLIENTE\n")
                .append("[L]--------------------------------\n")
                .append("[L]NOMBRE:\n")
                .append("[C]${infoCliente.Cliente}\n")
                .append("[L]DOCUMENTO: \n")
                .append("[C]${infoCliente.Nit} / ${infoCliente.Dui} \n")
                .append("[L]N.R.C: ${infoCliente.Nrc} \n")
                .append("[L]ACTIVIDAD ECONOMICA: \n")
                .append("[C]$giroCliente \n")
                .append("[L]NOMBRE SUCURSAL: \n")
                .append("[C]${infoPedido.Nombre_sucursal}\n")
                .append("[L]DIRECCION: \n")
                .append("[C]$direccionCliente\n")
                .append("[L]TIPO DOCUMENTO:\n")
                .append("[C]$documento \n")
                .append("[L]--------------------------------\n")
                .append("[C]DETALLE DEL DOCUMENTO\n")
                .append("[L]--------------------------------\n")
                .append(detalleBuilder.toString())
                .append("[L]--------------------------------\n")
                .append("[L]SUB-TOTAL: [R] $ ${String.format("%.2f", infoPedido.Suma)} \n")
                .append("[L]IVA: [R] $ ${String.format("%.2f", infoPedido.Iva)} \n")
                .append("[L]IVA RET: [R] $ ${String.format("%.2f", infoPedido.Iva_Percibido)} \n")
                .append("[L]TOTAL: [R] $ ${String.format("%.2f", total)} \n")
                .append("[L]VENDIDO POR: $vendedor\n")
                .append("[L]FECHA: $fecha \n")
                .append("[C]¡GRACIAS POR SU COMPRA! \n")
                .append("[C]<b>$textoPieFormateado</b>\n")
                .append(" \n")
                .append(" \n")
                .append(" \n")

            val textoImprmir = normalizarTexto(ticket.toString())
            printer.printFormattedText(textoImprmir)
        }
    }

    //Funcion para redimencionar el logo
    private fun redimensionarLogo(bitmap: Bitmap, anchoMaximo: Int) : Bitmap {
        val proporcion = anchoMaximo.toFloat() / bitmap.width
        val altoNuevo = (bitmap.height * proporcion).toInt()

        return bitmap.scale(anchoMaximo, altoNuevo)
    }

    //Funcion para dividir en lineas
    private fun dividirEnLineas(texto: String, maxCaracteres: Int): String {
        return texto.chunked(maxCaracteres).joinToString("\n[C]")
    }

    // Función para dividir en varias líneas la descripcion del prducto
    private fun dividirDescripcion(texto: String, maxLength: Int = 16): List<String> {
        val lineas = mutableListOf<String>()
        var inicio = 0
        while (inicio < texto.length) {
            val fin = (inicio + maxLength).coerceAtMost(texto.length)
            lineas.add(texto.substring(inicio, fin))
            inicio += maxLength
        }
        return lineas
    }

    //Funcion para los permisos Bluetooth
    private fun permisosBluetooth() {
        val permissions = when {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            }
            else -> {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            }
        }

        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, deniedPermissions.toTypedArray(), 1001)
        } else {
            //Toast.makeText(this, "Permisos Bluetooth concedidos ✅", Toast.LENGTH_SHORT).show()
        }
    }

    //FUNCION PARA IMPRIMIR EL RECIBO INTREGRADO
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun imprimirReciboIntegrado(){
        val empresa = preferencias.getString("empresa", "").orEmpty()
        val direccion = preferencias.getString("direccion", "").orEmpty()
        val nrc = preferencias.getString("nrc", "").orEmpty()
        val nit = preferencias.getString("nit", "").orEmpty()
        val giro = preferencias.getString("giro", "").orEmpty()
        val dteUrlQRHacienda = preferencias.getString("dteUrlQRHacienda", "").orEmpty()
        val dteUrlQRempresa = preferencias.getString("dteUrlQRempresa", "").orEmpty()
        val textoPie = "ESTE DOCUMENTO NO TIENE VALIDEZ FISCAL"

        val infoPedido = pedidosController.obtenerInformacionPedido(idpedido, this@Detallepedido)
        val infoCliente = clientesController.obtenerInformacionCliente(this@Detallepedido, idcliente)

        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val impresorIntegrado = preferencias.getString("impresorIntegrado", "sinNombre")

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device : BluetoothDevice? = bluetoothAdapter.bondedDevices.firstOrNull {
            it.name.contains(impresorIntegrado.toString())
        }

        if(device != null){
            val connection = BluetoothConnection(device)

            connection.connect()

            val printer = EscPosPrinter(connection, 160, 48f, 32)


            // ===============================
            // Preparar logo y texto
            // ===============================
            val prefs = getSharedPreferences("MisImagenes", MODE_PRIVATE)
            val filePath = prefs.getString("imagenFile", null)

            // Variable para el logo final
            val logoOriginal: Bitmap = if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    BitmapFactory.decodeResource(resources, R.drawable.nologo)
                }
            } else {
                BitmapFactory.decodeResource(resources, R.drawable.nologo)
            }

            // Redimensionar
            val logoRedimensionado = redimensionarLogo(logoOriginal, 384)

            val direccionFormateada = dividirEnLineas(direccion, 32)
            val empresaFormateada = dividirEnLineas(empresa, 32)
            val giroFormateada = dividirEnLineas(giro, 32)
            val textoPieFormateado = dividirEnLineas(textoPie, 32)
            val giroCliente = dividirEnLineas(infoCliente!!.dteGiro!!, 32)
            val direccionCliente = dividirEnLineas(infoPedido!!.Sucursal_Direccion!!,32)

            // ===============================
            // Formateando Datos Fiscales DTE
            // ===============================

            val codigoGeneracion = dividirEnLineas(infoPedido.dteCodigoGeneracion!!, 32)
            val numeroControl = dividirEnLineas(infoPedido.dteNumeroControl!!, 32)
            val selloRecepcion = dividirEnLineas(infoPedido.dteSelloRecibido!!, 32)

            val fecha = infoPedido.Fecha_creado?.substring(0, 10).orEmpty()
            val documento = when(infoPedido.Tipo_documento){
                "CF" -> "CREDITO FISCAL"
                "FC" -> "FACTURA"
                "RE" -> "REMISIÓN"
                else -> "RECIBO"
            }

            // ===============================
            // Configurando la impresion de los Qr
            // ===============================
            val qrHacienda = dteUrlQRHacienda + "${infoPedido.dteAmbiente}&codGen=${infoPedido.dteCodigoGeneracion}&fechaEmi=$fecha"
            val qrEmpresa = dteUrlQRempresa + "${infoPedido.dteCodigoGeneracion}"

            val textoVerificacion = dividirEnLineas("Verificacion con $empresa",32)

            val qr =if(dteUrlQRempresa != "0") {("[C]<qrcode size='30'>$qrHacienda</qrcode>\n" +
                    "[C] Qr Hacienda \n" +
                    "\n" +
                    "[C]<qrcode size='30'>$qrEmpresa</qrcode>\n" +
                    "[C] $textoVerificacion \n")}
            else{
                "[C]<qrcode size='30'>$qrHacienda</qrcode>\n" +
                        " \n" +
                        "[C] Qr Hacienda \n"
            }


            // ===============================
            // Detalle del pedido desde controlador
            // ===============================
            val listaDetalle = pedidosController.obtenerDetallePedido(idpedido, this@Detallepedido)
            var total = 0f
            val detalleBuilder = StringBuilder()

            // ===============================
            // Concatenando a la Descripcion, la Cantidad, Codigo de Barra y Bonificados
            // ===============================
            listaDetalle.forEach { item ->
                val descripcionPartes = if(item.Bonificado!! > 0){
                    if(infoCliente.Nrc == "193-7" || infoCliente.Nrc == "1937"){
                        dividirDescripcion(
                            (item.Codigo_de_barra + " - " + item.Cantidad.toString() + " " + item.Descripcion + " - BONIFICADOS: " + item.Bonificado) ?: ""
                        )
                    }else{
                        dividirDescripcion(
                            (item.Cantidad.toString() + " " + item.Descripcion + " - BONIFICADOS: " + item.Bonificado) ?: ""
                        )
                    }
                }else{
                    if(infoCliente.Nrc == "193-7" || infoCliente.Nrc == "1937"){
                        dividirDescripcion(
                            (item.Codigo_de_barra + " - " + item.Cantidad.toString() + " " + item.Descripcion) ?: ""
                        )
                    }else{
                        dividirDescripcion(
                            (item.Cantidad.toString() + " " + item.Descripcion) ?: ""
                        )
                    }
                }

                // ===============================
                // Calculo del detalle para mostrar precio sin iva
                // ===============================
                val totalVenta = if(documento.contentEquals("CREDITO FISCAL")){
                    item.Total_iva!!.toDouble() / 1.13
                }else{
                    item.Total_iva
                }

                //Funcion para cortar la descripcion en varias lineas
                descripcionPartes.forEachIndexed { index, parte ->
                    if (index == 0) {
                        detalleBuilder.append("[L]- $parte [R]$ ${String.format("%.4f", totalVenta)}\n")
                    } else {
                        detalleBuilder.append("[L]$parte\n")
                    }
                }

                total += item.Total_iva ?: 0f
            }

            total -= infoPedido.Iva_Percibido!!

            if(infoPedido.Enviado == 1 && infoPedido.pedido_dte == 1){
                // ===============================
                // Construir ticket completo DTE
                // ===============================
                val ticket = StringBuilder()
                    .append("[C]<img>")
                    .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logoRedimensionado))
                    .append("</img>\n")
                    .append("[C]$empresaFormateada\n")
                    .append("[C]$direccionFormateada\n")
                    .append("[C]NIT: $nit\n")
                    .append("[C]NRC: $nrc\n")
                    .append("[C]$giroFormateada\n")
                    .append("[L]--------------------------------\n")
                    .append("[C]DATOS DEL CLIENTE\n")
                    .append("[L]--------------------------------\n")
                    .append("[L]NOMBRE:\n")
                    .append("[C]${infoCliente.Cliente}\n")
                    .append("[L]DOCUMENTO: \n")
                    .append("[C]${infoCliente.Nit} / ${infoCliente.Dui} \n")
                    .append("[L]N.R.C: ${infoCliente.Nrc} \n")
                    .append("[L]ACTIVIDAD ECONOMICA: \n")
                    .append("[C]$giroCliente \n")
                    .append("[L]NOMBRE SUCURSAL: \n")
                    .append("[C]${infoPedido.Nombre_sucursal}\n")
                    .append("[L]DIRECCION: \n")
                    .append("[C]$direccionCliente\n")
                    .append("[L]--------------------------------\n")
                    .append("[C]DOCUMENTO TRIBUTARIO ELECTRONICO\n")
                    .append("[L]--------------------------------\n")
                    .append("[L]TIPO DOCUMENTO:\n")
                    .append("[C]$documento \n")
                    .append("[L]FECHA DE EMISIÓN\n")
                    .append("[C]${infoPedido.Fecha_creado} \n")
                    .append("[L]CODIGO DE GENERACION \n")
                    .append("[C]$codigoGeneracion \n")
                    .append("[L]NUMERO DE CONTROL \n")
                    .append("[C]$numeroControl \n")
                    .append("[L]SELLO DE RECEPCION\n")
                    .append("[C]$selloRecepcion \n")
                    .append("[C]TERMINOS: ${infoPedido.Terminos}\n")
                    .append("[L]--------------------------------\n")
                    .append(qr)
                    .append("[L]--------------------------------\n")
                    .append("[C]DETALLE DEL DOCUMENTO\n")
                    .append("[L]--------------------------------\n")
                    .append(detalleBuilder.toString())
                    .append("[L]--------------------------------\n")
                    .append("[L]SUB-TOTAL: [R] $ ${String.format("%.2f", infoPedido.Suma)} \n")
                    .append("[L]IVA: [R] $ ${String.format("%.2f", infoPedido.Iva)} \n")
                    .append("[L]IVA RET: [R] $ ${String.format("%.2f", infoPedido.Iva_Percibido)} \n")
                    .append("[L]TOTAL: [R] $ ${String.format("%.2f", total)} \n")
                    .append("[L]VENDIDO POR: $vendedor\n")
                    .append("[L]FECHA: $fecha \n")
                    .append("[C]¡GRACIAS POR SU COMPRA! \n")
                    .append("[C]<b>$textoPieFormateado</b>\n")
                    .append(" \n")
                    .append(" \n")
                    .append(" \n")

                val textoImprmir = normalizarTexto(ticket.toString())
                printer.printFormattedText(textoImprmir)
            }else{
                // ===============================
                // Construir ticket Normal
                // ===============================
                val ticket = StringBuilder()
                    .append("[C]<img>")
                    .append(PrinterTextParserImg.bitmapToHexadecimalString(printer, logoRedimensionado))
                    .append("</img>\n")
                    .append("[C]$empresaFormateada\n")
                    .append("[C]$direccionFormateada\n")
                    .append("[C]NIT: $nit\n")
                    .append("[C]NRC: $nrc\n")
                    .append("[C]$giroFormateada\n")
                    .append("[L]--------------------------------\n")
                    .append("[C]DATOS DEL CLIENTE\n")
                    .append("[L]--------------------------------\n")
                    .append("[L]NOMBRE:\n")
                    .append("[C]${infoCliente.Cliente}\n")
                    .append("[L]DOCUMENTO: \n")
                    .append("[C]${infoCliente.Nit} / ${infoCliente.Dui} \n")
                    .append("[L]N.R.C: ${infoCliente.Nrc} \n")
                    .append("[L]ACTIVIDAD ECONOMICA: \n")
                    .append("[C]$giroCliente \n")
                    .append("[L]NOMBRE SUCURSAL: \n")
                    .append("[C]${infoPedido.Nombre_sucursal}\n")
                    .append("[L]DIRECCION: \n")
                    .append("[C]$direccionCliente\n")
                    .append("[L]TIPO DOCUMENTO:\n")
                    .append("[C]$documento \n")
                    .append("[L]--------------------------------\n")
                    .append("[C]DETALLE DEL DOCUMENTO\n")
                    .append("[L]--------------------------------\n")
                    .append(detalleBuilder.toString())
                    .append("[L]--------------------------------\n")
                    .append("[L]SUB-TOTAL: [R] $ ${String.format("%.2f", infoPedido.Suma)} \n")
                    .append("[L]IVA: [R] $ ${String.format("%.2f", infoPedido.Iva)} \n")
                    .append("[L]IVA RET: [R] $ ${String.format("%.2f", infoPedido.Iva_Percibido)} \n")
                    .append("[L]TOTAL: [R] $ ${String.format("%.2f", total)} \n")
                    .append("[L]VENDIDO POR: $vendedor\n")
                    .append("[L]FECHA: $fecha \n")
                    .append("[C]¡GRACIAS POR SU COMPRA! \n")
                    .append("[C]<b>$textoPieFormateado</b>\n")
                    .append(" \n")
                    .append(" \n")
                    .append(" \n")

                val textoImprmir = normalizarTexto(ticket.toString())
                printer.printFormattedText(textoImprmir)
            }

        }else{
            Toast.makeText(this@Detallepedido, "NO ENCONTRADO", Toast.LENGTH_SHORT)
                .show()
        }

    }


    private fun normalizarTexto(texto: String): String {
        val original = "ÁÀÂÄáàâäÉÈÊËéèêëÍÌÎÏíìîïÓÒÔÖóòôöÚÙÛÜúùûüÑñÇç"
        val reemplazo = "AAAAaaaaEEEEeeeeIIIIiiiiOOOOooooUUUUuuuuNnCc"

        var resultado = texto
        for (i in original.indices) {
            resultado = resultado.replace(original[i], reemplazo[i])
        }

        // Elimina caracteres no ASCII
        resultado = resultado.replace(Regex("[^\\x00-\\x7F]"), "")
        return resultado
    }



    override fun onDestroy() {
        super.onDestroy()
    }

}