package com.example.acae30.ui.pedidos

import android.R
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.acae30.Funciones
import com.example.acae30.controllers.ClientesController
import com.example.acae30.controllers.InventarioController
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.local.models.Inventario
import com.example.acae30.data.remote.api.clientes.ClientesApi
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.InventarioRepository
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.databinding.ActivityProductoAgregarBinding
import com.example.acae30.modelos.InventarioLotesModel
import com.example.acae30.modelos.InventarioPrecios
import com.example.acae30.ui.factories.ProductoAgregarViewModelFactory
import com.example.acae30.ui.inventario.InventarioTiempoReal
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.DecimalFormatSymbols
import java.util.Locale

class Producto_agregar : AppCompatActivity() {
    private var idproducto: Int? = 0
    private var precio_iva: Float = 0.toFloat()
    //private var precio : Float = 0f
    private var cantidad: Float = 0.toFloat()
    private var idpedido: Int = 0
    private var idcliente: Int? = 0
    private var idpedidodetalle: Int? = 0
    private var nombrecliente: String? = ""
    private var idvisita = 0
    private var codigo = ""
    private var idapi = 0
    private var listPrecios: ArrayList<InventarioPrecios>? = null
    private var datosProducto: Inventario? = null
    private var proviene: String? = ""
    //private var total_param: Float? = null
    private var precioEditado: Float = 0.toFloat()
    private var existenciaProducto: Float = 0f
    private var getSucursalPosition: Int? = null
    private var codEmpleado: Int = 0
    //private var url: String? = null
    private var codigoProducto: String = ""
    private var clienteMayorista = "N"

    // REFACTORIZACIÓN MVVM: Variables de Arquitectura
    private lateinit var viewModel: ProductoAgregarViewModel
    private lateinit var binding : ActivityProductoAgregarBinding
    private var decPrecios: Int = 2
    private var decTotales: Int = 2

    //---------
    //VARIABLES PARA CONTROLAR LA ESCALA SELECCIONADA
    //---------
    private var cantidadEscala = 0f
    private var idEscala: Int = 0

    //---------
    //VARIABLES PARA LOS CONTROLADORES Y FUNCIONES
    //---------
    private var funciones = Funciones()
    private var inventarioController = InventarioController()
    private var clientesController = ClientesController()
    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"

    //---------
    //VARIABLES DE CONTROL
    //---------
    private var sinExistencias: Int = 0
    private var precioAutorizadoUtilizado: Int = 0
    private var precioAutorizado: Float = 0f
    private var modificarPrecio : Boolean = false
    //private var precioIvaPersonalizado : Float = 0f
    private var bonificacion : Float = 0f
    private var mostrarPrecioApp : Int = 0 

    private var unidadActual: String = "UNI"
    private var idUnidad = 0
    private var equivaleUni: Float = 0f
    private var equivaleFra: Float = 0f
    private var uniEquivale: String? = null

    //private var condicionMercado = ""

    //private val utilidades = CrearSslNoSeguro()
    private var inventarioTiempoReal : Boolean = false

    private lateinit var listadoLotes : ArrayList<InventarioLotesModel>
    private var lotesActivos = 0
    private var loteSeleccionado : String? = null
    private var fechaVencimientoLote : String? = null
    private var idLoteSeleccionado : Int? = null
    private var unidadesLote : Float = 0f
    private var fraccionesLote : Float = 0f

    private var tipoBonificacion: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityProductoAgregarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de la arquitectura
        val db = AppDatabase.getInstance(this)
        val inventarioRepo = InventarioRepository(db.inventarioDao())
        
        // Configuramos el servidor para Retrofit
        val servidorUrl = funciones.getServidor(
            getSharedPreferences("CONFIG_SERVIDOR", MODE_PRIVATE).getString("ip", ""), 
            getSharedPreferences("CONFIG_SERVIDOR", MODE_PRIVATE).getInt("puerto", 0).toString(), 
            this
        )
        val clientesApi = RetrofitCliente.obtenerApi<ClientesApi>(servidorUrl, this)
        
        val clientesRepo = ClientesRepository(db.clienteDao(), clientesApi)
        val pedidosRepo = PedidosRepository(db.pedidosDao(), db.reporteDao())
        
        val factory = ProductoAgregarViewModelFactory(inventarioRepo, clientesRepo, pedidosRepo, servidorUrl, this)
        viewModel = ViewModelProvider(this, factory)[ProductoAgregarViewModel::class.java]

        observarViewModel()

        //-----------
        //SETEANDO LAS SHARED PREFERENCES
        //-----------
        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)
        codEmpleado = preferencias!!.getInt("Idvendedor", 0)
        sinExistencias = if(preferencias!!.getString("pedidos_sin_existencia", "") == "S") 1 else 0
        modificarPrecio = preferencias!!.getBoolean("modificar_precio_app", false)
        mostrarPrecioApp = preferencias!!.getInt("precio_mostrar_app", 0)
        decPrecios = preferencias!!.getInt("decPrecios",2)
        decTotales = preferencias!!.getInt("decTotales",2)
        inventarioTiempoReal = preferencias!!.getBoolean("inventarioTiempoReal", false)
        tipoBonificacion = preferencias!!.getString("tipoBonificacion","").toString()

        //-----------
        //SETEANDO LOS INTENT QUE VIENEN DESDE EL FORMULARIO ANTERIOR
        //-----------
        idproducto = intent.getIntExtra("idproducto", 0)
        idpedido = intent.getIntExtra("idpedido", 0)
        idcliente = intent.getIntExtra("idcliente", 0)
        idpedidodetalle = intent.getIntExtra("idpedidodetalle", 0)
        nombrecliente = intent.getStringExtra("nombrecliente")
        idvisita = intent.getIntExtra("visitaid", 0)
        codigo = intent.getStringExtra("codigo").toString()
        idapi = intent.getIntExtra("idapi", 0)
        getSucursalPosition = intent.getIntExtra("sucursalPosition", 0)
        proviene = intent.getStringExtra("proviene")

        // Carga inicial del producto
        viewModel.cargarProducto(idproducto!!, idcliente!!, unidadActual)

        // Si estamos en modo edición, cargamos el detalle desde el ViewModel
        if (proviene == "editar" && idpedidodetalle != null && idpedidodetalle!! > 0) {
            viewModel.cargarDetallePedido(idpedidodetalle!!)
        }

        cargarOpcionesGenerales()
    }

    //------------------------------------------------------------------------
     // Observar los estados del ViewModel.
     //-----------------------------------------------------------------------
    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar cambios en el precio final calculado
                viewModel.precioFinal.collect { p ->
                    precio_iva = p
                    // Si el precio cambia, actualizamos el texto personalizado si está visible.
                    if (binding.tvPrecioPersonalizado.visibility == View.VISIBLE) {
                        binding.tvPrecioPersonalizado.text = String.format(Locale.getDefault(), "%.${decPrecios}f", p)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar el total de la línea
                viewModel.totalLinea.collect { total ->
                    binding.txttotal.text = String.format(Locale.getDefault(), "%.${decTotales}f", total)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar la bonificación calculada
                viewModel.bonificado.collect { regalias ->
                    binding.txtBonificados.text = regalias.toString()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar Stock Desglosado (UNI) - Puede ser entero o decimal
                viewModel.stockUni.collect { uni ->
                    binding.txtexistencia.text = uni
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar Stock Desglosado (FRA)
                viewModel.stockFra.collect { fra ->
                    binding.txtExistenciasFra.text = fra
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Actualizar valor de validación de existencia
                viewModel.stockTotalValidacion.collect { totalFracciones ->
                    existenciaProducto = totalFracciones
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar respuesta de Token de Autorización
                viewModel.precioAutorizado.collect { precio ->
                    if (precio != null) {
                        precioAutorizado = precio
                        AlertaPrecio(this@Producto_agregar)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar Detalle del Pedido (Modo Edición)
                viewModel.detallePedido.collect { detalle ->
                    if (detalle != null && proviene == "editar") {
                        // Configuramos la UI con los datos del producto ya guardado
                        configurarModoEdicion(detalle)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar si el precio es personalizado para bloquear la UI
                viewModel.esPrecioPersonalizado.collect { esEspecial ->
                    binding.apply {
                        if (esEspecial) {
                            tvPrecioPersonalizado.visibility = View.VISIBLE
                            spprecio.visibility = View.GONE
                            btneditarprecio.visibility = View.GONE
                            tvPrecioPersonalizado.text = String.format(Locale.getDefault(), "%.${decPrecios}f", precio_iva)
                        } else {
                            tvPrecioPersonalizado.visibility = View.GONE
                            spprecio.visibility = View.VISIBLE
                            // Solo mostramos editar si no es modo edición
                            if (proviene != "editar") {
                                btneditarprecio.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar Info del Producto
                viewModel.producto.collect { p ->
                    if (p != null) {
                        datosProducto = p
                        binding.txtcodigo.text = p.Codigo
                        binding.txtdescripcion.text = p.descripcion
                        codigoProducto = p.Codigo.toString()
                        
                        // Si no estamos en edición, inicializamos con precio base
                        if (proviene != "editar") {
                            precio_iva = p.Precio_iva ?: 0f
                            Totalizar(1f)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar Lotes
                viewModel.listaLotes.collect { lotes ->
                    if (lotes.isNotEmpty()) {
                        binding.lyLote.visibility = View.VISIBLE
                        if(proviene == "editar"){
                            binding.spLotesAgregarProducto.visibility = View.GONE
                            binding.etLoteSeleccionado.visibility = View.VISIBLE
                        } else {
                            binding.spLotesAgregarProducto.visibility = View.VISIBLE
                            binding.etLoteSeleccionado.visibility = View.GONE
                        }
                        lotesActivos = 1
                        
                        // Mapear a modelo anterior para compatibilidad con funciones existentes
                        listadoLotes = ArrayList(lotes.map { 
                            InventarioLotesModel(it.id, it.idProducto, it.codigoProducto, it.lote, it.fechaVencimiento, it.unidades, it.fracciones) 
                        })
                        cargarLotes()

                        // Si estamos en edición, una vez cargada la lista de lotes,
                        // intentamos refrescar la info del lote seleccionado.
                        if (proviene == "editar") {
                            viewModel.detallePedido.value?.lote?.let { 
                                cargarInfoLoteSeleccionado(it)
                            }
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Manejar eventos de navegación y errores
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is ProductoAgregarViewModel.UIEvent.ProductoGuardado -> {
                            provieneDetallePedido(idpedido, idcliente, nombrecliente, idvisita, codigo, "visita", idapi, getSucursalPosition)
                        }
                        is ProductoAgregarViewModel.UIEvent.Error -> {
                            funciones.mostrarAlerta(event.mensaje, this@Producto_agregar, binding.lienzo)
                        }
                        null -> {}
                    }
                    if (event != null) viewModel.resetEvents()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Actualizar el total cuando cambie el precio
        binding.spprecio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //MUESTRA EL VALOR SELECCIONADO EN EL PRECIO
                //Toast.makeText(applicationContext, "Valor: "+parent!!.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show()

                val nuevaCadena = parent!!.getItemAtPosition(position).toString()

                val valor = nuevaCadena.substringBefore(" ").toDoubleOrNull()

                //MUESTRA EL VALOR SELECCIONADO DEL PRECIO
                //Toast.makeText(applicationContext, "Valor Seleccionado: "+ valor.toString(), Toast.LENGTH_LONG).show()

                if (nuevaCadena.last() == '*') {
                    precio_iva = precioEditado
                } else {
                    val nuevoValor = precioFromList(valor.toString())
                    precio_iva = nuevoValor
                }

                cantidadEscala = inventarioController.obtenerEscalaSeleccionada(this@Producto_agregar,
                    idproducto!!, precio_iva, unidadActual)

                Totalizar(cantidad)

            }
        }

        binding.txtcantidad.setText(String.format("%.2f".format(cantidad)))

        binding.imgbtnatras.setOnClickListener {
            if(proviene == "editar"){

                provieneDetallePedido(idpedido, idcliente, nombrecliente, idvisita, codigo, "visita", idapi, getSucursalPosition)

            }else{
                if(inventarioTiempoReal){
                    val intento = Intent(this@Producto_agregar, InventarioTiempoReal::class.java)
                    intento.putExtra("idcliente", idcliente)
                    intento.putExtra("nombrecliente", nombrecliente)
                    intento.putExtra("busqueda", true)
                    intento.putExtra("idpedido", idpedido)
                    intento.putExtra("visitaid", idvisita)
                    intento.putExtra("codigo", codigo)
                    intento.putExtra("idapi", idapi)
                    intento.putExtra("sucursalPosition", getSucursalPosition)
                    intento.putExtra("facturaExportacion", false)
                    startActivity(intento)
                }else{
                    val intento = Intent(
                        this@Producto_agregar,
                        com.example.acae30.ui.inventario.Inventario::class.java
                    )
                    intento.putExtra("idcliente", idcliente)
                    intento.putExtra("nombrecliente", nombrecliente)
                    intento.putExtra("busqueda", true)
                    intento.putExtra("idpedido", idpedido)
                    intento.putExtra("visitaid", idvisita)
                    intento.putExtra("codigo", codigo)
                    intento.putExtra("idapi", idapi)
                    intento.putExtra("sucursalPosition", getSucursalPosition)
                    intento.putExtra("facturaExportacion", false)
                    startActivity(intento)
                }
            }

        }

        binding.btnagregar.setOnClickListener {
            /*
             * CÓDIGO ANTERIOR (Comentado):
             * if(modificarPrecio){ agregarProducto() } else { ... confirmarToken ... }
             */

            // NUEVO CÓDIGO: Delegamos la lógica de autorización al ViewModel
            if (modificarPrecio) {
                agregarProducto()
            } else {
                if (precioAutorizadoUtilizado == 1) {
                    // Si se usó un precio autorizado, confirmamos el token en el servidor antes de guardar.
                    prepararYConfirmarToken()
                } else {
                    agregarProducto()
                }
            }
        }

        binding.btneliminar.setOnClickListener {
            /*
             * CÓDIGO ANTERIOR (Comentado):
             * CoroutineScope(Dispatchers.IO).launch { deleteDetalle(idpedidodetalle!!) }
             */

            // NUEVO CÓDIGO: Delegamos la eliminación al ViewModel
            if (idpedidodetalle != null && idpedidodetalle!! > 0) {
                viewModel.eliminarProducto(idpedidodetalle!!, idpedido)
            }
        }

        binding.spunidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                                        id: Long) {
                val itemSeleccionado = binding.spunidad.selectedItem.toString()
                
                // 1. Reiniciamos equivalencias para evitar usar datos de la unidad anterior
                equivaleUni = 0f
                equivaleFra = 0f

                when(itemSeleccionado){
                    "UNIDAD" -> {
                        unidadActual = "UNI"
                        uniEquivale = "UNI"
                        validarCantidad(binding.txtcantidad.text.toString())
                    }
                    "FRACCION" -> {
                        unidadActual = "FRA"
                        uniEquivale = "FRA"
                        validarCantidad(binding.txtcantidad.text.toString())
                    }
                    else -> {
                        unidadActual = itemSeleccionado
                        // 2. Buscamos la equivalencia en segundo plano
                        lifecycleScope.launch(Dispatchers.IO) {
                            val unidadMedida = inventarioController.obtenerIdUnidadMedida(this@Producto_agregar, idproducto!!, unidadActual)
                            withContext(Dispatchers.Main) {
                                if(unidadMedida != null){
                                    idUnidad = unidadMedida.id ?: 0
                                    uniEquivale = unidadMedida.unidades
                                    if (uniEquivale == "UNI") equivaleUni = unidadMedida.equivale else equivaleFra = unidadMedida.equivale
                                }
                                // 3. Validamos SOLO cuando ya tenemos los factores de conversión actualizados
                                validarCantidad(binding.txtcantidad.text.toString())
                            }
                        }
                    }
                }
                
                cargarListadoPrecios(unidadActual)
                verificarBonificados(unidadActual)
            }
        }

        binding.btneditarprecio.setOnClickListener {
            /*
             * CÓDIGO ANTERIOR (Comentado):
             * if(modificarPrecio){ AlertaPrecio(...) } else { verificarPrecioAutorizado(...) }
             */
             
            // NUEVO CÓDIGO: Delegamos la consulta de autorización al ViewModel
            if(modificarPrecio){
                AlertaPrecio(this@Producto_agregar)
            }else{
                viewModel.buscarTokenAutorizacion(codEmpleado, codigoProducto)
            }
        }

        binding.txtcantidad.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(cantidad: Editable) {
                if(cantidad.toString() == "."){
                    binding.txtcantidad.setText("0.")
                    binding.txtcantidad.setSelection(binding.txtcantidad.text.length)
                }
                validarCantidad(cantidad.toString())
            }
        })

        binding.spLotesAgregarProducto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val lote = parent?.getItemAtPosition(position).toString()

                cargarInfoLoteSeleccionado(lote)

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //NADA IMPLEMNTADO
            }

        }


        CambioCantidad()
    }

    private fun cargarOpcionesGenerales(){
        this@Producto_agregar.lifecycleScope.launch {

            // 1. Obtener información del cliente (Solo para saber si es Mayorista)
            val cliente = clientesController.obtenerInformacionCliente(this@Producto_agregar, idcliente!!)
            if (cliente != null) {
                clienteMayorista = cliente.Mayorista.toString().trim()
            }

            // 2. Determinar la Escala inicial
            cantidadEscala = inventarioController.obtenerEscalaSeleccionada(this@Producto_agregar, idproducto!!, precio_iva, unidadActual)

            // 3. Configurar visibilidad de botones
            if (idpedidodetalle!! > 0) {
                binding.btneliminar.visibility = View.VISIBLE
            } else {
                binding.btneliminar.visibility = View.GONE
            }

            // REFACTORIZACIÓN MVVM: Eliminamos toda la lógica manual de precios y visibilidad de aquí.
            // Ahora confiamos plenamente en observarViewModel() para que pinte la UI.
            
            runOnUiThread {
                cargarUnidadesMedida()
                cargarListadoPrecios(unidadActual)
            }
        }
    }

    //---------------------------------------------------------------------------------
    //Configura la interfaz cuando se entra en modo edición.
    //---------------------------------------------------------------------------------
    private fun configurarModoEdicion(detalle: com.example.acae30.data.local.entity.PedidoDetalleEntity) {
        binding.apply {
            btnagregar.text = "ACTUALIZAR PRODUCTO"
            txttituloproducto.text = "ACTUALIZAR PRODUCTO"
            btneditarprecio.visibility = View.INVISIBLE

            // Lote
            etLoteSeleccionado.setText(detalle.lote)
            // Solo intentamos cargar info del lote si la lista ya está inicializada
            if (::listadoLotes.isInitialized) {
                detalle.lote?.let { cargarInfoLoteSeleccionado(it) }
            }

            // Cantidad y Precio
            cantidad = detalle.cantidad.toFloat()
            precio_iva = detalle.precioIva.toFloat()
            precioEditado = if (detalle.precioEditado == "*") precio_iva else 0f

            txtcantidad.setText(String.format(Locale.getDefault(), "%.0f", cantidad))
            
            Totalizar(cantidad)
        }
    }

    //---------------------------------------
    //FUNCION PARA CARGAR LOS LOSTES EN EL SPINNER
    //---------------------------------------
    private fun cargarLotes(){
        val listaLotes = mutableListOf<String>()
        listaLotes.add("-- SELECCIONE --")

        for(item in listadoLotes){
            listaLotes.add(item.lote + " | " + item.fechaVencimiento)
        }

        val adapter = ArrayAdapter(this@Producto_agregar, R.layout.simple_spinner_item, listaLotes)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spLotesAgregarProducto.adapter = adapter
    }

    //-----------------------------------------
    // FUNCION PARA OBTENER LOS DATOS DEL LOTE SELECCIONADO
    //-----------------------------------------
    private fun cargarInfoLoteSeleccionado(lote: String){
        if(lote != "-- SELECCIONE --"){
            loteSeleccionado = lote.substringBefore(" |")

            // Verificamos que la lista de lotes esté cargada antes de filtrar.
            if (::listadoLotes.isInitialized) {
                val loteEnSeleccion = listadoLotes.filter { it.lote == loteSeleccionado }
                for(item in loteEnSeleccion){
                    fechaVencimientoLote = item.fechaVencimiento
                    idLoteSeleccionado = item.id
                    unidadesLote = item.unidades
                    fraccionesLote = item.fracciones
                }
            }
        }else{
            loteSeleccionado = null
            fechaVencimientoLote = null
            idLoteSeleccionado = null
            unidadesLote = 0f
            fraccionesLote = 0f
        }

        // Actualizamos el stock en el ViewModel enviando los valores del lote.
        datosProducto?.let {
            viewModel.actualizarStock(it, unidadesLote, fraccionesLote)
        }
    }

    //FUNCION PARA OBTENER LA BONIFICACION POR PRODUCTO O CLIENTE
    private fun verificarBonificados(unidad: String){
        //OBTENIENDO LA BONIFICACION PERSONALIZADA POR CLIENTE

        val clienteBonificado = clientesController.obtenerBonificacionCliente(idcliente!!,
            idproducto!!,this@Producto_agregar)

        bonificacion = when(tipoBonificacion){

            "T" -> {
                if(clienteBonificado > 0) clienteBonificado else datosProducto!!.Bonificado!!
            }

            "BC" -> {
                if(unidad == "UNI") clienteBonificado else 0f
            }

            "BP" -> {
                if(unidad == "UNI") datosProducto!!.Bonificado!! else 0f
            }

            else -> {
                0f
            }

        }

    }

    private fun cargarUnidadesMedida(){
        this@Producto_agregar.lifecycleScope.launch {
            try {
                val hojaCarga = preferencias!!.getBoolean("Hoja_carga_inventario_app", false)
                val unidades = inventarioController.listadoUnidadesMedidaProductoById(this@Producto_agregar, idproducto!!, hojaCarga)
                val adapter = ArrayAdapter<String>(this@Producto_agregar, R.layout.simple_spinner_dropdown_item)

                val detalle = viewModel.detallePedido.value
                if(proviene == "editar" && detalle != null){
                    val unidadSeleccionada = detalle.unidad?.trim() ?: ""
                    idUnidad = detalle.idUnidad
                    equivaleFra = detalle.equivaleFra.toFloat()
                    equivaleUni = detalle.equivaleUni.toFloat()
                    uniEquivale = detalle.uniEquivale

                    adapter.add(unidadSeleccionada)
                    unidadActual = when(unidadSeleccionada){
                        "UNIDAD" -> "UNI"
                        "FRACCION" -> "FRAC"
                        else -> unidadSeleccionada
                    }
                }
                
                adapter.addAll(unidades)
                binding.spunidad.adapter = adapter

            }catch (e:Exception){
                Timber.e(e, "[PRODUCTO_AGREGAR] ERROR AL CARGAR LAS UNIDADES DE MEDIDA")
            }
        }
    }

    private fun cargarListadoPrecios(unidadMedida : String){
        this@Producto_agregar.lifecycleScope.launch {
            listPrecios = inventarioController.obtenerEscalaPrecios(this@Producto_agregar, idproducto!!, false, unidadMedida)
            val precioss = ArrayList<String>()

            // Usamos el estado del ViewModel para evitar consultas SQL manuales.
            val detalle = viewModel.detallePedido.value
            if(proviene == "editar" && detalle != null){
                val precioSeleccionado = detalle.precioIva
                precioss.add("${String.format("%.${decPrecios}f", precioSeleccionado)}")
            }

            //-----------------------
            //Agregado el precio asignado en la ficha del producto para las unidades
            //-----------------------
            val p = viewModel.producto.value
            if(unidadMedida == "UNI" && p != null){
                precioss.add("${String.format("%.${decPrecios}f", p.Precio_iva)}") 
            }

            //------------------------
            //Agregando el precio asignado en la ficha del producto para las fracciones.
            //------------------------
            if(unidadMedida == "FRA" && p != null){
                precioss.add("${String.format("%.${decPrecios}f", p.Precio_u_iva)}") 
            }

            listPrecios!!.forEach {
                val unidad_cantidad = " (" + "${String.format("%.0f", it.Cantidad)}" + " ${it.Unidad} )"
                precioss.add("${String.format("%.${decPrecios}f", it.Precio_iva)}" + " ${it.Nombre}" + unidad_cantidad)
            }

            val adapterPrecios = ArrayAdapter(this@Producto_agregar, R.layout.simple_spinner_item, precioss)
            adapterPrecios.setDropDownViewResource(com.example.acae30.R.layout.support_simple_spinner_dropdown_item)
            binding.spprecio.adapter = adapterPrecios

            if(proviene != "editar"){
                val totalIndices = binding.spprecio.adapter?.count ?: 0
                if(mostrarPrecioApp in 0 until totalIndices){
                    binding.spprecio.setSelection(mostrarPrecioApp, true)
                }else{
                    binding.spprecio.setSelection(0, true)
                }
            }
        }
    }

    //---------------------------------------
    //Funcion utilitaria para utilizar '.' al inicio de la cantidad
    //12-03-2026
    //---------------------------------------
    private fun String.toSafeDecimal(): Float {

        var value = this.trim()

        if (value.startsWith(".")) {
            value = "0$value"
        }

        return value.toFloat()
    }

    //FUNCION PARA VALIDAD CANTIDAD PARA ESCARRSA
    private fun validarCantidad(cantidadIngresada: String) {
        lifecycleScope.launch {
            if (cantidadIngresada.isNotEmpty()) {
                cantidad = cantidadIngresada.toSafeDecimal()

                // 1. Determinar la capacidad de fracción (si es 0 o 1, lo tratamos como base 1 para no anular valores)
                val realFraccion = datosProducto?.Fraccion ?: 0f
                val capacidadParaCalculo = if (realFraccion > 1f) realFraccion else 1f

                // 2. Normalizar la cantidad ingresada a la unidad base de validación (Fracciones o Unidades decimales)
                var cantidadNormalizada = 0f
                when (unidadActual) {
                    "UNI" -> {
                        cantidadNormalizada = if (realFraccion > 1f) cantidad * capacidadParaCalculo else cantidad
                    }
                    "FRA" -> {
                        cantidadNormalizada = cantidad
                    }
                    else -> {
                        // Unidades especiales (Sixpack, etc.)
                        if (equivaleUni > 0f) {
                            cantidadNormalizada = if (realFraccion > 1f) (cantidad * equivaleUni) * capacidadParaCalculo else cantidad * equivaleUni
                        } else if (equivaleFra > 0f) {
                            cantidadNormalizada = cantidad * equivaleFra
                        }
                    }
                }

                // 3. Determinar el umbral mínimo de la escala seleccionada
                val umbralEscala = if (realFraccion > 1f) cantidadEscala * capacidadParaCalculo else cantidadEscala

                // 4. Validar contra Existencias y Escalas
                if ((cantidadNormalizada > existenciaProducto || cantidad <= 0f) && sinExistencias == 0) {
                    runOnUiThread {
                        binding.txtcantidad.error = "No puede Agregar una cantidad mayor a las existencias actuales"
                        binding.btnagregar.setBackgroundResource(com.example.acae30.R.drawable.border_btndisable)
                        binding.btnagregar.isEnabled = false
                    }
                } else if (cantidadNormalizada < umbralEscala && clienteMayorista == "N") {
                    runOnUiThread {
                        binding.txtcantidad.error = "La cantidad no es válida para el precio seleccionado"
                        binding.btnagregar.setBackgroundResource(com.example.acae30.R.drawable.border_btndisable)
                        binding.btnagregar.isEnabled = false
                    }
                } else {
                    runOnUiThread {
                        binding.txtcantidad.error = null // LIMPÌAMOS EL ERROR SI TO DO ESTÁ BIEN
                        binding.btnagregar.isEnabled = true
                        Totalizar(cantidad)
                        binding.btnagregar.setBackgroundResource(com.example.acae30.R.drawable.border_btnenviar)
                    }
                }

            } else {
                runOnUiThread {
                    binding.txtcantidad.error = "Campo no puede quedar vacio"
                    binding.btnagregar.isEnabled = false
                    cantidad = 0.toFloat()
                    Totalizar(cantidad)
                }
            }
        }
    }

    //MODIFICACION PARA LA PAPELERIA DM
    //EDITAR CANTIDAD DE PRODUCTO SIN BORRAR
    //23-08-2022
    private fun CambioCantidad() {
        binding.txtcantidad.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.txtcantidad.setText("${String.format("", cantidad)}");
            }
        }
    }

    private fun Totalizar(cantidad: Float) {
        // NUEVO CÓDIGO: Delegamos el cálculo al ViewModel.
        // Determinamos la base y el factor según la unidad seleccionada
        val (base, factor) = when(unidadActual) {
            "UNI" -> "UNI" to 1f
            "FRA" -> "FRA" to 1f
            else -> {
                // Unidades adicionales (Sixpack, Cora, etc.)
                if (equivaleUni > 0f) "UNI" to equivaleUni
                else if (equivaleFra > 0f) "FRA" to equivaleFra
                else "UNI" to 1f
            }
        }

        viewModel.recalcularValores(
            idCliente = idcliente!!,
            idProducto = idproducto!!,
            cantidad = cantidad,
            unidad = unidadActual,
            unidadBase = base,
            factorEquivalencia = factor,
            tipoBonif = tipoBonificacion
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
    }

    //FUNCION PARA VALIDAD SI EL INGRESO ES NUMERICO DECIMAL
    private fun isNumeric(cadena: String): Boolean {
        return try {
            cadena.toFloat()
            return true
        } catch (nfe: NumberFormatException) {
            return false
        }
    }

    //MODIFICADA LA CANTIDAD DE DECIMALES A 4
    //MODIFICADA 08/01/2024
    private fun precioFromList(cadena: String): Float {
        var nuevoValor = 0.toFloat()

        if (isNumeric(cadena)) {
            nuevoValor = cadena.toFloat()
        } else {
            listPrecios!!.forEach {
                var valorPrecio = "${String.format("%.${decPrecios}f".format(it.Precio_iva) )}"
                var unidad_cantidad = ""
                if (it.Cantidad!! > 0.toFloat()) {
                    unidad_cantidad = " (" + "${String.format("%.2f".format(it.Cantidad) )}" + ")"

                }
                if (cadena == valorPrecio + " ${it.Nombre}" + unidad_cantidad) {
                    nuevoValor = valorPrecio.toFloat()
                }
            }
        }

        return nuevoValor
    } // Busca en la lista de precios y retorna el precio que se ha encontrado

    //MODIFICADA LA CANTIDAD DE DECIMALES A 4
    private fun AlertaPrecio(contexto: Producto_agregar) {
        val dialogo = Dialog(this)
        dialogo.setContentView(com.example.acae30.R.layout.alerta_precio)
        var nuevoprecio = dialogo.findViewById<EditText>(com.example.acae30.R.id.nuevoprecio)
        nuevoprecio.isEnabled = true

        var cadena_precio = binding.spprecio.selectedItem.toString()

        var nuevo_precio = 0.toFloat()

        if (cadena_precio.last() == '*') {
            nuevo_precio =
                cadena_precio.replace(cadena_precio.substring(cadena_precio.length - 1), "")
                    .toFloat()
        } else {
            nuevo_precio = precioFromList(cadena_precio)
        }

        if(!modificarPrecio){
            nuevoprecio.isEnabled = false
            nuevoprecio!!.setText("${String.format("%.${decPrecios}f".format(precioAutorizado) )}")
        }

        // Actualizar el total cuando cambie la cantidad
        nuevoprecio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable) {
                val nuevaCantidad = s.toString()
                if (nuevaCantidad == "") {
                    nuevoprecio.error = "Campo no puede quedar Vacio"
                }
            }
        })

        // Validar los decimales
        nuevoprecio.filters = arrayOf<InputFilter>(object : InputFilter {
            var decimalFormatSymbols: DecimalFormatSymbols = DecimalFormatSymbols()
            override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dstart: Int,
                dend: Int
            ): CharSequence {
                val indexPoint: Int =
                    dest.toString().indexOf(decimalFormatSymbols.decimalSeparator)
                if (indexPoint == -1) return source
                val decimals = dend - (indexPoint + 1)
                return if (decimals < 4) source else "" //MODIFICADA LA CANTIDAD DE DECIMALES
            }
        })

        // Acccion de click al agregar precio
        dialogo.findViewById<Button>(com.example.acae30.R.id.btnguardarnuevoprecio).setOnClickListener {
            //VARIABLE PARA DETERMINAR SI EL PRECIO ES MODIFICADO O NO
            precioAutorizadoUtilizado = if(!modificarPrecio) 1 else 0

            try {
                val unidad = binding.spunidad.selectedItem.toString()

                binding.spprecio.adapter = null

                // Agregar precios a lista

                val precioss = ArrayList<String>()

                // Agregamos el nuevo precio a la lista
                var valorNuevoPrecio = nuevoprecio.text.toString()

                if (valorNuevoPrecio == "" || valorNuevoPrecio == null) {
                    precioss.add("${String.format("%.${decPrecios}f".format(0.toFloat()) )}" + "*")
                    precioEditado = 0.toFloat()
                } else {
                    precioss.add("${String.format("%.${decPrecios}f".format(valorNuevoPrecio.toFloat()) )}" + "*")
                    precioEditado = valorNuevoPrecio.toFloat()
                }

                if (listPrecios!!.size > 0) {
                    if (unidad == "UNIDAD") {
                        precioss.add("${String.format("%.${decPrecios}f".format(datosProducto!!.Precio_iva) )}")
                        listPrecios!!.forEach {
                            if (it.Unidad == "UNI" || it.Unidad == "") {
                                var unidad_cantidad = ""
                                if (it.Cantidad!! > 0.toFloat()) {
                                    unidad_cantidad =
                                        " (" + "${String.format("%.2f".format(it.Cantidad) )}" + ")"
                                }
                                precioss.add(
                                    "${
                                        String.format(
                                            "%.${decPrecios}f".format(it.Precio_iva)
                                        )
                                    }" + " ${it.Nombre}" + unidad_cantidad
                                )

                            }
                        }
                    }

                    if (unidad == "FRACCIÓN") {
                        listPrecios!!.forEach {
                            if (it.Unidad == "FRA") {
                                var unidad_cantidad = ""
                                if (it.Cantidad!! > 0.toFloat()) {
                                    unidad_cantidad =
                                        " (" + "${String.format("%.2f".format(it.Cantidad) )}" + ")"
                                }
                                precioss.add(
                                    "${
                                        String.format(
                                            "%.${decPrecios}f".format(it.Precio_iva)
                                        )
                                    }" + " ${it.Nombre}" + unidad_cantidad
                                )
                            }
                        }
                    }

                }

                // Consultar inventario precios

                var adapterPrecios = ArrayAdapter(
                    contexto,
                    R.layout.simple_spinner_item,
                    precioss
                )
                adapterPrecios.setDropDownViewResource(com.example.acae30.R.layout.support_simple_spinner_dropdown_item)
                binding.spprecio.adapter = adapterPrecios

                dialogo.dismiss()
            } catch (e: Exception) {
                dialogo.dismiss()
                val alert: Snackbar = Snackbar.make(
                    binding.lienzo,
                    e.message.toString(),
                    Snackbar.LENGTH_LONG
                )
                alert.view.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this@Producto_agregar, com.example.acae30.R.color.moderado))
                alert.show()
            }

        }//boton eliminar

        dialogo.show()

    } //muestra la alerta para agregar precio

    //---------------------------------------------------------------------------------
     // Prepara el objeto de detalle del pedido y solicita la confirmación del token al ViewModel.
    //---------------------------------------------------------------------------------
    private fun prepararYConfirmarToken() {
        val bonificados = binding.txtBonificados.text.toString().toInt()
        val totalIvaStr = binding.txttotal.text.toString().replace(",", ".")
        val totalIva = totalIvaStr.toDoubleOrNull() ?: 0.0

        val detalle = com.example.acae30.data.local.entity.PedidoDetalleEntity(
            id = idpedidodetalle ?: 0,
            idPedido = idpedido,
            idProducto = idproducto!!,
            descripcion = binding.txtdescripcion.text.toString(),
            cantidad = cantidad.toDouble(),
            unidad = unidadActual,
            idUnidad = idUnidad,
            precio = (precio_iva / 1.13).toDouble(),
            precioIva = precio_iva.toDouble(),
            total = (totalIva / 1.13),
            totalIva = totalIva,
            precioOferta = 0.0,
            bonificado = bonificados,
            descuento = 0.0,
            precioEditado = if (precioEditado > 0) "*" else "",
            idInventarioPrecios = idEscala,
            codigoBarra = datosProducto?.codigo_de_barra ?: "",
            equivaleUni = equivaleUni.toDouble(),
            equivaleFra = equivaleFra.toDouble(),
            uniEquivale = uniEquivale,
            comentario = 0,
            tipo = if (datosProducto?.Tipo?.trim() == "Producto") "PRD" else "SVC",
            metodoGestion = datosProducto?.MetodoGestion ?: "NINGUNO",
            tipoFiscal = when (datosProducto?.TipoFiscal) {
                "Gravado" -> "G"
                "Exento" -> "E"
                else -> "NS"
            },
            idLote = idLoteSeleccionado,
            lote = loteSeleccionado,
            fechaVencimiento = fechaVencimientoLote,
            ordenDespacho = 0
        )

        viewModel.confirmarTokenYGuardar(codEmpleado, codigoProducto, detalle)
    }

    private fun agregarProducto() {
        /*
         * CÓDIGO ANTERIOR (Comentado):
         * val bonificacion = binding.txtBonificados.text.toString().toInt()
         * ... validaciones y AddDetallePedido() manual
         */

        // NUEVO CÓDIGO: Construimos la entidad y la enviamos al ViewModel
        val bonificados = binding.txtBonificados.text.toString().toInt()
        val totalIvaStr = binding.txttotal.text.toString().replace(",", ".")
        val totalIva = totalIvaStr.toDoubleOrNull() ?: 0.0
        
        val detalle = com.example.acae30.data.local.entity.PedidoDetalleEntity(
            id = idpedidodetalle ?: 0,
            idPedido = idpedido,
            idProducto = idproducto!!,
            descripcion = binding.txtdescripcion.text.toString(),
            cantidad = cantidad.toDouble(),
            unidad = unidadActual,
            idUnidad = idUnidad,
            precio = (precio_iva / 1.13).toDouble(),
            precioIva = precio_iva.toDouble(),
            total = (totalIva / 1.13),
            totalIva = totalIva,
            precioOferta = 0.0,
            bonificado = bonificados,
            descuento = 0.0,
            precioEditado = if (precioEditado > 0) "*" else "",
            idInventarioPrecios = idEscala,
            codigoBarra = datosProducto?.codigo_de_barra ?: "",
            equivaleUni = equivaleUni.toDouble(),
            equivaleFra = equivaleFra.toDouble(),
            uniEquivale = uniEquivale,
            comentario = 0,
            tipo = if (datosProducto?.Tipo?.trim() == "Producto") "PRD" else "SVC",
            metodoGestion = datosProducto?.MetodoGestion ?: "NINGUNO",
            tipoFiscal = when (datosProducto?.TipoFiscal) {
                "Gravado" -> "G"
                "Exento" -> "E"
                else -> "NS"
            },
            idLote = idLoteSeleccionado,
            lote = loteSeleccionado,
            fechaVencimiento = fechaVencimientoLote,
            ordenDespacho = 0 
        )

        viewModel.guardarProducto(detalle)
    }

    //FUNCION PARA REGRESAR AL DETALLE DEL PEDIDO
    private fun provieneDetallePedido(idpedido: Int, idcliente: Int?, nombrecliente: String?, idvisita: Int, codigo: String, visita: String,
        idapi: Int,
        sucursalPosition: Int?
    ) {
        val intento = Intent(this@Producto_agregar, Detallepedido::class.java)
        intento.putExtra("idpedido", idpedido)
        intento.putExtra("idcliente", idcliente)
        intento.putExtra("nombrecliente", nombrecliente)
        intento.putExtra("visitaid", idvisita)
        intento.putExtra("codigo", codigo)
        intento.putExtra("from", visita)
        intento.putExtra("idapi", idapi)
        intento.putExtra("sucursalPosition", sucursalPosition)
        intento.putExtra("facturaExportacion",false)
        startActivity(intento)
        finish()
    }
}