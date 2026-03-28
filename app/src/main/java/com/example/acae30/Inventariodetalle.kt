package com.example.acae30

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acae30.controllers.InventarioController
import com.example.acae30.databinding.ActivityInventariodetalleBinding
import com.example.acae30.listas.InventarioDetalleAdapter
import com.example.acae30.modelos.InventarioPrecios
import kotlinx.coroutines.launch
import androidx.core.content.edit

class Inventariodetalle : AppCompatActivity() {

    private lateinit var binding: ActivityInventariodetalleBinding
    private var idinventario = 0

    private lateinit var preferences: SharedPreferences
    private var instancia = "CONFIG_SERVIDOR"
    private var unidadMedida = "UNI"

    private var inventarioController = InventarioController()
    private var funciones = Funciones()

    private var inventarioTiempoReal : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventariodetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences(instancia, Context.MODE_PRIVATE)
        idinventario = preferences.getInt("idProducto", 0)

        inventarioTiempoReal = preferences.getBoolean("inventarioTiempoReal", false)

        binding.imageView7.setOnClickListener {
            AlertaPrecio(this@Inventariodetalle)  //muestra la alerta
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }//anula el boton atras

    override fun onStart() {
        super.onStart()

        binding.imgbtnatras.setOnClickListener {

            preferences.edit {
                remove("idProducto")
            }

            if(inventarioTiempoReal){

                lifecycleScope.launch {
                    funciones.limpiarHojaCarga(this@Inventariodetalle)
                }

                val intento = Intent(this, InventarioTiempoReal::class.java)
                startActivity(intento)
                finish()
            }else{
                val intento = Intent(this, com.example.acae30.Inventario::class.java)
                startActivity(intento)
                finish()
            }


        }//BOTON ATRAS

        binding.spunidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                unidadMedida = when(binding.spunidad.selectedItem.toString()){
                    "UNIDAD" -> "UNI"
                    "FRACCION" -> "FRA"
                    else -> binding.spunidad.selectedItem.toString()
                }

                cargarEscalas(unidadMedida)
            }
        }


        cargarInformacionProducto()
        cargarEscalas(unidadMedida)
        cargarUnidadesMedidas()
    }

    private fun cargarUnidadesMedidas(){
        this@Inventariodetalle.lifecycleScope.launch {
            try {

                val hojaCarga = preferences.getBoolean("Hoja_carga_inventario_app", false)
                val unidades = inventarioController.listadoUnidadesMedidaProductoById(this@Inventariodetalle, idinventario, hojaCarga)
                val unidadesMedida = ArrayAdapter<String>(this@Inventariodetalle, android.R.layout.simple_spinner_dropdown_item)
                unidadesMedida.addAll(unidades)
                binding.spunidad.adapter = unidadesMedida

            }catch (e:Exception){
                println("ERROR AL CARGAR LAS UNIDADESD DE MEDIDA -> "  + e.message)
            }
        }
    }

    private fun cargarInformacionProducto(){
        this@Inventariodetalle.lifecycleScope.launch {
            try {
                val producto = inventarioController.obtenerInformacionProductoPorId(this@Inventariodetalle ,idinventario, false)
                with(binding){
                    txtcodigo.text = producto!!.Codigo
                    txtdescripcion.text = producto.descripcion
                    txtprecio.text = "$" + String.format("%.4f", producto.Precio_iva)
                    txtexistencia.text =  "${String.format("%.2f", producto.Existencia)} " + " " + if(producto.Unidad_medida.isNullOrBlank()) "UNI" else producto.Unidad_medida  //producto.Existencia!!.toInt().toString() + " UNI"
                    txtexistenciaFracciones.text = "${String.format("%.2f", producto.Existencia_u)} " + " " + if(producto.Nombre_fraccion.isNullOrBlank()) "FRA" else producto.Nombre_fraccion
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Inventariodetalle, "ERROR AL CARGAR EL DETALLE DEL PRODUCTO", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun cargarEscalas(unidadMedida : String){
        this@Inventariodetalle.lifecycleScope.launch {
            try{
                val lista = inventarioController.obtenerEscalaPrecios(this@Inventariodetalle, idinventario, false, unidadMedida)
                if(lista.size > 0){
                    ArmarLista(lista)
                }
            }catch (e: Exception){
                runOnUiThread {
                    Toast.makeText(this@Inventariodetalle, "ERROR AL CARGAR LAS ESCALAS -> ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun ArmarLista(lista: ArrayList<InventarioPrecios>) {

        val mLayoutManager = LinearLayoutManager(
            this@Inventariodetalle,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.listaprecios.layoutManager = mLayoutManager
        val adapter = InventarioDetalleAdapter(lista, this@Inventariodetalle)
        binding.listaprecios.adapter = adapter

    }

    private fun AlertaPrecio(contexto: com.example.acae30.Inventariodetalle) {
        val dialogo = Dialog(this)
        dialogo.setContentView(R.layout.alerta_costo)
        val costoProducto = dialogo.findViewById<TextView>(R.id.txtcosto)

        val producto = inventarioController.obtenerInformacionProductoPorId(this@Inventariodetalle ,idinventario, false)

        costoProducto!!.text = String.format("%.4f", producto!!.costo_iva)

        dialogo.show()

    } //muestra la alerta para MOSTRAR EL COSTO

}