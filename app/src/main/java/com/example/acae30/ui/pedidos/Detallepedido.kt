package com.example.acae30.ui.pedidos

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
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.database.getFloatOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.usb.UsbConnection
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.example.acae30.AlertDialogo
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.Utilidades.CrearSslNoSeguro
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.InventarioController
import com.example.acae30.controllers.PedidosController
import com.example.acae30.controllers.VisitaController
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.local.entity.ClienteSucursalEntity
import com.example.acae30.data.local.entity.PedidosEntity
import com.example.acae30.data.remote.api.clientes.ClientesApi
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.data.repository.SettingsRepository
import com.example.acae30.databinding.ActivityDetallepedidoBinding
import com.example.acae30.domain.usecase.ActualizarSucursalPedidoUseCase
import com.example.acae30.domain.usecase.GetSucursalesUseCase
import com.example.acae30.domain.usecase.pedidos.ActualizarNombreClienteUseCase
import com.example.acae30.domain.usecase.pedidos.ActualizarTotalesFiscalesUseCase
import com.example.acae30.domain.usecase.pedidos.CalcularTotalesFiscalesUseCase
import com.example.acae30.domain.usecase.pedidos.CrearPedidoUseCase
import com.example.acae30.domain.usecase.pedidos.EliminarPedidoUseCase
import com.example.acae30.domain.usecase.pedidos.EnviarPedidoUseCase
import com.example.acae30.domain.usecase.pedidos.GetDetallePedidoFlowUseCase
import com.example.acae30.domain.usecase.pedidos.GetTicketDataUseCase
import com.example.acae30.domain.usecase.pedidos.GetPedidosBorradoresUseCase
import com.example.acae30.domain.usecase.pedidos.ObtenerCantidadItemsUseCase
import com.example.acae30.listas.PedidoDetalleAdapter
import com.example.acae30.listas.PedidosBorradoresAdapter
import com.example.acae30.listas.SucursalBusquedaAdapter
import com.example.acae30.modelos.Cliente
import com.example.acae30.modelos.DetallePedido
import com.example.acae30.modelos.JSONmodels.CabezeraPedidoSend
import com.example.acae30.modelos.Sucursales
import com.example.acae30.modelos.dataPedidos
import com.example.acae30.ui.clientes.Clientes
import com.example.acae30.ui.factories.DetallePedidoViewModelFactory
import com.example.acae30.ui.inventario.Inventario
import com.example.acae30.ui.inventario.InventarioTiempoReal
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Timer
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.schedule

class Detallepedido : AppCompatActivity() {

    private var idcliente: Int = 0
    private var nombre: String? = ""
    private var idpedido = 0
    private var idvisita = 0
    private var codigo = ""
    private var idapi = 0
    private var from: String? = ""
    
    private var idVendedor = 0
    private var vendedor = ""
    private var ip = ""
    private var puerto = 0
    
    var total = 0f
    private var tipoDocumento: String = ""
    private var terminosPedidos: String? = null
    private var nombreSucursalPedido: String? = ""
    private var latitud = "0"
    private var longitud = "0"

    private var alerta: AlertDialogo? = null
    private var funciones = Funciones()
    private var pedidosController = PedidosController()
    private var inventarioController = InventarioController()
    private var visitaController = VisitaController()
    private lateinit var preferencias: SharedPreferences

    private val instancia = "CONFIG_SERVIDOR"

    // REFACTORIZACIÓN MVVM: ViewModel centralizado
    private lateinit var viewModel: DetallePedidoViewModel
    private lateinit var adapterDetalle: PedidoDetalleAdapter
    private var listaSucursalesFull: List<ClienteSucursalEntity> = emptyList()

    private var enviandoPedido = false
    private var guardandoPedido = false
    private var FacturaExportacion = false
    private var idPedidoServidor = 0
    private var infoCliente : Cliente? = null

    private lateinit var binding: ActivityDetallepedidoBinding
    private var P_Imprimir_TK_Venta: Boolean = false
    private var cantidadItemsPedido : Int = 0
    private var limiteItemPedido : Int = 0
    private var isProcessing = false
    private var inventarioTiempoReal: Boolean = false

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
        
        // GPS
        val gpsParam = intento.getStringExtra("gps") ?: "0,0"
        if (gpsParam.contains(",")) {
            latitud = gpsParam.split(",")[0]
            longitud = gpsParam.split(",")[1]
        }

        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        
        setupViewModel()
        setupRecyclerView()

        idVendedor = preferencias.getInt("Idvendedor", 0)
        vendedor = preferencias.getString("Vendedor", "").toString()
        ip = preferencias.getString("ip", "").toString()
        puerto = preferencias.getInt("puerto", 0)
        P_Imprimir_TK_Venta = preferencias.getBoolean("P_Imprimir_TK_Venta", false)
        inventarioTiempoReal = preferencias.getBoolean("inventarioTiempoReal", false)
        val tipoVentaLocal = preferencias.getBoolean("tipoVentaLocal", false)

        // CONFIGURACIÓN INICIAL DE UI
        binding.tvDocumentoSeleccionado.visibility = View.GONE
        binding.tvTipoenvio.visibility = View.GONE
        binding.sinSucursal.visibility = View.GONE
        
        // MULTIPLES PEDIDOS: Solo visible si tipoVentaLocal es true
        binding.btnGestionPedidos.visibility = if (tipoVentaLocal) View.VISIBLE else View.GONE

        // NUEVO PROCESO: Cargar todo de forma asíncrona mediante el ViewModel
        setupViewModelObservers()
        viewModel.cargarSucursales(idcliente)
        viewModel.cargarInfoPedido(idpedido, idcliente, this)
        viewModel.observarDetallePedido(idpedido)
        viewModel.cargarCantidadItems(idpedido)
        viewModel.observarBorradores()

        binding.imbtnatras.setOnClickListener { menuPedidos() }
        
        binding.btnGestionPedidos.setOnClickListener {
            if (from == "visita") {
                actualizarNombreSiProcede()
                mostrarDialogoPedidosAbiertos()
            } else {
                Toast.makeText(this, "OPCIÓN DISPONIBLE SOLO EN NUEVO PEDIDO", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imgbtnadd.setOnClickListener {
            if (cantidadItemsPedido < limiteItemPedido) {
                actualizarNombreSiProcede()
                val clase = if(inventarioTiempoReal) InventarioTiempoReal::class.java else Inventario::class.java
                val intentAdd = Intent(this, clase).apply {
                    putExtra("idcliente", idcliente)
                    putExtra("nombrecliente", binding.txtCliente.text.toString())
                    putExtra("busqueda", true)
                    putExtra("idpedido", idpedido)
                    putExtra("visitaid", idvisita)
                    putExtra("codigo", codigo)
                    putExtra("idapi", idapi)
                    putExtra("facturaExportacion", FacturaExportacion)
                }
                startActivity(intentAdd)
                finish()
            } else {
                Toast.makeText(this, "LIMITE DE ITEMS ALCANZADO", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btncancelar.setOnClickListener {
            if(isProcessing) return@setOnClickListener
            deshabilitarOpciones()
            AlertaEliminar()
        }

        binding.btnenviar.setOnClickListener {
            if(isProcessing) return@setOnClickListener

            // VALIDACIÓN: El pedido debe tener al menos un producto para ser enviado
            if (cantidadItemsPedido <= 0) {
                Toast.makeText(this, "NO PUEDE ENVIAR UN PEDIDO SIN PRODUCTOS REGISTRADOS", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            deshabilitarOpciones()
            enviandoPedido = true
            guardandoPedido = false
            
            // Si terminosPedidos está vacío (nuevo pedido sin interacción), intentamos usar el del cliente
            val terminosAValidar = if (terminosPedidos.isNullOrEmpty()) infoCliente?.Terminos_cliente ?: "Contado" else terminosPedidos
            
            // REFACTORIZACIÓN MVVM: Validación de saldo asíncrona en el ViewModel para evitar ANR
            viewModel.verificarSaldoYProceder(this, idcliente, total, terminosAValidar ?: "")
        }

        binding.btnguardar.setOnClickListener {
            if(isProcessing) return@setOnClickListener

            // VALIDACIÓN: El pedido debe tener al menos un producto para ser guardado
            if (cantidadItemsPedido <= 0) {
                Toast.makeText(this, "NO PUEDE GUARDAR UN PEDIDO SIN PRODUCTOS REGISTRADOS", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            deshabilitarOpciones()
            if(cantidadItemsPedido <= limiteItemPedido){
                actualizarNombreSiProcede()
                guardandoPedido = true
                enviandoPedido = false
                // Al guardar localmente NO validamos crédito con el servidor, 
                // permitiendo que el vendedor almacene el pedido aunque no tenga señal.
                alertaPago(total)
            } else {
                habilitarOpciones()
                Toast.makeText(this, "EXCEDE EL LIMITE DE ITEMS", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnexportar.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    viewModel.obtenerDatosImpresion(idpedido)
                } else { permisosBluetooth() }
            } else { @Suppress("MissingPermission") viewModel.obtenerDatosImpresion(idpedido) }
        }

        binding.btnInvalidar.setOnClickListener {
            if(isProcessing) return@setOnClickListener
            deshabilitarOpciones()
            mensajeInvalidarDTE(this, "¿Desea Invalidar este Pedido?", idPedidoServidor, idpedido)
        }

        setupSpinners()
    }

    private fun setupViewModel() {
        val db = AppDatabase.getInstance(this)
        val servidor = funciones.getServidor(preferencias.getString("ip", "") ?: "", preferencias.getInt("puerto", 0).toString(), this)
        val clientesApi = RetrofitCliente.obtenerApi<ClientesApi>(servidor, this)
        val clientesRepository = ClientesRepository(db.clienteDao(), clientesApi)
        val pedidosRepository = PedidosRepository(db.pedidosDao(), db.reporteDao())
        val settingsRepository = SettingsRepository(this)
        
        val factory = DetallePedidoViewModelFactory(
            GetSucursalesUseCase(clientesRepository), 
            ActualizarSucursalPedidoUseCase(clientesRepository, pedidosRepository), 
            ObtenerCantidadItemsUseCase(pedidosRepository),
            GetDetallePedidoFlowUseCase(pedidosRepository),
            CalcularTotalesFiscalesUseCase(),
            ActualizarTotalesFiscalesUseCase(pedidosRepository),
            EnviarPedidoUseCase(pedidosRepository, this),
            EliminarPedidoUseCase(pedidosRepository),
            GetTicketDataUseCase(pedidosRepository, clientesRepository, settingsRepository),
            GetPedidosBorradoresUseCase(pedidosRepository),
            CrearPedidoUseCase(pedidosRepository, clientesRepository),
            ActualizarNombreClienteUseCase(pedidosRepository)
        )
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[DetallePedidoViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapterDetalle = PedidoDetalleAdapter(this) { i ->
            if (from == "visita") {
                val data = adapterDetalle.currentList[i]
                val intentAdd = Intent(this, Producto_agregar::class.java).apply {
                    putExtra("idpedidodetalle", data.Id)
                    putExtra("idpedido", idpedido)
                    putExtra("idcliente", idcliente)
                    putExtra("nombrecliente", binding.txtCliente.text.toString())
                    putExtra("idproducto", data.Id_producto)
                    putExtra("proviene", "editar")
                    putExtra("total_param", data.Total_iva)
                    putExtra("facturaExportacion", FacturaExportacion)
                }
                startActivity(intentAdd)
                finish()
            }
        }
        binding.reciclerdetalle.apply {
            layoutManager = LinearLayoutManager(this@Detallepedido)
            adapter = adapterDetalle
        }
    }

    private fun setupViewModelObservers() {
        viewModel.sucursales.observe(this) { sucursales ->
            listaSucursalesFull = sucursales
            
            // Si ya hay una sucursal seleccionada en el pedido, la mostramos en el spinner (visual)
            if (nombreSucursalPedido != null && nombreSucursalPedido != "") {
                val listVisual = listOf(nombreSucursalPedido!!)
                val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, listVisual)
                binding.spSucursal.adapter = adaptador
                binding.spSucursal.setSelection(0)
            } else {
                val listVisual = listOf("-- TOQUE PARA SELECCIONAR SUCURSAL --")
                val adaptador = ArrayAdapter(this, android.R.layout.simple_spinner_item, listVisual)
                binding.spSucursal.adapter = adaptador
            }

            // REFACTORIZACIÓN MVVM: Solo manejamos visibilidad automática si NO estamos consultando un pedido
            if (from != "ver") {
                if (sucursales.isEmpty()) {
                    binding.lySucursal.visibility = View.GONE
                    binding.tvsucursal.visibility = View.GONE
                    binding.sinSucursal.visibility = View.GONE
                } else {
                    binding.lySucursal.visibility = View.VISIBLE
                    binding.tvsucursal.visibility = View.VISIBLE
                    binding.sinSucursal.visibility = View.GONE
                }
                validarSelecciones()
            }
        }

        viewModel.cantidadItems.observe(this) { cantidad ->
            cantidadItemsPedido = cantidad
            binding.cantidadItems.text = "CANT. ITEMS: $cantidadItemsPedido"
        }

        viewModel.detallePedido.observe(this) { lista ->
            adapterDetalle.submitList(lista)
        }

        viewModel.totalesFiscales.observe(this) { resultado ->
            binding.txtSumas.text = String.format("%.2f", resultado.sumas)
            binding.txtIva.text = String.format("%.2f", resultado.iva)
            binding.txtIvaPerci.text = String.format("%.2f", resultado.ivaPerci)
            binding.txttotal.text = String.format("%.2f", resultado.totalFinal)
            total = resultado.totalFinal.toFloat()
        }

        viewModel.infoPedido.observe(this) { info ->
            info?.let {
                tipoDocumento = it.Tipo_documento ?: "FC"
                terminosPedidos = it.Terminos ?: ""
                nombreSucursalPedido = it.Nombre_sucursal ?: ""
                idPedidoServidor = it.Id_pedido_sistema ?: 0
                
                // REFACTORIZACIÓN MVVM: Restauramos la visualización de la fecha de creación
                if (!it.Fecha_creado.isNullOrEmpty()) {
                    // Si viene en formato SQLite (yyyy-MM-dd HH:mm:ss), intentamos mostrarlo más amigable
                    val fechaFormateada = try {
                        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                        val date = inputFormat.parse(it.Fecha_creado!!)
                        date?.let { d -> outputFormat.format(d) } ?: it.Fecha_creado
                    } catch (e: Exception) {
                        it.Fecha_creado
                    }
                    binding.fechaCreacion.text = fechaFormateada
                }

                validarProcesoPedidosUI(it)
                actualizarSeleccionesSpinners()
                if (from == "visita") validarSelecciones()
            }
        }

        viewModel.infoCliente.observe(this) { info ->
            infoCliente = info
            codigo = info?.Codigo ?: ""
            terminosDelCliente(info?.Terminos_cliente ?: "Contado")
            
            // REFACTORIZACIÓN MULTIPLES PEDIDOS: Si el código es 01, permitimos editar el nombre
            if (from == "visita" && codigo == "01") {
                binding.txtCliente.isEnabled = true
            } else if (from != "ver") {
                binding.txtCliente.isEnabled = false
            }
        }

        viewModel.validacionSaldo.observe(this) { resultado ->
            resultado?.let {
                if (it.esValido) {
                    // Solo procedemos al envío, ya que el guardado local no dispara esta validación
                    procederAlEnvio()
                } else { 
                    habilitarOpciones()
                    funciones.mostrarAlerta(it.mensajeError ?: "ERROR DE SALDO", this, binding.lienzo) 
                }
                viewModel.resetValidacionSaldo()
            }
        }

        viewModel.envioExitoso.observe(this) { exito ->
            exito?.let {
                if (it) { 
                    descargarInventario()
                    Toast.makeText(this, "PEDIDO ENVIADO EXITOSAMENTE", Toast.LENGTH_SHORT).show()
                    pedidoEnviado() 
                } 
                else { habilitarOpciones(); funciones.mostrarAlerta("ERROR AL ENVIAR EL PEDIDO", this, binding.lienzo) }
                viewModel.resetEnvioStatus()
            }
        }

        viewModel.eliminacionExitosa.observe(this) { exito ->
            exito?.let {
                if (it) {
                    val tipoVentaLocal = preferencias.getBoolean("tipoVentaLocal", false)
                    // MULTIPLES PEDIDOS: Si hay más borradores y estamos en modo local, cambiamos al siguiente
                    val listaBorradores = viewModel.pedidosBorradores.value ?: emptyList()
                    val siguiente = listaBorradores.firstOrNull { p -> p.id != idpedido }
                    
                    if (tipoVentaLocal && siguiente != null) {
                        enviandoPedido = false // Asegurar que no se muestre animación de envío
                        viewModel.cambiarPedidoActivo(siguiente, this)
                        Toast.makeText(this, "PEDIDO ELIMINADO, MOSTRANDO SIGUIENTE", Toast.LENGTH_SHORT).show()
                        habilitarOpciones()
                    } else {
                        // Era el último/único o no estamos en modo local, salimos
                        finalizarVistaActual()
                    }
                } else {
                    habilitarOpciones()
                    funciones.mostrarAlerta("ERROR AL ELIMINAR EL PEDIDO", this, binding.lienzo)
                }
                viewModel.resetEliminacionStatus()
            }
        }

        viewModel.ticketData.observe(this) { data ->
            data?.let {
                imprimirTicketProcesado(it)
                viewModel.resetTicketData()
            }
        }

        viewModel.pedidosBorradores.observe(this) { lista ->
            // Se actualiza la lista en memoria para el diálogo si estuviera abierto
            actualizarListaBorradoresDialogo(lista)
        }

        viewModel.pedidoCambiadoContexto.observe(this) { pedido ->
            pedido?.let {
                idpedido = it.id
                idcliente = it.idCliente
                nombre = it.nombreCliente
                codigo = "" // El código se suele usar en la búsqueda, aquí ya tenemos el nombre e ID
                
                // Actualizar UI
                binding.txtCliente.setText(it.nombreCliente)
                Toast.makeText(this, "CAMBIADO A: ${it.nombreCliente}", Toast.LENGTH_SHORT).show()
                
                viewModel.resetPedidoCambiadoContexto()
            }
        }

        viewModel.cargando.observe(this) { cargando ->
            if (cargando) {
                if (enviandoPedido) alerta?.Enviando() else alerta?.Cargando()
            } else {
                // MULTIPLES PEDIDOS: Aseguramos que el diálogo se oculte ANTES de cualquier cambio de pedido
                alerta?.dismisss()
            }
        }
    }

    private fun procederAlEnvio() {
        if (cantidadItemsPedido <= limiteItemPedido) {
            if (cantidadItemsPedido > 0) {
                actualizarNombreSiProcede()
                enviandoPedido = true
                val facturacionLocal = preferencias.getBoolean("tipoVentaLocal", false)
                if (!facturacionLocal) alertaPago(total) else envioAlerta()
            }
        } else { habilitarOpciones(); Toast.makeText(this, "EXCEDE LIMITE DE ITEMS", Toast.LENGTH_SHORT).show() }
    }

    private fun actualizarNombreSiProcede() {
        if (codigo == "01") {
            val nombreIngresado = binding.txtCliente.text.toString()
            if (nombreIngresado.isNotEmpty()) {
                viewModel.actualizarNombreCliente(idpedido, nombreIngresado)
                nombre = nombreIngresado // Actualizar variable local también
            }
        }
    }

    private fun setupSpinners() {
        val documentos = funciones.documentosDeFacturacion(this)
        val adapterDoc = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, documentos)
        binding.spDocumento.adapter = adapterDoc

        // Usar una vista transparente sobre el spinner para capturar el clic de forma confiable
        binding.spSucursal.isEnabled = false
        binding.viewSucursalClick.setOnClickListener {
            if (from == "visita") {
                mostrarDialogoSucursales()
            }
        }

        binding.spTipoEnvio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val env = p?.getItemAtPosition(pos).toString()
                terminosPedidos = if(env == "CONTADO") "Contado" else "Credito"
                pedidosController.actualizarTerminosEnvio(terminosPedidos!!, idpedido, this@Detallepedido)
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        binding.spDocumento.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val doc = p?.getItemAtPosition(pos).toString()
                tipoDocumento = when(doc){
                    "FACTURA" -> "FC"
                    "CREDITO FISCAL" -> "CF"
                    "RECIBO" -> "RC"
                    "REMISIÓN" -> "RE"
                    else -> "FC"
                }
                limiteItemPedido = preferencias.getInt(when(tipoDocumento){
                    "FC" -> "numItemFactura"
                    "CF" -> "numItemCreFiscal"
                    "RC" -> "numItemRecibo"
                    "RE" -> "numItemRemision"
                    else -> "numItemFactura"
                }, 0)
                pedidosController.updateTipoDocumento(tipoDocumento, idpedido, this@Detallepedido)
                pedidosController.actualizarTotalesPedido(this@Detallepedido, idpedido, true)
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun actualizarSeleccionesSpinners() {
        // REFACTORIZACIÓN MVVM: Si el pedido es nuevo y no tiene términos aún, usamos los del cliente
        val terminosAFijar = if (terminosPedidos.isNullOrEmpty() || terminosPedidos == "") {
            infoCliente?.Terminos_cliente ?: "Contado"
        } else {
            terminosPedidos
        }

        val sEnv = if (terminosAFijar.contentEquals("Contado", ignoreCase = true)) 0 else 1
        
        if (binding.spTipoEnvio.adapter != null && sEnv < binding.spTipoEnvio.adapter.count) {
            binding.spTipoEnvio.setSelection(sEnv, true)
        }
        
        val sDoc = when(tipoDocumento){
            "FC" -> 0
            "CF" -> 1
            "RC" -> 2
            "RE" -> 3
            else -> 0
        }
        if (binding.spDocumento.adapter != null && sDoc < binding.spDocumento.adapter.count) {
            binding.spDocumento.setSelection(sDoc, true)
        }
    }

    private fun terminosDelCliente(terminos: String){
        val lista = if(terminos == "Contado") listOf("CONTADO") else listOf("CONTADO", "CREDITO")
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, lista)
        binding.spTipoEnvio.adapter = adapter
        
        // Sincronizamos la selección después de cargar el adaptador
        actualizarSeleccionesSpinners()
    }

    private fun validarProcesoPedidosUI(pedido: com.example.acae30.modelos.Pedidos) {
        binding.txtCliente.setText(nombre)
        
        // REFACTORIZACIÓN MVVM: Centralización de lógica de visibilidad según estado del pedido
        when(from) {
            "ver" -> {
                // Título descriptivo según el estado
                binding.tvTituloPedido.text = if (pedido.Enviado == 1) "PEDIDO ENVIADO" else "PEDIDO ALMACENADO"
                
                // Mostrar sucursal como texto estilizado
                if (nombreSucursalPedido.isNullOrEmpty()) {
                    binding.lySucursal.visibility = View.GONE
                    binding.tvsucursal.visibility = View.GONE
                    binding.sinSucursal.visibility = View.GONE
                } else {
                    // Mantenemos el contenedor visible para el estilo pero ocultamos interacciones
                    binding.lySucursal.visibility = View.VISIBLE
                    binding.tvsucursal.visibility = View.VISIBLE
                    binding.spSucursal.visibility = View.GONE
                    binding.viewSucursalClick.visibility = View.GONE
                    
                    binding.sinSucursal.visibility = View.VISIBLE
                    binding.sinSucursal.text = nombreSucursalPedido
                }

                // Deshabilitar edición general
                binding.btnAgregarComentario.visibility = View.GONE
                binding.imgbtnadd.visibility = View.GONE
                binding.btnguardar.visibility = View.GONE
                binding.btncancelar.visibility = View.GONE
                
                // BLOQUEO DE CONTROLES: Se ocultan los Spinners y se muestran TextViews con la info enviada
                binding.spDocumento.visibility = View.GONE
                binding.spTipoEnvio.visibility = View.GONE
                
                binding.tvDocumentoSeleccionado.visibility = View.VISIBLE
                binding.tvTipoenvio.visibility = View.VISIBLE
                
                // Seteamos la información con la que se envió/almacenó el pedido
                binding.tvTipoenvio.text = terminosPedidos?.uppercase()
                binding.tvDocumentoSeleccionado.text = when(tipoDocumento) {
                    "FC" -> "FACTURA"
                    "CF" -> "CREDITO FISCAL"
                    "RC" -> "RECIBO"
                    "RE" -> "REMISIÓN"
                    else -> "FACTURA"
                }

                binding.imbtnatras.visibility = View.VISIBLE

                // Casos específicos de envío y DTE
                if (pedido.Enviado == 1) {
                    binding.btnenviar.visibility = View.GONE
                    
                    // Mostrar Invalidar según reglas originales
                    if (pedido.pedido_dte == 1) {
                        binding.btnInvalidar.visibility = View.VISIBLE
                    } else if (pedido.Tipo_documento == "RC") {
                        binding.btnInvalidar.visibility = View.VISIBLE
                    } else {
                        binding.btnInvalidar.visibility = View.GONE
                    }
                    
                } else if (pedido.Enviado == 0 && pedido.Cerrado == 1) {
                    // Pedido almacenado localmente pero no enviado
                    binding.btnenviar.visibility = View.VISIBLE
                    binding.btnenviar.isEnabled = true
                    binding.btnenviar.setBackgroundResource(R.drawable.border_btnactualizar)
                    binding.btnInvalidar.visibility = View.GONE
                }

                // Lógica de Impresión (Exportar)
                if (P_Imprimir_TK_Venta && pedido.pedido_dte_error != 2) {
                    binding.btnexportar.visibility = View.VISIBLE
                } else {
                    binding.btnexportar.visibility = View.GONE
                }

                permisosBluetooth()
            }
            "visita" -> {
                // Flujo de creación de nuevo pedido
                binding.tvTituloPedido.text = "NUEVO PEDIDO"
                
                // MULTIPLES PEDIDOS: Ocultar comentario si es venta local
                val tipoVentaLocal = preferencias.getBoolean("tipoVentaLocal", false)
                binding.btnAgregarComentario.visibility = if (tipoVentaLocal) View.GONE else View.VISIBLE
                
                binding.imgbtnadd.visibility = View.VISIBLE
                binding.btnguardar.visibility = View.VISIBLE
                binding.btncancelar.visibility = View.VISIBLE
                
                // En modo visita (nuevo), los Spinners están visibles y habilitados
                // La visibilidad de lySucursal se gestiona dinámicamente en el observador de sucursales
                binding.spDocumento.visibility = View.VISIBLE
                binding.spTipoEnvio.visibility = View.VISIBLE
                binding.sinSucursal.visibility = View.GONE
                binding.tvDocumentoSeleccionado.visibility = View.GONE
                binding.tvTipoenvio.visibility = View.GONE

                binding.btnenviar.visibility = View.VISIBLE
                // REFACTORIZACIÓN: Se oculta el botón atrás para obligar a usar Eliminar Pedido
                binding.imbtnatras.visibility = View.GONE 
                binding.btnexportar.visibility = View.GONE
                binding.btnInvalidar.visibility = View.GONE

                if (codigo == "01") {
                    binding.txtCliente.isEnabled = true
                }
            }
        }
    }

    private fun AlertaEliminar() {
        Dialog(this).apply {
            setCancelable(false)
            setContentView(R.layout.alert_eliminar)
            findViewById<Button>(R.id.btneliminar).setOnClickListener {
                viewModel.eliminarPedido(idpedido)
                dismiss()
            }
            findViewById<Button>(R.id.btncancelar).setOnClickListener { habilitarOpciones(); dismiss() }
            show()
        }
    }

    private fun updateSharedPreferencesFinalizarVisita(){
        preferencias.edit { remove("visita"); remove("busqueda") }
    }

    private fun menuPedidos(){
        startActivity(Intent(this, Pedido::class.java))
        finish()
    }

    private fun regresarVisita(){
        val intentVisita = Intent(this, Visita::class.java).apply {
            putExtra("idcliente", idcliente)
            putExtra("nombrecliente", nombre)
            putExtra("visitaid", idvisita)
            putExtra("codigo", codigo)
            putExtra("idapi", idapi)
        }
        startActivity(intentVisita)
        finish()
    }

    private fun alertaPago(total: Float){
        Dialog(this).apply {
            setContentView(R.layout.vista_cobro)
            setCancelable(false)
            val etTotal = findViewById<TextInputEditText>(R.id.txtTotalPago)
            etTotal.setText(String.format("$%.2f", total))
            val etCambio = findViewById<TextInputEditText>(R.id.txtCambioPago)
            val etPago = findViewById<TextInputEditText>(R.id.txtEfectivoPago)
            val etOrden = findViewById<TextInputEditText>(R.id.txtNumeroOrden)
            val spForma = findViewById<Spinner>(R.id.spFormaPago)

            // Contenedores opcionales para otros métodos de pago
            val lyCheque = findViewById<LinearLayout>(R.id.lyContenedorCheque)
            val lyTarjeta = findViewById<LinearLayout>(R.id.lyContenedorTarjeta)
            val lyDeposito = findViewById<LinearLayout>(R.id.lyContenedorDeposito)

            spForma.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                    val seleccion = spForma.selectedItem.toString()
                    lyCheque.visibility = if (seleccion == "CHEQUE") View.VISIBLE else View.GONE
                    lyTarjeta.visibility = if (seleccion == "TARJETA") View.VISIBLE else View.GONE
                    lyDeposito.visibility = if (seleccion == "DEPOSITO") View.VISIBLE else View.GONE
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
            
            etPago.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(pago: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val pVal = pago?.toString()?.toFloatOrNull() ?: 0f
                    etCambio.setText(String.format("%.2f", pVal - total))
                }
                override fun afterTextChanged(p0: Editable?) {}
            })

            findViewById<Button>(R.id.btnaceptar).setOnClickListener {
                val nOrden = etOrden.text.toString()
                
                // REIMPLEMENTACIÓN DE VALIDACIÓN: NRC 1937 o 193-7 requiere Número de Orden
                if ((infoCliente?.Nrc == "193-7" || infoCliente?.Nrc == "1937") && nOrden.isEmpty()) {
                    Toast.makeText(this@Detallepedido, "EL NÚMERO DE ORDEN ES OBLIGATORIO", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val forma = spForma.selectedItem.toString()
                val pagoMonto = etPago.text.toString().toFloatOrNull() ?: 0f

                CoroutineScope(Dispatchers.IO).launch {
                    // REFACTORIZACIÓN MULTIPLES PEDIDOS: Si el código es 01, guardamos el nombre ingresado
                    if (codigo == "01") {
                        pedidosController.actualizarNombreClientePedido(this@Detallepedido, binding.txtCliente.text.toString(), idpedido)
                    }

                    // Se obtienen los valores de los campos adicionales si existen
                    val bancoCheque = findViewById<TextInputEditText>(R.id.tvBanco).text.toString()
                    val cuentaCheque = findViewById<TextInputEditText>(R.id.tvNumCuentaCheque).text.toString()
                    val numCheque = findViewById<TextInputEditText>(R.id.tvNumCheque).text.toString()
                    
                    val tarjeta = findViewById<TextInputEditText>(R.id.tvTarjeta).text.toString()
                    val nombreTarjeta = findViewById<TextInputEditText>(R.id.tvNombreTarjeta).text.toString()
                    val numTarjeta = findViewById<TextInputEditText>(R.id.tvNumTarjeta).text.toString()
                    
                    val bancoDep = findViewById<TextInputEditText>(R.id.tvDeposito).text.toString()
                    val cuentaDep = findViewById<TextInputEditText>(R.id.tvNumCuentaDeposito).text.toString()
                    val numDep = findViewById<TextInputEditText>(R.id.tvNumDeposito).text.toString()

                    pedidosController.actualizarPagoCambioPedido(this@Detallepedido, idpedido, 
                        pagoMonto, pagoMonto - total,
                        if(forma == "EFECTIVO") pagoMonto else 0f,
                        if(forma == "CHEQUE") pagoMonto else 0f,
                        if(forma == "TARJETA") pagoMonto else 0f,
                        if(forma == "DEPOSITO") pagoMonto else 0f,
                        nOrden, 
                        bancoCheque, cuentaCheque, numCheque,
                        tarjeta, nombreTarjeta, numTarjeta,
                        bancoDep, cuentaDep, numDep,
                        forma)
                }
                dismiss()
                if(enviandoPedido) envioAlerta() else if(guardandoPedido) guardarPedido()
            }
            findViewById<Button>(R.id.btncancelar).setOnClickListener { habilitarOpciones(); dismiss() }
            show()
        }
    }

    private fun envioAlerta(){
        Dialog(this).apply {
            setCancelable(false)
            setContentView(R.layout.dialog_cancelar)
            findViewById<TextView>(R.id.tvTitulo).text = "INFORMACIÓN"
            findViewById<TextView>(R.id.tvMensaje).text = "¿DESEA ENVIAR EL PEDIDO?"
            findViewById<TextView>(R.id.tvUpdate).apply {
                text = "ACEPTAR"
                setOnClickListener { dismiss(); viewModel.enviarPedido(idpedido) }
            }
            findViewById<TextView>(R.id.tvCancel).setOnClickListener { enviandoPedido = false; habilitarOpciones(); dismiss() }
            show()
        }
    }

    private fun pedidoEnviado(){
        val tipoVentaLocal = preferencias.getBoolean("tipoVentaLocal", false)
        val listaBorradores = viewModel.pedidosBorradores.value ?: emptyList()
        val siguiente = listaBorradores.firstOrNull { p -> p.id != idpedido }

        // Resetear flag de envío para que el siguiente pedido muestre cargando normal si fuera necesario
        enviandoPedido = false

        if (tipoVentaLocal && siguiente != null) {
            viewModel.cambiarPedidoActivo(siguiente, this)
            habilitarOpciones()
        } else {
            finalizarVistaActual()
        }
    }

    private fun finalizarVistaActual() {
        val visita = if (idvisita > 0) visitaController.obtenerVisitaPorID(idvisita, this@Detallepedido) else null
        if(visita != null && visita.Abierta) regresarVisita() else { updateSharedPreferencesFinalizarVisita(); menuPedidos() }
    }

    private fun guardarPedido(){
        descargarInventario()
        pedidosController.actualizarEstadoAlGuardar(idpedido, this, binding.lienzo)
        alerta?.pedidoGuardado()
        Timer().schedule(2000){ runOnUiThread { habilitarOpciones(); alerta?.dismisss(); pedidoEnviado() } }
    }

    private fun descargarInventario(){
        if(preferencias.getBoolean("Hoja_carga_inventario_app", false)){
            CoroutineScope(Dispatchers.IO).launch { inventarioController.descargarProductosInventario(idpedido, this@Detallepedido) }
        }
    }

    private fun validarSelecciones(){
        // Si el cliente tiene sucursales registradas, es obligatorio seleccionar una.
        // Si no tiene sucursales, permitimos continuar.
        val tieneSucursales = listaSucursalesFull.isNotEmpty()
        val sucursalSeleccionada = !nombreSucursalPedido.isNullOrEmpty() && 
                                  nombreSucursalPedido != "-- SELECCIONE UNA SUCURSAL --" && 
                                  nombreSucursalPedido != "-- TOQUE PARA SELECCIONAR SUCURSAL --"
        
        val e = if (tieneSucursales) sucursalSeleccionada else true
        
        binding.btnguardar.isEnabled = e
        binding.btnenviar.isEnabled = e
        binding.btnenviar.setBackgroundResource(if(e) R.drawable.border_btnactualizar else R.drawable.border_btndisable)
        binding.btnguardar.setBackgroundResource(if(e) R.drawable.border_btnenviar else R.drawable.border_btndisable)
    }

    private var adapterBorradores: PedidosBorradoresAdapter? = null
    
    private fun mostrarDialogoPedidosAbiertos() {
        val dialog = Dialog(this)
        val vista = LayoutInflater.from(this).inflate(R.layout.dialog_pedidos_abiertos, null)
        dialog.setContentView(vista)
        dialog.setCancelable(true)

        val rv = vista.findViewById<RecyclerView>(R.id.rvPedidosBorradores)
        val btnNuevo = vista.findViewById<Button>(R.id.btnNuevoPedido)
        val btnCerrar = vista.findViewById<Button>(R.id.btnCerrar)

        adapterBorradores = PedidosBorradoresAdapter(idpedido) { seleccionado ->
            viewModel.cambiarPedidoActivo(seleccionado, this)
            dialog.dismiss()
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapterBorradores
        
        // Cargar lista inicial
        viewModel.pedidosBorradores.value?.let { adapterBorradores?.submitList(it) }

        btnNuevo.setOnClickListener {
            val intentSeleccion = Intent(this@Detallepedido, Clientes::class.java)
            startActivity(intentSeleccion)
            finish()
            dialog.dismiss()
        }

        btnCerrar.setOnClickListener { dialog.dismiss() }

        dialog.show()
        dialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun actualizarListaBorradoresDialogo(lista: List<PedidosEntity>) {
        adapterBorradores?.submitList(lista)
    }

    private fun mostrarDialogoSucursales() {
        val dialog = Dialog(this)
        val vista = LayoutInflater.from(this).inflate(R.layout.dialog_buscar_sucursal, null)
        dialog.setContentView(vista)
        dialog.setCancelable(true)
        
        val etBuscar = vista.findViewById<TextInputEditText>(R.id.etBuscarSucursal)
        val rvSucursales = vista.findViewById<RecyclerView>(R.id.rvSucursales)
        val btnCancelar = vista.findViewById<Button>(R.id.btnCancelar)

        val adaptador = SucursalBusquedaAdapter(listaSucursalesFull) { sucursal ->
            nombreSucursalPedido = sucursal.nombreSucursal
            // Actualizar visualmente el spinner
            val listVisual = listOf(nombreSucursalPedido!!)
            val adapterVisual = ArrayAdapter(this, android.R.layout.simple_spinner_item, listVisual)
            binding.spSucursal.adapter = adapterVisual
            binding.spSucursal.setSelection(0)
            
            // Persistir selección y validar botones
            validarSelecciones()
            viewModel.seleccionarSucursal(idpedido, idcliente, nombreSucursalPedido!!)
            
            dialog.dismiss()
        }

        rvSucursales.layoutManager = LinearLayoutManager(this)
        rvSucursales.adapter = adaptador

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adaptador.filter.filter(s)
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        btnCancelar.setOnClickListener { dialog.dismiss() }
        
        dialog.show()
        // Ajustar tamaño del diálogo
        dialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun habilitarOpciones(){ isProcessing = false; binding.apply { btnenviar.isEnabled = true; btncancelar.isEnabled = true; btnguardar.isEnabled = true } }
    private fun deshabilitarOpciones(){ isProcessing = true; binding.apply { btnenviar.isEnabled = false; btncancelar.isEnabled = false; btnguardar.isEnabled = false } }

    private fun mensajeInvalidarDTE(context: Context, mensaje: String, idPServidor : Int, idPed: Int){
        AlertDialog.Builder(context).setTitle("INVALIDAR DTE").setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { d, _ -> 
                CoroutineScope(Dispatchers.IO).launch { pedidosController.obtenerDocumentosTransmitidosInvalidados(idPServidor, context, idPed) }
                habilitarOpciones(); d.dismiss() 
            }
            .setNegativeButton("CANCELAR"){ d, _ -> habilitarOpciones(); d.dismiss() }
            .setCancelable(false).setIcon(R.drawable.ic_information).show()
    }

    /**
     * REFACTORIZACIÓN MVVM & CLEAN ARCHITECTURE: Nuevo método de impresión desacoplado.
     * 1. El ViewModel recolecta datos de Room (GetTicketDataUseCase).
     * 2. La Activity solo maneja la conexión física y el renderizado final.
     * 3. Se delega el formato a TicketFormatter de forma dinámica.
     */
    @android.annotation.SuppressLint("MissingPermission")
    private fun imprimirTicketProcesado(data: com.example.acae30.domain.models.TicketData) {
        try {
            val settings = SettingsRepository(this)
            val tipoImpresora = settings.getTipoImpresora()
            val formatter = com.example.acae30.Utilidades.TicketFormatter()
            
            // Preparar Logo
            val prefs = getSharedPreferences("MisImagenes", MODE_PRIVATE)
            val filePath = prefs.getString("imagenFile", null)
            val logoOriginal: Bitmap = if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)
                else BitmapFactory.decodeResource(resources, R.drawable.nologo)
            } else {
                BitmapFactory.decodeResource(resources, R.drawable.nologo)
            }

            when(tipoImpresora) {
                "BT" -> {
                    val btConnection = BluetoothPrintersConnections.selectFirstPaired()
                    if (btConnection != null) {
                        // Bluetooth suele soportar 32 caracteres por línea
                        val printer = EscPosPrinter(btConnection, 160, 48f, 32)
                        val ticket = formatter.formatTicket(printer, data, logoOriginal, 32)
                        printer.printFormattedText(ticket)
                    } else {
                        Toast.makeText(this, "No se encontró impresora Bluetooth", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    // Impresión Integrada
                    val impresorIntegrado = settings.getImpresorIntegrado()
                    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device : BluetoothDevice? = bluetoothAdapter.bondedDevices.firstOrNull { 
                        it.name.contains(impresorIntegrado) 
                    }

                    if(device != null) {
                        val connection = BluetoothConnection(device)
                        connection.connect()
                        // Impresoras integradas suelen ser de 58mm (28 caracteres aprox)
                        val printer = EscPosPrinter(connection, 160, 48f, 28)
                        val ticket = formatter.formatTicket(printer, data, logoOriginal, 28)
                        printer.printFormattedText(ticket)
                    } else {
                        Toast.makeText(this, "No se encontró impresora Integrada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "[IMPRESION] ERROR AL IMPRIMIR EL COMPROBANTE ")
            Toast.makeText(this, "Error al imprimir: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /* 
     * CÓDIGO ANTIGUO (Restaurado): Se mantiene comentado para referencia y comparación histórica.
     * Este código utilizaba controladores directos y lógica de formateo mezclada con la UI.
     * 
    @android.annotation.SuppressLint("MissingPermission")
    private fun imprimirRecibo() { ... }
    ...
     */
    //FUNCION PARA DETERMINAR LA CONEXION DE LA IMPRESORA
    /*
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
            //e.printStackTrace()
            //Toast.makeText(this, "Error al imprimir: ${e.message}", Toast.LENGTH_LONG).show()

            Timber.e(e, "[IMPRESION] ERROR AL IMPRIMIR EL COMPROBANTE ")

            Toast.makeText(
                this,
                "${e.javaClass.simpleName}: ${e.message ?: "Sin mensaje"}",
                Toast.LENGTH_LONG
            ).show()

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
        //val infoCliente = clientesController.obtenerInformacionCliente(this@Detallepedido, idcliente)

        val usbManager = getSystemService(USB_SERVICE) as UsbManager

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
                BitmapFactory.decodeResource(resources, com.example.acae30.R.drawable.nologo)
            }
        } else {
            BitmapFactory.decodeResource(resources, com.example.acae30.R.drawable.nologo)
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
        //var total = 0f
        val detalleBuilder = StringBuilder()

        // ===============================
        // Concatenando a la Descripcion, la Cantidad, Codigo de Barra y Bonificados
        // ===============================
        listaDetalle.forEach { item ->
            val descripcionPartes = if(item.Bonificado!! > 0){
                if(infoCliente!!.Nrc == "193-7" || infoCliente!!.Nrc == "1937"){
                    dividirDescripcion(
                        (item.Codigo_de_barra + " - " + item.Cantidad.toString() + " " + item.Descripcion + " - BONIFICADOS: " + item.Bonificado) ?: ""
                    )
                }else{
                    dividirDescripcion(
                        (item.Cantidad.toString() + " " + item.Descripcion + " - BONIFICADOS: " + item.Bonificado) ?: ""
                    )
                }
            }else{
                if(infoCliente!!.Nrc == "193-7" || infoCliente!!.Nrc == "1937"){
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

            //total += item.Total_iva ?: 0f
        }

        val totalFacturado = total - infoPedido.Iva_Percibido!!

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
                .append("[C]${infoCliente!!.Cliente}\n")
                .append("[L]DOCUMENTO: \n")
                .append("[C]${infoCliente!!.Nit} / ${infoCliente!!.Dui} \n")
                .append("[L]N.R.C: ${infoCliente!!.Nrc} \n")
                .append("[L]ACTIVIDAD ECONOMICA: \n")
                .append("[C]$giroCliente \n")
                .append("[L]NOMBRE SUCURSAL: \n")
                .append("[C]${infoPedido.Nombre_sucursal}\n")
                .append("[L]DIRECCION: \n")
                .append("[C]$direccionCliente\n")
                .append("[L]--------------------------------\n")
                .append("[C]DOCUMENTO ELECTRONICO\n")
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
                .append("[L]TOTAL: [R] $ ${String.format("%.2f", totalFacturado)} \n")
                .append("[L]VENDIDO POR: $vendedor\n")
                .append("[L]FECHA: $fecha \n")
                .append("[C]¡GRACIAS POR SU COMPRA! \n")
                .append("[C]<b>$textoPieFormateado</b>\n")
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
                .append("[C]${infoCliente!!.Cliente}\n")
                .append("[L]DOCUMENTO: \n")
                .append("[C]${infoCliente!!.Nit} / ${infoCliente!!.Dui} \n")
                .append("[L]N.R.C: ${infoCliente!!.Nrc} \n")
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
                .append("[L]TOTAL: [R] $ ${String.format("%.2f", totalFacturado)} \n")
                .append("[L]VENDIDO POR: $vendedor\n")
                .append("[L]FECHA: $fecha \n")
                .append("[C]¡GRACIAS POR SU COMPRA! \n")
                .append("[C]<b>$textoPieFormateado</b>\n")
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
        //val infoCliente = clientesController.obtenerInformacionCliente(this@Detallepedido, idcliente)

        //val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val impresorIntegrado = preferencias.getString("impresorIntegrado", "sinNombre")

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device : BluetoothDevice? = bluetoothAdapter.bondedDevices.firstOrNull {
            it.name.contains(impresorIntegrado.toString())
        }

        if(device != null){
            val connection = BluetoothConnection(device)

            connection.connect()

            val printer = EscPosPrinter(connection, 160, 48f, 28)


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
                    BitmapFactory.decodeResource(resources, com.example.acae30.R.drawable.nologo)
                }
            } else {
                BitmapFactory.decodeResource(resources, com.example.acae30.R.drawable.nologo)
            }

            // Redimensionar
            val logoRedimensionado = redimensionarLogo(logoOriginal, 384)

            val direccionFormateada = dividirEnLineas(direccion, 28)
            val empresaFormateada = dividirEnLineas(empresa, 28)
            val giroFormateada = dividirEnLineas(giro, 28)
            val textoPieFormateado = dividirEnLineas(textoPie, 28)
            val giroCliente = dividirEnLineas(infoCliente!!.dteGiro!!, 28)
            val direccionCliente = dividirEnLineas(infoPedido!!.Sucursal_Direccion!!,28)

            // ===============================
            // Formateando Datos Fiscales DTE
            // ===============================

            val codigoGeneracion = dividirEnLineas(infoPedido.dteCodigoGeneracion!!, 28)
            val numeroControl = dividirEnLineas(infoPedido.dteNumeroControl!!, 28)
            val selloRecepcion = dividirEnLineas(infoPedido.dteSelloRecibido!!, 28)

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

            val textoVerificacion = dividirEnLineas("Verificacion con $empresa",28)

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
            //var total = 0f
            val detalleBuilder = StringBuilder()

            // ===============================
            // Concatenando a la Descripcion, la Cantidad, Codigo de Barra y Bonificados
            // ===============================
            listaDetalle.forEach { item ->
                val descripcionPartes = if(item.Bonificado!! > 0){
                    if(infoCliente!!.Nrc == "193-7" || infoCliente!!.Nrc == "1937"){
                        dividirDescripcion(
                            (item.Codigo_de_barra + " - " + item.Cantidad.toString() + " " + item.Descripcion + " - BONIFICADOS: " + item.Bonificado) ?: ""
                        )
                    }else{
                        dividirDescripcion(
                            (item.Cantidad.toString() + " " + item.Descripcion + " - BONIFICADOS: " + item.Bonificado) ?: ""
                        )
                    }
                }else{
                    if(infoCliente!!.Nrc == "193-7" || infoCliente!!.Nrc == "1937"){
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

                //total += item.Total_iva ?: 0f
            }

            val totalFacturado = total - infoPedido.Iva_Percibido!!

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
                    .append("[L]------------------------------\n")
                    .append("[C]DATOS DEL CLIENTE\n")
                    .append("[L]------------------------------\n")
                    .append("[L]NOMBRE:\n")
                    .append("[C]${infoCliente!!.Cliente}\n")
                    .append("[L]DOCUMENTO: \n")
                    .append("[C]${infoCliente!!.Nit} / ${infoCliente!!.Dui} \n")
                    .append("[L]N.R.C: ${infoCliente!!.Nrc} \n")
                    .append("[L]ACTIVIDAD ECONOMICA: \n")
                    .append("[C]$giroCliente \n")
                    .append("[L]NOMBRE SUCURSAL: \n")
                    .append("[C]${infoPedido.Nombre_sucursal}\n")
                    .append("[L]DIRECCION: \n")
                    .append("[C]$direccionCliente\n")
                    .append("[L]------------------------------\n")
                    .append("[C]DOCUMENTO ELECTRONICO\n")
                    .append("[L]------------------------------\n")
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
                    .append("[L]------------------------------\n")
                    .append(qr)
                    .append("[L]------------------------------\n")
                    .append("[C]DETALLE DEL DOCUMENTO\n")
                    .append("[L]------------------------------\n")
                    .append(detalleBuilder.toString())
                    .append("[L]------------------------------\n")
                    .append("[L]SUB-TOTAL: [R] $ ${String.format("%.2f", infoPedido.Suma)} \n")
                    .append("[L]IVA: [R] $ ${String.format("%.2f", infoPedido.Iva)} \n")
                    .append("[L]IVA RET: [R] $ ${String.format("%.2f", infoPedido.Iva_Percibido)} \n")
                    .append("[L]TOTAL: [R] $ ${String.format("%.2f", totalFacturado)} \n")
                    .append("[L]VENDIDO POR: $vendedor\n")
                    .append("[L]FECHA: $fecha \n")
                    .append("[C]¡GRACIAS POR SU COMPRA! \n")
                    .append("[C]<b>$textoPieFormateado</b>\n")
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
                    .append("[L]------------------------------\n")
                    .append("[C]DATOS DEL CLIENTE\n")
                    .append("[L]------------------------------\n")
                    .append("[L]NOMBRE:\n")
                    .append("[C]${infoCliente!!.Cliente}\n")
                    .append("[L]DOCUMENTO: \n")
                    .append("[C]${infoCliente!!.Nit} / ${infoCliente!!.Dui} \n")
                    .append("[L]N.R.C: ${infoCliente!!.Nrc} \n")
                    .append("[L]ACTIVIDAD ECONOMICA: \n")
                    .append("[C]$giroCliente \n")
                    .append("[L]NOMBRE SUCURSAL: \n")
                    .append("[C]${infoPedido.Nombre_sucursal}\n")
                    .append("[L]DIRECCION: \n")
                    .append("[C]$direccionCliente\n")
                    .append("[L]TIPO DOCUMENTO:\n")
                    .append("[C]$documento \n")
                    .append("[L]------------------------------\n")
                    .append("[C]DETALLE DEL DOCUMENTO\n")
                    .append("[L]------------------------------\n")
                    .append(detalleBuilder.toString())
                    .append("[L]------------------------------\n")
                    .append("[L]SUB-TOTAL: [R] $ ${String.format("%.2f", infoPedido.Suma)} \n")
                    .append("[L]IVA: [R] $ ${String.format("%.2f", infoPedido.Iva)} \n")
                    .append("[L]IVA RET: [R] $ ${String.format("%.2f", infoPedido.Iva_Percibido)} \n")
                    .append("[L]TOTAL: [R] $ ${String.format("%.2f", totalFacturado)} \n")
                    .append("[L]VENDIDO POR: $vendedor\n")
                    .append("[L]FECHA: $fecha \n")
                    .append("[C]¡GRACIAS POR SU COMPRA! \n")
                    .append("[C]<b>$textoPieFormateado</b>\n")
                    .append(" \n")

                val textoImprmir = normalizarTexto(ticket.toString())
                printer.printFormattedText(textoImprmir)
            }

        }else{
            Toast.makeText(this@Detallepedido, "NO ENCONTRADO", Toast.LENGTH_SHORT)
                .show()
        }

    }

    //Funcion para Normalizar Texo, eliminar tildes, caracteres especiales, etc.
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
    */

    private fun permisosBluetooth() {
        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
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
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
