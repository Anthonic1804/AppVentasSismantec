package com.example.acae30.ui.inventario

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.acae30.ui.pedidos.Detallepedido
import com.example.acae30.Funciones
import com.example.acae30.Inicio
import com.example.acae30.ui.inventario.Inventariodetalle
import com.example.acae30.NuevoPrecioAutorizado
import com.example.acae30.ui.pedidos.Producto_agregar
import com.example.acae30.R
import com.example.acae30.controllers.InventarioController
import com.example.acae30.listas.InventarioAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Inventario : AppCompatActivity() {
    private var recicle: RecyclerView? = null
    private var tvInventarioHeader: TextView? = null
    private var funciones: Funciones? = null
    private var busqueda: SearchView? = null
    private var busquedaProducto: Boolean = false
    private var idcliente: Int? = 0
    private var nombrecliente: String? = ""
    private var idpedido = 0
    private var idvisita = 0
    private var codigo = ""
    private var idapi = 0
    private var scanner: ImageButton? = null
    private var vista: ConstraintLayout? = null

    //VARIABLE MODULO TOKEN
    private var busquedaToken : Boolean = false

    //VARIABLES TABLA CONFIG DE LA APP
    private var vistaInventario: Int = 0 //INVENTARIO 1 -> VISTA MINIATURA  2-> VISTA EN LISTA
    private var sinExistencias: Int = 0  // 1 -> Si    0 -> no
    private var getSucursalPosition: Int? = null

    //VARIABLES PARA SHAREDPREFERENCES
    //private var preferences : SharedPreferences? = null
    private lateinit var preferences: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var productSearch : String? = null

    private var inventarioController = InventarioController()

    private var FacturaExportacion = false
    private var idvendedor = 0
    private var hojaCarga = 0

    private lateinit var tvUpdate : TextView
    private lateinit var tvCancel : TextView
    private lateinit var tvTitulo : TextView
    private lateinit var tvMensaje : TextView
    private lateinit var alerta : ConstraintLayout
    private lateinit var btnActualizarInventario: FloatingActionButton
    private lateinit var btnBuscarRecargas: FloatingActionButton


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)
        //supportActionBar?.hide()
        busqueda = findViewById(R.id.busquedainv)

        btnActualizarInventario = findViewById(R.id.btnActualizarInventario)
        btnBuscarRecargas = findViewById(R.id.btnBuscarRecargas)
        tvInventarioHeader = findViewById(R.id.tvInventarioHeader)

        alerta = findViewById(R.id.lyInventarioAlerta)
        preferences = getSharedPreferences(instancia, MODE_PRIVATE)

        busquedaProducto = intent.getBooleanExtra("busqueda", false)
        busquedaToken = intent.getBooleanExtra("tokenBusqueda", false)

        idcliente = intent.getIntExtra("idcliente", 0)
        nombrecliente = intent.getStringExtra("nombrecliente")
        idpedido = intent.getIntExtra("idpedido", 0)
        idvisita = intent.getIntExtra("visitaid", 0)
        codigo = intent.getStringExtra("codigo").toString()
        idapi = intent.getIntExtra("idapi", 0)
        FacturaExportacion = intent.getBooleanExtra("facturaExportacion", false)

        idvendedor = preferences.getInt("Idvendedor", 0)
        hojaCarga = preferences.getInt("hojaCarga", 0)

        //println("FACTURA DE EXPORTACION -> $FacturaExportacion")

        preferences = getSharedPreferences(instancia, MODE_PRIVATE)

        sinExistencias = if(preferences.getString("pedidos_sin_existencia", "") == "S") 1 else 0
        //println("PRODUCTOS SIN EXISTENCIAS -> " + sinExistencias)
        vistaInventario = preferences.getInt("vistaInventario", 0)

        vista = findViewById(R.id.vistaalerta)

        recicle = findViewById(R.id.reciInvent)

        funciones = Funciones()

        scanner = findViewById(R.id.btnscanner)

        scanner!!.findFocus()

        tvInventarioHeader!!.text = "INVENTARIO"

        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(scanner!!.windowToken, 0)

        // BOTON ESCANER DE CODIGO DE BARRA
        scanner!!.setOnClickListener {
            val integrador = IntentIntegrator(this@Inventario)
            integrador.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrador.setPrompt("LECTOR DE CODIGOS DE BARRA - SISMANTEC")
            integrador.setCameraId(0)
            integrador.setBeepEnabled(true)
            integrador.setBarcodeImageEnabled(true)
            integrador.initiateScan()
        }

        //CAPTURANDO POSICIONES DE LOS SPINNER
        // getSucursalPosition = intent.getIntExtra("sucursalPosition", 0)

        btnActualizarInventario.setOnClickListener {
            alertaInventario()
        }

        btnBuscarRecargas.setOnClickListener {
            mensajeRecargarHoja()
        }

    }

    private fun alertaInventario() {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMensaje = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        tvTitulo.text = "INFORMACIÓN"
        tvMensaje.text = "¿DESEA ACTUALIZAR LA INFORMACION DEL INVENTARIO?"
        tvUpdate.text = "ACEPTAR"

        tvUpdate.setOnClickListener {
            updateDialog.dismiss()
            CoroutineScope(Dispatchers.IO).launch {
                inventarioController.actualizarInventarioHojaCarga(0, hojaCarga, idvendedor, this@Inventario, alerta!!)
            }
            Atras(alerta)
        }

        tvCancel.setOnClickListener {

            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    override fun onStart() {
        super.onStart()

        //DESHABILITANDO EL BOTON DE SCANNER
        //EN BUSQUEDA DE PEDIDO
        if(idcliente != 0){
            scanner!!.visibility = View.GONE
            btnActualizarInventario.visibility = View.GONE
            btnBuscarRecargas.visibility = View.GONE
        }

        //SETEA LA BUSQUEDA DEL SEARCHVIEW
        //SI HAY DATO ALMACENADO EN ESTE
        productSearch = preferences.getString("buscarProducto", "")
        if(productSearch != null){
            busqueda!!.setQuery("$productSearch", true)
        }

        Busqueda()
        actualizarListadeInventario()

        val hojaCarga = preferences.getBoolean("Hoja_carga_inventario_app", false)
        if(!hojaCarga){
            btnBuscarRecargas.visibility = View.GONE
            btnActualizarInventario.visibility = View.GONE
        }
    }

    //FUNCION PARA ACTUALIZAR LA LISTA DEL INVENTARIO
    private fun actualizarListadeInventario(){
        this@Inventario.lifecycleScope.launch {
            try {
                val lista = inventarioController.obtenerInformacionProductoPorString(this@Inventario, "", "")
                MostrarLista(lista)
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Inventario, e.message, Toast.LENGTH_SHORT).show()
                }

            }

        }
    }

    //LECTURA DE CODIGO DE BARRAS
    @OptIn(DelicateCoroutinesApi::class)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            // If QRCode has no data.
            if (result.contents == null) {
                runOnUiThread {
                    Toast.makeText(this@Inventario, "Lectura Cancelada", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                val busqueda = result.contents.toString().trim()
                val item = inventarioController.obtenerInformacionProductoPorString(this@Inventario, busqueda, "")

                if(item.size > 0){
                    var id : Int = 0
                    item.forEach {
                        id = it.Id!!
                    }
                    val intento = Intent(this@Inventario, Inventariodetalle::class.java)
                    intento.putExtra("idproducto", id)
                    startActivity(intento)
                    finish()
                }else{
                    Toast.makeText(this@Inventario, "No hay coincidencias", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun Atras(view: View) {
        if (busquedaProducto) {

            eliminarBusqueda()

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
        } else {

            if(busquedaToken){
                val intent = Intent(this@Inventario, NuevoPrecioAutorizado::class.java)
                startActivity(intent)
                finish()
            }else{
                eliminarBusqueda()

                val intento = Intent(this, Inicio::class.java)
                startActivity(intento)
                finish()
            }

        }

    }

    //IMPLEMENTADA LA FUNCION DE NO AGREGAR PRODUCTOS SIN EXISTENCIAS
    private fun MostrarLista(list: List<com.example.acae30.data.local.models.Inventario>) {
            try {
                if (list.isNotEmpty()) {
                    //MOSTRANDO INVENTARIO EN VISTA LISTA
                    if(vistaInventario == 2){
                        val mLayoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                        recicle!!.layoutManager = mLayoutManager
                        val adapter = InventarioAdapter(list, this, vistaInventario) { position ->
                            if (busquedaProducto) {
                                val existeniasProducto = list[position].Existencia!!.toFloat()
                                val condicionMercado = list[position].condicionMercado.toString()
                                if ((sinExistencias == 0 && existeniasProducto <= 0f) && condicionMercado != "OFERTA") {
                                    Toast.makeText(
                                        this@Inventario,
                                        "NO SE PUEDEN AGREGAR PRODUCTOS SIN EXISTENCIAS",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    buscarProducto(busqueda!!.query.toString())

                                    val intento =
                                        Intent(this@Inventario, Producto_agregar::class.java)
                                    intento.putExtra("idproducto", list.get(position).Id)
                                    intento.putExtra("idcliente", idcliente)
                                    intento.putExtra("nombrecliente", nombrecliente)
                                    intento.putExtra("codigo", codigo)
                                    intento.putExtra("idpedido", idpedido)
                                    intento.putExtra("visitaid", idvisita)
                                    intento.putExtra("from", "visita")
                                    intento.putExtra("proviene", "buscar_producto")
                                    intento.putExtra("total_param", 0.toFloat())
                                    // CÓDIGO VIEJO: intento.putExtra("sucursalPosition", getSucursalPosition)
                                    intento.putExtra("facturaExportacion", FacturaExportacion)
                                    startActivity(intento)
                                    finish()
                                }
                            } else {

                                if (busquedaToken) {
                                    val intent =
                                        Intent(this@Inventario, NuevoPrecioAutorizado::class.java)
                                    intent.putExtra("codigo", list.get(position).Codigo.toString())
                                    intent.putExtra(
                                        "producto",
                                        list.get(position).descripcion.toString()
                                    )
                                    intent.putExtra("precio", list.get(position).Precio_iva)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    buscarProducto(busqueda!!.query.toString())

                                    preferences.edit {
                                        putInt("idProducto", list[position].Id!!)
                                    }

                                    val intento =
                                        Intent(this@Inventario, Inventariodetalle::class.java)
                                    startActivity(intento)
                                }
                            }
                        }
                        recicle!!.adapter = adapter
                    }else{
                        //MOSTRANDO INVENTARIO EN VISTA MINIATURA
                        val gridLayoutManayer =
                            GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
                        recicle!!.layoutManager = gridLayoutManayer
                        val adapter = InventarioAdapter(list, this, vistaInventario) { position ->
                            if (busquedaProducto) {
                                val existeniasProducto = list[position].Existencia!!.toFloat()
                                val condicionMercado = list[position].condicionMercado.toString()
                                if ((sinExistencias == 0 && existeniasProducto <= 0f) && condicionMercado != "OFERTA") {
                                    Toast.makeText(
                                        this@Inventario,
                                        "NO SE PUEDEN AGREGAR PRODUCTOS SIN EXISTENCIAS",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    buscarProducto(busqueda!!.query.toString())

                                    val intento =
                                        Intent(this@Inventario, Producto_agregar::class.java)
                                    intento.putExtra("idproducto", list.get(position).Id)
                                    intento.putExtra("idcliente", idcliente)
                                    intento.putExtra("nombrecliente", nombrecliente)
                                    intento.putExtra("codigo", codigo)
                                    intento.putExtra("idpedido", idpedido)
                                    intento.putExtra("visitaid", idvisita)
                                    intento.putExtra("from", "visita")
                                    intento.putExtra("proviene", "buscar_producto")
                                    intento.putExtra("total_param", 0.toFloat())
                                    // CÓDIGO VIEJO: intento.putExtra("sucursalPosition", getSucursalPosition)
                                    intento.putExtra("facturaExportacion", FacturaExportacion)
                                    startActivity(intento)
                                    finish()
                                }
                            } else {

                                if (busquedaToken) {
                                    val intent =
                                        Intent(this@Inventario, NuevoPrecioAutorizado::class.java)
                                    intent.putExtra("codigo", list.get(position).Codigo.toString())
                                    intent.putExtra(
                                        "producto",
                                        list.get(position).descripcion.toString()
                                    )
                                    intent.putExtra("precio", list.get(position).Precio_iva)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    buscarProducto(busqueda!!.query.toString())

                                    val intento =
                                        Intent(this@Inventario, Inventariodetalle::class.java)
                                    intento.putExtra("idproducto", list.get(position).Id)
                                    startActivity(intento)
                                    finish()
                                }
                            }
                        }
                        recicle!!.adapter = adapter
                    }

                } else {
                    runOnUiThread {
                        //Toast.makeText(this@Inventario, "NO SE ENCONTRARON DATOS", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Inventario, e.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    //FUNCION DE BUSQUEDA DE PRODUCTOS DINAMICA
    //MODIFICACION PARA LA PAPELERIA DM
    //23-08-2022
    private fun Busqueda() {
        busqueda!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(texto: String): Boolean {
                val dSearch = inventarioController.obtenerInformacionProductoPorString(this@Inventario ,texto.uppercase(), "")
                this@Inventario.MostrarLista(dSearch)
                return false
            }

        })
    }
    //filtro para busca en la bd

    //FUNCION PARA ALMACENAR LA BUSQUEDA DEL PRODUCTO EN MEMORIA
    private fun buscarProducto(busqueda : String){
        preferences.edit {
            putString("buscarProducto", busqueda)
        }
    }

    //FUNCION PARA ELIMINAR LA BUSQUEDA DE PRODUCTO EN MEMORIA
    private fun eliminarBusqueda(){
        val dataSearch = preferences.getString("buscarProducto","")
        if(dataSearch != ""){
            val deleteSearch = preferences.edit()
            deleteSearch.remove("buscarProducto")
            deleteSearch.apply()
        }
    }

    // BOTON PARA RETROCEDER
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //super.onBackPressed();
    }

    //FUNCION PARA MOSTRAR EL DIALOG DE RECARGA DE HOJA
    private fun mensajeRecargarHoja() {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)

        val numeroHojaCarga = preferences.getInt("hojaCarga", 0)
        val idVendedor = preferences.getInt("Idvendedor", 0)

        updateDialog.setContentView(R.layout.dialog_cancelar)
        tvUpdate = updateDialog.findViewById(R.id.tvUpdate)
        tvCancel = updateDialog.findViewById(R.id.tvCancel)
        tvMensaje = updateDialog.findViewById(R.id.tvMensaje)
        tvTitulo = updateDialog.findViewById(R.id.tvTitulo)

        tvTitulo.text = "INFORMACIÓN"
        tvMensaje.text = "¿DESEA REALIZAR LA RECARGA DE SU HOJA?"
        tvUpdate.text = "ACEPTAR"

        tvUpdate.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                //inventarioController.obtenerHojaRecargas(this@Inventario,idHojaCarga, vista!!)
                inventarioController.obtenerInventarioHojaCarga(true, numeroHojaCarga, idVendedor, this@Inventario)
            }
            updateDialog.dismiss()
        }

        tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }
}