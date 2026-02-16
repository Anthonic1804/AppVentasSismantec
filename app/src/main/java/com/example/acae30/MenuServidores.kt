package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.ConexionController
import com.example.acae30.databinding.ActivityMenuServidoresBinding
import com.example.acae30.listas.ServidoresAdapter
import com.example.acae30.modelos.Servidores.ServidoresModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenuServidores : AppCompatActivity() {

    private lateinit var binding : ActivityMenuServidoresBinding
    private var conexionController = ConexionController()
    private var menu: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMenuServidoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mostrarListadoServidores()

        menu = intent.getStringExtra("Menu").toString()
        if (menu.contains("CONFIG")){
            binding.btnRegresarConfig.visibility = View.VISIBLE
            binding.btnConectarServidor.visibility = View.GONE
        }


    }

    override fun onStart() {
        super.onStart()

        binding.btnNuevoServidor.setOnClickListener {
            nuevoServidor()
        }

        binding.btnConectarServidor.setOnClickListener {
            conectarServidor()
        }

        binding.btnRegresarConfig.setOnClickListener {
            menuConfiguracion()
        }

    }

    //-----------------------------------
    //Funcion para Mostrar el Listado de Servidor
    //-----------------------------------
    private fun mostrarListadoServidores(){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val listaServidores = conexionController.obtenerListadoServidores(this@MenuServidores)
                if(listaServidores.size > 0){
                    withContext(Dispatchers.Main){
                        obtenerListaServidores(listaServidores)
                    }
                }
            }catch (e:Exception){
                println("ERROR AL MOSTRAR EL LISTADO DE SERVIDORES -> " + e.message)
            }
        }
    }

    //-----------------------------------
    //Funcion para Obtener el listado de servidores
    //-----------------------------------
    private fun obtenerListaServidores(lista: List<ServidoresModel>){
        if(lista.isNotEmpty()){
            val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.listadoServidores.layoutManager = mLayoutManager

            val adapter = ServidoresAdapter(lista, this){position ->

                val i = lista[position]

                val enlace = Intent(this@MenuServidores, NuevoServidor::class.java)
                enlace.putExtra("proceso", "editar")
                enlace.putExtra("idServidor", i.id)
                enlace.putExtra("nombreServidor", i.nombre.trim())
                enlace.putExtra("ipServidor", i.ip.trim())
                enlace.putExtra("puertoServidor", i.puerto.trim())
                enlace.putExtra("Menu", menu)
                startActivity(enlace)
                finish()

            }
            binding.listadoServidores.adapter = adapter
        }
    }

    //-----------------------------------
    //Función para redireccionar a la Activity de Registro de Servidor
    //-----------------------------------
    private fun nuevoServidor(){
        val enlace = Intent(this@MenuServidores, NuevoServidor::class.java)
        enlace.putExtra("proceso", "nuevo")
        enlace.putExtra("Menu", menu)
        startActivity(enlace)
        finish()
    }

    //-----------------------------------
    //Funcion para Redirigir a la conexion del servidor
    //-----------------------------------
    private fun conectarServidor(){
        val enlace = Intent(this@MenuServidores, MainActivity::class.java)
        startActivity(enlace)
        finish()
    }

    //-----------------------------------
    //Funcion para Redirigir aL Menú Configuracion
    //-----------------------------------
    private fun menuConfiguracion(){
        val enlace = Intent(this@MenuServidores, Configuracion::class.java)
        startActivity(enlace)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed()
    }

}