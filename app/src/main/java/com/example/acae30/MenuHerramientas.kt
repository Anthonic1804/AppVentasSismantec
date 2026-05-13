package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.acae30.controllers.HojaCargaController
import com.example.acae30.databinding.ActivityMenuHerramientasBinding
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MenuHerramientas : AppCompatActivity() {

    private var funciones = Funciones()
    private var hojaCargaController = HojaCargaController()
    private lateinit var binding : ActivityMenuHerramientasBinding

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var idVendedor : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuHerramientasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarResponsiveMenu()

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        idVendedor = preferences.getInt("Idvendedor", 0)

    }

    override fun onStart() {
        super.onStart()

        binding.cvRecalcularHoja.setOnClickListener {
            obtenerUnidadesVendidasParaRecalculo()
        }

        binding.btnAtras.setOnClickListener {
            menuPrincipal()
        }

    }

    private fun obtenerUnidadesVendidasParaRecalculo(){
        val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val fecha = LocalDate.parse(funciones.obtenerFecha(), formato)

        lifecycleScope.launch {
            val lista = hojaCargaController.obtenerUnidadesVendidasYRecalcularHoja(this@MenuHerramientas, fecha, idVendedor)
            if(lista.isNotEmpty()){
                Timber.d("[UI] PRODUCTO ENCONTRADOS -> $lista")
            }else{
                Timber.d("[UI] NO HAY REGISTROS")
            }
        }

    }

    private fun menuPrincipal(){
        val enlace = Intent(this@MenuHerramientas, Inicio::class.java)
        startActivity(enlace)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }

    //CONFIGURACION EL RESPONSIVE DEL MENU
    private fun configurarResponsiveMenu(){
        val flow = binding.flow
        val orientation = resources.configuration.orientation

        val cantidad = when(orientation){
            Configuration.ORIENTATION_LANDSCAPE -> 4
            else -> 2
        }

        flow.setMaxElementsWrap(cantidad)
    }
}