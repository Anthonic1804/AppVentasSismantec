package com.example.acae30.ui.clientes

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.Inicio
import com.example.acae30.R
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.repository.CuentasRepository
import com.example.acae30.databinding.ActivityCuentasListBinding
import com.example.acae30.listas.ClienteAdapter
import com.example.acae30.modelos.Cliente
import com.example.acae30.ui.abonos.AbonosCxc
import com.example.acae30.ui.abonos.NuevoAbono
import com.example.acae30.ui.factories.CuentasViewModelFactory
import kotlinx.coroutines.launch

class Cuentas_list : AppCompatActivity() {
    private var preferences : SharedPreferences? = null
    private var instancia = "CONFIG_SERVIDOR"
    private var busquedaCliente : String? = null
    private var vista = ""
    private lateinit var viewModel: CuentasViewModel
    private lateinit var binding : ActivityCuentasListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuentasListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences(instancia, MODE_PRIVATE)
        vista = preferences!!.getString("vista", "").toString()

        // REFACTORIZACIÓN MVVM: Inicialización de Arquitectura Limpia
        val dao = AppDatabase.getInstance(this).cuentasDao()
        val repository = CuentasRepository(dao)
        val factory = CuentasViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CuentasViewModel::class.java]

        configuracionInicial()
        observarViewModel()
    }

    //-------------------------------------------------------------
    // Configuración inicial de vistas y eventos de clic.
    //-------------------------------------------------------------
    private fun configuracionInicial() {
        when(vista){
            "abono" -> {
                binding.tvTituloCxc.text = getString(R.string.listado_de_cuentas_nuevo_abono)
            }
            else -> {
                binding.tvTituloCxc.text = getString(R.string.listado_de_cuentas)
            }
        }

        binding.btnAtras.setOnClickListener {
            atras()
        }

        busqueda()
    }

    //-------------------------------------------------------------
    // REFACTORIZACIÓN MVVM: Observar los cambios de datos en el ViewModel de forma reactiva.
    //-------------------------------------------------------------

    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar lista de clientes con deudas
                viewModel.clientesConCuentas.collect { lista ->
                    if (lista.isNotEmpty()) {
                        mostrarLista(ArrayList(lista))
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        busquedaCliente = preferences!!.getString("busquedaCliente", "")
        if(busquedaCliente != ""){
            binding.searchCuenta.setQuery("$busquedaCliente", true)
        }

        //-------------------------------------------------------------
        // REFACTORIZACIÓN MVVM: Disparar la carga de datos inicial
        //-------------------------------------------------------------
        viewModel.cargarClientesConCuentas(busquedaCliente ?: "")
    }

    private fun atras() {
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

    //-------------------------------------------------------------
    //FUNCION PARA MANTENER LA BUSQUEDA DEL CLIENTE
    //-------------------------------------------------------------
    private fun buscarCliente(busqueda : String){
        preferences!!.edit {
            putString("busquedaCliente", busqueda)
        }
    }

    //-------------------------------------------------------------
    //FUNCION PARA ELIMINAR LA BUSQUEDA PERSISTENTE DEL CLIENTE
    //-------------------------------------------------------------
    private fun eliminarBusqueda(){
        val clientSearch = preferences!!.getString("busquedaCliente", "")
        preferences!!.edit {
            if (clientSearch != "") {
                remove("busquedaCliente")
            }
            if (vista == "abono") {
                remove("vista")
            }
        }
    }

    private fun busqueda() {
        binding.searchCuenta.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(texto: String): Boolean {
                // REFACTORIZACIÓN MVVM: ejecutar la búsqueda en el ViewModel
                viewModel.cargarClientesConCuentas(texto)
                return false
            }
        })
    }

    private fun mostrarLista(list: ArrayList<Cliente>?) {
        try {
            if (list!!.isNotEmpty()) {
                val mLayoutManager =
                    LinearLayoutManager(this@Cuentas_list, LinearLayoutManager.VERTICAL, false)
                binding.lista.layoutManager = mLayoutManager
                
                val adapter =
                    ClienteAdapter(list, this@Cuentas_list, this@Cuentas_list, 0) { position ->
                        val cliente = list[position]

                        // REFACTORIZACIÓN MVVM: El proceso de navegación se decide aquí de forma limpia
                        irADetalle(cliente)
                    }
                binding.lista.adapter = adapter
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar lista: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun irADetalle(cliente: Cliente) {
        when (vista) {
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
    }
}