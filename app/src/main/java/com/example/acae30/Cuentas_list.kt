package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.controllers.CuentasController
import com.example.acae30.database.Database
import com.example.acae30.databinding.ActivityCuentasListBinding
import com.example.acae30.listas.ClienteAdapter
import com.example.acae30.modelos.Cliente
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class Cuentas_list : AppCompatActivity() {
    private var bd: Database? = null

    private var preferences : SharedPreferences? = null
    private var instancia = "CONFIG_SERVIDOR"
    private var busquedaCliente : String? = null

    private var vista = ""

    private var funciones = Funciones()
    private var cuentasController = CuentasController()
    private lateinit var binding : ActivityCuentasListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuentasListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bd = Database(this)

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        vista = preferences!!.getString("vista", "").toString()

        when(vista){
            "abono" -> {
                binding.tvTituloCxc.text = getString(R.string.listado_de_cuentas_nuevo_abono)
            }
            else -> {
                binding.tvTituloCxc.text = getString(R.string.listado_de_cuentas)
            }
        }


    }

    override fun onStart() {
        super.onStart()

        busquedaCliente = preferences!!.getString("busquedaCliente", "")
        if(busquedaCliente != ""){
            binding.searchCuenta.setQuery("$busquedaCliente", true)
        }

        binding.btnAtras.setOnClickListener {

            when(vista){
                "abono" -> {
                    eliminarBusqueda()

                    val intento = Intent(this, AbonosCxc::class.java)
                    startActivity(intento)
                    finish()
                }
                else -> {
                    eliminarBusqueda()

                    val intento = Intent(this, Inicio::class.java)
                    startActivity(intento)
                    finish()
                }
            }
        }
        //GlobalScope.launch(Dispatchers.IO) {
        this@Cuentas_list.lifecycleScope.launch {
            try {
                val data = obtenerCuentas()
                if (data.size > 0) {
                    MostrarLista(data)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Cuentas_list, e.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        } //ejecuta la funcion asyncrona

        busqueda() //recibe los parametro de busqueda
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
     //   super.onBackPressed()
       // finish()
    }

    private fun obtenerCuentas() : ArrayList<Cliente>{

        val listado : ArrayList<Cliente> = if(!busquedaCliente.isNullOrEmpty()){
            cuentasController.obtenerCuentasPorNombre(busquedaCliente.toString(), this@Cuentas_list)
        }else{
            cuentasController.obtenerTodaslasCxC(this@Cuentas_list)
        }

        return listado
    }

    //FUNCION PARA MANTENER LA BUSQUEDA DEL CLIENTE
    private fun buscarCliente(busqueda : String){
        val clientSearch = preferences!!.edit()
        clientSearch.putString("busquedaCliente", busqueda)
        clientSearch.apply()
    }

    //FUNCION PARA ELIMINAR LA BUSQUEDA PERSISTENTE DEL CLIENTE
    private fun eliminarBusqueda(){
        val clientSearch = preferences!!.getString("busquedaCliente", "")
        val deleteSearch = preferences!!.edit()
        if(clientSearch != ""){
            deleteSearch.remove("busquedaCliente")
        }
        if(vista == "abono"){
            deleteSearch.remove("vista")
        }
        deleteSearch.apply()
    }

    //BUSQUEDA DE CLIENTES DINAMICA
    //MODIFICACION PARA LA LIBRERIA DM
    //31-08-2022
    private fun busqueda() {
        binding.searchCuenta.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(texto: String): Boolean {
                val dSearch = cuentasController.obtenerCuentasPorNombre(texto, this@Cuentas_list)
                MostrarLista(dSearch)
                return false
            }

        })
    } //obtiene los resultados de la busqueda

    private fun CountCuenta(idcliente: Int): Int {
        val bd = bd!!.readableDatabase
        try {
            val cursor =
                bd!!.rawQuery("SELECT COUNT(*) FROM cuentas where Id_cliente=$idcliente AND status LIKE '%PENDIENTE%'", null)
            val cuentas = 0
            return if (cursor.count > 0) {
                cursor.count
            } else {
                cuentas
            }
            cursor.close()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            bd.close()
        }
    } //revisa si tiene cuentas el cliente

    private fun MostrarLista(list: ArrayList<Cliente>?) {
        try {
            if (list!!.size > 0) {
                val mLayoutManager =
                    LinearLayoutManager(this@Cuentas_list, LinearLayoutManager.VERTICAL, false)
                binding.lista.layoutManager = mLayoutManager
                val adapter =
                    ClienteAdapter(list, this@Cuentas_list, this@Cuentas_list, 0) { position ->
                        val cliente = list[position]
                        runOnUiThread {
                            //GlobalScope.launch(Dispatchers.IO) {
                            this@Cuentas_list.lifecycleScope.launch {
                                try {
                                    val cuentas = CountCuenta(cliente.Id!!)
                                    if (cuentas > 0) {
                                        when(vista){
                                            "abono" -> {
                                                val intento = Intent(this@Cuentas_list, NuevoAbono::class.java)
                                                intento.putExtra("idcliente", cliente.Id!!)
                                                startActivity(intento)
                                                finish()
                                            }
                                            else -> {
                                                buscarCliente(binding.searchCuenta.query.toString())

                                                val intento = Intent(this@Cuentas_list, CuentasDetalle::class.java)
                                                intento.putExtra("idcliente", cliente.Id!!)
                                                intento.putExtra("nombrecliente", cliente.Cliente!!)
                                                startActivity(intento)
                                                finish()
                                            }
                                        }
                                    } else {
                                        runOnUiThread {
                                            funciones.mostrarAlerta ("Este cliente no Tiene cuentas Pendientes", this@Cuentas_list, binding.lienzo)
                                        }
                                    }
                                } catch (e: Exception) {
                                    runOnUiThread {
                                        funciones.mostrarAlerta("ERROR: AL MOSTRAR EL LISTADO" + e.message.toString(), this@Cuentas_list, binding.lienzo)
                                    }
                                }
                            }
                        }

                    }
                binding.lista.adapter = adapter

            }
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }
}