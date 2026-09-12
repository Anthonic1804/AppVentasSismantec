package com.example.acae30.ui.pedidos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.acae30.AlertDialogo
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.remote.api.clientes.ClientesApi
import com.example.acae30.data.remote.api.retrofit.RetrofitCliente
import com.example.acae30.data.repository.ClientesRepository
import com.example.acae30.data.repository.CuentasRepository
import com.example.acae30.data.repository.PedidosRepository
import com.example.acae30.data.repository.VisitasRepository
import com.example.acae30.databinding.ActivityVisitaBinding
import com.example.acae30.modelos.Visitas
import com.example.acae30.ui.factories.VisitasViewModelFactory
import com.example.acae30.ui.clientes.Clientes
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.cancel

class Visita : AppCompatActivity() {
    private var idcliente = 0
    private var nombre = ""
    private var codigo = ""
    private var latitud = "0"
    private var longitud = "0"
    private var idpedido = 0
    private var alerta: AlertDialogo? = null
    private var idvisitaGLOBAL: Int? = 0
    private var idvisitaApi = 0

    // REFACTORIZACIÓN MVVM: ViewModel para la gestión de Visitas
    private lateinit var viewModel: VisitasViewModel

    private var funciones = Funciones()
    lateinit var preferencias: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private lateinit var binding: ActivityVisitaBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar datos del intent
        idcliente = intent.getIntExtra("idcliente", 0)
        nombre = intent.getStringExtra("nombrecliente").toString()
        codigo = intent.getStringExtra("codigo").toString()
        idpedido = intent.getIntExtra("idpedido", 0)
        idvisitaApi = intent.getIntExtra("idapi", 0)
        idvisitaGLOBAL = intent.getIntExtra("visitaid", 0)

        // REFACTORIZACIÓN MVVM: Inicialización del ViewModel siguiendo el patrón del proyecto
        val db = AppDatabase.getInstance(this)
        val servidor = funciones.getServidor(getSharedPreferences(instancia, MODE_PRIVATE).getString("ip", "") ?: "", 
            getSharedPreferences(instancia, MODE_PRIVATE).getInt("puerto", 0).toString(), this)
        val clientesApi = RetrofitCliente.obtenerApi<ClientesApi>(servidor, this)
        
        val visitasRepository = VisitasRepository(this)
        val pedidosRepository = PedidosRepository(db.pedidosDao(), db.reporteDao())
        val clientesRepository = ClientesRepository(db.clienteDao(), clientesApi)
        val cuentasRepository = CuentasRepository(db.cuentasDao())
        
        val factory = VisitasViewModelFactory(visitasRepository, pedidosRepository, clientesRepository, cuentasRepository)
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[VisitasViewModel::class.java]

        alerta = AlertDialogo(this, this)
        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)

        // UI Setup inicial
        binding.txtcodigo.text = codigo
        binding.txtnombre.text = nombre

        // Configurar ViewModel
        setupObservers()

        // Inicializar lógica del GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermissions()

        // Verificar estado de la visita y mora
        if (idvisitaGLOBAL!! > 0) {
            viewModel.cargarVisitaPorId(idvisitaGLOBAL!!)
        } else {
            viewModel.verificarVisitaActiva(idcliente)
        }
        viewModel.verificarMora(idcliente)

        // Botón Iniciar Visita
        binding.btnvisita.setOnClickListener {
            val ubicacion = "$latitud,$longitud"
            val idVendedor = preferencias.getInt("Idvendedor", 0)
            viewModel.iniciarVisita(idcliente, nombre, idVendedor, ubicacion)
        }

        // Botón Finalizar Visita
        binding.btnfinvisita.setOnClickListener {
            val ubicacion = "$latitud,$longitud"
            val idVendedor = preferencias.getInt("Idvendedor", 0)
            idvisitaGLOBAL?.let { idLocal ->
                viewModel.finalizarVisita(idLocal, idVendedor, ubicacion)
                updateSharedPreferencesFinalizarVisita()
            }
        }

        // Botón Crear Pedido
        binding.btnpedido.setOnClickListener {
            val ubicacion = "$latitud,$longitud"
            idvisitaGLOBAL?.let { idLocal ->
                viewModel.crearPedido(idcliente, nombre, idLocal, idvisitaApi, ubicacion)
            }
        }

        binding.imbtnatras.setOnClickListener {
            if (idvisitaGLOBAL!! < 1) {
                val intento = Intent(this, Clientes::class.java)
                startActivity(intento)
                finish()
            }
        }
    }

    //--------------------------------------------------------------------------
    //Configura los estados del ViewModel para actualizar la UI.
    //--------------------------------------------------------------------------
    private fun setupObservers() {
        // Observar estado de la visita para mostrar/ocultar botones
        viewModel.estadoVisita.observe(this) { estado ->
            when (estado) {
                is VisitasViewModel.EstadoVisita.Activa -> {
                    idvisitaGLOBAL = estado.idLocal
                    idvisitaApi = estado.idServidor
                    binding.btnvisita.visibility = View.GONE
                    binding.btnfinvisita.visibility = View.VISIBLE
                    binding.imbtnatras.visibility = View.GONE
                    binding.btnpedido.visibility = View.VISIBLE
                    
                    if (idpedido > 0) {
                        binding.btnpedido.text = getString(R.string.nuevo_pedido)
                    } else {
                        binding.btnpedido.text = getString(R.string.pedido)
                    }
                }
                VisitasViewModel.EstadoVisita.SinIniciar -> {
                    idvisitaGLOBAL = 0
                    binding.btnvisita.visibility = View.VISIBLE
                    binding.btnfinvisita.visibility = View.GONE
                    binding.btnpedido.visibility = View.GONE
                    binding.imbtnatras.visibility = View.VISIBLE
                }
                VisitasViewModel.EstadoVisita.Finalizada -> {
                    // Navegación manejada por el observador de navegación
                }
            }
        }

        // Observar si el cliente está en mora
        viewModel.clienteMoroso.observe(this) { esMoroso ->
            binding.cvClienteMora.visibility = if (esMoroso) View.VISIBLE else View.GONE
            preferencias.edit {
                putInt("clienteMoroso", if (esMoroso) 1 else 0)
            }
        }

        // Observar eventos de navegación
        viewModel.navegacion.observe(this) { evento ->
            evento?.let {
                when (it) {
                    is VisitasViewModel.EventoNavegacion.IrADetallePedido -> {
                        val intento = Intent(this, Detallepedido::class.java).apply {
                            putExtra("idcliente", idcliente)
                            putExtra("nombrecliente", nombre)
                            putExtra("codigo", codigo)
                            putExtra("idpedido", it.idPedido)
                            putExtra("visitaid", it.idVisita)
                            putExtra("idapi", it.idApi)
                            putExtra("from", "visita")
                            putExtra("gps", "$latitud,$longitud")
                        }
                        startActivity(intento)
                        finish()
                    }
                    VisitasViewModel.EventoNavegacion.IrAPedidoPrincipal -> {
                        startActivity(Intent(this, Pedido::class.java))
                        finish()
                    }
                    VisitasViewModel.EventoNavegacion.IrAClientes -> {
                        startActivity(Intent(this, Clientes::class.java))
                        finish()
                    }
                }
                viewModel.resetNavegacion()
            }
        }

        // Observar diálogos de carga
        viewModel.cargando.observe(this) { cargando ->
            if (cargando) {
                alerta?.Cargando()
            } else {
                alerta?.dismisss()
            }
        }

        // Observar mensajes tipo Toast
        viewModel.mensaje.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            updateGPS()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() 
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateGPS()
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateGPS() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                latitud = it.latitude.toString()
                longitud = it.longitude.toString()
            }
        }
    }

    private fun updateSharedPreferencesFinalizarVisita() {
        preferencias.edit {
            remove("visita")
            remove("busqueda")
        }
    }

    override fun onStop() {
        preferencias.edit {
            putString("nombrecliente", nombre)
            putString("codigo", codigo)
            putInt("idcliente", idcliente)
        }
        super.onStop()
    }

    override fun onRestart() {
        nombre = preferencias.getString("nombrecliente", "") ?: ""
        codigo = preferencias.getString("codigo", "") ?: ""
        idcliente = preferencias.getInt("idcliente", 0)

        preferencias.edit {
            remove("nombrecliente")
            remove("codigo")
            remove("idcliente")
        }
        binding.txtcodigo.text = codigo
        binding.txtnombre.text = nombre
        super.onRestart()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.cancel()
    }

}
