package com.example.acae30

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.acae30.databinding.ActivityAgregarProductosDevolucionBinding

class AgregarProductosDevolucion : AppCompatActivity() {

    private lateinit var binding : ActivityAgregarProductosDevolucionBinding

    private var idProducto : Int = 0
    private var codigo : String = ""
    private var descripcion : String = ""
    private var existencia : Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarProductosDevolucionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idProducto = intent.getIntExtra("idProducto", 0)
        codigo = intent.getStringExtra("codigo").toString()
        descripcion = intent.getStringExtra("descripcion").toString()
        existencia = intent.getFloatExtra("existencia", 0f)

    }

    override fun onStart() {
        super.onStart()

        binding.tvCodigoProducto.text = codigo
        binding.tvProductoNombre.text = descripcion
        binding.txtTotalDevolucion.setText(existencia.toString())

        binding.btnAtras.setOnClickListener {
            listadoProductos()
        }

    }

    private fun listadoProductos(){
        val intento = Intent(this, ListadoProductosSolicitud::class.java)
        intento.putExtra("vista", "devolucion")
        startActivity(intento)
        finish()
    }



}