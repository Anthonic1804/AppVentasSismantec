package com.example.acae30

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.acae30.controllers.ConexionController
import com.example.acae30.databinding.ActivityNuevoServidorBinding
import com.example.acae30.modelos.Servidores.ServidoresModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NuevoServidor : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoServidorBinding
    private val conexionController = ConexionController()
    private val funciones = Funciones()
    private var procesando = false
    private var proceso : String = ""
    private var nombreServidor : String = ""
    private var ipServidor : String = ""
    private var puertoServidor : String = ""
    private var idServidor : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNuevoServidorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        proceso = intent.getStringExtra("proceso").toString()
        nombreServidor = intent.getStringExtra("nombreServidor").toString()
        ipServidor = intent.getStringExtra("ipServidor").toString()
        puertoServidor = intent.getStringExtra("puertoServidor").toString()
        idServidor = intent.getIntExtra("idServidor", 0)

        if (proceso.contains("editar")){
            binding.apply {
                btnGuardarServidor.text = "ACTUALIZAR"
                txtip.setText(ipServidor)
                txtpuerto.setText(puertoServidor)
                txtNombreServidor.setText(nombreServidor)
            }
        }


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

    }

    //-----------------------------------
    //Función para regresar al menú principal de Servidor
    //-----------------------------------
    private fun menuServidor(){
        val enlace = Intent(this@NuevoServidor, MenuServidores::class.java)
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

        procesando = true
        binding.btnGuardarServidor.isEnabled = false
        binding.btnCancelarServidor.isEnabled = false
        binding.lyIp.isEnabled = false
        binding.lyPuerto.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {

            val hayInternet = funciones.isInternetAvailable(this@NuevoServidor)

            withContext(Dispatchers.Main){

                if(hayInternet){
                    if(conexionController.validarDatosConexion(ip, puerto, nombre)){

                        val respuesta = conexionController.verificarConexionServidor(ip, puerto)

                        if(respuesta == "Conexion Exitosa"){

                            if(proceso.contains("editar")){
                                val obj = ServidoresModel(idServidor, nombre, ip, puerto)
                                conexionController.actualizarServidor(this@NuevoServidor, obj)
                                Toast.makeText(this@NuevoServidor, "SE ACTUALIZO CORRECTAMENTE EL SERVIDOR", Toast.LENGTH_SHORT)
                                    .show()
                            }else{
                                conexionController.almacenarServidorSQLite(this@NuevoServidor, ip, puerto, nombre)
                                Toast.makeText(this@NuevoServidor, "CONEXION EXITOSA CON EL SERVIDOR", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            menuServidor()
                        }else{
                            Toast.makeText(this@NuevoServidor, "ERROR AL CONEXION CON EL SERVIDOR", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }else{
                        Toast.makeText(this@NuevoServidor, "DATOS INCORRECTOS EN LA CONEXION", Toast.LENGTH_SHORT)
                            .show()
                    }
                }else{
                    Toast.makeText(this@NuevoServidor, "NO TIENE CONEXION A INTERNET", Toast.LENGTH_SHORT)
                        .show()
                }

                procesando = false
                binding.btnGuardarServidor.isEnabled = true
                binding.btnCancelarServidor.isEnabled = true
                binding.lyIp.isEnabled = true
                binding.lyPuerto.isEnabled = true
                binding.progressBar.visibility = View.GONE

            }
        }
    }

}