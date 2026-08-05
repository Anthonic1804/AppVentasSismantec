package com.example.acae30.ui.cuentas

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.R
import com.example.acae30.data.local.appDatabase.AppDatabase
import com.example.acae30.data.repository.CuentasRepository
import com.example.acae30.databinding.ActivityCuentasDetalleBinding
import com.example.acae30.listas.CuentaAdapter
import com.example.acae30.modelos.Cuenta
import com.example.acae30.ui.factories.CuentasViewModelFactory
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale

class CuentasDetalle : AppCompatActivity() {
    private var idcliente = 0
    private var nombrecliente = ""
    private lateinit var binding: ActivityCuentasDetalleBinding
    private lateinit var viewModel: CuentasViewModel
    private lateinit var adapter: CuentaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuentasDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idcliente = intent.getIntExtra("idcliente", 0)
        nombrecliente = intent.getStringExtra("nombrecliente").toString()

        // Inicialización
        val dao = AppDatabase.getInstance(this).cuentasDao()
        val repository = CuentasRepository(dao)
        val factory = CuentasViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CuentasViewModel::class.java]

        configuracionInicial()
        observarViewModel()
    }

    //-------------------------------------------------------------
    // Configuración inicial de la interfaz.
    //-------------------------------------------------------------
    private fun configuracionInicial() {
        binding.txtcliente.text = nombrecliente
        
        adapter = CuentaAdapter(this)
        binding.rvlista.layoutManager = LinearLayoutManager(this)
        binding.rvlista.adapter = adapter

        binding.imgatras.setOnClickListener {
            Regresar()
        }

        binding.btnVencidas.setOnClickListener {
            binding.tvEncabezadoCuentas.text = getString(R.string.detalle_de_cuentas_vencidas)
            viewModel.cargarDetalleCuentas(idcliente, "Vencidas")
        }

        binding.btnVigentes.setOnClickListener {
            binding.tvEncabezadoCuentas.text = getString(R.string.detalle_de_cuentas_vigentes)
            viewModel.cargarDetalleCuentas(idcliente, "Vigentes")
        }

        binding.btnTodas.setOnClickListener {
            binding.tvEncabezadoCuentas.text = getString(R.string.detalle_de_cuentas)
            viewModel.cargarDetalleCuentas(idcliente, "Todas")
        }
    }

    //-------------------------------------------------------------
    // Observar cambios en el viewmodel
    //-------------------------------------------------------------
    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.detalleCuentas.collect { lista ->
                    if (lista.isNotEmpty()) {
                        binding.rvlista.visibility = View.VISIBLE
                        actualizarLista(lista)
                    } else {
                        binding.rvlista.visibility = View.GONE
                        binding.txttotal.text = "$ 0.00"
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Carga inicial
        viewModel.cargarDetalleCuentas(idcliente, "Todas")
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }

    private fun Regresar() {
        val intento = Intent(this, Cuentas_list::class.java)
        startActivity(intento)
        finish()
    }

    //-------------------------------------------------------------
    //Actualiza el adaptador y el total de la deuda.
    //-------------------------------------------------------------
    private fun actualizarLista(list: List<Cuenta>) {
        try {
            var totalDeuda = 0f
            list.forEach { totalDeuda += it.Saldo_actual ?: 0f }
            
            binding.txttotal.text = "$ " + String.format(Locale.getDefault(), "%.2f", totalDeuda)
            adapter.submitList(list)
        } catch (e: Exception) {
            //Toast.makeText(this, "Error al calcular total: ${e.message}", Toast.LENGTH_SHORT).show()
            Timber.e(e, "[CUENTASDETALLE] ERROR AL CALCULAR EL TOTAL DE LA DEUDA")
        }
    }
}