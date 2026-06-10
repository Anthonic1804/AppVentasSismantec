package com.example.acae30.ui.devoluciones

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.acae30.ui.solicitudes.ListadoProductosSolicitud
import com.example.acae30.controllers.SolicitudDevolucionesController
import com.example.acae30.databinding.ActivityAgregarProductosDevolucionBinding
import com.example.acae30.modelos.SolcitudDevolucion.SolicitudDevolucionDetalle
import kotlinx.coroutines.launch

class AgregarProductosDevolucion : AppCompatActivity() {

    private lateinit var binding : ActivityAgregarProductosDevolucionBinding

    private var idProducto : Int = 0
    private var codigo : String = ""
    private var descripcion : String = ""
    private var existencia : Float = 0f
    private var idDevolucion : Int = 0
    private var bueno : Float = 0f
    private var averia : Float = 0f
    private var solicitudDevolucion = SolicitudDevolucionesController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarProductosDevolucionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idProducto = intent.getIntExtra("idProducto", 0)
        codigo = intent.getStringExtra("codigo").toString()
        descripcion = intent.getStringExtra("descripcion").toString()
        existencia = intent.getFloatExtra("existencia", 0f)
        idDevolucion = intent.getIntExtra("idDevolucion", 0)
    }

    //FUNCION PARA VALIDAR LAS CANTIDAD INGRESADAS
    private fun validarDevolucion() : Boolean{
        var verificacion : Boolean = false
        bueno = if(binding.txtBueno.text.toString() == ""){
            0f
        }else{
            binding.txtBueno.text.toString().toFloat()
        }


        averia = if(binding.txtAveria.text.toString() == ""){
            0f
        }else{
            binding.txtAveria.text.toString().toFloat()
        }

        verificacion = existencia == (bueno + averia)

        return verificacion
    }

    override fun onStart() {
        super.onStart()

        binding.tvCodigoProducto.text = codigo
        binding.tvProductoNombre.text = descripcion
        binding.txtTotalDevolucion.setText(existencia.toString())

        binding.btnAtras.setOnClickListener {
            listadoProductos()
        }

        binding.btnaceptar.setOnClickListener {
            val verificar = validarDevolucion()
            if(!verificar){
                Toast.makeText(this@AgregarProductosDevolucion,"Las cantidad ingresadas no concuerdan", Toast.LENGTH_SHORT)
                    .show()
            }else{
                this@AgregarProductosDevolucion.lifecycleScope.launch {
                    val obj : SolicitudDevolucionDetalle = SolicitudDevolucionDetalle(
                        idDevolucion,
                        0,
                        "",
                        idProducto,
                        codigo,
                        descripcion,
                        "",
                        existencia,
                        bueno,
                        averia,
                        ""
                    )

                    val registrado = solicitudDevolucion.agregarProductoDetalleDevolucion(this@AgregarProductosDevolucion, obj)

                    if(registrado){
                        runOnUiThread {
                            Toast.makeText(this@AgregarProductosDevolucion, "PRODUCTO AGREGADO CORRECTAMENTE", Toast.LENGTH_SHORT)
                                .show()
                            nuevaDevolucion()
                        }
                    }
                }

            }
        }

    }

    private fun listadoProductos(){
        val intento = Intent(this, ListadoProductosSolicitud::class.java)
        intento.putExtra("vista", "devolucion")
        intento.putExtra("idDevolucion", idDevolucion)
        startActivity(intento)
        finish()
    }

    private fun nuevaDevolucion(){
        val intento = Intent(this, NuevaDevolucion::class.java)
        intento.putExtra("idDevolucion", idDevolucion)
        startActivity(intento)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //  super.onBackPressed()

        //   finish()
    }//anula el boton atras



}