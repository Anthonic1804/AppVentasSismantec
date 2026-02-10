package com.example.acae30

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.acae30.databinding.ActivityMenuServidoresBinding

class MenuServidores : AppCompatActivity() {

    private lateinit var binding : ActivityMenuServidoresBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMenuServidoresBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    override fun onStart() {
        super.onStart()

        binding.btnNuevoServidor.setOnClickListener {
            nuevoServidor()
        }

    }

    //-----------------------------------
    //Función para redireccionar a la Activity de Registro de Servidor
    //-----------------------------------
    private fun nuevoServidor(){
        val enlace = Intent(this@MenuServidores, NuevoServidor::class.java)
        startActivity(enlace)
        finish()
    }

}