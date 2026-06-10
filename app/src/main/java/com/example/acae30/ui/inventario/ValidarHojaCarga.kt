package com.example.acae30.ui.inventario

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.Funciones
import com.example.acae30.R
import com.example.acae30.carga_datos
import com.example.acae30.controllers.HojaCargaController
import com.example.acae30.databinding.ActivityValidarHojaCargaBinding
import com.example.acae30.listas.ValidarHojaAdapter
import com.example.acae30.modelos.InventarioHojaValidar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ValidarHojaCarga : AppCompatActivity() {

    private lateinit var binding : ActivityValidarHojaCargaBinding
    private var funciones = Funciones()
    private var hojaController = HojaCargaController()

    private var idvendedor = 0
    private var hojaCargaActiva = 0
    private val instancia = "CONFIG_SERVIDOR"
    private lateinit var preferencias: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityValidarHojaCargaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencias = getSharedPreferences(instancia, MODE_PRIVATE)
        idvendedor = preferencias.getInt("Idvendedor", 0)
        hojaCargaActiva = preferencias.getInt("hojaCarga", 0)

        cargarDetalle()

    }

    override fun onStart() {
        super.onStart()

        binding.btnRechazarHoja.setOnClickListener {
            mensajeConfirmacion("CANCELAR")
        }

        binding.btnAceptarHoja.setOnClickListener {
            mensajeConfirmacion("ACEPTAR")
        }

        binding.txtBusquedaProducto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(string: Editable) {
                val busqueda : ArrayList<InventarioHojaValidar> = hojaController.validarProductoPorString(this@ValidarHojaCarga,string.toString())
                this@ValidarHojaCarga.armarLista(busqueda)
            }
        })

    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeConfirmacion(tipo: String){
        val mensaje = if(tipo.contains("CANCELAR")){
            "¿DESEA CANCELAR EL PROCESO DE VALIDACION?"
        }else{
            "¿DESEA VALIDAR LA HOJA DE CARGA?"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage(mensaje)
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()

                if(tipo.contains("CANCELAR")){
                    lifecycleScope.launch(Dispatchers.IO) {
                        funciones.eliminarInformacion(this@ValidarHojaCarga)
                    }

                    cargarDatos()
                }else{

                    lifecycleScope.launch(Dispatchers.IO) {
                        val productoSinValidar = hojaController.obtenerProductosSinValidar(this@ValidarHojaCarga)
                        if(productoSinValidar > 0){
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@ValidarHojaCarga,
                                    "HAY PRODUCTOS SIN VALIDAR",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }else{
                            val aceptada = hojaController.validarHojaCargaServidor(this@ValidarHojaCarga, hojaCargaActiva, idvendedor)
                            if(aceptada){
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@ValidarHojaCarga,
                                        "PRODUCTO VALIDADOS CORRECTAMENTE",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                                    cargarDatos()
                                }
                            }else{
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@ValidarHojaCarga,
                                        "ERROR DE CONEXION, INTENTE NUEVAMENTE",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                        }
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

    //FUNCION PARA REGRESAR A LA CARGA DE DATOS
    private fun cargarDatos(){
        val intent = Intent(this@ValidarHojaCarga, carga_datos::class.java)
        startActivity(intent)
        finish()
    }

    //FUNCION PARA CARGAR EL DETALLE DE LA HOJA DE CARGA
    private fun cargarDetalle(){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val lista = hojaController.validarProductoPorString(this@ValidarHojaCarga, "")
                armarLista(lista)
            }catch (e:Exception){
                println("ERROR AL OBTENER EL DETALLE DE LA HOJA DE CARGA -> " + e.message)
            }
        }
    }

    //FUNCION PARA ARMAR EL LISTADO EN EL RECYCLERVIEW
    private fun armarLista(lista : ArrayList<InventarioHojaValidar>) {
        val mLayoutManager = LinearLayoutManager(
            this@ValidarHojaCarga,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.ListaProductosSolicitados.layoutManager = mLayoutManager
        val adapter = ValidarHojaAdapter(lista) { item, isChecked ->
            val estado = if (isChecked) {
                1
            } else {
                0
            }
            hojaController.actualizarProductoValidacionHojaCarga(this, item.Codigo!!, estado)
        }
        binding.ListaProductosSolicitados.adapter = adapter

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }//anula el boton atras

}