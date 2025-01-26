package com.example.acae30

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.acae30.databinding.ActivityNuevaDevolucionBinding

class NuevaDevolucion : AppCompatActivity() {

    private lateinit var binding : ActivityNuevaDevolucionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevaDevolucionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        binding.btnAtras.setOnClickListener {
            mensajeCancelar()
        }

        binding.btnAgregarProducto.setOnClickListener {
            agregarProducto()
        }
    }

    private fun menuDevoluciones(){
        val intento = Intent(this, MenuDevoluciones::class.java)
        startActivity(intento)
        finish()
    }

    //FUNCION DE MENSAJES DE ERROR Y CONFIRMACION
    private fun mensajeCancelar(){
        val dialog = AlertDialog.Builder(this)
            .setTitle("INFORMACION")
            .setMessage("¿DESEA CANCELAR EL PROCESO?")
            .setPositiveButton("ACEPTAR") { view, _ ->
                view.dismiss()
                menuDevoluciones()
            }
            .setNegativeButton("CANCELAR"){view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .setIcon(R.drawable.ic_information)
            .create()

        dialog.show()
    }

    private fun agregarProducto(){
        val intento = Intent(this, ListadoProductosSolicitud::class.java)
        intento.putExtra("vista", "devolucion")
        startActivity(intento)
        finish()
    }
}