package com.example.acae30.ui.servidores

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.databinding.ActivityNuevoServidorBinding
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.repository.ServidoresRepository
import com.example.acae30.ui.factories.ServidoresViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevoServidor : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoServidorBinding
    // REFACTORIZACIÓN: Se elimina ConexionController y se usa ServidoresViewModel
    private lateinit var viewModel: ServidoresViewModel
    private val funciones = Funciones()
    private var procesando = false
    private var proceso : String = ""
    private var nombreServidor : String = ""
    private var ipServidor : String = ""
    private var puertoServidor : String = ""
    private var idServidor : Int = 0
    private var menu : String = ""
    private var sslActivo : Int = 0

    private var preferencias: SharedPreferences? = null
    private val instancia = "CONFIG_SERVIDOR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNuevoServidorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        proceso = intent.getStringExtra("proceso").toString()
        nombreServidor = intent.getStringExtra("nombreServidor").toString()
        ipServidor = intent.getStringExtra("ipServidor").toString()
        puertoServidor = intent.getStringExtra("puertoServidor").toString()
        idServidor = intent.getIntExtra("idServidor", 0)
        menu = intent.getStringExtra("Menu").toString()

        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)

        // INICIALIZACIÓN MVVM: Configuración del ViewModel con su Factory
        val dao = AppDatabase.getInstance(this).servidoresDao()
        val repository = ServidoresRepository(dao)
        val factory = ServidoresViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ServidoresViewModel::class.java]

        // REFACTORIZACIÓN: Observamos los cambios del ViewModel de forma reactiva
        observarViewModel()

        if (proceso.contains("editar")){
            binding.apply {
                btnGuardarServidor.text = "ACTUALIZAR"
                txtip.setText(ipServidor)
                txtpuerto.setText(puertoServidor)
                txtNombreServidor.setText(nombreServidor)
                btnEliminarServidor.visibility = View.VISIBLE
            }
        }
    }

    // REFACTORIZACIÓN: Los flujos (Flows) del ViewModel se observan aquí. 
    // Esto centraliza la lógica de respuesta a eventos de la base de datos o red.
    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Escucha el resultado de la prueba de conexión
                viewModel.resultadoConexion.collect { respuesta ->
                    if (respuesta.isNotEmpty()) {
                        manejarRespuestaConexion(respuesta)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Escucha si la operación CRUD terminó exitosamente
                viewModel.operacionExitosa.collect { exitosa ->
                    if (exitosa) {
                        viewModel.resetOperacion()
                        menuServidor()
                    }
                }
            }
        }
    }

    private fun manejarRespuestaConexion(respuesta: String) {
        if (respuesta == "Conexion Exitosa") {
            val ip = binding.txtip.text.toString().trim()
            val puerto = binding.txtpuerto.text.toString().trim()
            val nombre = binding.txtNombreServidor.text.toString().trim()

            // REFACTORIZACIÓN: Las llamadas a la base de datos ahora pasan por el ViewModel
            if (proceso.contains("editar")) {
                viewModel.actualizarServidor(idServidor, nombre, ip, puerto, sslActivo)
                Toast.makeText(this, "SE ACTUALIZO CORRECTAMENTE EL SERVIDOR", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.registrarServidor(nombre, ip, puerto, sslActivo)
                Toast.makeText(this, "CONEXION EXITOSA CON EL SERVIDOR", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "ERROR AL CONEXION CON EL SERVIDOR: $respuesta", Toast.LENGTH_SHORT).show()
            terminarProceso()
        }
    }

    private fun terminarProceso() {
        procesando = false
        binding.btnGuardarServidor.isEnabled = true
        binding.btnCancelarServidor.isEnabled = true
        binding.lyIp.isEnabled = true
        binding.lyPuerto.isEnabled = true
        binding.progressBar.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()

        binding.btnGuardarServidor.setOnClickListener {
            if(proceso.contains("editar")){
                mensajeConfirmacion("¿Desea Actualizar el Servidor?", "EDITAR")
            }else{
                mensajeConfirmacion("¿Desea Registrar el Servidor?", "REGISTRAR")
            }
        }

        binding.btnCancelarServidor.setOnClickListener {
            mensajeConfirmacion("¿Desea Cancelar el Proceso?", "CANCELAR")
        }

        binding.cbxActivarSSL.setOnCheckedChangeListener { _, isChecked ->
            sslActivo = if(isChecked) 1 else 0
        }

        binding.btnEliminarServidor.setOnClickListener {
            mensajeConfirmacion("¿Desea Eliminar el Servidor?", "ELIMINAR")
        }
    }

    //-----------------------------------
    //Función para regresar al menú principal de Servidor
    //-----------------------------------
    private fun menuServidor(){
        val enlace = Intent(this@NuevoServidor, MenuServidores::class.java)
        enlace.putExtra("Menu", menu)
        startActivity(enlace)
        finish()
    }

    //-----------------------------------
    //Mensaje de Confirmacion de Proceso
    //-----------------------------------
    private fun mensajeConfirmacion(mensaje: String, tipo: String){
        val dialog = AlertDialog.Builder(this@NuevoServidor)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                when(tipo){
                    "CANCELAR" -> {
                        view.dismiss()
                        menuServidor()
                    }
                    "ELIMINAR" -> {
                        val idServidorActivo : Int = preferencias!!.getInt("idServidorActivo", 0)
                        if(idServidorActivo == idServidor){
                            Toast.makeText(this, "NO SE PUEDE ELIMINAR EL SERVIDOR ACTIVO", Toast.LENGTH_SHORT).show()
                        }else{
                            // REFACTORIZACIÓN: Eliminación delegada al ViewModel
                            viewModel.eliminarServidor(idServidor)
                            view.dismiss()
                        }
                    }
                    else -> {
                        verificarConexionServidor()
                        view.dismiss()
                    }
                }
            }
            .setNegativeButton("CANCELAR"){view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    //--------------------------------------
    //Función para verificar si la conexion con el servidor es valida
    //--------------------------------------
    private fun verificarConexionServidor(){
        if(procesando) return

        val ip = binding.txtip.text.toString().trim()
        val puerto = binding.txtpuerto.text.toString().trim()
        val nombre = binding.txtNombreServidor.text.toString().trim()

        if (ip.isEmpty() || nombre.isEmpty()) {
            Toast.makeText(this, "DATOS INCORRECTOS", Toast.LENGTH_SHORT).show()
            return
        }

        procesando = true
        binding.btnGuardarServidor.isEnabled = false
        binding.btnCancelarServidor.isEnabled = false
        binding.lyIp.isEnabled = false
        binding.lyPuerto.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        // REFACTORIZACIÓN: La lógica de red se dispara desde el ViewModel
        lifecycleScope.launch {
            val hayInternet = withContext(Dispatchers.IO) { funciones.isInternetAvailable(this@NuevoServidor) }
            if (hayInternet) {
                viewModel.verificarConexion(ip, puerto, sslActivo, this@NuevoServidor)
            } else {
                Toast.makeText(this@NuevoServidor, "SIN CONEXION A INTERNET", Toast.LENGTH_SHORT).show()
                terminarProceso()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { }
}
