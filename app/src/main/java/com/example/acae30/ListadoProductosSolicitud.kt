package com.example.acae30

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.SolicitudRecargasController
import com.example.acae30.databinding.ActivityListadoProductosSolicitudBinding
import com.example.acae30.listas.InventarioAdapter
import com.example.acae30.modelos.Inventario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListadoProductosSolicitud : AppCompatActivity() {

    private lateinit var bindind : ActivityListadoProductosSolicitudBinding
    private val solicitudController = SolicitudRecargasController()

    private lateinit var preferences: SharedPreferences
    private val instancia = "CONFIG_SERVIDOR"
    private var vistaInventario: Int = 0 //INVENTARIO 1 -> VISTA MINIATURA  2-> VISTA EN LISTA
    private var sinExistencias: Int = 0  // 1 -> Si    0 -> no

    private var idSolicitud : Int = 0
    private var proceso : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindind = ActivityListadoProductosSolicitudBinding.inflate(layoutInflater)
        setContentView(bindind.root)

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)

        sinExistencias = if(preferences.getString("pedidos_sin_existencia", "") == "S") 1 else 0
        vistaInventario = preferences.getInt("vistaInventario", 0)

        idSolicitud = intent.getIntExtra("idSolicitud", 0)
        proceso = intent.getStringExtra("proceso").toString()

    }

    override fun onStart() {
        super.onStart()
        bindind.btnAtras.setOnClickListener {
            mensajeCancelar()
        }

        cargarInventario()

        bindind.txtBusquedaProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(string: Editable) {
                val busqueda = solicitudController.obtenerInformacionProductoPorString(this@ListadoProductosSolicitud, string.toString())
                this@ListadoProductosSolicitud.mostrarLista(busqueda)
            }
        })
    }
    //FUNCION PARA ACTUALIZAR LA LISTA DEL INVENTARIO
    private fun cargarInventario(){
        this.lifecycleScope.launch {
            try {
                val lista = solicitudController.obtenerInformacionProductoPorString(this@ListadoProductosSolicitud,"")
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
                        if(sinExistencias == 0 && existeniasProducto == 0f || existeniasProducto < 0f){
                            Toast.makeText(this@ListadoProductosSolicitud, "NO SE PUEDEN AGREGAR PRODUCTOS SIN EXISTENCIAS", Toast.LENGTH_SHORT).show()
                        }else{
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
                            startActivity(intento)
                            finish()
                        }
                    }
                    bindind.listadoInventario.adapter = adapter
                }else{
                    //MOSTRANDO INVENTARIO EN VISTA MINIATURA
                    val gridLayoutManayer = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
                    bindind.listadoInventario.layoutManager = gridLayoutManayer
                    val adapter = InventarioAdapter(list, this, vistaInventario) { position ->
                        val existeniasProducto = list[position].Existencia!!.toFloat()
                        if(sinExistencias == 0 && existeniasProducto == 0f || existeniasProducto < 0f){
                            Toast.makeText(this@ListadoProductosSolicitud, "NO SE PUEDEN AGREGAR PRODUCTOS SIN EXISTENCIAS", Toast.LENGTH_SHORT).show()
                        }else{
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
                            startActivity(intento)
                            finish()
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
                nuevaSolicitud()
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
        startActivity(intent)
        finish()
    }
}