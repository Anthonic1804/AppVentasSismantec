package com.example.acae30.ui.inventario

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.AlertDialogo
import com.example.acae30.Inicio
import com.example.acae30.controllers.InventarioTiempoRealController
import com.example.acae30.data.remote.dto.InventarioTiempoRealDto
import com.example.acae30.databinding.ActivityInventarioTiempoRealBinding
import com.example.acae30.listas.InventarioTiempoRealAdapter
import com.example.acae30.ui.pedidos.Detallepedido
import com.example.acae30.ui.pedidos.Producto_agregar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InventarioTiempoReal : AppCompatActivity() {

    private val inventarioReal = InventarioTiempoRealController()
    private lateinit var binding : ActivityInventarioTiempoRealBinding
    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"

    private var busquedaProducto: Boolean = false
    private var idcliente: Int? = 0
    private var nombrecliente: String? = ""
    private var idpedido = 0
    private var idvisita = 0
    private var codigo = ""
    private var idapi = 0

    private var alerta: AlertDialogo? = null
    private var FacturaExportacion = false
    private var idvendedor = 0
    private var hojaCarga = 0

    private var sinExistencias: Int = 0  // 1 -> Si    0 -> no
    private var getSucursalPosition: Int? = null

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventarioTiempoRealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        busquedaProducto = intent.getBooleanExtra("busqueda", false)
        preferences = getSharedPreferences(instancia, MODE_PRIVATE)

        idcliente = intent.getIntExtra("idcliente", 0)
        nombrecliente = intent.getStringExtra("nombrecliente")
        idpedido = intent.getIntExtra("idpedido", 0)
        idvisita = intent.getIntExtra("visitaid", 0)
        codigo = intent.getStringExtra("codigo").toString()
        idapi = intent.getIntExtra("idapi", 0)
        FacturaExportacion = intent.getBooleanExtra("facturaExportacion", false)

        idvendedor = preferences.getInt("Idvendedor", 0)
        hojaCarga = preferences.getInt("hojaCarga", 0)
        alerta = AlertDialogo(this, this)

        sinExistencias = if(preferences.getString("pedidos_sin_existencia", "") == "S") 1 else 0

        //CAPTURANDO POSICIONES DE LOS SPINNER
        // getSucursalPosition = intent.getIntExtra("sucursalPosition", 0)

        preferences = getSharedPreferences(instancia, MODE_PRIVATE)
        actualizarListadeInventario(" ")

    }

    override fun onStart() {
        super.onStart()

        binding.btnatras.setOnClickListener {
            if(busquedaProducto){
                val intento = Intent(this, Detallepedido::class.java)
                intento.putExtra("idcliente", idcliente)
                intento.putExtra("nombrecliente", nombrecliente)
                intento.putExtra("idpedido", idpedido)
                intento.putExtra("visitaid", idvisita)
                intento.putExtra("codigo", codigo)
                intento.putExtra("idapi", idapi)
                intento.putExtra("from", "visita")
                // CÓDIGO VIEJO: intento.putExtra("sucursalPosition", getSucursalPosition)
                intento.putExtra("facturaExportacion", FacturaExportacion)
                startActivity(intento)
                finish()
            }else{
                regresarInicio()
            }
        }

        binding.txtBusquedaProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(string: Editable) {

                val texto = string.toString().trim()

                job?.cancel()
                job = lifecycleScope.launch {
                    delay(300) // espera 300ms

                    if (texto.isEmpty()) {
                        actualizarListadeInventario(" ")
                    } else {
                        actualizarListadeInventario(texto)
                    }
                }
            }
        })

    }

    //FUNCION PARA ACTUALIZAR LA LISTA DEL INVENTARIO
    private fun actualizarListadeInventario(busqueda: String){
        this@InventarioTiempoReal.lifecycleScope.launch {
            try {
                val lista = inventarioReal.obtenerInventarioPorDescripcion(this@InventarioTiempoReal, busqueda)
                mostrarLista(lista)
            } catch (e: Exception) {
                println("ERROR LA OBTENER EL INVENTARIO EN TIEMPO REAL -> " + e.message)
            }
        }
    }

    //IMPLEMENTADA LA FUNCION DE NO AGREGAR PRODUCTOS SIN EXISTENCIAS
    private fun mostrarLista(list: List<InventarioTiempoRealDto>) {
        try {
            if (list.isNotEmpty()) {
                val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                binding.listaInventarioReal.layoutManager = mLayoutManager
                val adapter = InventarioTiempoRealAdapter(list, this) { i ->
                    lifecycleScope.launch {
                        val id = list[i].id

                        if (busquedaProducto) {
                            val existeniasProducto = list[i].existencia.toFloat()
                            if (sinExistencias == 0 && existeniasProducto <= 0f) {
                                Toast.makeText(
                                    this@InventarioTiempoReal,
                                    "NO SE PUEDEN AGREGAR PRODUCTOS SIN EXISTENCIAS",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // REFACTORIZACIÓN: Indicador de carga y validación de descarga completa
                                if (descargarYValidarProducto(id)) {
                                    val intento =
                                        Intent(this@InventarioTiempoReal, Producto_agregar::class.java)
                                    intento.putExtra("idproducto", id)
                                    intento.putExtra("idcliente", idcliente)
                                    intento.putExtra("nombrecliente", nombrecliente)
                                    intento.putExtra("codigo", codigo)
                                    intento.putExtra("idpedido", idpedido)
                                    intento.putExtra("visitaid", idvisita)
                                    intento.putExtra("from", "visita")
                                    intento.putExtra("proviene", "buscar_producto")
                                    intento.putExtra("total_param", 0.toFloat())
                                    intento.putExtra("facturaExportacion", FacturaExportacion)
                                    startActivity(intento)
                                    finish()
                                }
                            }
                        } else {
                            if (descargarYValidarProducto(id)) {
                                preferences.edit {
                                    putInt("idProducto", id)
                                }
                                inventarioDetalle()
                            }
                        }
                    }
                }
                binding.listaInventarioReal.adapter = adapter
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this@InventarioTiempoReal, "Error al mostrar lista: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * REFACTORIZACIÓN: Nueva lógica de descarga segura.
     * Asegura que las 4 peticiones sean exitosas antes de permitir la navegación.
     * Implementa try-catch-finally para garantizar el cierre del diálogo de carga.
     */
    private suspend fun descargarYValidarProducto(id: Int): Boolean {
        var exito = false
        runOnUiThread { alerta?.Cargando() }
        
        try {
            // Realizamos las 4 peticiones y verificamos que todas devuelvan true
            val r1 = inventarioReal.obtenerProductoPorId(this, id)
            val r2 = inventarioReal.obtenerInventarioPreciosPorId(this, id)
            val r3 = inventarioReal.obtenerInventarioUnidadesPorId(this, id)
            val r4 = inventarioReal.obtenerInventarioLotesPorId(this, id)

            if (r1 && r2 && r3 && r4) {
                exito = true
            } else {
                runOnUiThread {
                    Toast.makeText(this, "ERROR DE CONEXIÓN: No se pudieron descargar todos los datos del producto", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "ERROR INESPERADO: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            runOnUiThread { alerta?.dismisss() }
        }
        return exito
    }

    private fun regresarInicio(){
        val enlace = Intent(this@InventarioTiempoReal, Inicio::class.java)
        startActivity(enlace)
        finish()
    }

    private fun inventarioDetalle(){
        val enlace = Intent(this@InventarioTiempoReal, Inventariodetalle::class.java)
        startActivity(enlace)
        finish()
    }
}