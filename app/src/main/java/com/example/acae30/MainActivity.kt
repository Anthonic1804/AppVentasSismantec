package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.acae30.Utilidades.CrearSslNoSeguro
import com.example.acae30.controllers.ClientesController
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.local.entity.ServidoresEntity
import com.example.acae30.data.repository.ServidoresRepository
import com.example.acae30.ui.factories.ServidoresViewModelFactory
import com.example.acae30.ui.servidores.ServidoresViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
    private var ip: TextView? = null
    private var puerto: TextView? = null
    private var vista: View? = null
    private var alerta: AlertDialogo? = null
    private val instancia = "CONFIG_SERVIDOR"
    private var preferencias: SharedPreferences? = null
    private var funciones: Funciones? = null
    private var reconfig = false
    private var listaServidor : Spinner? = null
    private var btnGuardarServidor: Button? = null

    private var cbxActivarSSL : CheckBox? = null

    private var clientesController = ClientesController()
    // REFACTORIZACIÓN: Cambio de ConexionController a ServidoresViewModel
    private lateinit var servidoresViewModel: ServidoresViewModel
    private var alert: AlertDialogo? = null

    private var nombreServidor: String = ""

    private lateinit var puntoVenta : TextView

    private val utilidades = CrearSslNoSeguro()

    private var idServidorActivo: Int = 0
    private var sslActivo: Int = 0

    // REFACTORIZACIÓN: Lista local para el Spinner
    private var listaServidoresEntity: List<ServidoresEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        funciones = Funciones()
        reconfig = intent.getBooleanExtra("reconfig", false)

        alerta = AlertDialogo(this, this)
        ip = findViewById(R.id.txtip)
        puerto = findViewById(R.id.txtpuerto)
        vista = findViewById(R.id.alerta)
        puntoVenta = findViewById(R.id.tvPuntoVenta)
        listaServidor = findViewById(R.id.spServidor)
        btnGuardarServidor = findViewById(R.id.btnGuardarServidorConexion)
        cbxActivarSSL = findViewById(R.id.cbxActivarSSL)


        //amarramos el widgets a las variables
        preferencias = getSharedPreferences(instancia, Context.MODE_PRIVATE)

        // INICIALIZACIÓN MVVM
        val dao = AppDatabase.getInstance(this).servidoresDao()
        val repository = ServidoresRepository(dao)
        val factory = ServidoresViewModelFactory(repository)
        servidoresViewModel = ViewModelProvider(this, factory)[ServidoresViewModel::class.java]

        observarViewModel()

        btnGuardarServidor!!.setOnClickListener {
            validar()
        }

        alert = AlertDialogo(this@MainActivity, this)

        cargaInicial()
        cargarServidores()
    }

    //-----------------------------------
    // REFACTORIZACIÓN MVVM: Observar cambios en el ViewModel
    //-----------------------------------
    private fun observarViewModel() {
        // Observar listado de servidores para el Spinner
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                servidoresViewModel.servidores.collect { lista ->
                    listaServidoresEntity = lista
                    actualizarSpinnerServidores(lista)
                }
            }
        }

        // Observar resultado de conexión para el login
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                servidoresViewModel.resultadoConexion.collect { respuesta ->
                    if (respuesta.isNotEmpty()) {
                        manejarRespuestaConexion(respuesta)
                    }
                }
            }
        }
    }

    private fun actualizarSpinnerServidores(lista: List<ServidoresEntity>) {
        val nombres = mutableListOf("-- SELECCIONE --")
        nombres.addAll(lista.map { it.nombre })
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        listaServidor!!.adapter = adapter
    }

    private fun manejarRespuestaConexion(respuesta: String) {
        if (respuesta == "Conexion Exitosa") {
            val editor = preferencias!!.edit()
            editor.putInt("puerto", puerto!!.text.toString().toInt())
            editor.putString("ip", ip!!.text.toString())
            editor.putString("puntoVenta", puntoVenta.text.toString())
            editor.putString("nombreServidor", nombreServidor)
            editor.putInt("idServidorActivo", idServidorActivo)
            editor.putInt("sslActivo", sslActivo)
            editor.apply()

            alerta!!.dismisss()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        } else {
            alerta!!.dismisss()
            val snack = Snackbar.make(this.vista!!, respuesta, Snackbar.LENGTH_LONG)
            snack.view.setBackgroundColor(resources.getColor(R.color.moderado))
            snack.show()
        }
    }

    override fun onStart() {
        super.onStart()

        listaServidor!!.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                nombreServidor = parent?.getItemAtPosition(position).toString()
                if(nombreServidor == "-- SELECCIONE --"){
                    ip!!.text = ""
                    puerto!!.text = ""
                    btnGuardarServidor!!.isEnabled = false
                }else{
                    // REFACTORIZACIÓN: Uso de lista cargada por el ViewModel
                    val servidorSeleccionado = listaServidoresEntity.find { it.nombre == nombreServidor }
                    servidorSeleccionado?.let {
                        val ipServidor = it.ip.trim()
                        val puertoServidor = it.puerto.trim()
                        idServidorActivo = it.id
                        sslActivo = it.ssl

                        ip!!.text = ipServidor
                        puerto!!.text = puertoServidor
                        btnGuardarServidor!!.isEnabled = true
                        cbxActivarSSL!!.isChecked = sslActivo == 1
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    //FUNCION PARA VALIDAR EL SERVIODR Y CARGA DE DATOS AUTOMATIVOS
    private fun cargaInicial(){
        //VALIDANDO LA CARGA AUTOMATICA DE LOS CATALOGOS
        val cargaAutomaticaCatalogos = preferencias!!.getBoolean("cargaAutomaticaCatalogos", false)
        if(!cargaAutomaticaCatalogos){
            validateServer()
        }else{
            //AQUI CARGARA LOS CATALOGOS DE CLIENTES

            alert!!.Cargando()

            CoroutineScope(Dispatchers.IO).launch {

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGANDO INFORMACION DE CLIENTES")
                }

                delay(1000)

                try {
                    clientesController.obtenerListadoClientes(this@MainActivity, alert!!)
                }catch (e:Exception){
                    println("ERROR AL OBTENER LOS CLIENES -> " + e.message)
                }

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGANDO INFORMACION DE SUCURSALES")
                }

                delay(1000)

                try {
                    //clientesController.obtenerClienteSucursalesServidor(this@MainActivity)
                    clientesController.obtenerListadoSucursales(this@MainActivity, alert!!)
                }catch (e:Exception){
                    println("ERROR AL OBTENER LAS SUCURSALES -> " + e.message)
                }

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGANDO PRECIOS PERSONALIZADOS")
                }

                delay(1000)

                try {
                    //clientesController.obtenerPreciosPersonalizados(this@MainActivity)
                    clientesController.obtenerClientesPrecios(this@MainActivity, alert!!)
                }catch (e: Exception){
                    println("ERROR AL OBTENER LOS PRECIOS PERSONALIZADO")
                }

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGANDO CUENTAS POR COBRAR")
                }

                delay(1000)

                try {
                    clientesController.obtenerListadoCuentasPendientes(this@MainActivity, alert!!)
                    //clientesController.obtenerCxcServidor(this@MainActivity)
                }catch (e:Exception){
                    println("ERROR AL OBTENER LAS CXC -> " + e.message)
                }

                delay(1000)

                withContext(Dispatchers.Main){
                    alert!!.changeText("CARGA COMPLETA!!")
                    alert!!.dismisss()

                    //AL FINALIZAR VALIDARÁ EL SERVIDOR
                    validateServer()
                }

            }
        }
    }

    private fun validateServer() {
        if (preferencias!!.contains("ip")) {
            if (reconfig) {
                ip!!.text = preferencias!!.getString("ip", "")
                puerto!!.text = preferencias!!.getInt("puerto", 0).toString()
            } else {
                if (preferencias!!.contains("sesion")) {
                    var sesionactiva = preferencias!!.getBoolean("sesion", false)
                    if (sesionactiva) {
                        val intento = Intent(this, Inicio::class.java)
                        startActivity(intento)
                        finish()
                    } else {
                        preferencias!!.edit().remove("Idvendedor").commit()
                        preferencias!!.edit().remove("Vendedor").commit()
                        val intento = Intent(this, Login::class.java)
                        startActivity(intento)
                        finish()
                    }
                } else {
                    preferencias!!.edit().remove("Idvendedor").commit()
                    preferencias!!.edit().remove("Vendedor").commit()
                    val intento = Intent(this, Login::class.java)
                    startActivity(intento)
                    finish()
                }
            } //valida si es una reconfiguracion

        }
    } //valida que ya se tenga la conexion al servidor guardada

    fun validar() {
        if (ip!!.text.isNotEmpty() && puntoVenta.text.isNotEmpty()) {
            alerta!!.Cargando()
            val v = vista
            CoroutineScope(Dispatchers.IO).launch {

                val ip = ip!!.text.toString()
                val p = puerto!!.text.toString()
                val pVenta = puntoVenta.text.toString()
                if (funciones!!.isInternetAvailable(this@MainActivity)) {
                    verificarConexion(ip, p, pVenta)
                } else {
                    funciones!!.mostrarAlerta("ENCIENDE EL WIFI PARA CONTINUAR", this@MainActivity, vista!!)
                    alerta!!.dismisss()
                }
            }

        } else {
            val alerta: Snackbar =
                Snackbar.make(this.vista!!, "Debes llenar los campos", Snackbar.LENGTH_LONG)
            alerta.view.setBackgroundColor(resources.getColor(R.color.moderado))
            alerta.show()

        }
    } //funcion que valida que haya internet,revisa si se han llenado las cajas y llama la peticio


    private fun verificarConexion(ip: String, puerto: String, pVenta: String) {
        // REFACTORIZACIÓN: Delegar la conexión al ViewModel
        servidoresViewModel.verificarConexion(ip, puerto, sslActivo, this@MainActivity)
    }

    override fun onStop() {
        if (ip!!.text.length > 0) {
            preferencias!!.edit().putString("lbl1", ip!!.text.toString()).commit()
        }
        if (puerto!!.text.length > 0) {
            preferencias!!.edit().putInt("lbl2", puerto!!.text.toString().toInt()).commit()
        }

        //guardamos si se ha escrito algo el usuario en las cajas de texto.
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        var cajaip = ""
        var cajapuerto = ""
        if (preferencias!!.contains("lbl1")) {
            cajaip = preferencias!!.getString("lbl1", "").toString()
            preferencias!!.edit().remove("lbl1").commit()
        }
        if (preferencias!!.contains("lbl2")) {
            cajapuerto = preferencias!!.getInt("lbl2", 0).toString()
            preferencias!!.edit().remove("lbl2").commit()
        }
        //obtenemos los datos si hay
        ip!!.text = cajaip
        puerto!!.text = cajapuerto
        //se asignan a las cajas


        //los removemos de las preferencias
    }

    override fun onDestroy() {
        preferencias!!.edit().remove("lbl1").commit()
        preferencias!!.edit().remove("lbl2").commit()
        //los removemos de las preferencias
        super.onDestroy()
    } //se llama cuando se destruya la actividad

    private var doubleBackToExitPressedOnce = false
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Presiona Nuevamente Para Salir", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)

    }//anula el boton atras

    //--------------------------------
    // REFACTORIZACIÓN: La carga ahora es automática vía observarViewModel()
    //--------------------------------
    private fun cargarServidores(){ }

}