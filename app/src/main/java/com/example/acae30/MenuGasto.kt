package com.example.acae30

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.GastosController
import com.example.acae30.databinding.ActivityMenuGastoBinding
import com.example.acae30.listas.AbonosAdapter
import com.example.acae30.listas.GastosAdapter
import com.example.acae30.modelos.Abono
import com.example.acae30.modelos.GastoModel
import kotlinx.coroutines.launch

class MenuGasto : AppCompatActivity() {
    private lateinit var binding : ActivityMenuGastoBinding
    private var funciones = Funciones()
    private var gastoController = GastosController()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuGastoBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    override fun onStart() {
        super.onStart()

        binding.nuevoGasto.setOnClickListener {
            nuevoGasto()
        }

        binding.btnAtras.setOnClickListener {
            inicio()
        }

        mostrarDatos()

    }

    //FUNCION MARA MOSTRAR LA LISTA DE ABONOS EN LA ACTIVIDAD
    private fun mostrarDatos(){
        this@MenuGasto.lifecycleScope.launch {
            try{
                val fecha = funciones.obtenerFecha()
                val lista = gastoController.obtenerGastosSQLite(this@MenuGasto)
                if(lista.size > 0){
                    armarLista(lista)
                }
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    //FUNCION PARA ARMAR EL LISTADO EN EL RECYCLERVIEW
    private fun armarLista(lista : ArrayList<GastoModel>) {
        val mLayoutManager = LinearLayoutManager(
            this@MenuGasto,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.listaGastos.layoutManager = mLayoutManager
        val adapter = GastosAdapter(lista, this@MenuGasto)
        binding.listaGastos.adapter = adapter

    }

    private fun nuevoGasto(){
        val intent = Intent(this@MenuGasto, NuevoGasto::class.java)
        startActivity(intent)
        finish()
    }

    private fun inicio(){
        val intent = Intent(this@MenuGasto, Inicio::class.java)
        startActivity(intent)
        finish()
    }
}