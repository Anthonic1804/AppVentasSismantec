package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.InventarioController
import com.example.acae30.controllers.SolicitudDevolucionesController
import com.example.acae30.controllers.SolicitudRecargasController
import com.example.acae30.databinding.ActivityListadoProductosSolicitudBinding
import com.example.acae30.listas.InventarioAdapter
import com.example.acae30.modelos.Inventario
import kotlinx.coroutines.launch

class ListadoProductosSolicitud : AppCompatActivity() {

    private lateinit var bindind : ActivityListadoProductosSolicitudBinding
    private val solicitudController = SolicitudRecargasController()
    private val inventarioController = InventarioController()
    private var solicitudDevolucion = SolicitudDevolucionesController()

    private lateinit var preferences: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var vistaInventario: Int = 0 //INVENTARIO 1 -> VISTA MINIATURA  2-> VISTA EN LISTA
    private var pedidoSinExistencia: Boolean = false

    private var idSolicitud : Int = 0
    private var proceso : String = ""
    private var idDevolucion : Int = 0
    private var idServidorSolicitud : Int = 0

    private var vista : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindind = ActivityListadoProductosSolicitudBinding.inflate(layoutInflater)
        setContentView(bindind.root)

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)

        pedidoSinExistencia = preferences.getBoolean("Solicitud_Carga_SinExistencia", false)
        vistaInventario = preferences.getInt("vistaInventario", 0)

        idSolicitud = intent.getIntExtra("idSolicitud", 0)
        idServidorSolicitud = intent.getIntExtra("idServidorSolicitud", 0)
        idDevolucion = intent.getIntExtra("idDevolucion", 0)
        proceso = intent.getStringExtra("proceso").toString()

        vista = intent.getStringExtra("vista").toString()

    }

    override fun onStart() {
        super.onStart()
        bindind.btnAtras.setOnClickListener {
            mensajeCancelar()
        }

        cargarInventario(vista)

        bindind.txtBusquedaProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(string: Editable) {
                val busqueda : ArrayList<Inventario> = when(vista){
                    "devolucion" -> {
                        inventarioController.obtenerInformacionProductoPorString(this@ListadoProductosSolicitud,string.toString(),"devolucion")
                    }

                    else -> {
                        solicitudController.obtenerInformacionProductoPorString(this@ListadoProductosSolicitud,string.toString())
                    }
                }
                //val busqueda = solicitudController.obtenerInformacionProductoPorString(this@ListadoProductosSolicitud, string.toString())
                this@ListadoProductosSolicitud.mostrarLista(busqueda)
            }
        })
    }

    //FUNCION PARA ACTUALIZAR LA LISTA DEL INVENTARIO
    private fun cargarInventario(vista : String){
        this.lifecycleScope.launch {
            try {
                val lista : ArrayList<Inventario> = when(vista){
                    "devolucion" -> {
                        inventarioController.obtenerInformacionProductoPorString(this@ListadoProductosSolicitud,"", "devolucion")
                    }

                    else -> {
                        solicitudController.obtenerInformacionProductoPorString(this@ListadoProductosSolicitud,"")
                    }
                }
                mostrarLista(lista)
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ListadoProductosSolicitud, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //IMPLEMENTADA LA FUNCION DE NO AGREGAR PRODUCTOS SIN EXISTENCIAS
    private fun mostrarLista(list: List<Inventario>) {
        try {
            if (list.isNotEmpty()) {
                //MOSTRANDO INVENTARIO EN VISTA LISTA
                if(vistaInventario == 2){
                    val mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    bindind.listadoInventario.layoutManager = mLayoutManager
                    val adapter = InventarioAdapter(list, this, vistaInventario) { position ->
                        val existeniasProducto = list[position].Existencia!!.toFloat()
                        if(!pedidoSinExistencia && existeniasProducto <= 0f){
                            Toast.makeText(this@ListadoProductosSolicitud, "NO SE PUEDEN AGREGAR PRODUCTOS SIN EXISTENCIAS", Toast.LENGTH_SHORT).show()
                        }else{
                            when(vista){
                                "devolucion" -> {
                                    val encontrado = solicitudDevolucion.obtenerProductoEnDevolucion(this@ListadoProductosSolicitud,
                                            list[position].Id!!, idDevolucion)
                                    if(encontrado){
                                        Toast.makeText(this@ListadoProductosSolicitud, "EL PRODUCTO YA ESTÁ AGREGADO AL DETALLE DE LA DEVOLUCION", Toast.LENGTH_SHORT)
                                            .show()
                                    }else{
                                        val intento = Intent(this, AgregarProductosDevolucion::class.java)
                                        intento.putExtra("idProducto", list[position].Id)
                                        intento.putExtra("codigo", list[position].Codigo)
                                        intento.putExtra("descripcion", list[position].descripcion)
                                        intento.putExtra("existencia", list[position].Existencia!!.toFloat())
                                        intento.putExtra("idDevolucion", idDevolucion)
                                        startActivity(intento)
                                        finish()
                                    }

                                }
                                else -> {
                                    val intento = Intent(this@ListadoProductosSolicitud, AgregarProductoSolicitud::class.java)
                                    intento.putExtra("proceso", proceso)
                                    intento.putExtra("idSolicitud", idSolicitud)
                                    intento.putExtra("idProducto", list[position].Id)
                                    intento.putExtra("codigo", list[position].Codigo)
                                    intento.putExtra("descripcion", list[position].descripcion)
                                    intento.putExtra("existencia", list[position].Existencia!!.toFloat())
                                    intento.putExtra("costo", list[position].Costo!!.toFloat())
                                    intento.putExtra("costoIva", list[position].costo_iva!!.toFloat())
                                    intento.putExtra("precio_u", list[position].Precio!!.toFloat())
                                    intento.putExtra("precio_u_iva", list[position].Precio_iva!!.toFloat())
                                    intento.putExtra("idServidorSolicitud", idServidorSolicitud)
                                    startActivity(intento)
                                    finish()
                                }
                            }
                        }
                    }
                    bindind.listadoInventario.adapter = adapter
                }else{
                    //MOSTRANDO INVENTARIO EN VISTA MINIATURA
                    val gridLayoutManayer = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
                    bindind.listadoInventario.layoutManager = gridLayoutManayer
                    val adapter = InventarioAdapter(list, this, vistaInventario) { position ->
                        val existeniasProducto = list[position].Existencia!!.toFloat()
                        if(!pedidoSinExistencia && existeniasProducto <= 0f){
                            Toast.makeText(this@ListadoProductosSolicitud, "NO SE PUEDEN AGREGAR PRODUCTOS SIN EXISTENCIAS", Toast.LENGTH_SHORT).show()
                        }else{
                            when(vista){
                                "devolucion" -> {
                                    val encontrado = solicitudDevolucion.obtenerProductoEnDevolucion(this@ListadoProductosSolicitud,
                                        list[position].Id!!, idDevolucion)
                                    if(encontrado){
                                        Toast.makeText(this@ListadoProductosSolicitud, "EL PRODUCTO YA ESTÁ AGREGADO AL DETALLE DE LA DEVOLUCION", Toast.LENGTH_SHORT)
                                            .show()
                                    }else{
                                        val intento = Intent(this, AgregarProductosDevolucion::class.java)
                                        intento.putExtra("idProducto", list[position].Id)
                                        intento.putExtra("codigo", list[position].Codigo)
                                        intento.putExtra("descripcion", list[position].descripcion)
                                        intento.putExtra("existencia", list[position].Existencia!!.toFloat())
                                        intento.putExtra("idDevolucion", idDevolucion)
                                        startActivity(intento)
                                        finish()
                                    }
                                }
                                else -> {
                                    val intento = Intent(this@ListadoProductosSolicitud, AgregarProductoSolicitud::class.java)
                                    intento.putExtra("proceso", proceso)
                                    intento.putExtra("idSolicitud", idSolicitud)
                                    intento.putExtra("idProducto", list[position].Id)
                                    intento.putExtra("codigo", list[position].Codigo)
                                    intento.putExtra("descripcion", list[position].descripcion)
                                    intento.putExtra("existencia", list[position].Existencia!!.toFloat())
                                    intento.putExtra("costo", list[position].Costo!!.toFloat())
                                    intento.putExtra("costoIva", list[position].costo_iva!!.toFloat())
                                    intento.putExtra("precio_u", list[position].Precio!!.toFloat())
                                    intento.putExtra("precio_u_iva", list[position].Precio_iva!!.toFloat())
                                    intento.putExtra("idServidorSolicitud", idServidorSolicitud)
                                    startActivity(intento)
                                    finish()
                                }
                            }
                        }
                    }
                    bindind.listadoInventario.adapter = adapter
                }

            } else {
                runOnUiThread {
                    //Toast.makeText(this@Inventario, "NO SE ENCONTRARON DATOS", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this@ListadoProductosSolicitud, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeCancelar(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage("¿DESEA CANCELAR EL PROCESO?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                when(vista){
                    "devolucion" -> {
                        nuevadevolucion()
                    }
                    else -> {
                        nuevaSolicitud()
                    }
                }
            }
            .setNegativeButton("CANCELAR"){view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    private fun nuevaSolicitud(){
        val intent = Intent(this, NuevaSolicitud::class.java)
        intent.putExtra("idSolicitud", idSolicitud)
        intent.putExtra("proceso", proceso)
        intent.putExtra("idServidorSolicitud", idServidorSolicitud)
        startActivity(intent)
        finish()
    }

    private fun nuevadevolucion(){
        val intent = Intent(this, NuevaDevolucion::class.java)
        intent.putExtra("idDevolucion", idDevolucion)
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }//anula el boton atras
}