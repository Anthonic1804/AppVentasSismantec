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
    private var searchJob: Job? = null
    private var ultimoQuery = ""
    private var searchVersion = 0
    private var cacheInicial: List<InventarioTiempoRealDto> = emptyList()
    private var cargandoCache = false

    companion object {
        private const val MIN_BUSQUEDA = 3
        private const val HINT_MINIMO = "Escribe al menos 3 letras"
    }

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
        binding.listaInventarioReal.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        cargarCacheInicial()

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
                if (texto == ultimoQuery) return

                job?.cancel()
                job = lifecycleScope.launch {
                    when {
                        texto.isEmpty() -> {
                            ultimoQuery = ""
                            mostrarCache()
                        }
                        texto.length < MIN_BUSQUEDA -> {
                            ultimoQuery = texto
                            mostrarCache()
                            binding.lyBusquedaProducto.helperText = HINT_MINIMO
                        }
                        else -> {
                            delay(300) // debounce solo en rama de red
                            ultimoQuery = texto
                            binding.lyBusquedaProducto.helperText = null
                            val version = ++searchVersion
                            actualizarListadeInventario(texto, version)
                        }
                    }
                }
            }
        })

    }

    //FUNCION PARA ACTUALIZAR LA LISTA DEL INVENTARIO
    //version: token para descartar respuestas viejas que llegan desordenadas
    private fun actualizarListadeInventario(busqueda: String, version: Int = ++searchVersion){
        searchJob?.cancel()
        searchJob = this@InventarioTiempoReal.lifecycleScope.launch {
            try {
                val lista = inventarioReal.obtenerInventarioPorDescripcion(this@InventarioTiempoReal, busqueda)
                if (version != searchVersion) return@launch // respuesta obsoleta
                mostrarLista(lista)
            } catch (e: kotlinx.coroutines.CancellationException) {
                // búsqueda cancelada por tecleo nuevo, no es error
            } catch (e: Exception) {
                if (version != searchVersion) return@launch
                println("ERROR LA OBTENER EL INVENTARIO EN TIEMPO REAL -> " + e.message)
                runOnUiThread {
                    Toast.makeText(
                        this@InventarioTiempoReal,
                        "Sin conexión, mostrando últimos resultados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // Se mantiene la lista anterior: no se vacía por fallo de red
            }
        }
    }

    //Carga una sola vez los primeros 20 para caja vacía / < 3 letras
    private fun cargarCacheInicial() {
        if (cargandoCache || cacheInicial.isNotEmpty()) {
            mostrarLista(cacheInicial)
            return
        }
        cargandoCache = true
        runOnUiThread { alerta?.Cargando() }
        lifecycleScope.launch {
            try {
                val pagina = inventarioReal.obtenerPaginaInicial(this@InventarioTiempoReal, 0, 20)
                cacheInicial = pagina
                // Solo pintar si el usuario no ya escribió una búsqueda válida
                if (ultimoQuery.length < MIN_BUSQUEDA) mostrarLista(cacheInicial)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                println("ERROR AL CARGAR CACHE INICIAL -> " + e.message)
                runOnUiThread {
                    Toast.makeText(
                        this@InventarioTiempoReal,
                        "Sin conexión, no se pudo cargar el inventario inicial",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                cargandoCache = false
                runOnUiThread { alerta?.dismisss() }
            }
        }
    }

    private fun mostrarCache() {
        binding.lyBusquedaProducto.helperText =
            if (ultimoQuery.isNotEmpty() && ultimoQuery.length < MIN_BUSQUEDA) HINT_MINIMO else null
        searchVersion++ // invalida respuestas en vuelo
        searchJob?.cancel()
        mostrarLista(cacheInicial)
        if (cacheInicial.isEmpty() && !cargandoCache) cargarCacheInicial()
    }

    private fun limpiarLista() {
        mostrarCache()
    }

    //IMPLEMENTADA LA FUNCION DE NO AGREGAR PRODUCTOS SIN EXISTENCIAS
    private fun mostrarLista(list: List<InventarioTiempoRealDto>) {
        try {
            if (list.isNotEmpty()) {
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
            } else {
                binding.listaInventarioReal.adapter = null
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