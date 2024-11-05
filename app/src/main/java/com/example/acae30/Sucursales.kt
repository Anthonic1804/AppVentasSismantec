package com.example.acae30

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.SucursalesController
import com.example.acae30.databinding.ActivitySucursalesBinding
import com.example.acae30.listas.AbonosAdapter
import com.example.acae30.listas.SucursalesAdapter
import com.example.acae30.modelos.SucursalesModel
import kotlinx.coroutines.launch

class Sucursales : AppCompatActivity() {

    private lateinit var binding : ActivitySucursalesBinding
    private var sucursalController = SucursalesController()
    private var idcliente = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySucursalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idcliente = intent.getIntExtra("idcliente", 0)

        mostrarDatos()

    }

    override fun onStart() {
        super.onStart()

        binding.btnNuevaSucursal.setOnClickListener {
            val intent = Intent(this@Sucursales, NuevaSucursal::class.java)
            intent.putExtra("idcliente", idcliente)
            startActivity(intent)
            finish()
        }

        binding.imageButton.setOnClickListener {
            val intent = Intent(this@Sucursales, ClientesDetalle::class.java)
            intent.putExtra("idcliente", idcliente)
            startActivity(intent)
            finish()
        }



    }

    //FUNCION MARA MOSTRAR LA LISTA DE ABONOS EN LA ACTIVIDAD
    private fun mostrarDatos(){
        this@Sucursales.lifecycleScope.launch {
            try{
                val lista = sucursalController.obtenerInfoSucursalesPorCliente(this@Sucursales, idcliente)
                if(lista.size > 0){
                    armarLista(lista)
                }
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    //FUNCION PARA ARMAR EL LISTADO EN EL RECYCLERVIEW
    private fun armarLista(lista: ArrayList<SucursalesModel>) {
        val mLayoutManager = LinearLayoutManager(
            this@Sucursales,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.listaSucursales.layoutManager = mLayoutManager
        val adapter = SucursalesAdapter(lista)
        binding.listaSucursales.adapter = adapter

    }


}