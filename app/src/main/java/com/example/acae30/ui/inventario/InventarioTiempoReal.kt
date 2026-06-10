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

        sinExistencias = if(preferences.getString("pedidos_sin_existencia", "") == "S") 1 else 0

        //CAPTURANDO POSICIONES DE LOS SPINNER
        getSucursalPosition = intent.getIntExtra("sucursalPosition", 0)

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
                intento.putExtra("sucursalPosition", getSucursalPosition)
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

                                cargarDatosProducto(id)

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
                                intento.putExtra("sucursalPosition", getSucursalPosition)
                                intento.putExtra("facturaExportacion", FacturaExportacion)
                                startActivity(intento)
                                finish()
                            }
                        } else {

                            cargarDatosProducto(id)

                            runOnUiThread {

                                preferences.edit {
                                    putInt("idProducto", id)
                                }

                                inventarioDetalle()
                            }
                        }
                    }
                }
                binding.listaInventarioReal.adapter = adapter
            } else {
                runOnUiThread {
                    //Toast.makeText(this@Inventario, "NO SE ENCONTRARON DATOS", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this@InventarioTiempoReal, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun cargarDatosProducto(id: Int){
        try {
            inventarioReal.obtenerProductoPorId(this@InventarioTiempoReal, id)
            inventarioReal.obtenerInventarioPreciosPorId(this@InventarioTiempoReal, id)
            inventarioReal.obtenerInventarioUnidadesPorId(this@InventarioTiempoReal, id)
            inventarioReal.obtenerInventarioLotesPorId(this@InventarioTiempoReal, id)
        }catch (e:Exception){
            println("ERROR AL CARGAR LOS DATOS DEL PRODUCTO -> " + e.message)
        }
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