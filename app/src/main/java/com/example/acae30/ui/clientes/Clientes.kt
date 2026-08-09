package com.example.acae30.ui.clientes

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.AlertDialogo
import com.example.acae30.Funciones
import com.example.acae30.Inicio
import com.example.acae30.R
import com.example.acae30.controllers.ClientesController
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.remote.api.clientes.ClientesApi
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.databinding.ActivityClientesBinding
import com.example.acae30.listas.ClienteAdapter
import com.example.acae30.modelos.Cliente
import com.example.acae30.ui.factories.ClientesViewModelFactory
import com.example.acae30.ui.historico.HistoricoPedidos
import com.example.acae30.ui.pedidos.Detallepedido
import com.example.acae30.ui.pedidos.Pedido
import com.example.acae30.ui.pedidos.Visita
import com.example.acae30.ui.clientes.firmarPagare
import com.example.acae30.ui.clientes.verPagare
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class Clientes : AppCompatActivity() {
    private var alert: AlertDialogo? = null
    private var busquedaPedido: Boolean = false
    private var visita = false
    private var cuentas = false

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvMsj : TextView
    private lateinit var tvTitulo : TextView

    private var preferences: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var dSearch : String? = null

    private var clienteHistorio : Boolean = false
    private var pagare : Boolean = false

    // private var clienteController = ClientesController()
    private var funciones = Funciones()
    private lateinit var binding : ActivityClientesBinding
    private var tipoVentaLocal = false

    // Declaración de ViewModel
    private lateinit var viewModel: ClientesViewModel

    //VARIABLES PARA LA CAPTURA DE LA GEOLOCALIZACION
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var latitud = "0"
    private var longitud = "0"

    private var cargarClientesPorRuta = ""
    private var P_Mantto_Clientes: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        clienteHistorio = intent.getBooleanExtra("Historico", false)
        cuentas = intent.getBooleanExtra("cuentas", false)

        preferences = getSharedPreferences(instancia, MODE_PRIVATE)
        busquedaPedido = preferences!!.getBoolean("busqueda", false)
        visita = preferences!!.getBoolean("visita", false)
        pagare = preferences!!.getBoolean("PagareObligatorio", false)
        cargarClientesPorRuta = preferences!!.getString("cargarClientesPorRuta", "").toString()

        P_Mantto_Clientes = preferences!!.getBoolean("P_Mantto_Clientes", false)
        tipoVentaLocal = preferences!!.getBoolean("tipoVentaLocal", false)

        // Inicialización
        val db = AppDatabase.getInstance(this)
        val clientesDao = db.clienteDao()
        val pedidosDao = db.pedidosDao()
        val reporteDao = db.reporteDao()
        
        val servidor = funciones.getServidor(preferences!!.getString("ip", ""), preferences!!.getInt("puerto", 0).toString(), this)
        val clientesApi = RetrofitCliente.obtenerApi<ClientesApi>(servidor, this)
        
        val clientesRepository = ClientesRepository(clientesDao, clientesApi)
        val pedidosRepository = PedidosRepository(pedidosDao, reporteDao)
        
        val factory = ClientesViewModelFactory(clientesRepository, pedidosRepository)
        viewModel = ViewModelProvider(this, factory)[ClientesViewModel::class.java]

        setupUI()
        observarViewModel()
    }

    private fun setupUI() {
        alert = AlertDialogo(this, this)
        
        if(visita || !P_Mantto_Clientes){
            binding.nuevoCliente.visibility = View.GONE
        }

        binding.imageButton.setOnClickListener {
            Atras(it)
        }

        binding.nuevoCliente.setOnClickListener {
            val intent = Intent(this@Clientes, NuevoCliente::class.java)
            intent.putExtra("latitud", latitud)
            intent.putExtra("longitud", longitud)
            startActivity(intent)
            finish()
        }

        setupBusqueda()
    }

    override fun onStart() {
        super.onStart()
        
        dSearch = preferences!!.getString("clienteBusqueda", "")
        if(dSearch != ""){
            binding.busquedainv.setQuery("$dSearch", true)
        }

        if(visita){
            binding.tvListadoClientes.text = getString(R.string.listado_de_clientes_nuevo_pedido)
        }else{
            binding.tvListadoClientes.text = getString(R.string.listado_de_clientes)
        }

        // Carga inicial de datos
        viewModel.cargarClientes(dSearch ?: "", 0)

        // OBTERNIENDO UBICACIÓN
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        capturarLocalizacion()
    }

    //-----------------------------------
    // Observar cambios en el ViewModel
    //-----------------------------------
    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar la lista de clientes
                viewModel.listaClientes.collect { lista ->
                    if (lista.isNotEmpty()) {
                        mostrarLista(ArrayList(lista))
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Manejar la navegación condicional (Pedido, Pagaré, Histórico, etc.)
                viewModel.eventoNavegacion.collect { destino ->
                    when (destino) {
                        is ClientesViewModel.Navegacion.IrADetallePedido -> {
                            val intento = Intent(this@Clientes, Detallepedido::class.java)
                            intento.putExtra("idcliente", destino.idCliente)
                            intento.putExtra("nombrecliente", destino.nombre)
                            intento.putExtra("codigo", destino.codigo)
                            intento.putExtra("idpedido", destino.idPedido)
                            intento.putExtra("visitaid", 0)
                            intento.putExtra("idapi", 0)
                            intento.putExtra("from", "visita")
                            startActivity(intento)
                            finish()
                        }
                        is ClientesViewModel.Navegacion.IrAVisita -> {
                            val intento = Intent(this@Clientes, Visita::class.java)
                            intento.putExtra("idcliente", destino.idCliente)
                            intento.putExtra("nombrecliente", destino.nombre)
                            intento.putExtra("codigo", destino.codigo)
                            startActivity(intento)
                            finish()
                        }
                        is ClientesViewModel.Navegacion.IrAFirmarPagare -> {
                            /* 
                             * Mostramos el mensaje de confirmación antes de la lectura.
                             */
                            mensajeDialogo(destino)
                        }
                        is ClientesViewModel.Navegacion.IrAHistorico -> {
                            val intento = Intent(this@Clientes, HistoricoPedidos::class.java)
                            intento.putExtra("idCliente", destino.idCliente)
                            intento.putExtra("nombreCliente", destino.nombre)
                            startActivity(intento)
                            finish()
                        }
                        is ClientesViewModel.Navegacion.IrADetalleCliente -> {
                            busquedaCliente(binding.busquedainv.query.toString())
                            val intento = Intent(this@Clientes, ClientesDetalle::class.java)
                            intento.putExtra("idcliente", destino.idCliente)
                            startActivity(intento)
                            finish()
                        }
                        null -> {}
                    }
                    if (destino != null) viewModel.resetNavegacion()
                }
            }
        }
    }

    private fun setupBusqueda() {
        binding.busquedainv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(texto: String): Boolean {
                // Búsqueda ViewModel
                viewModel.cargarClientes(texto.uppercase(), 0)
                return false
            }
        })
    }

    // Manejar el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun capturarLocalizacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            updateGPS()
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateGPS() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    latitud = location.latitude.toString()
                    longitud = location.longitude.toString()
                } ?: run {
                    latitud = "0"
                    longitud = "0"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener la ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sharedPreferencesFinalizarVisita(){
        preferences!!.edit {
            remove("visita")
            remove("busqueda")
        }
    }

    fun Atras(view: View) {
        if (busquedaPedido) {
            if (visita) {
                eliminarBusqueda()
                sharedPreferencesFinalizarVisita()
                val intento = Intent(this, Pedido::class.java)
                startActivity(intento)
                finish()
            } else {
                eliminarBusqueda()
                val intento = Intent(this, Detallepedido::class.java)
                startActivity(intento)
                finish()
            }
        } else {
            eliminarBusqueda()
            val intento = Intent(this, Inicio::class.java)
            startActivity(intento)
            finish()
        }

        if(clienteHistorio){
            val intento = Intent(this@Clientes, HistoricoPedidos::class.java)
            startActivity(intento)
            finish()
        }
    }

    private fun mostrarLista(list: ArrayList<Cliente>?) {
        try {
            if (list!!.isNotEmpty()) {
                val mLayoutManager = LinearLayoutManager(this@Clientes, LinearLayoutManager.VERTICAL, false)
                binding.lista.layoutManager = mLayoutManager
                val adapter = ClienteAdapter(list, this@Clientes, this@Clientes, 0) { position ->
                    val cliente = list[position]

                    viewModel.seleccionarCliente(
                        cliente = cliente,
                        busquedaPedido = busquedaPedido,
                        tipoVentaLocal = tipoVentaLocal,
                        pagareObligatorio = pagare,
                        historico = clienteHistorio
                    )
                }
                binding.lista.adapter = adapter
            }
        } catch (e: Exception) {
            Toast.makeText(this@Clientes, "Error al mostrar lista: ${e.message}", Toast.LENGTH_LONG).show()
            Timber.e(e,"[CLIENTE] ERROR AL MOSTRAR EL LISTADO DE CLIENTES")
        }
    }

    private fun busquedaCliente(busqueda : String){
        preferences!!.edit { putString("clienteBusqueda", busqueda) }
    }

    private fun eliminarBusqueda(){
        val dSearch = preferences?.getString("clienteBusqueda", "")
        if(dSearch != null && dSearch != ""){
            preferences?.edit { remove("clienteBusqueda") }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {}

    private fun mensajeDialogo(destino: ClientesViewModel.Navegacion.IrAFirmarPagare){
        val msjDialog = Dialog(this, R.style.Theme_Dialog)
        msjDialog.setCancelable(false)
        msjDialog.setContentView(R.layout.dialog_cancelar)
        
        tvUpdate = msjDialog.findViewById(R.id.tvUpdate)
        tvCancel = msjDialog.findViewById(R.id.tvCancel)
        tvMsj = msjDialog.findViewById(R.id.tvMensaje)
        tvTitulo = msjDialog.findViewById(R.id.tvTitulo)

        tvMsj.text = getString(R.string.desea_firmar_el_pagar)
        tvTitulo.text = getString(R.string.firmar_pagar)
        tvUpdate.text = getString(R.string.aceptar)
        tvCancel.text = getString(R.string.cancelar)

        tvUpdate.setOnClickListener {

            val intento = Intent(this@Clientes, verPagare::class.java)
            intento.putExtra("idcliente", destino.idCliente)
            intento.putExtra("nombreCliente", destino.nombre)
            intento.putExtra("direccionCliente", destino.direccion)
            intento.putExtra("duiCliente", destino.dui)
            intento.putExtra("limiteCredito", destino.limiteCredito)
            intento.putExtra("plazoCredito", destino.plazo)
            startActivity(intento)
            finish()
            
            msjDialog.dismiss()
        }

        tvCancel.setOnClickListener { msjDialog.dismiss() }
        msjDialog.show()
    }
}
