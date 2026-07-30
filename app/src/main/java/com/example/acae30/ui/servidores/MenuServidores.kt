package com.example.acae30.ui.servidores

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.Configuracion
import com.example.acae30.MainActivity
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.repository.ServidoresRepository
import com.example.acae30.databinding.ActivityMenuServidoresBinding
import com.example.acae30.ui.factories.ServidoresViewModelFactory
import kotlinx.coroutines.launch

class MenuServidores : AppCompatActivity() {

    private lateinit var binding: ActivityMenuServidoresBinding
    private var menu: String = ""
    private lateinit var adapter: ServidoresAdapter
    // REFACTORIZACIÓN: Se introduce el ViewModel para manejar los datos
    private lateinit var viewModel: ServidoresViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuServidoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        menu = intent.getStringExtra("Menu").toString()
        if (menu.contains("CONFIG")) {
            binding.btnRegresarConfig.visibility = View.VISIBLE
            binding.btnConectarServidor.visibility = View.GONE
        }

        // REFACTORIZACIÓN MVVM/ROOM: Inicialización del repositorio y ViewModel
        // El DAO se obtiene de la base de datos Room única de la app.
        val dao = AppDatabase.getInstance(this).servidoresDao()
        val repository = ServidoresRepository(dao)
        val factory = ServidoresViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ServidoresViewModel::class.java]

        setupRecyclerView()
        
        // REFACTORIZACIÓN REACTIVA: Observamos el Flow de servidores.
        // Cada vez que se registre o elimine un servidor, la lista se actualizará sola.
        observarViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ServidoresAdapter { servidor ->
            val enlace = Intent(this@MenuServidores, NuevoServidor::class.java)
            enlace.putExtra("proceso", "editar")
            enlace.putExtra("idServidor", servidor.id)
            enlace.putExtra("nombreServidor", servidor.nombre.trim())
            enlace.putExtra("ipServidor", servidor.ip.trim())
            enlace.putExtra("puertoServidor", servidor.puerto.trim())
            enlace.putExtra("Menu", menu)
            startActivity(enlace)
            finish()
        }

        binding.listadoServidores.layoutManager = LinearLayoutManager(this)
        binding.listadoServidores.adapter = adapter
    }

    // Observa el flujo de datos proveniente de Room a través del ViewModel
    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.servidores.collect { lista ->
                    // ListAdapter se encarga de calcular las diferencias (DiffUtil) eficientemente
                    adapter.submitList(lista)
                }
            }
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

    private fun nuevoServidor() {
        val enlace = Intent(this@MenuServidores, NuevoServidor::class.java)
        enlace.putExtra("proceso", "nuevo")
        enlace.putExtra("Menu", menu)
        startActivity(enlace)
        finish()
    }

    private fun conectarServidor() {
        val enlace = Intent(this@MenuServidores, MainActivity::class.java)
        startActivity(enlace)
        finish()
    }

    private fun menuConfiguracion() {
        val enlace = Intent(this@MenuServidores, Configuracion::class.java)
        startActivity(enlace)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { }
}
